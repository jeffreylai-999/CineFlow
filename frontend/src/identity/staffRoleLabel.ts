import type { StaffRole } from '@/identity/api/identityClient.ts'

export function staffRoleLabel(role: StaffRole): string {
  switch (role) {
    case 'ADMINISTRATOR':
      return 'Administrator'
    case 'BOOKING_STAFF':
      return 'Booking Staff'
    default: {
      const exhaustive: never = role
      return exhaustive
    }
  }
}
