import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page } from 'vitest/browser'
import axe from 'axe-core'
import type { StaffSession } from '@/identity/api/identityClient.ts'
import type { StaffSocket } from '@/identity/api/staffSocket.ts'
import { StaffAuthProvider } from '@/identity/StaffAuthProvider.tsx'
import { StaffLoginPage } from '@/identity/StaffLoginPage.tsx'
import { fakeIdentityClient } from '@/identity/test/fakeIdentityClient.ts'

const administrator: StaffSession = {
  accessToken: 'memory-access-token',
  expiresInSeconds: 900,
  staff: { id: 2, username: 'administrator', role: 'ADMINISTRATOR' },
}

function silentSocket(): StaffSocket {
  return {
    connect: vi.fn().mockResolvedValue(undefined),
    disconnect: vi.fn().mockResolvedValue(undefined),
  }
}

describe('StaffLoginPage', () => {
  it('signs Staff in and keeps the access token out of localStorage', async () => {
    const client = fakeIdentityClient({
      login: vi.fn().mockResolvedValue(administrator),
    })
    const onSignedIn = vi.fn()

    await render(
      <StaffAuthProvider client={client} socket={silentSocket()}>
        <StaffLoginPage onSignedIn={onSignedIn} />
      </StaffAuthProvider>,
    )

    await expect.element(page.getByRole('heading', { name: 'Staff sign in' })).toBeInTheDocument()
    await page.getByLabelText('Username').fill('administrator')
    await page.getByLabelText('Password').fill('AdminPassw0rd!')
    await page.getByRole('button', { name: 'Sign in' }).click()

    await expect.poll(() => onSignedIn.mock.calls.length).toBe(1)
    expect(client.login).toHaveBeenCalledWith('administrator', 'AdminPassw0rd!')
    expect(window.localStorage.getItem('accessToken')).toBeNull()
    expect(window.localStorage.getItem('cineflow.staff.session')).toBeNull()
    expect(window.sessionStorage.getItem('accessToken')).toBeNull()
  })

  it('shows a safe error when sign-in is rejected', async () => {
    const client = fakeIdentityClient({
      login: vi.fn().mockRejectedValue(new Error('invalid')),
    })

    await render(
      <StaffAuthProvider client={client} socket={silentSocket()}>
        <StaffLoginPage />
      </StaffAuthProvider>,
    )

    await page.getByLabelText('Username').fill('administrator')
    await page.getByLabelText('Password').fill('wrong')
    await page.getByRole('button', { name: 'Sign in' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Unable to sign in. Check your username and password.')
  })

  it('has no serious axe violations on the login route', async () => {
    const client = fakeIdentityClient({
      login: vi.fn(),
    })

    const screen = await render(
      <StaffAuthProvider client={client} socket={silentSocket()}>
        <StaffLoginPage />
      </StaffAuthProvider>,
    )
    await expect.element(page.getByRole('heading', { name: 'Staff sign in' })).toBeInTheDocument()

    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
