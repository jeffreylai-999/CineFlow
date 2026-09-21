import { Link, MemoryRouter, Route, Routes } from 'react-router'
import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page, userEvent } from 'vitest/browser'
import axe from 'axe-core'
import { SeatSelectionPage } from '@/booking/SeatSelectionPage.tsx'
import {
  CustomerRequestError,
  type CustomerClient,
  type CustomerSeat,
  type SeatHold,
  type ShowtimeSeats,
} from '@/booking/api/customerClient.ts'
import { type SeatAvailabilitySocket } from '@/booking/api/seatAvailabilitySocket.ts'

const seatMap: ShowtimeSeats = {
  showtimeId: 11,
  movieId: 1,
  movieTitle: 'Nebula Express',
  hallName: 'Fixture Hall',
  startsAtCinemaTime: '2099-06-20T19:30:00',
  timeZone: 'Asia/Kuala_Lumpur',
  adultPriceMyr: 28,
  childPriceMyr: 18,
  bookingLimit: 2,
  seats: [
    { id: 1, rowLabel: 'A', seatNumber: 1, label: 'A1', available: true },
    { id: 2, rowLabel: 'A', seatNumber: 2, label: 'A2', available: true },
    { id: 3, rowLabel: 'A', seatNumber: 3, label: 'A3', available: true },
    { id: 4, rowLabel: 'A', seatNumber: 4, label: 'A4', available: false },
  ],
}

function manySeats(rowCount: number, seatsPerRow: number): CustomerSeat[] {
  const seats: CustomerSeat[] = []
  for (let row = 0; row < rowCount; row += 1) {
    const rowLabel = String.fromCharCode(65 + row)
    for (let seatNumber = 1; seatNumber <= seatsPerRow; seatNumber += 1) {
      seats.push({
        id: row * seatsPerRow + seatNumber,
        rowLabel,
        seatNumber,
        label: `${rowLabel}${seatNumber}`,
        available: true,
      })
    }
  }
  return seats
}

function clientStub(overrides: Partial<CustomerClient> = {}): CustomerClient {
  return {
    listMovies: vi.fn(),
    getShowtimeSeats: vi.fn().mockResolvedValue(seatMap),
    ...overrides,
  }
}

function SeatSelectionHarness({
  client,
  socket,
}: {
  client: CustomerClient
  socket?: SeatAvailabilitySocket
}) {
  return (
    <>
      <nav>
        <Link to="/showtimes/11">Open showtime 11</Link>
        <Link to="/showtimes/12">Open showtime 12</Link>
        <Link to="/showtimes/not-a-showtime">Open invalid showtime</Link>
      </nav>
      <Routes>
        <Route
          path="/showtimes/:showtimeId"
          element={<SeatSelectionPage client={client} socket={socket} />}
        />
      </Routes>
    </>
  )
}

async function renderSeats(
  client: CustomerClient = clientStub(),
  path = '/showtimes/11',
  socket?: SeatAvailabilitySocket,
) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <SeatSelectionHarness client={client} socket={socket} />
    </MemoryRouter>,
  )
}

describe('SeatSelectionPage', () => {
  it('selects Seats with Ticket Types, updates the MYR total, and explains the Booking Limit', async () => {
    await renderSeats()

    await expect.element(page.getByRole('heading', { name: 'Choose Seats' })).toBeInTheDocument()
    await expect.element(page.getByRole('heading', { name: 'Booking summary' })).toBeInTheDocument()
    await expect.element(page.getByText(/A Booking can include at most 2 Seats/)).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Seat A4, unavailable' })).toBeInTheDocument()

    const firstSeat = page.getByRole('button', { name: 'Seat A1, available' })
    firstSeat.element().focus()
    await userEvent.keyboard('{Enter}')
    await expect.element(page.getByRole('button', { name: 'Seat A1, selected' })).toHaveAttribute(
      'aria-pressed',
      'true',
    )

    const secondSeat = page.getByRole('button', { name: 'Seat A2, available' })
    secondSeat.element().focus()
    await userEvent.keyboard(' ')
    await expect.element(page.getByRole('button', { name: 'Seat A2, selected' })).toHaveAttribute(
      'aria-pressed',
      'true',
    )
    await expect.element(page.getByText('Total RM 56.00')).toHaveAttribute('role', 'status')

    await page.getByLabelText('Seat A2 Ticket Type').selectOptions('CHILD')
    await expect.element(page.getByText('Total RM 46.00')).toHaveAttribute('role', 'status')

    await page.getByRole('button', { name: 'Seat A3, available' }).click()
    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('This Booking can include at most 2 Seats.')
    await expect.element(page.getByRole('button', { name: 'Seat A3, available' })).toBeInTheDocument()
  })

  it('keeps the Booking summary docked on a phone-sized viewport while Seats scroll', async () => {
    await page.viewport(390, 844)
    const tallMap: ShowtimeSeats = {
      ...seatMap,
      bookingLimit: 10,
      seats: manySeats(12, 8),
    }
    try {
      await renderSeats(clientStub({ getShowtimeSeats: vi.fn().mockResolvedValue(tallMap) }))
      await expect.element(page.getByRole('heading', { name: 'Choose Seats' })).toBeInTheDocument()

      for (const label of ['A1', 'A2', 'A3', 'A4', 'A5', 'A6', 'B1', 'B2']) {
        await page.getByRole('button', { name: `Seat ${label}, available` }).click()
      }

      const lastSeat = page.getByRole('button', { name: 'Seat L8, available' })
      await expect.element(lastSeat).toBeInTheDocument()
      const lastEl = lastSeat.element()
      lastEl.ownerDocument.defaultView?.scrollTo(0, lastEl.ownerDocument.documentElement.scrollHeight)

      const summary = page.getByRole('complementary', { name: 'Booking summary' })
      await expect.element(summary).toBeVisible()
      await expect.element(page.getByRole('heading', { name: 'Booking summary' })).toBeVisible()
      const summaryBox = summary.element().getBoundingClientRect()
      const lastBox = lastEl.getBoundingClientRect()
      expect(summaryBox.top).toBeGreaterThanOrEqual(0)
      expect(summaryBox.bottom).toBeLessThanOrEqual(844)
      expect(summaryBox.height).toBeGreaterThan(0)
      expect(lastBox.bottom).toBeLessThanOrEqual(summaryBox.top + 1)
    } finally {
      await page.viewport(1280, 720)
    }
  })

  it('keeps the Booking summary sticky while a tall Seat Map scrolls on a desktop viewport', async () => {
    await page.viewport(1280, 720)
    const tallMap: ShowtimeSeats = {
      ...seatMap,
      bookingLimit: 10,
      seats: manySeats(12, 8),
    }
    await renderSeats(clientStub({ getShowtimeSeats: vi.fn().mockResolvedValue(tallMap) }))
    await expect.element(page.getByRole('heading', { name: 'Choose Seats' })).toBeInTheDocument()

    for (const label of ['A1', 'A2', 'A3', 'A4', 'A5', 'A6', 'A7', 'A8', 'B1', 'B2']) {
      await page.getByRole('button', { name: `Seat ${label}, available` }).click()
    }

    const lastSeat = page.getByRole('button', { name: 'Seat L8, available' })
    await expect.element(lastSeat).toBeInTheDocument()
    lastSeat.element().scrollIntoView({ block: 'end' })

    const summary = page.getByRole('complementary', { name: 'Booking summary' })
    await expect.element(summary).toBeVisible()
    const summaryEl = summary.element()
    const summaryBox = summaryEl.getBoundingClientRect()
    expect(summaryBox.top).toBeGreaterThanOrEqual(0)
    expect(summaryBox.bottom).toBeLessThanOrEqual(720)
    expect(summaryBox.height).toBeGreaterThan(0)
    expect(getComputedStyle(summaryEl).overflowY).toBe('auto')
  })

  it('clears the previous Seat Map when the Showtime route is invalid', async () => {
    await renderSeats()
    await expect.element(page.getByRole('button', { name: 'Seat A1, available' })).toBeInTheDocument()
    await page.getByRole('button', { name: 'Seat A1, available' }).click()
    await expect.element(page.getByRole('button', { name: 'Seat A1, selected' })).toBeInTheDocument()

    await page.getByRole('link', { name: 'Open invalid showtime' }).click()

    await expect.element(page.getByRole('alert')).toHaveTextContent('Showtime not found.')
    await expect.element(page.getByRole('group', { name: 'Seat Map' })).not.toBeInTheDocument()
    await expect.element(page.getByRole('heading', { name: 'Choose Seats' })).not.toBeInTheDocument()
    await expect.element(page.getByText('Nebula Express')).not.toBeInTheDocument()
  })

  it('does not keep the previous Seat Map while another Showtime loads', async () => {
    let resolveNext!: (map: ShowtimeSeats) => void
    const client = clientStub({
      getShowtimeSeats: vi.fn((showtimeId: number) => {
        if (showtimeId === 11) {
          return Promise.resolve(seatMap)
        }
        return new Promise<ShowtimeSeats>((resolve) => {
          resolveNext = resolve
        })
      }),
    })
    await renderSeats(client)
    await expect.element(page.getByRole('button', { name: 'Seat A1, available' })).toBeInTheDocument()

    await page.getByRole('link', { name: 'Open showtime 12' }).click()

    await expect.element(page.getByRole('status')).toHaveTextContent('Loading Seats…')
    await expect.element(page.getByRole('group', { name: 'Seat Map' })).not.toBeInTheDocument()
    await expect.element(page.getByText('Nebula Express')).not.toBeInTheDocument()
    await expect
      .poll(() => vi.mocked(client.getShowtimeSeats).mock.calls.some((call) => call[0] === 12))
      .toBe(true)

    resolveNext({
      ...seatMap,
      showtimeId: 12,
      movieTitle: 'Other Gate',
      seats: [
        { id: 21, rowLabel: 'B', seatNumber: 1, label: 'B1', available: true },
      ],
    })

    await expect.element(page.getByRole('heading', { name: 'Choose Seats' })).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Seat B1, available' })).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Seat A1, available' })).not.toBeInTheDocument()
  })

  it('rejects Seat selection after the Booking Cutoff', async () => {
    const client = clientStub({
      getShowtimeSeats: vi.fn().mockRejectedValue(new CustomerRequestError(409, 'booking.cutoff')),
    })
    await renderSeats(client)
    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Online checkout is closed at the Booking Cutoff, 15 minutes before this Showtime.')
    await expect.element(page.getByRole('group', { name: 'Seat Map' })).not.toBeInTheDocument()
  })

  it('holds selected Seats and shows the remaining hold time', async () => {
    const createdAt = new Date()
    const unavailableMap: ShowtimeSeats = {
      ...seatMap,
      seats: [{ ...seatMap.seats[0], available: false }, ...seatMap.seats.slice(1)],
    }
    const client = clientStub({
      getShowtimeSeats: vi.fn().mockResolvedValueOnce(seatMap).mockResolvedValue(unavailableMap),
      createSeatHold: vi.fn().mockResolvedValue({
        holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
        showtimeId: 11,
        seatIds: [1],
        serverTime: createdAt.toISOString(),
        expiresAt: new Date(createdAt.getTime() + 600_000).toISOString(),
      }),
    })
    await renderSeats(client)
    await page.getByRole('button', { name: 'Seat A1, available' }).click()

    await page.getByRole('button', { name: 'Hold selected Seats' }).click()

    expect(client.createSeatHold).toHaveBeenCalledWith(11, [1])
    await expect
      .element(page.getByText('Seats held for 10:00.'))
      .toHaveTextContent('Seats held for 10:00.')
    await expect.element(page.getByRole('button', { name: 'Seat A1, selected' })).toHaveAttribute(
      'aria-pressed',
      'true',
    )
    await page.getByRole('button', { name: 'Seat A1, selected' }).click()
    await expect.element(page.getByLabelText('Seat A1 Ticket Type')).toBeInTheDocument()
  })

  it('does not change Seat selection while a Seat Hold request is in flight', async () => {
    let resolveHold!: (hold: SeatHold) => void
    const client = clientStub({
      createSeatHold: vi.fn(
        () =>
          new Promise<SeatHold>((resolve) => {
            resolveHold = resolve
          }),
      ),
    })
    await renderSeats(client)
    await page.getByRole('button', { name: 'Seat A1, available' }).click()
    await page.getByRole('button', { name: 'Hold selected Seats' }).click()
    await expect.element(page.getByRole('button', { name: 'Holding Seats…' })).toBeDisabled()

    await page.getByRole('button', { name: 'Seat A2, available' }).click()
    await expect.element(page.getByRole('button', { name: 'Seat A2, available' })).toBeInTheDocument()
    await expect.element(page.getByLabelText('Seat A2 Ticket Type')).not.toBeInTheDocument()

    const createdAt = new Date()
    resolveHold({
      holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
      showtimeId: 11,
      seatIds: [1],
      serverTime: createdAt.toISOString(),
      expiresAt: new Date(createdAt.getTime() + 600_000).toISOString(),
    })

    await expect.element(page.getByText('Seats held for 10:00.')).toBeInTheDocument()
    await expect.element(page.getByLabelText('Seat A1 Ticket Type')).toBeInTheDocument()
    await expect.element(page.getByLabelText('Seat A2 Ticket Type')).not.toBeInTheDocument()
    expect(client.createSeatHold).toHaveBeenCalledWith(11, [1])
  })

  it('refreshes authoritative availability after a Seat Availability event', async () => {
    let onAvailabilityChanged!: () => void
    const socket: SeatAvailabilitySocket = {
      connect: vi.fn((_, callback) => {
        onAvailabilityChanged = callback
      }),
      disconnect: vi.fn(),
    }
    const client = clientStub({
      getShowtimeSeats: vi
        .fn()
        .mockResolvedValueOnce(seatMap)
        .mockResolvedValueOnce({
          ...seatMap,
          seats: [
            { ...seatMap.seats[0], available: false },
            ...seatMap.seats.slice(1),
          ],
        }),
    })

    await renderSeats(client, '/showtimes/11', socket)
    await expect.element(page.getByRole('button', { name: 'Seat A1, available' })).toBeInTheDocument()
    await page.getByRole('button', { name: 'Seat A1, available' }).click()

    onAvailabilityChanged()

    await expect
      .element(page.getByRole('button', { name: 'Seat A1, unavailable' }))
      .toBeInTheDocument()
    await expect.element(page.getByText('No Seats selected yet.')).toBeInTheDocument()
    expect(socket.connect).toHaveBeenCalledWith(11, expect.any(Function))
    expect(client.getShowtimeSeats).toHaveBeenCalledTimes(2)
  })

  it('returns to current availability when the Seat Hold expires', async () => {
    const createdAt = new Date()
    const client = clientStub({
      createSeatHold: vi.fn().mockResolvedValue({
        holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
        showtimeId: 11,
        seatIds: [1],
        serverTime: createdAt.toISOString(),
        expiresAt: new Date(createdAt.getTime() - 1_000).toISOString(),
      }),
    })
    await renderSeats(client)
    await page.getByRole('button', { name: 'Seat A1, available' }).click()
    await page.getByRole('button', { name: 'Hold selected Seats' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Your Seat Hold expired. Current availability has been refreshed.')
    await expect.element(page.getByText('No Seats selected yet.')).toBeInTheDocument()
  })

  it('returns not-found when the Showtime Seat Map is missing', async () => {
    const client = clientStub({
      getShowtimeSeats: vi
        .fn()
        .mockRejectedValue(new CustomerRequestError(404, 'booking.showtime_not_found')),
    })
    await renderSeats(client)
    await expect.element(page.getByRole('alert')).toHaveTextContent('Showtime not found.')
    await expect.element(page.getByRole('group', { name: 'Seat Map' })).not.toBeInTheDocument()
  })

  it('keeps the Seat Map before the Booking summary in DOM order for Meaningful Sequence', async () => {
    await renderSeats()
    const seatMap = page.getByRole('group', { name: 'Seat Map' }).element()
    const summary = page.getByRole('complementary', { name: 'Booking summary' }).element()
    expect(seatMap.compareDocumentPosition(summary) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy()
  })

  it('shows a visible focus ring when a Seat receives keyboard focus', async () => {
    await renderSeats()
    await expect.element(page.getByRole('button', { name: 'Seat A1, available' })).toBeInTheDocument()

    page.getByRole('link', { name: 'Open showtime 11' }).element().focus()
    await userEvent.keyboard('{Tab}')
    await userEvent.keyboard('{Tab}')
    await userEvent.keyboard('{Tab}')
    await userEvent.keyboard('{Tab}')

    const seat = page.getByRole('button', { name: 'Seat A1, available' })
    await expect.poll(() => seat.element() === seat.element().ownerDocument.activeElement).toBe(true)

    const style = getComputedStyle(seat.element())
    const hasRingShadow = style.getPropertyValue('--tw-ring-shadow').trim() !== ''
    const hasBoxShadow = style.boxShadow !== 'none' && style.boxShadow !== ''
    const hasOutline = style.outlineStyle !== 'none' && style.outlineStyle !== ''
    expect(hasRingShadow || hasBoxShadow || hasOutline).toBe(true)
  })

  it('has no serious axe violations on the Seats route', async () => {
    const screen = await renderSeats()
    await expect.element(page.getByRole('button', { name: 'Seat A1, available' })).toBeInTheDocument()
    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
