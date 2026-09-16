import { MemoryRouter, Route, Routes } from 'react-router'
import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page, userEvent } from 'vitest/browser'
import axe from 'axe-core'
import { SeatSelectionPage } from '@/booking/SeatSelectionPage.tsx'
import { CatalogRequestError, type CatalogClient, type ShowtimeSeats } from '@/catalog/api/catalogClient.ts'

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
  checkoutOpen: true,
  seats: [
    { id: 1, rowLabel: 'A', seatNumber: 1, label: 'A1', available: true },
    { id: 2, rowLabel: 'A', seatNumber: 2, label: 'A2', available: true },
    { id: 3, rowLabel: 'A', seatNumber: 3, label: 'A3', available: true },
    { id: 4, rowLabel: 'A', seatNumber: 4, label: 'A4', available: false },
  ],
}

function clientStub(overrides: Partial<CatalogClient> = {}): CatalogClient {
  return {
    listMovies: vi.fn(),
    getShowtimeSeats: vi.fn().mockResolvedValue(seatMap),
    ...overrides,
  }
}

async function renderSeats(client: CatalogClient = clientStub()) {
  return render(
    <MemoryRouter initialEntries={['/showtimes/11']}>
      <Routes>
        <Route path="/showtimes/:showtimeId" element={<SeatSelectionPage client={client} />} />
      </Routes>
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

    await page.getByRole('button', { name: 'Seat A1, available' }).click()
    await page.getByRole('button', { name: 'Seat A2, available' }).click()
    await expect.element(page.getByRole('button', { name: 'Seat A1, selected' })).toHaveAttribute(
      'aria-pressed',
      'true',
    )
    await expect.element(page.getByText('Total RM 56.00')).toBeInTheDocument()

    await page.getByLabelText('Seat A2 Ticket Type').selectOptions('CHILD')
    await expect.element(page.getByText('Total RM 46.00')).toBeInTheDocument()

    await page.getByRole('button', { name: 'Seat A3, available' }).click()
    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('This Booking can include at most 2 Seats.')
    await expect.element(page.getByRole('button', { name: 'Seat A3, available' })).toBeInTheDocument()
  })

  it('is keyboard operable and keeps the summary on a narrow viewport', async () => {
    await page.viewport(390, 844)
    await renderSeats()
    await expect.element(page.getByRole('heading', { name: 'Choose Seats' })).toBeInTheDocument()

    const firstSeat = page.getByRole('button', { name: 'Seat A1, available' })
    firstSeat.element().focus()
    await userEvent.keyboard('{Enter}')
    await expect.element(page.getByRole('button', { name: 'Seat A1, selected' })).toBeInTheDocument()
    await expect.element(page.getByRole('heading', { name: 'Booking summary' })).toBeInTheDocument()
    await expect.element(page.getByText('Total RM 28.00')).toBeInTheDocument()
  })

  it('rejects Seat selection after the Booking Cutoff', async () => {
    const client = clientStub({
      getShowtimeSeats: vi.fn().mockRejectedValue(new CatalogRequestError(409, 'booking.cutoff')),
    })
    await renderSeats(client)
    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Online checkout closed 15 minutes before this Showtime.')
    await expect.element(page.getByRole('group', { name: 'Seat Map' })).not.toBeInTheDocument()
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
