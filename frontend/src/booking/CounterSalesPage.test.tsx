import { MemoryRouter, Route, Routes } from 'react-router'
import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page } from 'vitest/browser'
import axe from 'axe-core'
import {
  StaffBookingRequestError,
  type CounterShowtime,
  type StaffBookingClient,
} from '@/booking/api/staffBookingClient.ts'
import { CounterSalesPage } from '@/booking/CounterSalesPage.tsx'
import type { StaffSession } from '@/identity/api/identityClient.ts'

const bookingStaffSession: StaffSession = {
  accessToken: 'staff-token',
  expiresInSeconds: 900,
  staff: { id: 1, username: 'booking.staff', role: 'BOOKING_STAFF' },
}

const showtimes: CounterShowtime[] = [
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
  {
    id: 12,
    movieTitle: 'Second Contact',
    hallName: 'Fixture Hall',
    startsAtCinemaTime: '2099-06-20T21:00:00',
    timeZone: 'Asia/Kuala_Lumpur',
    adultPriceMyr: 24,
    childPriceMyr: 14,
    counterSalesOpen: true,
  },
]

function clientStub(overrides: Partial<StaffBookingClient> = {}): StaffBookingClient {
  return {
    listCounterShowtimes: vi.fn().mockResolvedValue(showtimes),
    getStaffSeatMap: vi.fn(),
    createCounterHold: vi.fn(),
    confirmCounterSale: vi.fn(),
    ...overrides,
  }
}

function renderCounterSales(client: StaffBookingClient) {
  return render(
    <MemoryRouter initialEntries={['/staff/counter-sales']}>
      <Routes>
        <Route
          path="/staff/counter-sales"
          element={
            <CounterSalesPage session={bookingStaffSession} client={client} onLogout={() => undefined} />
          }
        />
        <Route path="/staff/counter-sales/:showtimeId" element={<p>Counter sale seat map</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('CounterSalesPage', () => {
  it('lists Showtimes open for counter sales with prices and Seat selection links', async () => {
    const client = clientStub()
    await renderCounterSales(client)

    await expect.element(page.getByRole('heading', { name: 'Counter sales' })).toBeInTheDocument()
    await expect.element(page.getByRole('cell', { name: 'Nebula Express' })).toBeInTheDocument()
    await expect.element(page.getByRole('cell', { name: 'RM 28.00' })).toBeInTheDocument()
    await expect.element(page.getByRole('cell', { name: 'RM 18.00' })).toBeInTheDocument()

    const sellLinks = page.getByRole('link', { name: 'Sell Seats' })
    await expect.element(sellLinks.first()).toBeInTheDocument()
    await sellLinks.first().click()
    await expect.element(page.getByText('Counter sale seat map')).toBeInTheDocument()
  })

  it('shows an empty state when no Showtime is open for counter sales', async () => {
    const client = clientStub({ listCounterShowtimes: vi.fn().mockResolvedValue([]) })
    await renderCounterSales(client)

    await expect
      .element(page.getByText('No Showtimes are open for counter sales.'))
      .toBeInTheDocument()
  })

  it('shows a load error when the Showtime list fails', async () => {
    const client = clientStub({
      listCounterShowtimes: vi
        .fn()
        .mockRejectedValue(new StaffBookingRequestError(403, 'auth.forbidden')),
    })
    await renderCounterSales(client)

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Unable to load Showtimes. Try again shortly.')
  })

  it('has no serious axe violations', async () => {
    const client = clientStub()
    const screen = await renderCounterSales(client)
    await expect.element(page.getByRole('heading', { name: 'Counter sales' })).toBeInTheDocument()
    await expect.element(page.getByRole('cell', { name: 'Nebula Express' })).toBeInTheDocument()

    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
