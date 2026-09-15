import { render } from 'vitest-browser-react'
import { describe, expect, it } from 'vitest'
import { page } from 'vitest/browser'
import axe from 'axe-core'
import { MemoryRouter } from 'react-router'
import type { StaffSession } from '@/identity/api/identityClient.ts'
import { StaffHomePage } from '@/identity/StaffHomePage.tsx'

const bookingStaff: StaffSession = {
  accessToken: 'staff-token',
  expiresInSeconds: 900,
  staff: { id: 1, username: 'booking.staff', role: 'BOOKING_STAFF' },
}

const administrator: StaffSession = {
  accessToken: 'admin-token',
  expiresInSeconds: 900,
  staff: { id: 2, username: 'administrator', role: 'ADMINISTRATOR' },
}

describe('StaffHomePage', () => {
  it('hides Administrator navigation from Booking Staff', async () => {
    await render(
      <MemoryRouter>
        <StaffHomePage session={bookingStaff} onLogout={() => undefined} />
      </MemoryRouter>,
    )

    await expect.element(page.getByRole('heading', { name: 'Staff portal' })).toBeInTheDocument()
    await expect.element(page.getByText(/booking.staff/)).toBeInTheDocument()
    await expect.element(page.getByRole('link', { name: 'Overview' })).toBeInTheDocument()
    await expect.element(page.getByRole('link', { name: 'Movies' })).not.toBeInTheDocument()
    await expect.element(page.getByRole('link', { name: 'Staff accounts' })).not.toBeInTheDocument()
    await expect.element(page.getByRole('link', { name: 'Halls' })).not.toBeInTheDocument()
  })

  it('shows Administrator navigation for Movies, Halls, and Staff accounts', async () => {
    await render(
      <MemoryRouter>
        <StaffHomePage session={administrator} onLogout={() => undefined} />
      </MemoryRouter>,
    )

    await expect.element(page.getByText(/Administrator/)).toBeInTheDocument()
    await expect.element(page.getByRole('link', { name: 'Movies' })).toHaveAttribute('href', '/staff/movies')
    await expect.element(page.getByRole('link', { name: 'Halls' })).toHaveAttribute('href', '/staff/halls')
    await expect
      .element(page.getByRole('link', { name: 'Staff accounts' }))
      .toHaveAttribute('href', '/staff/accounts')
  })

  it('has no serious axe violations on the staff portal', async () => {
    const screen = await render(
      <MemoryRouter>
        <StaffHomePage session={administrator} onLogout={() => undefined} />
      </MemoryRouter>,
    )
    await expect.element(page.getByRole('heading', { name: 'Staff portal' })).toBeInTheDocument()

    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
