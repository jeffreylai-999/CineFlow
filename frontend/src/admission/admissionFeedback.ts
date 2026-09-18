import { isAdmissionRequestError } from '@/admission/api/admissionClient.ts'

export type AdmissionFeedback =
  | { kind: 'already-admitted' }
  | { kind: 'inline'; message: string }

export function admissionFeedback(error: unknown): AdmissionFeedback {
  if (isAdmissionRequestError(error)) {
    switch (error.code) {
      case 'admission.already_admitted':
        return { kind: 'already-admitted' }
      case 'admission.not_found':
        return {
          kind: 'inline',
          message: 'No Booking matches. Check the Booking Reference or scan the Ticket again.',
        }
      default:
        return { kind: 'inline', message: 'Unable to admit this Booking. Try again shortly.' }
    }
  }
  return { kind: 'inline', message: 'Unable to admit this Booking. Try again shortly.' }
}
