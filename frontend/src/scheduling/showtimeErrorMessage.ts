import { isSchedulingRequestError } from '@/scheduling/api/schedulingClient.ts'

const SAFE_MESSAGE = 'Unable to complete that schedule action. Try again shortly.'

const SHOWTIME_ERROR_CODES = [
  'scheduling.invalid_cinema_time',
  'scheduling.showtime_overlap',
  'scheduling.movie_archived',
  'scheduling.hall_archived',
  'scheduling.showtime_has_bookings',
  'scheduling.showtime_not_removable',
  'scheduling.movie_not_found',
  'scheduling.hall_not_found',
  'scheduling.showtime_not_found',
  'scheduling.invalid_price',
] as const

type ShowtimeErrorCode = (typeof SHOWTIME_ERROR_CODES)[number]

export function showtimeErrorMessage(error: unknown): string {
  if (!isSchedulingRequestError(error) || error.code === undefined || !isShowtimeErrorCode(error.code)) {
    return SAFE_MESSAGE
  }
  switch (error.code) {
    case 'scheduling.invalid_cinema_time':
      return 'Enter the Showtime in Cinema Time (Asia/Kuala_Lumpur).'
    case 'scheduling.showtime_overlap':
      return 'That Hall is occupied through the movie runtime and the fifteen-minute Cleaning Buffer.'
    case 'scheduling.movie_archived':
      return 'Archived Movies cannot receive new Showtimes.'
    case 'scheduling.hall_archived':
      return 'Archived Halls cannot receive new Showtimes.'
    case 'scheduling.showtime_has_bookings':
      return 'A Showtime with Bookings cannot be removed.'
    case 'scheduling.showtime_not_removable':
      return 'Only unused future Showtimes can be removed.'
    case 'scheduling.movie_not_found':
      return 'That Movie is not available to schedule.'
    case 'scheduling.hall_not_found':
      return 'That Hall is not available to schedule.'
    case 'scheduling.showtime_not_found':
      return 'That Showtime is no longer available.'
    case 'scheduling.invalid_price':
      return 'Adult and Child Ticket Prices must be numeric Malaysian Ringgit amounts from 0.01 to 999999.99 with at most two decimal places.'
    default: {
      const exhausted: never = error.code
      return exhausted
    }
  }
}

function isShowtimeErrorCode(code: string): code is ShowtimeErrorCode {
  return (SHOWTIME_ERROR_CODES as readonly string[]).includes(code)
}
