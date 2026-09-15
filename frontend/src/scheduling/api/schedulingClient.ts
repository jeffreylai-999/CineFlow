export type HallSummary = {
  id: number
  name: string
  rowCount: number
  seatsPerRow: number
  archivedAt?: string | null
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

export type Showtime = {
  id: number
  movieId: number
  movieTitle: string
  runtimeMinutes: number
  hallId: number
  hallName: string
  startsAt: string
  startsAtCinemaTime: string
  timeZone: string
  occupancyEndsAt: string
  adultPriceMyr: number
  childPriceMyr: number
}

export type CreateShowtimeInput = {
  movieId: number
  hallId: number
  startsAtLocal: string
  timeZone: 'Asia/Kuala_Lumpur'
  adultPriceMyr: number
  childPriceMyr: number
}

export type SchedulingClient = {
  listHalls: () => Promise<HallSummary[]>
  createHall: (input: CreateHallInput) => Promise<Hall>
  getHall: (id: number) => Promise<Hall>
  setSeatDisabled: (hallId: number, seatId: number, disabled: boolean) => Promise<Seat>
  archiveHall: (id: number) => Promise<Hall>
  listShowtimes: () => Promise<Showtime[]>
  createShowtime: (input: CreateShowtimeInput) => Promise<Showtime>
  updateShowtimePrices: (
    id: number,
    input: { adultPriceMyr: number; childPriceMyr: number },
  ) => Promise<Showtime>
  removeShowtime: (id: number) => Promise<void>
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
    listShowtimes: () =>
      readJson<Showtime[]>(fetcher('/api/showtimes', { headers: headers(accessToken) })),
    createShowtime: (input) =>
      readJson<Showtime>(
        fetcher('/api/showtimes', {
          method: 'POST',
          headers: jsonHeaders(accessToken),
          body: JSON.stringify(input),
        }),
      ),
    updateShowtimePrices: (id, input) =>
      readJson<Showtime>(
        fetcher(`/api/showtimes/${id}`, {
          method: 'PATCH',
          headers: jsonHeaders(accessToken),
          body: JSON.stringify(input),
        }),
      ),
    removeShowtime: async (id) => {
      const response = await fetcher(`/api/showtimes/${id}`, {
        method: 'DELETE',
        headers: headers(accessToken),
      })
      if (!response.ok) {
        throw await toError(response)
      }
    },
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
    throw await toError(response)
  }
  return (await response.json()) as T
}

async function toError(response: Response): Promise<SchedulingRequestError> {
  let code: string | undefined
  try {
    const body = (await response.json()) as { code?: string }
    code = body.code
  } catch {
    code = undefined
  }
  return new SchedulingRequestError(response.status, code)
}
