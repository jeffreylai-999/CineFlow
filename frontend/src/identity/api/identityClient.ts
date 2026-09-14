export type StaffRole = 'BOOKING_STAFF' | 'ADMINISTRATOR'

export type StaffProfile = {
  id: number
  username: string
  role: StaffRole
}

export type StaffSession = {
  accessToken: string
  expiresInSeconds: number
  staff: StaffProfile
}

export type IdentityClient = {
  login: (username: string, password: string) => Promise<StaffSession>
  refresh: () => Promise<StaffSession>
  logout: () => Promise<void>
}

export class IdentityRequestError extends Error {
  readonly status: number
  readonly code: string | undefined

  constructor(status: number, code: string | undefined) {
    super(`Identity request failed with status ${status}`)
    this.status = status
    this.code = code
  }
}

export function createIdentityClient(fetcher: typeof fetch = fetch): IdentityClient {
  let refreshInFlight: Promise<StaffSession> | null = null
  return {
    async login(username, password) {
      return readSession(
        await fetcher('/api/auth/login', {
          method: 'POST',
          credentials: 'include',
          headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
          body: JSON.stringify({ username, password }),
        }),
      )
    },
    refresh() {
      if (!refreshInFlight) {
        refreshInFlight = (async () =>
          readSession(
            await fetcher('/api/auth/refresh', {
              method: 'POST',
              credentials: 'include',
              headers: { Accept: 'application/json' },
            }),
          ))().finally(() => {
          refreshInFlight = null
        })
      }
      return refreshInFlight
    },
    async logout() {
      const response = await fetcher('/api/auth/logout', {
        method: 'POST',
        credentials: 'include',
      })
      if (!response.ok) {
        throw new IdentityRequestError(response.status, undefined)
      }
    },
  }
}

async function readSession(response: Response): Promise<StaffSession> {
  if (!response.ok) {
    let code: string | undefined
    try {
      const body = (await response.json()) as { code?: string }
      code = body.code
    } catch {
      code = undefined
    }
    throw new IdentityRequestError(response.status, code)
  }
  return (await response.json()) as StaffSession
}

export function isIdentityRequestError(error: unknown): error is IdentityRequestError {
  return error instanceof IdentityRequestError
}
