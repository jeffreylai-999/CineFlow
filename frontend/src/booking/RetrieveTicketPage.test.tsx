import { MemoryRouter, Route, Routes } from 'react-router'
import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page } from 'vitest/browser'
import axe from 'axe-core'
import {
  CustomerRequestError,
  type BookingConfirmation,
  type CustomerClient,
} from '@/booking/api/customerClient.ts'
import { RetrieveTicketPage } from '@/booking/RetrieveTicketPage.tsx'

const retrievedTicket: BookingConfirmation = {
  bookingReference: 'K7QX2M9T4D',
  showtimeId: 11,
  movieTitle: 'Nebula Express',
  hallName: 'Fixture Hall',
  startsAtCinemaTime: '2099-06-20T19:30:00',
  timeZone: 'Asia/Kuala_Lumpur',
  email: 'aisyah@example.com',
  seats: [{ seatId: 1, label: 'A1', ticketType: 'ADULT', priceMyr: 28 }],
  totalMyr: 28,
  admissionToken: null,
}

function clientStub(overrides: Partial<CustomerClient> = {}): CustomerClient {
  return {
    listMovies: vi.fn(),
    getShowtimeSeats: vi.fn(),
    createSeatHold: vi.fn(),
    checkout: vi.fn(),
    retrieveTicket: vi.fn(),
    ...overrides,
  }
}

function renderRetrieval(client: CustomerClient) {
  return render(
    <MemoryRouter initialEntries={['/ticket']}>
      <Routes>
        <Route path="/ticket" element={<RetrieveTicketPage client={client} />} />
        <Route path="/" element={<p>Catalog page</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

async function fillForm(email = 'aisyah@example.com', bookingReference = 'K7QX2M9T4D') {
  await page.getByLabelText('Email').fill(email)
  await page.getByLabelText('Booking Reference').fill(bookingReference)
}

describe('RetrieveTicketPage', () => {
  it('retrieves the Ticket with a matching email and Booking Reference', async () => {
    const client = clientStub({ retrieveTicket: vi.fn().mockResolvedValue(retrievedTicket) })
    await renderRetrieval(client)

    await expect.element(page.getByRole('heading', { name: 'Find your Ticket' })).toBeInTheDocument()

    await fillForm('AISYAH@example.com', ' k7qx2m9t4d ')
    await page.getByRole('button', { name: 'Find Ticket' }).click()

    expect(client.retrieveTicket).toHaveBeenCalledWith({
      email: 'AISYAH@example.com',
      bookingReference: 'k7qx2m9t4d',
    })
    await expect.element(page.getByRole('heading', { name: 'Your Ticket' })).toBeInTheDocument()
    await expect.element(page.getByText('K7QX2M9T4D')).toBeInTheDocument()
    await expect.element(page.getByText(/Nebula Express/)).toBeInTheDocument()
    await expect.element(page.getByText(/Seat A1 — Adult RM 28.00/)).toBeInTheDocument()
    await expect.element(page.getByText('Total RM 28.00')).toHaveAttribute('role', 'status')
    await expect.element(page.getByRole('img', { name: 'Ticket QR code' })).not.toBeInTheDocument()
    await expect.element(page.getByText(/Present this Booking Reference at admission/)).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Print Ticket' })).toBeInTheDocument()
  })

  it('shows one safe message when the combination does not match', async () => {
    const client = clientStub({
      retrieveTicket: vi.fn().mockRejectedValue(new CustomerRequestError(404, 'booking.retrieval_failed')),
    })
    await renderRetrieval(client)

    await fillForm()
    await page.getByRole('button', { name: 'Find Ticket' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent(
        'No Booking matches that email and Booking Reference. Check both values — retrieval stays open until seven days after the Showtime.',
      )
    await expect.element(page.getByRole('heading', { name: 'Your Ticket' })).not.toBeInTheDocument()
  })

  it('explains rate limiting', async () => {
    const client = clientStub({
      retrieveTicket: vi.fn().mockRejectedValue(new CustomerRequestError(429, 'booking.rate_limited')),
    })
    await renderRetrieval(client)

    await fillForm()
    await page.getByRole('button', { name: 'Find Ticket' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Too many attempts. Wait a moment and try again.')
  })

  it('shows a generic message for unexpected failures', async () => {
    const client = clientStub({
      retrieveTicket: vi.fn().mockRejectedValue(new Error('network down')),
    })
    await renderRetrieval(client)

    await fillForm()
    await page.getByRole('button', { name: 'Find Ticket' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Unable to retrieve the Booking. Try again shortly.')
  })

  it('links back to the catalog', async () => {
    const client = clientStub()
    await renderRetrieval(client)

    await page.getByRole('link', { name: 'Back to Showtimes' }).click()

    await expect.element(page.getByText('Catalog page')).toBeInTheDocument()
    expect(client.retrieveTicket).not.toHaveBeenCalled()
  })

  it('has no serious axe violations on the retrieval form', async () => {
    const screen = await renderRetrieval(clientStub())
    await expect.element(page.getByRole('heading', { name: 'Find your Ticket' })).toBeInTheDocument()
    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })

  it('has no serious axe violations on the retrieved Ticket', async () => {
    const client = clientStub({ retrieveTicket: vi.fn().mockResolvedValue(retrievedTicket) })
    const screen = await renderRetrieval(client)
    await fillForm()
    await page.getByRole('button', { name: 'Find Ticket' }).click()
    await expect.element(page.getByRole('heading', { name: 'Your Ticket' })).toBeInTheDocument()
    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
