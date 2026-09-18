import { describe, expect, it, vi } from 'vitest'
import { createStaffBookingClient } from '@/booking/api/staffBookingClient.ts'

const token = 'staff-access-token'

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('createStaffBookingClient', () => {
  it('lists Showtimes open for counter sales with the Staff token', async () => {
    const fetcher = vi.fn().mockResolvedValue(
      jsonResponse(200, [
        {
          id: 11,
          movieTitle: 'Nebula Express',
          hallName: 'Fixture Hall',
          startsAtCinemaTime: '2099-06-20T19:30:00',
          timeZone: 'Asia/Kuala_Lumpur',
          adultPriceMyr: 28,
          childPriceMyr: 18,
          counterSalesOpen: true,
        },
      ]),
    )
    const client = createStaffBookingClient(token, fetcher)

    await expect(client.listCounterShowtimes()).resolves.toMatchObject([
      { id: 11, movieTitle: 'Nebula Express', counterSalesOpen: true },
    ])
    expect(fetcher).toHaveBeenCalledWith('/api/staff/showtimes', {
      headers: { Accept: 'application/json', Authorization: `Bearer ${token}` },
    })
  })

  it('loads the Staff Seat Map with distinct Seat states', async () => {
    const fetcher = vi.fn().mockResolvedValue(
      jsonResponse(200, {
        showtimeId: 11,
        movieId: 1,
        movieTitle: 'Nebula Express',
        hallName: 'Fixture Hall',
        startsAtCinemaTime: '2099-06-20T19:30:00',
        timeZone: 'Asia/Kuala_Lumpur',
        adultPriceMyr: 28,
        childPriceMyr: 18,
        bookingLimit: 10,
        counterSalesOpen: true,
        seats: [{ id: 1, rowLabel: 'A', seatNumber: 1, label: 'A1', state: 'HELD' }],
      }),
    )
    const client = createStaffBookingClient(token, fetcher)

    await expect(client.getStaffSeatMap(11)).resolves.toMatchObject({
      showtimeId: 11,
      seats: [{ id: 1, state: 'HELD' }],
    })
    expect(fetcher).toHaveBeenCalledWith('/api/staff/showtimes/11/seats', {
      headers: { Accept: 'application/json', Authorization: `Bearer ${token}` },
    })
  })

  it('creates a counter Seat Hold', async () => {
    const fetcher = vi.fn().mockResolvedValue(
      jsonResponse(201, {
        holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
        showtimeId: 11,
        seatIds: [1, 2],
        serverTime: '2026-09-16T00:00:00Z',
        expiresAt: '2026-09-16T00:10:00Z',
      }),
    )
    const client = createStaffBookingClient(token, fetcher)

    await expect(client.createCounterHold(11, [2, 1])).resolves.toMatchObject({
      showtimeId: 11,
      seatIds: [1, 2],
    })
    expect(fetcher).toHaveBeenCalledWith('/api/staff/showtimes/11/holds', {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ seatIds: [2, 1] }),
    })
  })

  it('confirms a counter sale with the received Payment method and no card credentials', async () => {
    const fetcher = vi.fn().mockResolvedValue(
      jsonResponse(201, {
        bookingReference: 'K7QX2M9T4D',
        showtimeId: 11,
        movieTitle: 'Nebula Express',
        hallName: 'Fixture Hall',
        startsAtCinemaTime: '2099-06-20T19:30:00',
        timeZone: 'Asia/Kuala_Lumpur',
        email: null,
        seats: [{ seatId: 1, label: 'A1', ticketType: 'ADULT', priceMyr: 28 }],
        totalMyr: 28,
        admissionToken: 'opaque-admission-token',
      }),
    )
    const client = createStaffBookingClient(token, fetcher)
    const request = {
      holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
      tickets: [{ seatId: 1, ticketType: 'ADULT' as const }],
      method: 'CASH' as const,
      idempotencyKey: '0b3c6d74-7b3a-4f2f-9a1d-2f0d2f0d2f0d',
    }

    await expect(client.confirmCounterSale(11, request)).resolves.toMatchObject({
      bookingReference: 'K7QX2M9T4D',
      email: null,
    })
    expect(fetcher).toHaveBeenCalledWith('/api/staff/showtimes/11/confirm', {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    })
  })

  it('surfaces the Counter Sales Cutoff from a confirm', async () => {
    const fetcher = vi
      .fn()
      .mockResolvedValue(jsonResponse(409, { code: 'booking.counter_sales_cutoff' }))
    const client = createStaffBookingClient(token, fetcher)

    await expect(
      client.confirmCounterSale(11, {
        holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
        tickets: [{ seatId: 1, ticketType: 'ADULT' }],
        method: 'CARD',
        idempotencyKey: '0b3c6d74-7b3a-4f2f-9a1d-2f0d2f0d2f0d',
      }),
    ).rejects.toMatchObject({
      status: 409,
      code: 'booking.counter_sales_cutoff',
    })
  })
})
