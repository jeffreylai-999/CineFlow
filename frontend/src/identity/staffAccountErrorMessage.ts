import { isIdentityRequestError } from '@/identity/api/identityClient.ts'

export function staffAccountErrorMessage(error: unknown): string {
  if (!isIdentityRequestError(error)) {
    return 'Unable to update Staff accounts. Try again shortly.'
  }
  switch (error.code) {
    case 'staff.username_conflict':
      return 'A Staff account with this username already exists.'
    case 'request.invalid':
      return 'Those details are not valid.'
    default:
      return 'Unable to update Staff accounts. Try again shortly.'
  }
}
