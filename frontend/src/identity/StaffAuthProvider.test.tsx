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

function TokenProbe() {
  const auth = useStaffAuth()
  if (!auth.ready) {
    return <p role="status">Restoring session…</p>
  }
  return <p>token:{auth.session?.accessToken ?? 'none'}</p>
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
    expect(socket.connect).toHaveBeenCalledWith('expired-token')
    expect(socket.connect).toHaveBeenCalledWith('next-token')
  })
})
