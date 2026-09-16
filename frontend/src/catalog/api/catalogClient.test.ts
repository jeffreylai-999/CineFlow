import { describe, expect, it, vi } from 'vitest'
import { createCatalogClient } from '@/catalog/api/catalogClient.ts'

describe('createCatalogClient', () => {
  it('loads the public catalog', async () => {
    const fetcher = vi.fn().mockResolvedValue(
      new Response(JSON.stringify([{ title: 'Nebula Express', dates: [] }]), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    )
    const client = createCatalogClient(fetcher)
    await expect(client.listMovies()).resolves.toMatchObject([{ title: 'Nebula Express' }])
    expect(fetcher).toHaveBeenCalledWith('/api/movies', { headers: { Accept: 'application/json' } })
  })

  it('surfaces a Booking Cutoff from the Seat Map request', async () => {
    const fetcher = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ code: 'booking.cutoff' }), {
        status: 409,
        headers: { 'Content-Type': 'application/json' },
      }),
    )
    const client = createCatalogClient(fetcher)

    await expect(client.getShowtimeSeats(11)).rejects.toMatchObject({
      status: 409,
      code: 'booking.cutoff',
    })
  })
})
