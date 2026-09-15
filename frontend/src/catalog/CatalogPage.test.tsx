import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page } from 'vitest/browser'
import axe from 'axe-core'
import type { CatalogClient, Movie } from '@/catalog/api/catalogClient.ts'
import { CatalogPage } from '@/catalog/CatalogPage.tsx'

const nebulaExpress: Movie = {
  id: 1,
  title: 'Nebula Express',
  synopsis:
    'A courier crew races a sealed cargo across three colonies before the gate collapses.',
  genre: 'Adventure',
  runtimeMinutes: 118,
  ageRating: 'PG-13',
  posterUrl: 'https://cdn.example.test/posters/nebula-express.jpg',
}

describe('CatalogPage', () => {
  it('renders available Movies from the catalog client', async () => {
    let resolveMovies!: (movies: Movie[]) => void
    const client: CatalogClient = {
      listMovies: vi.fn(
        () =>
          new Promise<Movie[]>((resolve) => {
            resolveMovies = resolve
          }),
      ),
    }

    await render(<CatalogPage client={client} />)

    await expect.element(page.getByText('CineFlow')).toBeInTheDocument()
    await expect.element(page.getByRole('status')).toBeInTheDocument()
    await expect.element(page.getByText('Loading Movies…')).toBeInTheDocument()

    resolveMovies([nebulaExpress])

    await expect.element(page.getByRole('heading', { name: 'Nebula Express' })).toBeInTheDocument()
    await expect
      .element(page.getByText(/courier crew races a sealed cargo/i))
      .toBeInTheDocument()
    await expect.element(page.getByText(/Adventure · 118 min · PG-13/)).toBeInTheDocument()
    await expect
      .element(page.getByRole('img', { name: /Poster for Nebula Express/i }))
      .toHaveAttribute('src', nebulaExpress.posterUrl!)
    await expect.element(page.getByRole('heading', { name: 'Credits' })).toBeInTheDocument()
    await expect
      .element(page.getByRole('img', { name: 'The Movie Database' }))
      .toHaveAttribute('src', '/tmdb-logo.svg')
    await expect
      .element(
        page.getByText(
          'This product uses the TMDB API but is not endorsed or certified by TMDB.',
        ),
      )
      .toBeInTheDocument()
  })

  it('shows a safe error when the catalog client fails', async () => {
    const client: CatalogClient = {
      listMovies: vi.fn().mockRejectedValue(new Error('network down')),
    }

    await render(<CatalogPage client={client} />)

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Unable to load the Movie catalog. Try again shortly.')
  })

  it('has no serious axe violations on the ready catalog route', async () => {
    const client: CatalogClient = {
      listMovies: vi.fn().mockResolvedValue([nebulaExpress]),
    }

    const screen = await render(<CatalogPage client={client} />)
    await expect.element(page.getByRole('heading', { name: 'Nebula Express' })).toBeInTheDocument()

    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
