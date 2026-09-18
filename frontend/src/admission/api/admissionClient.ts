import type { TicketType } from '@/booking/api/customerClient.ts'

export type AdmittedSeat = {
  label: string
  ticketType: TicketType
}

export type AdmissionConfirmation = {
  bookingReference: string
  showtimeId: number
  movieTitle: string
  hallName: string
  startsAtCinemaTime: string
  timeZone: string
  seats: AdmittedSeat[]
  admittedAt: string
}

export type AdmissionClient = {
  admitByToken: (admissionToken: string) => Promise<AdmissionConfirmation>
  admitByReference: (bookingReference: string) => Promise<AdmissionConfirmation>
}

export class AdmissionRequestError extends Error {
  readonly status: number
  readonly code: string | undefined

  constructor(status: number, code: string | undefined) {
    super(`Admission request failed with status ${status}`)
    this.status = status
    this.code = code
  }
}

export function isAdmissionRequestError(error: unknown): error is AdmissionRequestError {
  return error instanceof AdmissionRequestError
}

export function createAdmissionClient(
  accessToken: string,
  fetcher: typeof fetch = fetch,
): AdmissionClient {
  return {
    admitByToken: (admissionToken) => admit(fetcher, accessToken, { admissionToken }),
    admitByReference: (bookingReference) => admit(fetcher, accessToken, { bookingReference }),
  }
}

async function admit(
  fetcher: typeof fetch,
  accessToken: string,
  body: { admissionToken: string } | { bookingReference: string },
): Promise<AdmissionConfirmation> {
  const response = await fetcher('/api/staff/admissions', {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  })
  if (!response.ok) {
    let code: string | undefined
    try {
      const problem = (await response.json()) as { code?: string }
      code = problem.code
    } catch {
      code = undefined
    }
    throw new AdmissionRequestError(response.status, code)
  }
  return (await response.json()) as AdmissionConfirmation
}
