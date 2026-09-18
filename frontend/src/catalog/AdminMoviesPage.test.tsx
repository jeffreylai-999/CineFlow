import type { ReactElement } from 'react'
import { MemoryRouter } from 'react-router'
import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page } from 'vitest/browser'
import axe from 'axe-core'
import { AdminMoviesPage } from '@/catalog/AdminMoviesPage.tsx'
import {
  CatalogAdminRequestError,
  type CatalogAdminClient,
  type ManagedMovie,
  type MovieProviderSettings,
  type MovieSearchHit,
} from '@/catalog/api/catalogAdminClient.ts'
import type { StaffSession } from '@/identity/api/identityClient.ts'

const administrator: StaffSession = {
  accessToken: 'admin-token',
  expiresInSeconds: 900,
  staff: { id: 2, username: 'administrator', role: 'ADMINISTRATOR' },
}

const imported: ManagedMovie = {
  id: 8,
  title: 'The Courier Gate',
  synopsis: 'A courier crew races a sealed cargo across three colonies.',
  genre: 'Adventure',
  runtimeMinutes: 118,
  ageRating: 'PG-13',
  posterUrl: 'https://image.tmdb.org/t/p/w500/courier-gate.jpg',
  sourceProvider: 'tmdb',
  externalId: '4242',
  sourceRefreshedAt: '2026-09-15T00:00:00Z',
}

const hit: MovieSearchHit = {
  externalId: '4242',
  title: 'The Courier Gate',
  year: '2024',
  posterUrl: imported.posterUrl,
  providerId: 'tmdb',
}

const tmdbOnly: MovieProviderSettings = {
  activeProviderId: 'tmdb',
  providers: [{ id: 'tmdb', displayName: 'TMDB' }],
}

const bothProviders: MovieProviderSettings = {
  activeProviderId: 'tmdb',
  providers: [
    { id: 'tmdb', displayName: 'TMDB' },
    { id: 'omdb', displayName: 'OMDb' },
  ],
}

function renderMovies(ui: ReactElement) {
  return render(<MemoryRouter>{ui}</MemoryRouter>)
}

function client(overrides: Partial<CatalogAdminClient> = {}): CatalogAdminClient {
  return {
    listMovies: vi.fn().mockResolvedValue([]),
    listProviders: vi.fn().mockResolvedValue(tmdbOnly),
    selectProvider: vi.fn().mockResolvedValue({ ...bothProviders, activeProviderId: 'omdb' }),
    search: vi.fn().mockResolvedValue([hit]),
    importMovie: vi.fn().mockResolvedValue(imported),
    refresh: vi.fn().mockResolvedValue({ ...imported, title: 'Refreshed Gate' }),
    archive: vi.fn().mockResolvedValue({
      ...imported,
      archivedAt: '2026-09-16T12:00:00Z',
      providerRetentionWarning: false,
    }),
    updateSchedulingFields: vi.fn().mockResolvedValue({ ...imported, runtimeMinutes: 130, ageRating: 'NC-16' }),
    ...overrides,
  }
}

describe('AdminMoviesPage', () => {
  it('lets an Administrator search, import, and keep local scheduling fields', async () => {
    const catalogClient = client({
      listMovies: vi.fn().mockResolvedValueOnce([]).mockResolvedValue([imported]),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )

    await expect.element(page.getByRole('heading', { name: 'Movies' })).toBeInTheDocument()
    const search = page.getByLabelText('Search TMDB')
    await search.fill('courier gate')
    await expect.element(page.getByRole('button', { name: 'Search' })).toBeEnabled()
    await page.getByRole('button', { name: 'Search' }).click()
    await expect.element(page.getByText('2024 · 4242')).toBeInTheDocument()

    await page.getByLabelText('Import runtime').fill('121')
    await page.getByLabelText('Import age rating').fill('PG-13')
    await page.getByRole('button', { name: 'Import' }).click()
    await expect.element(page.getByRole('status')).toHaveTextContent('Imported The Courier Gate.')
    await expect.element(page.getByRole('heading', { name: 'The Courier Gate' })).toBeInTheDocument()

    await page.getByLabelText('Runtime (minutes)').fill('130')
    await page.getByLabelText('Age rating').fill('NC-16')
    await page.getByRole('button', { name: 'Save scheduling fields' }).click()
    await expect
      .element(page.getByRole('status'))
      .toHaveTextContent('Updated runtime and age rating for The Courier Gate.')
    expect(catalogClient.importMovie).toHaveBeenCalledWith(
      { providerId: 'tmdb', externalId: '4242', runtimeMinutes: 121, ageRating: 'PG-13' },
      'admin-token',
    )
    expect(catalogClient.updateSchedulingFields).toHaveBeenCalledWith(
      8,
      { runtimeMinutes: 130, ageRating: 'NC-16' },
      'admin-token',
    )
  })

  it('lets an Administrator select OMDb without remapping existing Movies', async () => {
    const catalogClient = client({
      listMovies: vi.fn().mockResolvedValue([imported]),
      listProviders: vi.fn().mockResolvedValue(bothProviders),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )

    await expect.element(page.getByRole('group', { name: 'Active metadata provider' })).toBeInTheDocument()
    await expect.element(page.getByRole('radio', { name: 'OMDb' })).toBeEnabled()
    await page.getByRole('radio', { name: 'OMDb' }).click()
    await expect.element(page.getByRole('status')).toHaveTextContent('Now searching OMDb.')
    await expect.element(page.getByLabelText('Search OMDb')).toBeInTheDocument()
    await expect.element(page.getByText('Adventure · tmdb 4242')).toBeInTheDocument()
    expect(catalogClient.selectProvider).toHaveBeenCalledWith('omdb', 'admin-token')
    expect(catalogClient.refresh).not.toHaveBeenCalled()
  })

  it('surfaces a stale-provider import instead of sending the hit to the new adapter', async () => {
    const catalogClient = client({
      importMovie: vi
        .fn()
        .mockRejectedValue(new CatalogAdminRequestError(409, 'catalog.provider_mismatch')),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )
    await page.getByLabelText('Search TMDB').fill('courier gate')
    await expect.element(page.getByRole('button', { name: 'Search' })).toBeEnabled()
    await page.getByRole('button', { name: 'Search' }).click()
    await expect.element(page.getByRole('button', { name: 'Import' })).toBeEnabled()
    await page.getByRole('button', { name: 'Import' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('The active provider changed. Search again before importing.')
    expect(catalogClient.importMovie).toHaveBeenCalledWith(
      { providerId: 'tmdb', externalId: '4242' },
      'admin-token',
    )
  })

  it('clears search results when the Administrator selects a different provider', async () => {
    const catalogClient = client({
      listProviders: vi.fn().mockResolvedValue(bothProviders),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )

    await page.getByLabelText('Search TMDB').fill('courier gate')
    await expect.element(page.getByRole('button', { name: 'Search' })).toBeEnabled()
    await page.getByRole('button', { name: 'Search' }).click()
    await expect.element(page.getByRole('button', { name: 'Import' })).toBeInTheDocument()

    await page.getByRole('radio', { name: 'OMDb' }).click()
    await expect.element(page.getByRole('status')).toHaveTextContent('Now searching OMDb.')
    await expect.element(page.getByRole('button', { name: 'Import' })).not.toBeInTheDocument()
    expect(catalogClient.importMovie).not.toHaveBeenCalled()
  })

  it('does not offer Refresh for Movies whose source has no adapter', async () => {
    const catalogClient = client({
      listMovies: vi.fn().mockResolvedValue([
        {
          ...imported,
          id: 1,
          title: 'Nebula Express',
          sourceProvider: 'fixture',
          externalId: 'nebula-express',
        },
      ]),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )

    await expect.element(page.getByRole('heading', { name: 'Nebula Express' })).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Refresh metadata' })).not.toBeInTheDocument()
    expect(catalogClient.refresh).not.toHaveBeenCalled()
  })

  it('keeps the stored provider selected in the listed radios', async () => {
    const catalogClient = client({
      listProviders: vi.fn().mockResolvedValue({
        activeProviderId: 'tmdb',
        providers: [
          { id: 'tmdb', displayName: 'TMDB' },
          { id: 'omdb', displayName: 'OMDb' },
        ],
      }),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )

    await expect.element(page.getByRole('radio', { name: 'TMDB' })).toBeChecked()
    await expect.element(page.getByRole('radio', { name: 'OMDb' })).not.toBeChecked()
    await expect.element(page.getByLabelText('Search TMDB')).toBeInTheDocument()
  })

  it('does not invent a TMDB search label when the stored provider is missing from the list', async () => {
    const catalogClient = client({
      listProviders: vi.fn().mockResolvedValue({
        activeProviderId: 'tmdb',
        providers: [{ id: 'omdb', displayName: 'OMDb' }],
      }),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )

    await expect.element(page.getByRole('radio', { name: 'OMDb' })).not.toBeChecked()
    await expect.element(page.getByLabelText('Search TMDB')).not.toBeInTheDocument()
    await expect.element(page.getByLabelText('Search tmdb')).toBeInTheDocument()
  })

  it('lists only the providers the catalog client returns', async () => {
    await renderMovies(
      <AdminMoviesPage session={administrator} client={client()} onLogout={() => undefined} />,
    )
    await expect.element(page.getByRole('radio', { name: 'TMDB' })).toBeInTheDocument()
    await expect.element(page.getByRole('radio', { name: 'OMDb' })).not.toBeInTheDocument()
  })

  it('refreshes descriptive metadata through the catalog client', async () => {
    const catalogClient = client({
      listMovies: vi.fn().mockResolvedValue([imported]),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )
    await expect.element(page.getByRole('button', { name: 'Refresh metadata' })).toBeEnabled()
    await page.getByRole('button', { name: 'Refresh metadata' }).click()
    await expect.element(page.getByRole('status')).toHaveTextContent('Refreshed Refreshed Gate.')
    expect(catalogClient.refresh).toHaveBeenCalledWith(8, 'admin-token')
  })

  it('shows a retention warning and archives through the catalog client', async () => {
    const warned: ManagedMovie = {
      ...imported,
      providerRetentionWarning: true,
      providerRetentionExpiresAt: '2026-09-28T12:00:00Z',
    }
    const catalogClient = client({
      listMovies: vi.fn().mockResolvedValue([warned]),
      archive: vi.fn().mockResolvedValue({
        ...warned,
        archivedAt: '2026-09-16T12:00:00Z',
        providerRetentionWarning: false,
      }),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )

    await expect
      .element(page.getByText(/Provider metadata approaches the retention limit on 28 Sept 2026/))
      .toBeInTheDocument()
    await page.getByRole('button', { name: 'Archive Movie' }).click()
    await expect.element(page.getByRole('status')).toHaveTextContent('Archived The Courier Gate.')
    expect(catalogClient.archive).toHaveBeenCalledWith(8, 'admin-token')
    await expect.element(page.getByText(/Archived — excluded from the public catalog/)).toBeInTheDocument()
  })

  it('surfaces Retry-After when search is rate limited', async () => {
    const catalogClient = client({
      search: vi.fn().mockRejectedValue(new CatalogAdminRequestError(429, 'catalog.rate_limited', 12)),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )
    await page.getByLabelText('Search TMDB').fill('courier gate')
    await expect.element(page.getByRole('button', { name: 'Search' })).toBeEnabled()
    await page.getByRole('button', { name: 'Search' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Search is temporarily limited. Try again in 12 seconds.')
  })

  it('shows a safe error when the provider is unavailable', async () => {
    const catalogClient = client({
      search: vi
        .fn()
        .mockRejectedValue(new CatalogAdminRequestError(503, 'catalog.provider_unavailable')),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )
    await page.getByLabelText('Search TMDB').fill('courier gate')
    await expect.element(page.getByRole('button', { name: 'Search' })).toBeEnabled()
    await page.getByRole('button', { name: 'Search' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent(
        'The movie metadata provider is unavailable. Existing Movies remain in the catalog.',
      )
  })

  it('keeps search disabled until the initial catalog list settles', async () => {
    let resolveMovies!: (movies: ManagedMovie[]) => void
    const catalogClient = client({
      listMovies: vi.fn(
        () =>
          new Promise<ManagedMovie[]>((resolve) => {
            resolveMovies = resolve
          }),
      ),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )
    await page.getByLabelText('Search TMDB').fill('courier gate')
    await expect.element(page.getByRole('button', { name: 'Search' })).toBeDisabled()

    resolveMovies([])
    await expect.element(page.getByRole('button', { name: 'Search' })).toBeEnabled()
  })

  it('clears previous search results when a later search fails', async () => {
    const catalogClient = client({
      search: vi
        .fn()
        .mockResolvedValueOnce([hit])
        .mockRejectedValueOnce(new CatalogAdminRequestError(429, 'catalog.rate_limited', 12)),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )
    await page.getByLabelText('Search TMDB').fill('courier gate')
    await expect.element(page.getByRole('button', { name: 'Search' })).toBeEnabled()
    await page.getByRole('button', { name: 'Search' }).click()
    await expect.element(page.getByRole('button', { name: 'Import' })).toBeInTheDocument()

    await page.getByLabelText('Search TMDB').fill('other query')
    await expect.element(page.getByRole('button', { name: 'Search' })).toBeEnabled()
    await page.getByRole('button', { name: 'Search' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Search is temporarily limited. Try again in 12 seconds.')
    await expect.element(page.getByRole('button', { name: 'Import' })).not.toBeInTheDocument()
  })

  it('keeps an imported movie when the follow-up list fails', async () => {
    const catalogClient = client({
      listMovies: vi
        .fn()
        .mockResolvedValueOnce([])
        .mockRejectedValueOnce(new CatalogAdminRequestError(503, 'catalog.provider_unavailable')),
    })

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )
    await page.getByLabelText('Search TMDB').fill('courier gate')
    await expect.element(page.getByRole('button', { name: 'Search' })).toBeEnabled()
    await page.getByRole('button', { name: 'Search' }).click()
    await expect.element(page.getByRole('button', { name: 'Import' })).toBeEnabled()
    await page.getByRole('button', { name: 'Import' }).click()

    await expect.element(page.getByRole('status')).toHaveTextContent('Imported The Courier Gate.')
    await expect.element(page.getByRole('heading', { name: 'The Courier Gate' })).toBeInTheDocument()
    await expect.element(page.getByRole('alert')).not.toBeInTheDocument()
    expect(catalogClient.importMovie).toHaveBeenCalledTimes(1)
  })

  it('rejects a non-integer import runtime before calling the catalog client', async () => {
    const catalogClient = client()

    await renderMovies(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )
    await page.getByLabelText('Search TMDB').fill('courier gate')
    await expect.element(page.getByRole('button', { name: 'Search' })).toBeEnabled()
    await page.getByRole('button', { name: 'Search' }).click()
    await expect.element(page.getByRole('button', { name: 'Import' })).toBeEnabled()
    await page.getByLabelText('Import runtime').fill('abc')
    await page.getByRole('button', { name: 'Import' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent(
        'Enter a whole number of minutes for import runtime, or leave it blank to use the provider value.',
      )
    expect(catalogClient.importMovie).not.toHaveBeenCalled()
  })

  it('has no serious axe violations on the administrator movie route', async () => {
    const screen = await renderMovies(
      <AdminMoviesPage
        session={administrator}
        client={client({ listMovies: vi.fn().mockResolvedValue([imported]) })}
        onLogout={() => undefined}
      />,
    )
    await expect.element(page.getByRole('heading', { name: 'The Courier Gate' })).toBeInTheDocument()

    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
