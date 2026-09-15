import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page } from 'vitest/browser'
import axe from 'axe-core'
import { AdminMoviesPage } from '@/catalog/AdminMoviesPage.tsx'
import { CatalogAdminRequestError, type CatalogAdminClient, type ManagedMovie, type MovieSearchHit } from '@/catalog/api/catalogAdminClient.ts'
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
}

function client(overrides: Partial<CatalogAdminClient> = {}): CatalogAdminClient {
  return {
    listMovies: vi.fn().mockResolvedValue([]),
    search: vi.fn().mockResolvedValue([hit]),
    importMovie: vi.fn().mockResolvedValue(imported),
    refresh: vi.fn().mockResolvedValue({ ...imported, title: 'Refreshed Gate' }),
    updateSchedulingFields: vi.fn().mockResolvedValue({ ...imported, runtimeMinutes: 130, ageRating: 'NC-16' }),
    ...overrides,
  }
}

describe('AdminMoviesPage', () => {
  it('lets an Administrator search, import, and keep local scheduling fields', async () => {
    const catalogClient = client({
      listMovies: vi.fn().mockResolvedValueOnce([]).mockResolvedValue([imported]),
    })

    await render(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )

    await expect.element(page.getByRole('heading', { name: 'Movies' })).toBeInTheDocument()
    const search = page.getByLabelText('Search TMDB')
    await search.fill('courier gate')
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
      { externalId: '4242', runtimeMinutes: 121, ageRating: 'PG-13' },
      'admin-token',
    )
    expect(catalogClient.updateSchedulingFields).toHaveBeenCalledWith(
      8,
      { runtimeMinutes: 130, ageRating: 'NC-16' },
      'admin-token',
    )
  })

  it('refreshes descriptive metadata through the catalog client', async () => {
    const catalogClient = client({
      listMovies: vi.fn().mockResolvedValue([imported]),
    })

    await render(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )
    await page.getByRole('button', { name: 'Refresh metadata' }).click()
    await expect.element(page.getByRole('status')).toHaveTextContent('Refreshed Refreshed Gate.')
    expect(catalogClient.refresh).toHaveBeenCalledWith(8, 'admin-token')
  })

  it('shows a safe error when the provider is unavailable', async () => {
    const catalogClient = client({
      search: vi
        .fn()
        .mockRejectedValue(new CatalogAdminRequestError(503, 'catalog.provider_unavailable')),
    })

    await render(
      <AdminMoviesPage session={administrator} client={catalogClient} onLogout={() => undefined} />,
    )
    await page.getByLabelText('Search TMDB').fill('courier gate')
    await page.getByRole('button', { name: 'Search' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent(
        'The movie metadata provider is unavailable. Existing Movies remain in the catalog.',
      )
  })

  it('has no serious axe violations on the administrator movie route', async () => {
    const screen = await render(
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
