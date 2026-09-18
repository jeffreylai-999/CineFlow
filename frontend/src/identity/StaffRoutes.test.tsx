import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page } from 'vitest/browser'
import axe from 'axe-core'
import { MemoryRouter, Route, Routes } from 'react-router'
import type { CatalogAdminClient } from '@/catalog/api/catalogAdminClient.ts'
import type { StaffAccount, StaffSession } from '@/identity/api/identityClient.ts'
import { IdentityRequestError } from '@/identity/api/identityClient.ts'
import type { StaffSocket } from '@/identity/api/staffSocket.ts'
import { StaffAuthProvider } from '@/identity/StaffAuthProvider.tsx'
import { StaffRoutes } from '@/identity/StaffRoutes.tsx'
import { fakeIdentityClient } from '@/identity/test/fakeIdentityClient.ts'

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

const accounts: StaffAccount[] = [
  { id: 2, username: 'administrator', role: 'ADMINISTRATOR', active: true },
  { id: 1, username: 'booking.staff', role: 'BOOKING_STAFF', active: true },
]

function silentSocket(): StaffSocket {
  return {
    connect: vi.fn().mockResolvedValue(undefined),
    disconnect: vi.fn().mockResolvedValue(undefined),
  }
}

function silentCatalogAdminClient(): CatalogAdminClient {
  return {
    listMovies: vi.fn().mockResolvedValue([]),
    listProviders: vi.fn().mockResolvedValue({
      activeProviderId: 'tmdb',
      providers: [{ id: 'tmdb', displayName: 'TMDB' }],
    }),
    selectProvider: vi.fn(),
    search: vi.fn().mockResolvedValue([]),
    importMovie: vi.fn(),
    refresh: vi.fn(),
    updateSchedulingFields: vi.fn(),
  }
}

async function renderStaffRoutes(options: {
  session: StaffSession
  path?: string
  client?: ReturnType<typeof fakeIdentityClient>
}) {
  const client =
    options.client ??
    fakeIdentityClient({
      refresh: vi.fn().mockResolvedValue(options.session),
      listStaffAccounts: vi.fn().mockResolvedValue(accounts),
    })
  await render(
    <MemoryRouter initialEntries={[options.path ?? '/staff']}>
      <Routes>
        <Route
          path="/staff/*"
          element={
            <StaffAuthProvider client={client} socket={silentSocket()}>
              <StaffRoutes catalogAdminClient={silentCatalogAdminClient()} client={client} />
            </StaffAuthProvider>
          }
        />
      </Routes>
    </MemoryRouter>,
  )
  return client
}

describe('StaffRoutes counter sales', () => {
  it('shows Counter sales navigation to Booking Staff and opens the counter sales route', async () => {
    await renderStaffRoutes({ session: bookingStaff, path: '/staff' })

    await expect.element(page.getByRole('link', { name: 'Counter sales' })).toBeInTheDocument()
    await page.getByRole('link', { name: 'Counter sales' }).click()
    await expect.element(page.getByRole('heading', { name: 'Counter sales' })).toBeInTheDocument()
  })

  it('hides Counter sales from an Administrator, including a direct URL', async () => {
    await renderStaffRoutes({ session: administrator, path: '/staff/counter-sales' })

    await expect.element(page.getByRole('heading', { name: 'Staff portal' })).toBeInTheDocument()
    await expect.element(page.getByRole('link', { name: 'Counter sales' })).not.toBeInTheDocument()
    await expect.element(page.getByRole('heading', { name: 'Counter sales' })).not.toBeInTheDocument()
  })
})

describe('StaffRoutes staff administration', () => {
  it('hides Staff accounts from Booking Staff, including a direct URL', async () => {
    await renderStaffRoutes({ session: bookingStaff, path: '/staff/accounts' })

    await expect.element(page.getByRole('heading', { name: 'Staff portal' })).toBeInTheDocument()
    await expect.element(page.getByRole('link', { name: 'Staff accounts' })).not.toBeInTheDocument()
    await expect.element(page.getByRole('heading', { name: 'Staff accounts' })).not.toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Create Booking Staff' })).not.toBeInTheDocument()
  })

  it('lets an Administrator list and create Booking Staff without a default password', async () => {
    const created: StaffAccount = {
      id: 9,
      username: 'counter.staff',
      role: 'BOOKING_STAFF',
      active: true,
    }
    let listed = accounts
    const client = fakeIdentityClient({
      refresh: vi.fn().mockResolvedValue(administrator),
      listStaffAccounts: vi.fn(async () => listed),
      createStaffAccount: vi.fn(async (username: string, password: string) => {
        expect(password).toBe('CounterPass1!')
        created.username = username
        listed = [...listed, created]
        return created
      }),
    })

    await renderStaffRoutes({ session: administrator, path: '/staff', client })

    await expect.element(page.getByRole('link', { name: 'Staff accounts' })).toBeInTheDocument()
    await page.getByRole('link', { name: 'Staff accounts' }).click()

    await expect.element(page.getByRole('heading', { name: 'Staff accounts' })).toBeInTheDocument()
    await expect.element(page.getByRole('cell', { name: 'booking.staff' })).toBeInTheDocument()
    await expect.element(page.getByRole('cell', { name: 'Administrator' })).toBeInTheDocument()

    await page.getByLabelText('New username').fill('counter.staff')
    await page.getByLabelText('New password').fill('CounterPass1!')
    await page.getByRole('button', { name: 'Create Booking Staff' }).click()

    await expect.element(page.getByRole('cell', { name: 'counter.staff' })).toBeInTheDocument()
    expect(client.createStaffAccount).toHaveBeenCalledWith('counter.staff', 'CounterPass1!')
  })

  it('shows validation and conflict errors from Staff account changes', async () => {
    const client = fakeIdentityClient({
      refresh: vi.fn().mockResolvedValue(administrator),
      listStaffAccounts: vi.fn().mockResolvedValue(accounts),
      createStaffAccount: vi
        .fn()
        .mockRejectedValueOnce(new IdentityRequestError(400, 'request.invalid'))
        .mockRejectedValueOnce(new IdentityRequestError(409, 'staff.username_conflict')),
    })

    await renderStaffRoutes({ session: administrator, path: '/staff/accounts', client })
    await expect.element(page.getByRole('heading', { name: 'Staff accounts' })).toBeInTheDocument()

    await page.getByLabelText('New username').fill('x')
    await page.getByLabelText('New password').fill('short')
    await page.getByRole('button', { name: 'Create Booking Staff' }).click()
    expect(client.createStaffAccount).not.toHaveBeenCalled()

    await page.getByLabelText('New username').fill('valid.staff')
    await page.getByLabelText('New password').fill('ValidPassw0rd!')
    await page.getByRole('button', { name: 'Create Booking Staff' }).click()
    await expect.element(page.getByRole('alert')).toHaveTextContent('Those details are not valid.')

    await page.getByLabelText('New username').fill('booking.staff')
    await page.getByLabelText('New password').fill('AnotherPassw0rd!')
    await page.getByRole('button', { name: 'Create Booking Staff' }).click()
    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('A Staff account with this username already exists.')
  })

  it('lets an Administrator deactivate and reset Booking Staff', async () => {
    let listed: StaffAccount[] = accounts.map((account) => ({ ...account }))
    const client = fakeIdentityClient({
      refresh: vi.fn().mockResolvedValue(administrator),
      listStaffAccounts: vi.fn(async () => listed),
      deactivateStaffAccount: vi.fn(async (id: number) => {
        listed = listed.map((account) =>
          account.id === id ? { ...account, active: false } : account,
        )
      }),
      resetStaffPassword: vi
        .fn()
        .mockRejectedValueOnce(new IdentityRequestError(400, 'request.invalid'))
        .mockResolvedValue(undefined),
    })

    await renderStaffRoutes({ session: administrator, path: '/staff/accounts', client })
    await expect.element(page.getByRole('heading', { name: 'Staff accounts' })).toBeInTheDocument()

    await page.getByRole('button', { name: 'Reset password for booking.staff' }).click()
    expect(client.resetStaffPassword).not.toHaveBeenCalled()

    await page.getByLabelText('New password for booking.staff').fill('ResetPassw0rd!')
    await page.getByRole('button', { name: 'Reset password for booking.staff' }).click()
    expect(client.resetStaffPassword).toHaveBeenCalledWith(1, 'ResetPassw0rd!')
    await expect.element(page.getByRole('alert')).toHaveTextContent('Those details are not valid.')
    await expect
      .element(page.getByRole('group', { name: 'Create Booking Staff' }).getByRole('alert'))
      .not.toBeInTheDocument()

    await page.getByRole('button', { name: 'Reset password for booking.staff' }).click()
    expect(client.resetStaffPassword).toHaveBeenCalledTimes(2)

    await page.getByRole('button', { name: 'Deactivate booking.staff' }).click()
    expect(client.deactivateStaffAccount).toHaveBeenCalledWith(1)
    await expect.element(page.getByText('Deactivated')).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Deactivate booking.staff' })).not.toBeInTheDocument()
  })

  it('treats a failed list refresh after create as a load error', async () => {
    const client = fakeIdentityClient({
      refresh: vi.fn().mockResolvedValue(administrator),
      listStaffAccounts: vi
        .fn()
        .mockResolvedValueOnce(accounts)
        .mockRejectedValueOnce(new Error('offline')),
      createStaffAccount: vi.fn().mockResolvedValue({
        id: 9,
        username: 'counter.staff',
        role: 'BOOKING_STAFF',
        active: true,
      }),
    })

    await renderStaffRoutes({ session: administrator, path: '/staff/accounts', client })
    await expect.element(page.getByRole('heading', { name: 'Staff accounts' })).toBeInTheDocument()

    await page.getByLabelText('New username').fill('counter.staff')
    await page.getByLabelText('New password').fill('CounterPass1!')
    await page.getByRole('button', { name: 'Create Booking Staff' }).click()

    await expect
      .element(page.getByRole('alert'))
      .toHaveTextContent('Unable to load Staff accounts. Try again shortly.')
    expect(client.createStaffAccount).toHaveBeenCalledTimes(1)
  })

  it('disables every Booking Staff action while one mutation is pending', async () => {
    let finishReset: () => void = () => {}
    const listed: StaffAccount[] = [
      ...accounts,
      { id: 3, username: 'other.staff', role: 'BOOKING_STAFF', active: true },
    ]
    const client = fakeIdentityClient({
      refresh: vi.fn().mockResolvedValue(administrator),
      listStaffAccounts: vi.fn(async () => listed),
      resetStaffPassword: vi.fn(
        () =>
          new Promise<void>((resolve) => {
            finishReset = resolve
          }),
      ),
    })

    await renderStaffRoutes({ session: administrator, path: '/staff/accounts', client })
    await expect.element(page.getByRole('heading', { name: 'Staff accounts' })).toBeInTheDocument()

    await page.getByLabelText('New password for booking.staff').fill('ResetPassw0rd!')
    await page.getByRole('button', { name: 'Reset password for booking.staff' }).click()
    await expect.element(page.getByRole('button', { name: 'Reset password for other.staff' })).toBeDisabled()
    await expect.element(page.getByRole('button', { name: 'Deactivate other.staff' })).toBeDisabled()
    await expect.element(page.getByRole('button', { name: 'Create Booking Staff' })).toBeDisabled()
    finishReset()
  })

  it('disables row actions while create is pending', async () => {
    let finishCreate: (account: StaffAccount) => void = () => {}
    const client = fakeIdentityClient({
      refresh: vi.fn().mockResolvedValue(administrator),
      listStaffAccounts: vi.fn().mockResolvedValue(accounts),
      createStaffAccount: vi.fn(
        () =>
          new Promise<StaffAccount>((resolve) => {
            finishCreate = resolve
          }),
      ),
    })

    await renderStaffRoutes({ session: administrator, path: '/staff/accounts', client })
    await expect.element(page.getByRole('heading', { name: 'Staff accounts' })).toBeInTheDocument()

    await page.getByLabelText('New username').fill('counter.staff')
    await page.getByLabelText('New password').fill('CounterPass1!')
    await page.getByRole('button', { name: 'Create Booking Staff' }).click()
    await expect.element(page.getByRole('button', { name: 'Reset password for booking.staff' })).toBeDisabled()
    await expect.element(page.getByRole('button', { name: 'Deactivate booking.staff' })).toBeDisabled()
    finishCreate({
      id: 9,
      username: 'counter.staff',
      role: 'BOOKING_STAFF',
      active: true,
    })
  })

  it('disables create while the initial account list is loading', async () => {
    let finishList: (next: StaffAccount[]) => void = () => {}
    const client = fakeIdentityClient({
      refresh: vi.fn().mockResolvedValue(administrator),
      listStaffAccounts: vi.fn(
        () =>
          new Promise<StaffAccount[]>((resolve) => {
            finishList = resolve
          }),
      ),
    })

    await renderStaffRoutes({ session: administrator, path: '/staff/accounts', client })
    await expect.element(page.getByRole('heading', { name: 'Staff accounts' })).toBeInTheDocument()
    await expect.element(page.getByRole('button', { name: 'Create Booking Staff' })).toBeDisabled()
    finishList(accounts)
    await expect.element(page.getByRole('button', { name: 'Create Booking Staff' })).toBeEnabled()
  })

  it('has no serious axe violations on the Staff accounts route', async () => {
    const client = fakeIdentityClient({
      refresh: vi.fn().mockResolvedValue(administrator),
      listStaffAccounts: vi.fn().mockResolvedValue(accounts),
    })
    const screen = await render(
      <MemoryRouter initialEntries={['/staff/accounts']}>
        <Routes>
          <Route
            path="/staff/*"
            element={
              <StaffAuthProvider client={client} socket={silentSocket()}>
                <StaffRoutes catalogAdminClient={silentCatalogAdminClient()} client={client} />
              </StaffAuthProvider>
            }
          />
        </Routes>
      </MemoryRouter>,
    )
    await expect.element(page.getByRole('heading', { name: 'Staff accounts' })).toBeInTheDocument()

    const results = await axe.run(screen.container)
    const serious = results.violations.filter(
      (violation) => violation.impact === 'serious' || violation.impact === 'critical',
    )
    expect(serious).toEqual([])
  })
})
