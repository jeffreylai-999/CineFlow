import { MemoryRouter, Route, Routes } from 'react-router'
import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page } from 'vitest/browser'
import axe from 'axe-core'
import type { BookingConfirmation, SeatHold } from '@/booking/api/customerClient.ts'
import {
  StaffBookingRequestError,
  type StaffBookingClient,
  type StaffSeatMap,
} from '@/booking/api/staffBookingClient.ts'
import { CounterSalePage } from '@/booking/CounterSalePage.tsx'
import type { StaffSession } from '@/identity/api/identityClient.ts'

const bookingStaffSession: StaffSession = {
  accessToken: 'staff-token',
  expiresInSeconds: 900,
  staff: { id: 1, username: 'booking.staff', role: 'BOOKING_STAFF' },
}

const seatMap: StaffSeatMap = {
  showtimeId: 11,
  movieTitle: 'Nebula Express',
  hallName: 'Fixture Hall',
  startsAtCinemaTime: '2099-06-20T19:30:00',
  timeZone: 'Asia/Kuala_Lumpur',
  adultPriceMyr: 28,
  childPriceMyr: 18,
  bookingLimit: 4,
  counterSalesOpen: true,
  seats: [
    { id: 1, rowLabel: 'A', seatNumber: 1, label: 'A1', state: 'AVAILABLE' },
    { id: 2, rowLabel: 'A', seatNumber: 2, label: 'A2', state: 'AVAILABLE' },
    { id: 3, rowLabel: 'A', seatNumber: 3, label: 'A3', state: 'HELD' },
    { id: 4, rowLabel: 'A', seatNumber: 4, label: 'A4', state: 'BOOKED' },
    { id: 5, rowLabel: 'A', seatNumber: 5, label: 'A5', state: 'DISABLED' },
  ],
}

function activeHold(seatIds: number[] = [1, 2]): SeatHold {
  const now = Date.now()
  return {
    holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
    showtimeId: 11,
    seatIds,
    serverTime: new Date(now).toISOString(),
    expiresAt: new Date(now + 600_000).toISOString(),
  }
}

const confirmation: BookingConfirmation = {
  bookingReference: 'K7QX2M9T4D',
  showtimeId: 11,
  movieTitle: 'Nebula Express',
  hallName: 'Fixture Hall',
  startsAtCinemaTime: '2099-06-20T19:30:00',
  timeZone: 'Asia/Kuala_Lumpur',
  email: null,
  seats: [
    { seatId: 1, label: 'A1', ticketType: 'ADULT', priceMyr: 28 },
    { seatId: 2, label: 'A2', ticketType: 'CHILD', priceMyr: 18 },
  ],
  totalMyr: 46,
  admissionToken: 'opaque-admission-token',
}

function clientStub(overrides: Partial<StaffBookingClient> = {}): StaffBookingClient {
  return {
    listCounterShowtimes: vi.fn(),
    getStaffSeatMap: vi.fn().mockResolvedValue(seatMap),
    createCounterHold: vi.fn(),
    confirmCounterSale: vi.fn(),
    ...overrides,
  }
}

function renderCounterSale(client: StaffBookingClient) {
  return render(
    <MemoryRouter initialEntries={['/staff/counter-sales/11']}>
      <Routes>
        <Route path="/staff/counter-sales" element={<p>Counter sales list</p>} />
        <Route
          path="/staff/counter-sales/:showtimeId"
          element={
            <CounterSalePage session={bookingStaffSession} client={client} onLogout={() => undefined} />
          }
        />
      </Routes>
    </MemoryRouter>,
  )
}

describe('CounterSalePage', () => {
  it('shows Seat Hold, Booking, and Disabled Seat as distinct Seat states', async () => {
    const client = clientStub()
    await renderCounterSale(client)

    await expect.element(page.getByRole('heading', { name: 'Counter sale' })).toBeInTheDocument()

    const available = page.getByRole('button', { name: 'Seat A1, available' })
    const held = page.getByRole('button', { name: 'Seat A3, held' })
    const booked = page.getByRole('button', { name: 'Seat A4, booked' })
    const disabled = page.getByRole('button', { name: 'Seat A5, disabled' })
    await expect.element(available).toBeInTheDocument()
    await expect.element(held).toHaveAttribute('data-state', 'held')
    await expect.element(booked).toHaveAttribute('data-state', 'booked')
    await expect.element(disabled).toHaveAttribute('data-state', 'disabled')
    await expect.element(held).toHaveAttribute('aria-disabled', 'true')
    await expect.element(booked).toHaveAttribute('aria-disabled', 'true')
    await expect.element(page.getByText('◐')).toBeInTheDocument()
    await expect.element(page.getByText('✕')).toBeInTheDocument()

    await held.click({ force: true })
    await booked.click({ force: true })
    await disabled.click({ force: true })
    await expect.element(page.getByText('No Seats selected yet.')).toBeInTheDocument()
  })

  it('completes a Cash counter sale for a Walk-in Customer and shows the printable Ticket', async () => {
    const client = clientStub({
      createCounterHold: vi.fn().mockResolvedValue(activeHold()),
      confirmCounterSale: vi.fn().mockResolvedValue(confirmation),
    })
    await renderCounterSale(client)

    await page.getByRole('button', { name: 'Seat A1, available' }).click()
    await page.getByRole('button', { name: 'Seat A2, available' }).click()
    await page.getByLabelText('Seat A2 Ticket Type').selectOptions('CHILD')
    await expect.element(page.getByText('Total RM 46.00')).toHaveAttribute('role', 'status')

    await page.getByRole('button', { name: 'Hold selected Seats' }).click()
    expect(client.createCounterHold).toHaveBeenCalledWith(11, [1, 2])
    await expect.element(page.getByText(/Seats held for 10:00/)).toBeInTheDocument()

    await page.getByRole('button', { name: 'Confirm RM 46.00 Cash sale' }).click()
    expect(client.confirmCounterSale).toHaveBeenCalledWith(11, {
      holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
      tickets: [
        { seatId: 1, ticketType: 'ADULT' },
        { seatId: 2, ticketType: 'CHILD' },
      ],
      method: 'CASH',
      idempotencyKey: expect.any(String),
    })

    await expect.element(page.getByRole('heading', { name: 'Booking confirmed' })).toBeInTheDocument()
    await expect.element(page.getByText('K7QX2M9T4D')).toBeInTheDocument()
    await expect.element(page.getByText('Ticket for Walk-in Customer')).toBeInTheDocument()
    await expect.element(page.getByRole('img', { name: 'Ticket QR code' })).toBeInTheDocument()
    await expect.element(page.getByText(/Seat A1 — Adult RM 28.00/)).toBeInTheDocument()
    await expect.element(page.getByText(/Seat A2 — Child RM 18.00/)).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Print Ticket' })).toBeInTheDocument()
  })

  it('records a received Card Payment without card credentials', async () => {
    const client = clientStub({
      createCounterHold: vi.fn().mockResolvedValue(activeHold([1])),
      confirmCounterSale: vi.fn().mockResolvedValue(confirmation),
    })
    await renderCounterSale(client)

    await page.getByRole('button', { name: 'Seat A1, available' }).click()
    await page.getByRole('button', { name: 'Hold selected Seats' }).click()
    await page.getByRole('radio', { name: 'Card' }).click()

    await page.getByRole('button', { name: 'Confirm RM 28.00 Card sale' }).click()
    expect(client.confirmCounterSale).toHaveBeenCalledWith(11, {
      holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
      tickets: [{ seatId: 1, ticketType: 'ADULT' }],
      method: 'CARD',
      idempotencyKey: expect.any(String),
    })
    await expect.element(page.getByRole('heading', { name: 'Booking confirmed' })).toBeInTheDocument()
  })

  it('explains when the Counter Sales Cutoff has passed', async () => {
    const client = clientStub({
      createCounterHold: vi.fn().mockResolvedValue(activeHold()),
      confirmCounterSale: vi
        .fn()
        .mockRejectedValue(new StaffBookingRequestError(409, 'booking.counter_sales_cutoff')),
    })
    await renderCounterSale(client)

    await page.getByRole('button', { name: 'Seat A1, available' }).click()
    await page.getByRole('button', { name: 'Hold selected Seats' }).click()
    await page.getByRole('button', { name: /Confirm RM 28.00 Cash sale/ }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Counter sales closed fifteen minutes after the Showtime started.')
  })

  it('refreshes availability when selected Seats are no longer available', async () => {
    const client = clientStub({
      createCounterHold: vi
        .fn()
        .mockRejectedValue(new StaffBookingRequestError(409, 'booking.seats_unavailable')),
    })
    await renderCounterSale(client)

    await page.getByRole('button', { name: 'Seat A1, available' }).click()
    await page.getByRole('button', { name: 'Hold selected Seats' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent(
        'One or more selected Seats are no longer available. Current availability has been refreshed.',
      )
  })

  it('clears the sale when the Seat Hold is no longer active', async () => {
    const client = clientStub({
      createCounterHold: vi.fn().mockResolvedValue(activeHold()),
      confirmCounterSale: vi
        .fn()
        .mockRejectedValue(new StaffBookingRequestError(409, 'booking.hold_expired')),
    })
    await renderCounterSale(client)

    await page.getByRole('button', { name: 'Seat A1, available' }).click()
    await page.getByRole('button', { name: 'Hold selected Seats' }).click()
    await expect.element(page.getByText(/Seats held for 10:00/)).toBeInTheDocument()
    await page.getByRole('button', { name: /Confirm RM 28.00 Cash sale/ }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('The Seat Hold is no longer active. Choose Seats again to restart the sale.')
    await expect.element(page.getByRole('button', { name: 'Hold selected Seats' })).toBeInTheDocument()
  })

  it('enforces the Booking Limit during selection', async () => {
    const client = clientStub({
      getStaffSeatMap: vi.fn().mockResolvedValue({ ...seatMap, bookingLimit: 1 }),
    })
    await renderCounterSale(client)

    await page.getByRole('button', { name: 'Seat A1, available' }).click()
    await page.getByRole('button', { name: 'Seat A2, available' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('This Booking can include at most 1 Seat.')
    await expect.element(page.getByLabelText('Seat A2 Ticket Type')).not.toBeInTheDocument()
  })

  it('locks Ticket Type editing while the sale is confirming', async () => {
    let finishConfirm: (value: BookingConfirmation) => void = () => {}
    const client = clientStub({
      createCounterHold: vi.fn().mockResolvedValue(activeHold([1])),
      confirmCounterSale: vi.fn(
        () =>
          new Promise<BookingConfirmation>((resolve) => {
            finishConfirm = resolve
          }),
      ),
    })
    await renderCounterSale(client)

    await page.getByRole('button', { name: 'Seat A1, available' }).click()
    await page.getByRole('button', { name: 'Hold selected Seats' }).click()
    await expect.element(page.getByText(/Seats held for 10:00/)).toBeInTheDocument()
    await page.getByRole('button', { name: 'Confirm RM 28.00 Cash sale' }).click()

    await expect.element(page.getByLabelText('Seat A1 Ticket Type')).toBeDisabled()
    finishConfirm(confirmation)
    await expect.element(page.getByRole('heading', { name: 'Booking confirmed' })).toBeInTheDocument()
  })

  it('reports an unknown Showtime', async () => {
    const client = clientStub({
      getStaffSeatMap: vi
        .fn()
        .mockRejectedValue(new StaffBookingRequestError(404, 'booking.showtime_not_found')),
    })
    await renderCounterSale(client)

    await expect.element(page.getByRole('alert')).toHaveTextContent('Showtime not found.')
  })

  it('has no serious axe violations', async () => {
    const client = clientStub()
    const screen = await renderCounterSale(client)
    await expect.element(page.getByRole('heading', { name: 'Counter sale' })).toBeInTheDocument()

    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
