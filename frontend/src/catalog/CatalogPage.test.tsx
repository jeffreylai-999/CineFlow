import { MemoryRouter } from 'react-router'
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
  dates: [
    {
      cinemaDate: '2099-06-20',
      showtimes: [
        {
          id: 11,
          hallName: 'Fixture Hall',
          startsAtCinemaTime: '2099-06-20T19:30:00',
          timeZone: 'Asia/Kuala_Lumpur',
          adultPriceMyr: 28,
          childPriceMyr: 18,
          checkoutOpen: true,
        },
        {
          id: 12,
          hallName: 'Fixture Hall',
          startsAtCinemaTime: '2099-06-20T21:00:00',
          timeZone: 'Asia/Kuala_Lumpur',
          adultPriceMyr: 22,
          childPriceMyr: 12,
          checkoutOpen: false,
        },
      ],
    },
    {
      cinemaDate: '2099-06-21',
      showtimes: [
        {
          id: 13,
          hallName: 'Fixture Hall',
          startsAtCinemaTime: '2099-06-21T19:30:00',
          timeZone: 'Asia/Kuala_Lumpur',
          adultPriceMyr: 30,
          childPriceMyr: 20,
          checkoutOpen: true,
        },
      ],
    },
  ],
}

function catalogClient(movies: Movie[] | Promise<Movie[]> | Error): CatalogClient {
  return {
    listMovies:
      movies instanceof Error
        ? vi.fn().mockRejectedValue(movies)
        : vi.fn().mockResolvedValue(movies),
    getShowtimeSeats: vi.fn(),
  }
}

describe('CatalogPage', () => {
  it('renders Movies with Showtimes grouped by Cinema date', async () => {
    let resolveMovies!: (movies: Movie[]) => void
    const client: CatalogClient = {
      listMovies: vi.fn(
        () =>
          new Promise<Movie[]>((resolve) => {
            resolveMovies = resolve
          }),
      ),
      getShowtimeSeats: vi.fn(),
    }

    await render(
      <MemoryRouter>
        <CatalogPage client={client} />
      </MemoryRouter>,
    )

    await expect.element(page.getByText('CineFlow')).toBeInTheDocument()
    await expect.element(page.getByRole('status')).toBeInTheDocument()
    await expect.element(page.getByText('Loading Movies…')).toBeInTheDocument()

    resolveMovies([nebulaExpress])

    await expect.element(page.getByRole('heading', { name: 'Nebula Express' })).toBeInTheDocument()
    await expect
      .element(page.getByText(/courier crew races a sealed cargo/i))
      .toBeInTheDocument()
    await expect.element(page.getByText(/Adventure · 118 min · PG-13/)).toBeInTheDocument()
    await expect.element(page.getByRole('heading', { name: '2099-06-20' })).toBeInTheDocument()
    await expect.element(page.getByRole('heading', { name: '2099-06-21' })).toBeInTheDocument()
    await expect
      .element(page.getByRole('link', { name: /2099-06-20T19:30:00 · Fixture Hall/ }))
      .toHaveAttribute('href', '/showtimes/11')
    await expect.element(page.getByText(/Adult RM 28.00 · Child RM 18.00/)).toBeInTheDocument()
    await expect
      .element(page.getByText(/Online checkout closed 15 minutes before this Showtime/))
      .toBeInTheDocument()
    await expect.element(page.getByRole('link', { name: /2099-06-20T21:00:00/ })).not.toBeInTheDocument()
    await expect
      .element(page.getByRole('img', { name: /Poster for Nebula Express/i }))
      .toHaveAttribute('src', nebulaExpress.posterUrl!)
    await expect.element(page.getByRole('heading', { name: 'Credits' })).toBeInTheDocument()
  })

  it('shows a safe error when the catalog client fails', async () => {
    await render(
      <MemoryRouter>
        <CatalogPage client={catalogClient(new Error('network down'))} />
      </MemoryRouter>,
    )

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Unable to load the Movie catalog. Try again shortly.')
  })

  it('has no serious axe violations on the ready catalog route', async () => {
    const screen = await render(
      <MemoryRouter>
        <CatalogPage client={catalogClient([nebulaExpress])} />
      </MemoryRouter>,
    )
    await expect.element(page.getByRole('heading', { name: 'Nebula Express' })).toBeInTheDocument()

    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
