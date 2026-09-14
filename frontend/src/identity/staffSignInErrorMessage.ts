import { isIdentityRequestError } from '@/identity/api/identityClient.ts'

export function staffSignInErrorMessage(error: unknown): string {
  if (isIdentityRequestError(error) && error.code === 'auth.rate_limited') {
    return 'Too many sign-in attempts. Try again shortly.'
  }
  return 'Unable to sign in. Check your username and password.'
}
