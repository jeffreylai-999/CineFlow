export type HallSummary = {
  id: number
  name: string
  rowCount: number
  seatsPerRow: number
  archivedAt: string | null
}

export type Seat = {
  id: number
  rowLabel: string
  seatNumber: number
  label: string
  disabled: boolean
}

export type Hall = HallSummary & {
  seats: Seat[]
}

export type CreateHallInput = {
  name: string
  rowCount: number
  seatsPerRow: number
}

export type SchedulingClient = {
  listHalls: () => Promise<HallSummary[]>
  createHall: (input: CreateHallInput) => Promise<Hall>
  getHall: (id: number) => Promise<Hall>
  setSeatDisabled: (hallId: number, seatId: number, disabled: boolean) => Promise<Seat>
  archiveHall: (id: number) => Promise<Hall>
}

export class SchedulingRequestError extends Error {
  readonly status: number
  readonly code: string | undefined

  constructor(status: number, code: string | undefined) {
    super(`Scheduling request failed with status ${status}`)
    this.status = status
    this.code = code
  }
}

export function isSchedulingRequestError(error: unknown): error is SchedulingRequestError {
  return error instanceof SchedulingRequestError
}

export function createSchedulingClient(
  accessToken: string,
  fetcher: typeof fetch = fetch,
): SchedulingClient {
  return {
    listHalls: () =>
      readJson<HallSummary[]>(fetcher('/api/halls', { headers: headers(accessToken) })),
    createHall: (input) =>
      readJson<Hall>(
        fetcher('/api/halls', {
          method: 'POST',
          headers: jsonHeaders(accessToken),
          body: JSON.stringify(input),
        }),
      ),
    getHall: (id) =>
      readJson<Hall>(fetcher(`/api/halls/${id}`, { headers: headers(accessToken) })),
    setSeatDisabled: (hallId, seatId, disabled) =>
      readJson<Seat>(
        fetcher(`/api/halls/${hallId}/seats/${seatId}`, {
          method: 'PATCH',
          headers: jsonHeaders(accessToken),
          body: JSON.stringify({ disabled }),
        }),
      ),
    archiveHall: (id) =>
      readJson<Hall>(
        fetcher(`/api/halls/${id}/archive`, {
          method: 'POST',
          headers: headers(accessToken),
        }),
      ),
  }
}

function headers(accessToken: string): HeadersInit {
  return {
    Accept: 'application/json',
    Authorization: `Bearer ${accessToken}`,
  }
}

function jsonHeaders(accessToken: string): HeadersInit {
  return {
    ...headers(accessToken),
    'Content-Type': 'application/json',
  }
}

async function readJson<T>(responsePromise: Promise<Response>): Promise<T> {
  const response = await responsePromise
  if (!response.ok) {
    let code: string | undefined
    try {
      const body = (await response.json()) as { code?: string }
      code = body.code
    } catch {
      code = undefined
    }
    throw new SchedulingRequestError(response.status, code)
  }
  return (await response.json()) as T
}
