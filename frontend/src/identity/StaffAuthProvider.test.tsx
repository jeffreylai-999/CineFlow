import { render } from 'vitest-browser-react'
import { describe, expect, it, vi } from 'vitest'
import { page } from 'vitest/browser'
import type { IdentityClient, StaffSession } from '@/identity/api/identityClient.ts'
import type { StaffSocket } from '@/identity/api/staffSocket.ts'
import { StaffAuthProvider } from '@/identity/StaffAuthProvider.tsx'
import { useStaffAuth } from '@/identity/staffAuthContext.ts'

const expired: StaffSession = {
  accessToken: 'expired-token',
  expiresInSeconds: 900,
  staff: { id: 2, username: 'administrator', role: 'ADMINISTRATOR' },
}

const refreshed: StaffSession = {
  ...expired,
  accessToken: 'next-token',
}

const shortLived: StaffSession = {
  ...expired,
  expiresInSeconds: 60,
}

function TokenProbe() {
  const auth = useStaffAuth()
  if (!auth.ready) {
    return <p role="status">Restoring session…</p>
  }
  return (
    <>
      <p>token:{auth.session?.accessToken ?? 'none'}</p>
      <button type="button" onClick={() => void auth.signOut()}>
        Log out
      </button>
    </>
  )
}

describe('StaffAuthProvider', () => {
  it('reconnects STOMP after refresh when the access token is rejected', async () => {
    const client: IdentityClient = {
      login: vi.fn(),
      refresh: vi
        .fn()
        .mockResolvedValueOnce(expired)
        .mockResolvedValueOnce(refreshed),
      logout: vi.fn(),
    }
    const socket: StaffSocket = {
      connect: vi.fn(async (token: string) => {
        if (token === 'expired-token') {
          throw new Error('unauthorized')
        }
      }),
      disconnect: vi.fn().mockResolvedValue(undefined),
    }

    await render(
      <StaffAuthProvider client={client} socket={socket}>
        <TokenProbe />
      </StaffAuthProvider>,
    )

    await expect.element(page.getByText('token:next-token')).toBeInTheDocument()
    expect(socket.connect).toHaveBeenCalledWith('expired-token', expect.any(Function))
    expect(socket.connect).toHaveBeenCalledWith('next-token', expect.any(Function))
  })

  it('ignores a scheduled refresh after sign-out', async () => {
    let finishRefresh: ((session: StaffSession) => void) | undefined
    const refresh = vi
      .fn()
      .mockResolvedValueOnce(shortLived)
      .mockImplementation(
        () =>
          new Promise<StaffSession>((resolve) => {
            finishRefresh = resolve
          }),
      )
    const client: IdentityClient = {
      login: vi.fn(),
      refresh,
      logout: vi.fn().mockResolvedValue(undefined),
    }
    const socket: StaffSocket = {
      connect: vi.fn().mockResolvedValue(undefined),
      disconnect: vi.fn().mockResolvedValue(undefined),
    }

    await render(
      <StaffAuthProvider client={client} socket={socket}>
        <TokenProbe />
      </StaffAuthProvider>,
    )

    await expect.element(page.getByText('token:expired-token')).toBeInTheDocument()
    await expect.poll(() => finishRefresh).toBeTruthy()
    await page.getByRole('button', { name: 'Log out' }).click()
    await expect.element(page.getByText('token:none')).toBeInTheDocument()
    finishRefresh?.(refreshed)
    await expect.element(page.getByText('token:none')).toBeInTheDocument()
  })

  it('refreshes and reconnects after a live socket drops', async () => {
    let onDisconnected: (() => void) | undefined
    const client: IdentityClient = {
      login: vi.fn(),
      refresh: vi.fn().mockResolvedValueOnce(expired).mockResolvedValueOnce(refreshed),
      logout: vi.fn(),
    }
    const socket: StaffSocket = {
      connect: vi.fn(async (_token, disconnected) => {
        onDisconnected = disconnected
      }),
      disconnect: vi.fn().mockResolvedValue(undefined),
    }

    await render(
      <StaffAuthProvider client={client} socket={socket}>
        <TokenProbe />
      </StaffAuthProvider>,
    )

    await expect.element(page.getByText('token:expired-token')).toBeInTheDocument()
    await expect.poll(() => onDisconnected).toBeTruthy()
    onDisconnected?.()
    await expect.element(page.getByText('token:next-token')).toBeInTheDocument()
  })

  it('recovers again after a session refresh cancels a pending backoff', async () => {
    const refresh = vi
      .fn()
      .mockResolvedValueOnce({ ...expired, accessToken: 't1', expiresInSeconds: 900 })
      .mockResolvedValueOnce({ ...expired, accessToken: 't2', expiresInSeconds: 60 })
      .mockResolvedValueOnce({ ...expired, accessToken: 't3', expiresInSeconds: 900 })
      .mockResolvedValue({ ...expired, accessToken: 't4', expiresInSeconds: 900 })
    const client: IdentityClient = {
      login: vi.fn(),
      refresh,
      logout: vi.fn(),
    }
    const socket: StaffSocket = {
      connect: vi.fn(async (token: string) => {
        if (token === 't4') {
          return
        }
        throw new Error('ws down')
      }),
      disconnect: vi.fn().mockResolvedValue(undefined),
    }

    await render(
      <StaffAuthProvider client={client} socket={socket}>
        <TokenProbe />
      </StaffAuthProvider>,
    )

    await expect.element(page.getByText('token:t4'), { timeout: 8_000 }).toBeInTheDocument()
    expect(socket.connect).toHaveBeenCalledWith('t4', expect.any(Function))
  })

  it('stops recovering a down socket after a few attempts', async () => {
    const refresh = vi.fn().mockImplementation(async () => ({
      ...expired,
      accessToken: `token-${refresh.mock.calls.length}`,
    }))
    const client: IdentityClient = {
      login: vi.fn(),
      refresh,
      logout: vi.fn(),
    }
    const socket: StaffSocket = {
      connect: vi.fn().mockRejectedValue(new Error('ws down')),
      disconnect: vi.fn().mockResolvedValue(undefined),
    }

    await render(
      <StaffAuthProvider client={client} socket={socket}>
        <TokenProbe />
      </StaffAuthProvider>,
    )

    await expect.poll(() => refresh.mock.calls.length, { timeout: 8_000 }).toBe(4)
    await new Promise((resolve) => window.setTimeout(resolve, 200))
    expect(refresh.mock.calls.length).toBe(4)
  })
})
