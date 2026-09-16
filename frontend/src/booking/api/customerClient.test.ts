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

  it('confirms a Booking through checkout', async () => {
    const fetcher = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          bookingReference: 'K7QX2M9T4D',
          showtimeId: 11,
          movieTitle: 'Nebula Express',
          hallName: 'Fixture Hall',
          startsAtCinemaTime: '2099-06-20T19:30:00',
          timeZone: 'Asia/Kuala_Lumpur',
          email: 'aisyah@example.com',
          seats: [{ seatId: 1, label: 'A1', ticketType: 'ADULT', priceMyr: 28 }],
          totalMyr: 28,
          admissionToken: 'opaque-admission-token',
        }),
        { status: 201, headers: { 'Content-Type': 'application/json' } },
      ),
    )
    const client = createCustomerClient(fetcher)
    const request = {
      holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
      email: 'aisyah@example.com',
      tickets: [{ seatId: 1, ticketType: 'ADULT' as const }],
      cardNumber: '4242424242424242',
      idempotencyKey: '0b3c6d74-7b3a-4f2f-9a1d-2f0d2f0d2f0d',
    }

    await expect(client.checkout(11, request)).resolves.toMatchObject({
      bookingReference: 'K7QX2M9T4D',
      admissionToken: 'opaque-admission-token',
    })
    expect(fetcher).toHaveBeenCalledWith('/api/showtimes/11/checkout', {
      method: 'POST',
      headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    })
  })

  it('surfaces a declined Payment from checkout', async () => {
    const fetcher = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ code: 'booking.payment_declined' }), {
        status: 402,
        headers: { 'Content-Type': 'application/json' },
      }),
    )
    const client = createCustomerClient(fetcher)

    await expect(
      client.checkout(11, {
        holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
        email: 'aisyah@example.com',
        tickets: [{ seatId: 1, ticketType: 'ADULT' }],
        cardNumber: '4000000000000002',
        idempotencyKey: '0b3c6d74-7b3a-4f2f-9a1d-2f0d2f0d2f0d',
      }),
    ).rejects.toMatchObject({
      status: 402,
      code: 'booking.payment_declined',
    })
  })
})
