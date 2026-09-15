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
const SESSION_CHANNEL = 'cineflow.staff.session'
const REFRESH_EARLY_MS = 60_000
const HANDOFF_WAIT_MS = 50

type CachedSession = {
  at: number
  session: StaffSession
}

type HandoffMessage =
  | { kind: 'session'; at: number; session: StaffSession }
  | { kind: 'clear' }
  | { kind: 'request' }

let memoryCache: CachedSession | null = null
let fallbackLock: Promise<unknown> = Promise.resolve()
const channel = createSessionChannel()

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
          const cached = await takeCachedSession(Date.now())
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

export function resetStaffSessionHandoff(): void {
  memoryCache = null
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

async function takeCachedSession(now: number): Promise<StaffSession | null> {
  const local = remainingSession(memoryCache, now)
  if (local) {
    return local
  }
  return requestCachedSession()
}

function remainingSession(cached: CachedSession | null, now: number): StaffSession | null {
  if (!cached || typeof cached.at !== 'number' || typeof cached.session?.accessToken !== 'string') {
    return null
  }
  const remainingMs = cached.at + cached.session.expiresInSeconds * 1000 - now
  if (remainingMs <= REFRESH_EARLY_MS) {
    return null
  }
  return {
    ...cached.session,
    expiresInSeconds: Math.max(1, Math.floor(remainingMs / 1000)),
  }
}

function requestCachedSession(): Promise<StaffSession | null> {
  if (!channel) {
    return Promise.resolve(null)
  }
  return waitForHandoff(channel)
}

function waitForHandoff(handoff: BroadcastChannel): Promise<StaffSession | null> {
  return new Promise((resolve) => {
    const finish = () => resolve(remainingSession(memoryCache, Date.now()))
    const timer = window.setTimeout(() => {
      handoff.removeEventListener('message', onMessage)
      finish()
    }, HANDOFF_WAIT_MS)
    function onMessage(event: MessageEvent<unknown>) {
      const data = parseHandoff(event.data)
      if (data?.kind !== 'session') {
        return
      }
      window.clearTimeout(timer)
      handoff.removeEventListener('message', onMessage)
      finish()
    }
    handoff.addEventListener('message', onMessage)
    handoff.postMessage({ kind: 'request' } satisfies HandoffMessage)
  })
}

function writeCachedSession(session: StaffSession, now = Date.now()): void {
  memoryCache = { at: now, session }
  channel?.postMessage({ kind: 'session', at: now, session } satisfies HandoffMessage)
}

function clearCachedSession(): void {
  memoryCache = null
  channel?.postMessage({ kind: 'clear' } satisfies HandoffMessage)
}

function createSessionChannel(): BroadcastChannel | null {
  try {
    if (typeof BroadcastChannel === 'undefined') {
      return null
    }
    const next = new BroadcastChannel(SESSION_CHANNEL)
    next.addEventListener('message', (event: MessageEvent<unknown>) => {
      applyHandoff(parseHandoff(event.data))
    })
    return next
  } catch {
    return null
  }
}

function applyHandoff(data: HandoffMessage | null): void {
  if (!data) {
    return
  }
  switch (data.kind) {
    case 'session':
      if (typeof data.at === 'number' && typeof data.session?.accessToken === 'string') {
        memoryCache = { at: data.at, session: data.session }
      }
      return
    case 'clear':
      memoryCache = null
      return
    case 'request':
      if (memoryCache && remainingSession(memoryCache, Date.now())) {
        channel?.postMessage({
          kind: 'session',
          at: memoryCache.at,
          session: memoryCache.session,
        } satisfies HandoffMessage)
      }
      return
    default: {
      const exhausted: never = data
      return exhausted
    }
  }
}

function parseHandoff(data: unknown): HandoffMessage | null {
  if (!data || typeof data !== 'object' || !('kind' in data)) {
    return null
  }
  const kind = (data as { kind: unknown }).kind
  if (kind === 'clear' || kind === 'request') {
    return { kind }
  }
  if (kind === 'session') {
    const message = data as { at?: unknown; session?: StaffSession }
    if (typeof message.at === 'number' && typeof message.session?.accessToken === 'string') {
      return { kind: 'session', at: message.at, session: message.session }
    }
  }
  return null
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
