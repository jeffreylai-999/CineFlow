import { type TicketType } from '@/booking/api/customerClient.ts'

export function parseTicketType(value: string): TicketType {
  switch (value) {
    case 'ADULT':
    case 'CHILD':
      return value
    default:
      throw new Error(`Unknown Ticket Type: ${value}`)
  }
}

export function ticketPrice(
  ticketType: TicketType,
  prices: { adultPriceMyr: number; childPriceMyr: number },
): number {
  switch (ticketType) {
    case 'ADULT':
      return prices.adultPriceMyr
    case 'CHILD':
      return prices.childPriceMyr
    default: {
      const exhaustive: never = ticketType
      return exhaustive
    }
  }
}
