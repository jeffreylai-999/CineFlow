import { describe, expect, it, vi } from 'vitest'
import { createCustomerClient } from '@/booking/api/customerClient.ts'

describe('createCustomerClient', () => {
  it('loads the public catalog', async () => {
    const fetcher = vi.fn().mockResolvedValue(
      new Response(JSON.stringify([{ title: 'Nebula Express', dates: [] }]), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    )
    const client = createCustomerClient(fetcher)
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
    const client = createCustomerClient(fetcher)

    await expect(client.getShowtimeSeats(11)).rejects.toMatchObject({
      status: 409,
      code: 'booking.cutoff',
    })
  })

  it('creates a Seat Hold for the selected Seats', async () => {
    const fetcher = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
          showtimeId: 11,
          seatIds: [1, 2],
          serverTime: '2026-09-16T00:00:00Z',
          expiresAt: '2026-09-16T00:10:00Z',
        }),
        { status: 201, headers: { 'Content-Type': 'application/json' } },
      ),
    )
    const client = createCustomerClient(fetcher)

    await expect(client.createSeatHold(11, [2, 1])).resolves.toMatchObject({
      showtimeId: 11,
      seatIds: [1, 2],
    })
    expect(fetcher).toHaveBeenCalledWith('/api/showtimes/11/holds', {
      method: 'POST',
      headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
      body: JSON.stringify({ seatIds: [2, 1] }),
    })
  })
})
