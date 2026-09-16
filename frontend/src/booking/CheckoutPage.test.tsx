import { MemoryRouter, Route, Routes } from 'react-router'
import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page } from 'vitest/browser'
import axe from 'axe-core'
import {
  CustomerRequestError,
  type BookingConfirmation,
  type CustomerClient,
  type SeatHold,
  type ShowtimeSeats,
} from '@/booking/api/customerClient.ts'
import { CheckoutPage, type CheckoutLocationState } from '@/booking/CheckoutPage.tsx'

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
  ],
}

function activeHold(): SeatHold {
  const now = Date.now()
  return {
    holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
    showtimeId: 11,
    seatIds: [1, 2],
    serverTime: new Date(now).toISOString(),
    expiresAt: new Date(now + 600_000).toISOString(),
  }
}

function checkoutState(overrides: Partial<CheckoutLocationState> = {}): CheckoutLocationState {
  return {
    hold: activeHold(),
    holdReceivedAt: performance.now(),
    selection: { 1: 'ADULT', 2: 'CHILD' },
    map: seatMap,
    ...overrides,
  }
}

const confirmation: BookingConfirmation = {
  bookingReference: 'K7QX2M9T4D',
  showtimeId: 11,
  movieTitle: 'Nebula Express',
  hallName: 'Fixture Hall',
  startsAtCinemaTime: '2099-06-20T19:30:00',
  timeZone: 'Asia/Kuala_Lumpur',
  email: 'aisyah@example.com',
  seats: [
    { seatId: 1, label: 'A1', ticketType: 'ADULT', priceMyr: 28 },
    { seatId: 2, label: 'A2', ticketType: 'CHILD', priceMyr: 18 },
  ],
  totalMyr: 46,
  admissionToken: 'opaque-admission-token',
}

function clientStub(overrides: Partial<CustomerClient> = {}): CustomerClient {
  return {
    listMovies: vi.fn(),
    getShowtimeSeats: vi.fn().mockResolvedValue(seatMap),
    createSeatHold: vi.fn(),
    checkout: vi.fn(),
    ...overrides,
  }
}

function renderCheckout(client: CustomerClient, state: CheckoutLocationState | null = checkoutState()) {
  return render(
    <MemoryRouter initialEntries={[{ pathname: '/showtimes/11/checkout', state }]}>
      <Routes>
        <Route path="/showtimes/:showtimeId" element={<p>Seat selection page</p>} />
        <Route path="/showtimes/:showtimeId/checkout" element={<CheckoutPage client={client} />} />
      </Routes>
    </MemoryRouter>,
  )
}

async function payWithCard(email = 'aisyah@example.com', cardNumber = '4242424242424242') {
  await page.getByLabelText('Email').fill(email)
  await page.getByLabelText('Card number').fill(cardNumber)
}

describe('CheckoutPage', () => {
  it('completes checkout and shows the Ticket with Booking Reference and QR code', async () => {
    const client = clientStub({ checkout: vi.fn().mockResolvedValue(confirmation) })
    await renderCheckout(client)

    await expect.element(page.getByRole('heading', { name: 'Checkout' })).toBeInTheDocument()
    await expect.element(page.getByText(/Nebula Express/)).toBeInTheDocument()
    await expect.element(page.getByText(/Seats held for 10:00/)).toBeInTheDocument()
    await expect.element(page.getByText('Total RM 46.00')).toHaveAttribute('role', 'status')

    await page.getByLabelText('Seat A2 Ticket Type').selectOptions('ADULT')
    await expect.element(page.getByText('Total RM 56.00')).toHaveAttribute('role', 'status')

    await payWithCard()
    await page.getByRole('button', { name: 'Pay RM 56.00' }).click()

    expect(client.checkout).toHaveBeenCalledWith(11, {
      holdId: 'aabccabe-79c4-44d8-b38f-a1b64d4526d8',
      email: 'aisyah@example.com',
      tickets: [
        { seatId: 1, ticketType: 'ADULT' },
        { seatId: 2, ticketType: 'ADULT' },
      ],
      cardNumber: '4242424242424242',
      idempotencyKey: expect.any(String),
    })

    await expect.element(page.getByRole('heading', { name: 'Booking confirmed' })).toBeInTheDocument()
    await expect.element(page.getByText('K7QX2M9T4D')).toBeInTheDocument()
    await expect.element(page.getByRole('img', { name: 'Ticket QR code' })).toBeInTheDocument()
    await expect.element(page.getByText(/Seat A1 — Adult RM 28.00/)).toBeInTheDocument()
    await expect.element(page.getByText(/Seat A2 — Child RM 18.00/)).toBeInTheDocument()
    await expect.element(page.getByText('Total RM 46.00')).toHaveAttribute('role', 'status')
    await expect.element(page.getByRole('button', { name: 'Print Ticket' })).toBeInTheDocument()
  })

  it('keeps the Seat Hold active after a declined Payment and retries with the same idempotency key', async () => {
    const checkout = vi
      .fn()
      .mockRejectedValueOnce(new CustomerRequestError(402, 'booking.payment_declined'))
      .mockResolvedValueOnce(confirmation)
    const client = clientStub({ checkout })
    await renderCheckout(client)

    await payWithCard()
    await page.getByRole('button', { name: /Pay RM/ }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent(
        'Payment was declined. No Payment was recorded and your Seats are still held — check the card number and try again.',
      )

    await page.getByRole('button', { name: /Pay RM/ }).click()

    await expect.element(page.getByRole('heading', { name: 'Booking confirmed' })).toBeInTheDocument()
    expect(checkout).toHaveBeenCalledTimes(2)
    const firstKey = checkout.mock.calls[0][1].idempotencyKey
    expect(firstKey).toEqual(expect.any(String))
    expect(checkout.mock.calls[1][1].idempotencyKey).toBe(firstKey)
  })

  it('explains when the Seat Hold is no longer active and links back to Seat selection', async () => {
    const client = clientStub({
      checkout: vi.fn().mockRejectedValue(new CustomerRequestError(409, 'booking.hold_expired')),
    })
    await renderCheckout(client)

    await payWithCard()
    await page.getByRole('button', { name: /Pay RM/ }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Your Seat Hold is no longer active. Choose Seats again to restart checkout.')
    await expect.element(page.getByRole('link', { name: 'Choose Seats again' })).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: /Pay RM/ })).toBeDisabled()

    await page.getByRole('link', { name: 'Choose Seats again' }).click()
    await expect.element(page.getByText('Seat selection page')).toBeInTheDocument()
  })

  it('refreshes the stored Ticket Prices when entering checkout', async () => {
    const client = clientStub({
      getShowtimeSeats: vi.fn().mockResolvedValue({ ...seatMap, adultPriceMyr: 33.5 }),
    })
    await renderCheckout(client)

    await expect.element(page.getByRole('heading', { name: 'Checkout' })).toBeInTheDocument()
    await expect.element(page.getByText('Total RM 51.50')).toHaveAttribute('role', 'status')
    await expect.element(page.getByRole('button', { name: 'Pay RM 51.50' })).toBeInTheDocument()
    expect(client.getShowtimeSeats).toHaveBeenCalledWith(11)
  })

  it('redirects to Seat selection when the checkout state is missing', async () => {
    const client = clientStub()
    await renderCheckout(client, null)

    await expect.element(page.getByText('Seat selection page')).toBeInTheDocument()
    await expect.element(page.getByRole('heading', { name: 'Checkout' })).not.toBeInTheDocument()
    expect(client.checkout).not.toHaveBeenCalled()
  })

  it('shows the already-confirmed note when a replay has no Admission token', async () => {
    const client = clientStub({
      checkout: vi.fn().mockResolvedValue({ ...confirmation, admissionToken: null }),
    })
    await renderCheckout(client)

    await payWithCard()
    await page.getByRole('button', { name: /Pay RM/ }).click()

    await expect.element(page.getByRole('heading', { name: 'Booking confirmed' })).toBeInTheDocument()
    await expect.element(page.getByText('K7QX2M9T4D')).toBeInTheDocument()
    await expect.element(page.getByRole('img', { name: 'Ticket QR code' })).not.toBeInTheDocument()
    await expect
      .element(page.getByText(/This Booking was already confirmed/))
      .toBeInTheDocument()
  })

  it('expires the Seat Hold countdown during checkout', async () => {
    const now = Date.now()
    const expiredHold: SeatHold = {
      ...activeHold(),
      serverTime: new Date(now).toISOString(),
      expiresAt: new Date(now - 1_000).toISOString(),
    }
    const client = clientStub()
    await renderCheckout(client, checkoutState({ hold: expiredHold }))

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Your Seat Hold expired before the Payment completed. Choose Seats again to restart checkout.')
    await expect.element(page.getByRole('button', { name: /Pay RM/ })).toBeDisabled()
    expect(client.checkout).not.toHaveBeenCalled()
  })

  it('has no serious axe violations on the checkout form', async () => {
    const screen = await renderCheckout(clientStub())
    await expect.element(page.getByRole('heading', { name: 'Checkout' })).toBeInTheDocument()
    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })

  it('has no serious axe violations on the confirmed Ticket', async () => {
    const client = clientStub({ checkout: vi.fn().mockResolvedValue(confirmation) })
    const screen = await renderCheckout(client)
    await payWithCard()
    await page.getByRole('button', { name: /Pay RM/ }).click()
    await expect.element(page.getByRole('heading', { name: 'Booking confirmed' })).toBeInTheDocument()
    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
