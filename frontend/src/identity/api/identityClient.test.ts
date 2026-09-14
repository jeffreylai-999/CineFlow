import { afterEach, describe, expect, it, vi } from 'vitest'
import { createIdentityClient, type StaffSession } from '@/identity/api/identityClient.ts'

const SESSION_CACHE_KEY = 'cineflow.staff.session'

const session: StaffSession = {
  accessToken: 'access-token',
  expiresInSeconds: 900,
  staff: { id: 1, username: 'booking.staff', role: 'BOOKING_STAFF' },
}

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

function refreshFetcher(handler: () => Promise<Response> | Response) {
  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    if (typeof input === 'string' && input === '/api/auth/refresh' && init?.method === 'POST') {
      return handler()
    }
    throw new Error(`unexpected ${init?.method} ${String(input)}`)
  })
}

afterEach(() => {
  localStorage.removeItem(SESSION_CACHE_KEY)
})

describe('createIdentityClient refresh coordination', () => {
  it('lets only one client refresh while others reuse the new access token', async () => {
    const fetcher = refreshFetcher(() => jsonResponse(session))
    const first = createIdentityClient(fetcher)
    const second = createIdentityClient(fetcher)

    const [fromFirst, fromSecond] = await Promise.all([first.refresh(), second.refresh()])

    expect(fetcher).toHaveBeenCalledTimes(1)
    expect(fromFirst).toEqual(session)
    expect(fromSecond).toEqual(session)
  })

  it('skips a second refresh while the shared access token is still fresh', async () => {
    const fetcher = refreshFetcher(() => jsonResponse(session))
    const first = createIdentityClient(fetcher)
    const second = createIdentityClient(fetcher)

    await first.refresh()
    await second.refresh()

    expect(fetcher).toHaveBeenCalledTimes(1)
  })

  it('refreshes again when the cached access token is within a minute of expiry', async () => {
    const nearExpiry: StaffSession = { ...session, expiresInSeconds: 60 }
    const rotated: StaffSession = { ...session, accessToken: 'rotated-token' }
    const fetcher = refreshFetcher(() => jsonResponse(nearExpiry))
      .mockResolvedValueOnce(jsonResponse(nearExpiry))
      .mockResolvedValueOnce(jsonResponse(rotated))
    const client = createIdentityClient(fetcher)

    await expect(client.refresh()).resolves.toEqual(nearExpiry)
    await expect(client.refresh()).resolves.toEqual(rotated)
    expect(fetcher).toHaveBeenCalledTimes(2)
  })

  it('does not reuse a failed refresh', async () => {
    const fetcher = refreshFetcher(() => jsonResponse(session))
      .mockResolvedValueOnce(jsonResponse({ code: 'TOKEN_REUSE' }, 401))
      .mockResolvedValueOnce(jsonResponse(session))
    const client = createIdentityClient(fetcher)

    await expect(client.refresh()).rejects.toMatchObject({ status: 401 })
    await expect(client.refresh()).resolves.toEqual(session)
    expect(fetcher).toHaveBeenCalledTimes(2)
  })

  it('clears the shared session on logout so the next refresh hits the network', async () => {
    const fetcher = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      if (typeof input === 'string' && input === '/api/auth/refresh') {
        return jsonResponse(session)
      }
      if (typeof input === 'string' && input === '/api/auth/logout' && init?.method === 'POST') {
        return new Response(null, { status: 204 })
      }
      throw new Error(`unexpected ${init?.method} ${String(input)}`)
    })
    const client = createIdentityClient(fetcher)

    await client.refresh()
    await client.logout()
    await client.refresh()

    expect(fetcher.mock.calls.filter(([url]) => url === '/api/auth/refresh')).toHaveLength(2)
  })
})
