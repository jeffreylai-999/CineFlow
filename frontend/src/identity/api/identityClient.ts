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

const REFRESH_LOCK = 'cineflow-staff-refresh'
const SESSION_CACHE_KEY = 'cineflow.staff.session'
const REFRESH_EARLY_MS = 60_000

type CachedSession = {
  at: number
  session: StaffSession
}

let fallbackLock: Promise<unknown> = Promise.resolve()

export function createIdentityClient(fetcher: typeof fetch = fetch): IdentityClient {
  let refreshInFlight: Promise<StaffSession> | null = null
  return {
    async login(username, password) {
      const session = await readSession(
        await fetcher('/api/auth/login', {
          method: 'POST',
          credentials: 'include',
          headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
          body: JSON.stringify({ username, password }),
        }),
      )
      writeCachedSession(session)
      return session
    },
    refresh() {
      if (!refreshInFlight) {
        refreshInFlight = withRefreshLock(async () => {
          const cached = readCachedSession()
          if (cached) {
            return cached
          }
          const session = await readSession(
            await fetcher('/api/auth/refresh', {
              method: 'POST',
              credentials: 'include',
              headers: { Accept: 'application/json' },
            }),
          )
          writeCachedSession(session)
          return session
        }).finally(() => {
          refreshInFlight = null
        })
      }
      return refreshInFlight
    },
    async logout() {
      await withRefreshLock(async () => {
        clearCachedSession()
        const response = await fetcher('/api/auth/logout', {
          method: 'POST',
          credentials: 'include',
        })
        if (!response.ok) {
          throw new IdentityRequestError(response.status, undefined)
        }
      })
    },
  }
}

async function withRefreshLock<T>(work: () => Promise<T>): Promise<T> {
  const locks = globalThis.navigator?.locks
  if (typeof locks?.request === 'function') {
    return locks.request(REFRESH_LOCK, () => work())
  }
  const next = fallbackLock.then(work, work)
  fallbackLock = next.then(
    () => undefined,
    () => undefined,
  )
  return next
}

function readCachedSession(now = Date.now()): StaffSession | null {
  try {
    const raw = localStorage.getItem(SESSION_CACHE_KEY)
    if (!raw) {
      return null
    }
    const cached = JSON.parse(raw) as CachedSession
    if (typeof cached.at !== 'number' || typeof cached.session?.accessToken !== 'string') {
      clearCachedSession()
      return null
    }
    const remainingMs = cached.at + cached.session.expiresInSeconds * 1000 - now
    if (remainingMs <= REFRESH_EARLY_MS) {
      return null
    }
    return cached.session
  } catch {
    return null
  }
}

function writeCachedSession(session: StaffSession, now = Date.now()): void {
  try {
    const cached: CachedSession = { at: now, session }
    localStorage.setItem(SESSION_CACHE_KEY, JSON.stringify(cached))
  } catch {
    // Private mode can throw; the next refresh will hit the network.
  }
}

function clearCachedSession(): void {
  try {
    localStorage.removeItem(SESSION_CACHE_KEY)
  } catch {
    // Ignore quota / access errors on teardown.
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
