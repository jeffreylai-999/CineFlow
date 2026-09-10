import { render, screen, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import type { CatalogClient, Movie } from './api/catalogClient.ts'
import { CatalogPage } from './CatalogPage.tsx'

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
  it('renders available movies from the catalog client', async () => {
    const client: CatalogClient = {
      listMovies: vi.fn().mockResolvedValue([nebulaExpress]),
    }

    render(<CatalogPage client={client} />)

    expect(screen.getByText('CineFlow')).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent('Loading Movies')

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Nebula Express' })).toBeInTheDocument()
    })

    expect(screen.getByText(/courier crew races a sealed cargo/i)).toBeInTheDocument()
    expect(screen.getByText(/Adventure · 118 min · PG-13/)).toBeInTheDocument()
    expect(screen.getByRole('img', { name: /Poster for Nebula Express/i })).toHaveAttribute(
      'src',
      nebulaExpress.posterUrl,
    )
  })

  it('shows a safe error when the catalog client fails', async () => {
    const client: CatalogClient = {
      listMovies: vi.fn().mockRejectedValue(new Error('network down')),
    }

    render(<CatalogPage client={client} />)

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(
        'Unable to load the Movie catalog. Try again shortly.',
      )
    })
  })
})
