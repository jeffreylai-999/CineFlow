import type {
  BookingConfirmation,
  CheckoutTicket,
  SeatHold,
  TicketType,
} from '@/booking/api/customerClient.ts'

export type { BookingConfirmation, CheckoutTicket, SeatHold, TicketType }

export type CounterShowtime = {
  id: number
  movieTitle: string
  hallName: string
  startsAtCinemaTime: string
  timeZone: string
  adultPriceMyr: number
  childPriceMyr: number
}

export type StaffSeatState = 'AVAILABLE' | 'HELD' | 'BOOKED' | 'DISABLED'

export type StaffSeat = {
  id: number
  rowLabel: string
  seatNumber: number
  label: string
  state: StaffSeatState
}

export type StaffSeatMap = {
  showtimeId: number
  movieTitle: string
  hallName: string
  startsAtCinemaTime: string
  timeZone: string
  adultPriceMyr: number
  childPriceMyr: number
  bookingLimit: number
  counterSalesOpen: boolean
  seats: StaffSeat[]
}

export type CounterPaymentMethod = 'CASH' | 'CARD'

export type CounterSaleRequest = {
  holdId: string
  tickets: CheckoutTicket[]
  method: CounterPaymentMethod
  idempotencyKey: string
}

export type StaffBookingClient = {
  listCounterShowtimes: () => Promise<CounterShowtime[]>
  getStaffSeatMap: (showtimeId: number) => Promise<StaffSeatMap>
  createCounterHold: (showtimeId: number, seatIds: number[]) => Promise<SeatHold>
  confirmCounterSale: (showtimeId: number, request: CounterSaleRequest) => Promise<BookingConfirmation>
}

export class StaffBookingRequestError extends Error {
  readonly status: number
  readonly code: string | undefined

  constructor(status: number, code: string | undefined) {
    super(`Staff booking request failed with status ${status}`)
    this.status = status
    this.code = code
  }
}

export function isStaffBookingRequestError(error: unknown): error is StaffBookingRequestError {
  return error instanceof StaffBookingRequestError
}

export function createStaffBookingClient(
  accessToken: string,
  fetcher: typeof fetch = fetch,
): StaffBookingClient {
  return {
    listCounterShowtimes: () =>
      readJson<CounterShowtime[]>(
        fetcher('/api/staff/showtimes', { headers: headers(accessToken) }),
      ),
    getStaffSeatMap: (showtimeId) =>
      readJson<StaffSeatMap>(
        fetcher(`/api/staff/showtimes/${showtimeId}/seats`, { headers: headers(accessToken) }),
      ),
    createCounterHold: (showtimeId, seatIds) =>
      readJson<SeatHold>(
        fetcher(`/api/staff/showtimes/${showtimeId}/holds`, {
          method: 'POST',
          headers: jsonHeaders(accessToken),
          body: JSON.stringify({ seatIds }),
        }),
      ),
    confirmCounterSale: (showtimeId, request) =>
      readJson<BookingConfirmation>(
        fetcher(`/api/staff/showtimes/${showtimeId}/confirm`, {
          method: 'POST',
          headers: jsonHeaders(accessToken),
          body: JSON.stringify(request),
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
    throw await toError(response)
  }
  return (await response.json()) as T
}

async function toError(response: Response): Promise<StaffBookingRequestError> {
  let code: string | undefined
  try {
    const body = (await response.json()) as { code?: string }
    code = body.code
  } catch {
    code = undefined
  }
  return new StaffBookingRequestError(response.status, code)
}
