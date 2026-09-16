import { isStaffBookingRequestError } from '@/booking/api/staffBookingClient.ts'

/**
 * Seat Hold creation carries no idempotency key, so an ambiguous failure may
 * still have created the Hold; retrying the same Seats then conflicts with it.
 * The fallback says so instead of promising a safe retry.
 */
export function counterHoldErrorMessage(error: unknown): string {
  if (isStaffBookingRequestError(error)) {
    return counterSaleErrorMessage(error)
  }
  return 'Unable to hold Seats. If the sale was interrupted, the Seats may show as held until the interrupted Seat Hold expires.'
}

export function counterSaleErrorMessage(error: unknown): string {
  if (isStaffBookingRequestError(error)) {
    switch (error.code) {
      case 'booking.counter_sales_cutoff':
        return 'Counter sales closed fifteen minutes after the Showtime started.'
      case 'booking.seats_unavailable':
        return 'One or more selected Seats are no longer available. Current availability has been refreshed.'
      case 'booking.limit':
        return 'The Seat selection exceeds the Booking Limit.'
      case 'booking.rate_limited':
        return 'Too many attempts. Wait a moment and try again.'
      default:
        return 'Unable to complete the sale. Try again shortly.'
    }
  }
  return 'Unable to complete the sale. Retrying is safe — the same Booking is reused and the Payment is recorded only once.'
}
