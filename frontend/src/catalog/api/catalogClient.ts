export type TicketType = 'ADULT' | 'CHILD'

export type CatalogShowtime = {
  id: number
  hallName: string
  startsAtCinemaTime: string
  timeZone: string
  adultPriceMyr: number
  childPriceMyr: number
  checkoutOpen: boolean
}

export type CatalogShowtimeDate = {
  cinemaDate: string
  showtimes: CatalogShowtime[]
}

export type Movie = {
  id: number
  title: string
  synopsis: string
  genre: string
  runtimeMinutes: number
  ageRating: string
  posterUrl: string | null
  dates: CatalogShowtimeDate[]
}

export type CustomerSeat = {
  id: number
  rowLabel: string
  seatNumber: number
  label: string
  available: boolean
}

export type ShowtimeSeats = {
  showtimeId: number
  movieId: number
  movieTitle: string
  hallName: string
  startsAtCinemaTime: string
  timeZone: string
  adultPriceMyr: number
  childPriceMyr: number
  bookingLimit: number
  checkoutOpen: boolean
  seats: CustomerSeat[]
}

export type CatalogClient = {
  listMovies: () => Promise<Movie[]>
  getShowtimeSeats: (showtimeId: number) => Promise<ShowtimeSeats>
}

export class CatalogRequestError extends Error {
  readonly status: number
  readonly code: string | undefined

  constructor(status: number, code: string | undefined) {
    super(`Catalog request failed with status ${status}`)
    this.status = status
    this.code = code
  }
}

export function isCatalogRequestError(error: unknown): error is CatalogRequestError {
  return error instanceof CatalogRequestError
}

export function createCatalogClient(fetcher: typeof fetch = fetch): CatalogClient {
  return {
    async listMovies() {
      return readJson<Movie[]>(fetcher('/api/movies', { headers: { Accept: 'application/json' } }))
    },
    async getShowtimeSeats(showtimeId) {
      return readJson<ShowtimeSeats>(
        fetcher(`/api/showtimes/${showtimeId}/seats`, { headers: { Accept: 'application/json' } }),
      )
    },
  }
}

async function readJson<T>(responsePromise: Promise<Response>): Promise<T> {
  const response = await responsePromise
  if (!response.ok) {
    throw await toError(response)
  }
  return (await response.json()) as T
}

async function toError(response: Response): Promise<CatalogRequestError> {
  let code: string | undefined
  try {
    const body = (await response.json()) as { code?: string }
    code = body.code
  } catch {
    code = undefined
  }
  return new CatalogRequestError(response.status, code)
}
