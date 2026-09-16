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
  seats: CustomerSeat[]
}

export type SeatHold = {
  holdId: string
  showtimeId: number
  seatIds: number[]
  serverTime: string
  expiresAt: string
}

export type CheckoutTicket = {
  seatId: number
  ticketType: TicketType
}

export type CheckoutRequest = {
  holdId: string
  email: string
  tickets: CheckoutTicket[]
  cardNumber: string
  idempotencyKey: string
}

export type BookedSeat = {
  seatId: number
  label: string
  ticketType: TicketType
  priceMyr: number
}

export type BookingConfirmation = {
  bookingReference: string
  showtimeId: number
  movieTitle: string
  hallName: string
  startsAtCinemaTime: string
  timeZone: string
  email: string
  seats: BookedSeat[]
  totalMyr: number
  admissionToken: string | null
}

export type CustomerClient = {
  listMovies: () => Promise<Movie[]>
  getShowtimeSeats: (showtimeId: number) => Promise<ShowtimeSeats>
  createSeatHold: (showtimeId: number, seatIds: number[]) => Promise<SeatHold>
  checkout: (showtimeId: number, request: CheckoutRequest) => Promise<BookingConfirmation>
}

export class CustomerRequestError extends Error {
  readonly status: number
  readonly code: string | undefined

  constructor(status: number, code: string | undefined) {
    super(`Customer catalog request failed with status ${status}`)
    this.status = status
    this.code = code
  }
}

export function isCustomerRequestError(error: unknown): error is CustomerRequestError {
  return error instanceof CustomerRequestError
}

export function createCustomerClient(fetcher: typeof fetch = fetch): CustomerClient {
  return {
    async listMovies() {
      return readJson<Movie[]>(fetcher('/api/movies', { headers: { Accept: 'application/json' } }))
    },
    async getShowtimeSeats(showtimeId) {
      return readJson<ShowtimeSeats>(
        fetcher(`/api/showtimes/${showtimeId}/seats`, { headers: { Accept: 'application/json' } }),
      )
    },
    async createSeatHold(showtimeId, seatIds) {
      return readJson<SeatHold>(
        fetcher(`/api/showtimes/${showtimeId}/holds`, {
          method: 'POST',
          headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
          body: JSON.stringify({ seatIds }),
        }),
      )
    },
    async checkout(showtimeId, request) {
      return readJson<BookingConfirmation>(
        fetcher(`/api/showtimes/${showtimeId}/checkout`, {
          method: 'POST',
          headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
          body: JSON.stringify(request),
        }),
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

async function toError(response: Response): Promise<CustomerRequestError> {
  let code: string | undefined
  try {
    const body = (await response.json()) as { code?: string }
    code = body.code
  } catch {
    code = undefined
  }
  return new CustomerRequestError(response.status, code)
}
