import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page, userEvent } from 'vitest/browser'
import axe from 'axe-core'
import { MemoryRouter } from 'react-router'
import { AdmissionPage } from '@/admission/AdmissionPage.tsx'
import {
  AdmissionRequestError,
  type AdmissionClient,
  type AdmissionConfirmation,
} from '@/admission/api/admissionClient.ts'
import type { TicketScanner } from '@/admission/api/ticketScanner.ts'
import type { StaffSession } from '@/identity/api/identityClient.ts'

const bookingStaff: StaffSession = {
  accessToken: 'staff-token',
  expiresInSeconds: 900,
  staff: { id: 1, username: 'booking.staff', role: 'BOOKING_STAFF' },
}

const confirmation: AdmissionConfirmation = {
  bookingReference: 'K7M2PQ9X4D',
  movieTitle: 'Nebula Express',
  hallName: 'Hall 1',
  startsAtCinemaTime: '2099-06-20T19:30:00',
  timeZone: 'Asia/Kuala_Lumpur',
  seats: [
    { label: 'A1', ticketType: 'ADULT' },
    { label: 'A2', ticketType: 'CHILD' },
  ],
  admittedAt: '2026-09-16T00:00:00Z',
}

function fakeAdmissionClient(overrides: Partial<AdmissionClient> = {}): AdmissionClient {
  return {
    admitByToken: vi.fn().mockResolvedValue(confirmation),
    admitByReference: vi.fn().mockResolvedValue(confirmation),
    ...overrides,
  }
}

function fakeScanner(): TicketScanner & { emit: (token: string) => void } {
  let callback: ((token: string) => void) | null = null
  return {
    start: vi.fn(async (_video: HTMLVideoElement, onToken: (token: string) => void) => {
      callback = onToken
    }),
    stop: vi.fn(() => {
      callback = null
    }),
    emit: (token) => callback?.(token),
  }
}

function failingScanner(): TicketScanner {
  return {
    start: vi.fn().mockRejectedValue(new Error('no camera')),
    stop: vi.fn(),
  }
}

describe('AdmissionPage', () => {
  it('admits a Booking through the manual Booking Reference fallback', async () => {
    const client = fakeAdmissionClient()
    await render(
      <MemoryRouter>
        <AdmissionPage session={bookingStaff} client={client} onLogout={vi.fn()} />
      </MemoryRouter>,
    )

    await page.getByLabelText('Booking Reference').fill('k7m2pq9x4d')
    await page.getByRole('button', { name: 'Admit Booking' }).click()

    const heading = page.getByRole('heading', { name: 'Booking admitted' })
    await expect.element(heading).toBeInTheDocument()
    await expect.element(heading).toHaveFocus()
    await expect.element(page.getByText('Nebula Express · 2099-06-20T19:30:00 Asia/Kuala_Lumpur · Hall 1')).toBeInTheDocument()
    await expect.element(page.getByText('K7M2PQ9X4D')).toBeInTheDocument()
    await expect.element(page.getByText('Seat A1 — Adult')).toBeInTheDocument()
    await expect.element(page.getByText('Seat A2 — Child')).toBeInTheDocument()
    await expect.element(page.getByText('Admit 2 people for this Booking.')).toBeInTheDocument()
    expect(client.admitByReference).toHaveBeenCalledWith('K7M2PQ9X4D')
    expect(client.admitByToken).not.toHaveBeenCalled()
  })

  it('returns to an empty form with focus when admitting another Booking', async () => {
    const client = fakeAdmissionClient()
    await render(
      <MemoryRouter>
        <AdmissionPage session={bookingStaff} client={client} onLogout={vi.fn()} />
      </MemoryRouter>,
    )

    await page.getByLabelText('Booking Reference').fill('K7M2PQ9X4D')
    await page.getByRole('button', { name: 'Admit Booking' }).click()
    await expect.element(page.getByRole('heading', { name: 'Booking admitted' })).toBeInTheDocument()

    await page.getByRole('button', { name: 'Admit another Booking' }).click()

    const input = page.getByLabelText('Booking Reference')
    await expect.element(input).toBeInTheDocument()
    await expect.element(input).toHaveValue('')
    await expect.element(input).toHaveFocus()
  })

  it('reports a repeated Admission without admitting again', async () => {
    const client = fakeAdmissionClient({
      admitByReference: vi.fn().mockRejectedValue(new AdmissionRequestError(409, 'admission.already_admitted')),
    })
    await render(
      <MemoryRouter>
        <AdmissionPage session={bookingStaff} client={client} onLogout={vi.fn()} />
      </MemoryRouter>,
    )

    await page.getByLabelText('Booking Reference').fill('K7M2PQ9X4D')
    await page.getByRole('button', { name: 'Admit Booking' }).click()

    const heading = page.getByRole('heading', { name: 'Already admitted' })
    await expect.element(heading).toBeInTheDocument()
    await expect.element(heading).toHaveFocus()
    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent(
        'This Booking was already admitted. Its Ticket has been used before — do not admit the group again without checking.',
      )
  })

  it('reports an unknown Booking Reference safely and keeps the form', async () => {
    const client = fakeAdmissionClient({
      admitByReference: vi.fn().mockRejectedValue(new AdmissionRequestError(404, 'admission.not_found')),
    })
    await render(
      <MemoryRouter>
        <AdmissionPage session={bookingStaff} client={client} onLogout={vi.fn()} />
      </MemoryRouter>,
    )

    await page.getByLabelText('Booking Reference').fill('ZZZZZZZZZZ')
    await page.getByRole('button', { name: 'Admit Booking' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('No Booking matches. Check the Booking Reference or scan the Ticket again.')
    await expect.element(page.getByLabelText('Booking Reference')).toBeInTheDocument()
    await expect.element(page.getByRole('heading', { name: 'Booking admitted' })).not.toBeInTheDocument()
  })

  it('shows a safe message when the Admission request fails', async () => {
    const client = fakeAdmissionClient({
      admitByReference: vi.fn().mockRejectedValue(new Error('network down')),
    })
    await render(
      <MemoryRouter>
        <AdmissionPage session={bookingStaff} client={client} onLogout={vi.fn()} />
      </MemoryRouter>,
    )

    await page.getByLabelText('Booking Reference').fill('K7M2PQ9X4D')
    await page.getByRole('button', { name: 'Admit Booking' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Unable to admit this Booking. Try again shortly.')
  })

  it('submits the Booking Reference from the keyboard', async () => {
    const client = fakeAdmissionClient()
    await render(
      <MemoryRouter>
        <AdmissionPage session={bookingStaff} client={client} onLogout={vi.fn()} />
      </MemoryRouter>,
    )

    await page.getByLabelText('Booking Reference').fill('K7M2PQ9X4D')
    await userEvent.keyboard('{Enter}')

    await expect.element(page.getByRole('heading', { name: 'Booking admitted' })).toBeInTheDocument()
    expect(client.admitByReference).toHaveBeenCalledWith('K7M2PQ9X4D')
  })

  it('admits a Booking from a scanned Ticket QR token', async () => {
    const client = fakeAdmissionClient()
    const scanner = fakeScanner()
    await render(
      <MemoryRouter>
        <AdmissionPage session={bookingStaff} client={client} onLogout={vi.fn()} scanner={scanner} />
      </MemoryRouter>,
    )

    await page.getByRole('button', { name: 'Start camera' }).click()
    await expect.element(page.getByText('Camera on. Point it at the Ticket QR code.')).toBeInTheDocument()

    scanner.emit('scanned-qr-token')

    await expect.element(page.getByRole('heading', { name: 'Booking admitted' })).toBeInTheDocument()
    expect(client.admitByToken).toHaveBeenCalledWith('scanned-qr-token')
    expect(client.admitByReference).not.toHaveBeenCalled()
    await vi.waitFor(() => {
      expect(scanner.stop).toHaveBeenCalled()
    })
  })

  it('ignores a duplicate scan while the first Admission is still running', async () => {
    let finish: (value: AdmissionConfirmation) => void = () => {}
    const client = fakeAdmissionClient({
      admitByToken: vi.fn(
        () =>
          new Promise<AdmissionConfirmation>((resolve) => {
            finish = resolve
          }),
      ),
    })
    const scanner = fakeScanner()
    await render(
      <MemoryRouter>
        <AdmissionPage session={bookingStaff} client={client} onLogout={vi.fn()} scanner={scanner} />
      </MemoryRouter>,
    )

    await page.getByRole('button', { name: 'Start camera' }).click()
    await expect.element(page.getByText('Camera on. Point it at the Ticket QR code.')).toBeInTheDocument()

    scanner.emit('scanned-qr-token')
    scanner.emit('scanned-qr-token')
    scanner.emit('scanned-qr-token')

    await vi.waitFor(() => {
      expect(client.admitByToken).toHaveBeenCalledTimes(1)
    })
    finish(confirmation)
    await expect.element(page.getByRole('heading', { name: 'Booking admitted' })).toBeInTheDocument()
  })

  it('announces when a different Ticket is scanned while an Admission is running', async () => {
    let finish: (value: AdmissionConfirmation) => void = () => {}
    const client = fakeAdmissionClient({
      admitByToken: vi.fn(
        () =>
          new Promise<AdmissionConfirmation>((resolve) => {
            finish = resolve
          }),
      ),
    })
    const scanner = fakeScanner()
    await render(
      <MemoryRouter>
        <AdmissionPage session={bookingStaff} client={client} onLogout={vi.fn()} scanner={scanner} />
      </MemoryRouter>,
    )

    await page.getByRole('button', { name: 'Start camera' }).click()
    await expect.element(page.getByText('Camera on. Point it at the Ticket QR code.')).toBeInTheDocument()

    scanner.emit('first-token')
    scanner.emit('second-token')

    await expect
      .element(
        page.getByText(
          'Still admitting the previous Booking. Wait for the result before scanning the next Ticket.',
        ),
      )
      .toBeInTheDocument()
    expect(client.admitByToken).toHaveBeenCalledTimes(1)

    finish(confirmation)
    await expect.element(page.getByRole('heading', { name: 'Booking admitted' })).toBeInTheDocument()
  })

  it('does not auto-resubmit a failed scan while the same QR stays in view', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true })
    const client = fakeAdmissionClient({
      admitByToken: vi.fn().mockRejectedValue(new AdmissionRequestError(404, 'admission.not_found')),
    })
    const scanner = fakeScanner()
    try {
      await render(
        <MemoryRouter>
          <AdmissionPage session={bookingStaff} client={client} onLogout={vi.fn()} scanner={scanner} />
        </MemoryRouter>,
      )

      await page.getByRole('button', { name: 'Start camera' }).click()
      await expect.element(page.getByText('Camera on. Point it at the Ticket QR code.')).toBeInTheDocument()

      scanner.emit('bad-token')
      await expect
        .element(page.getByRole('alert'))
        .toHaveTextContent('No Booking matches. Check the Booking Reference or scan the Ticket again.')
      expect(client.admitByToken).toHaveBeenCalledTimes(1)

      await vi.advanceTimersByTimeAsync(4_000)
      scanner.emit('bad-token')
      scanner.emit('bad-token')
      expect(client.admitByToken).toHaveBeenCalledTimes(1)

      await page.getByRole('button', { name: 'Stop camera' }).click()
      await page.getByRole('button', { name: 'Start camera' }).click()
      await expect.element(page.getByText('Camera on. Point it at the Ticket QR code.')).toBeInTheDocument()
      scanner.emit('bad-token')
      await expect.element(page.getByRole('alert')).toBeInTheDocument()
      expect(client.admitByToken).toHaveBeenCalledTimes(2)
    } finally {
      vi.useRealTimers()
    }
  })

  it('keeps the manual fallback when the camera is unavailable', async () => {
    const client = fakeAdmissionClient()
    await render(
      <MemoryRouter>
        <AdmissionPage session={bookingStaff} client={client} onLogout={vi.fn()} scanner={failingScanner()} />
      </MemoryRouter>,
    )

    await page.getByRole('button', { name: 'Start camera' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Camera scanning is unavailable. Enter the Booking Reference instead.')

    await page.getByLabelText('Booking Reference').fill('K7M2PQ9X4D')
    await page.getByRole('button', { name: 'Admit Booking' }).click()
    await expect.element(page.getByRole('heading', { name: 'Booking admitted' })).toBeInTheDocument()
  })

  it('has no serious axe violations', async () => {
    const client = fakeAdmissionClient()
    const screen = await render(
      <MemoryRouter>
        <AdmissionPage session={bookingStaff} client={client} onLogout={vi.fn()} scanner={fakeScanner()} />
      </MemoryRouter>,
    )
    await expect.element(page.getByRole('heading', { name: 'Admission' })).toBeInTheDocument()

    await page.getByRole('button', { name: 'Start camera' }).click()
    await expect.element(page.getByText('Camera on. Point it at the Ticket QR code.')).toBeInTheDocument()

    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
