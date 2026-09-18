import { describe, expect, it, vi } from 'vitest'
import {
  AdmissionRequestError,
  createAdmissionClient,
  isAdmissionRequestError,
  type AdmissionConfirmation,
} from '@/admission/api/admissionClient.ts'

const confirmation: AdmissionConfirmation = {
  bookingReference: 'K7M2PQ9X4D',
  movieTitle: 'Nebula Express',
  hallName: 'Hall 1',
  startsAtCinemaTime: '2099-06-20T19:30:00',
  timeZone: 'Asia/Kuala_Lumpur',
  seats: [
    { label: 'A1', ticketType: 'ADULT' },
    { label: 'A2', ticketType: 'CHILD' },
  ],
  admittedAt: '2026-09-16T00:00:00Z',
}

function jsonResponse(body: unknown, status = 201): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('createAdmissionClient', () => {
  it('posts an Admission token with the Staff access token', async () => {
    const fetcher = vi.fn().mockResolvedValue(jsonResponse(confirmation))
    const client = createAdmissionClient('staff-access-token', fetcher)

    await expect(client.admitByToken('qr-token')).resolves.toEqual(confirmation)
    expect(fetcher).toHaveBeenCalledWith('/api/staff/admissions', {
      method: 'POST',
      credentials: 'include',
      headers: {
        Accept: 'application/json',
        Authorization: 'Bearer staff-access-token',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ admissionToken: 'qr-token' }),
    })
  })

  it('posts a Booking Reference for the manual fallback', async () => {
    const fetcher = vi.fn().mockResolvedValue(jsonResponse(confirmation))
    const client = createAdmissionClient('staff-access-token', fetcher)

    await expect(client.admitByReference('K7M2PQ9X4D')).resolves.toEqual(confirmation)
    expect(fetcher).toHaveBeenCalledWith(
      '/api/staff/admissions',
      expect.objectContaining({ body: JSON.stringify({ bookingReference: 'K7M2PQ9X4D' }) }),
    )
  })

  it('raises the stable problem code for an already-admitted Booking', async () => {
    const fetcher = vi
      .fn()
      .mockResolvedValue(jsonResponse({ code: 'admission.already_admitted' }, 409))
    const client = createAdmissionClient('staff-access-token', fetcher)

    const failure = await client.admitByToken('qr-token').catch((error: unknown) => error)
    expect(isAdmissionRequestError(failure)).toBe(true)
    expect(failure).toBeInstanceOf(AdmissionRequestError)
    expect((failure as AdmissionRequestError).status).toBe(409)
    expect((failure as AdmissionRequestError).code).toBe('admission.already_admitted')
  })

  it('raises the stable problem code for an unknown Booking', async () => {
    const fetcher = vi.fn().mockResolvedValue(jsonResponse({ code: 'admission.not_found' }, 404))
    const client = createAdmissionClient('staff-access-token', fetcher)

    await expect(client.admitByReference('ZZZZZZZZZZ')).rejects.toMatchObject({
      status: 404,
      code: 'admission.not_found',
    })
  })

  it('survives a non-problem error body', async () => {
    const fetcher = vi.fn().mockResolvedValue(new Response('upstream down', { status: 502 }))
    const client = createAdmissionClient('staff-access-token', fetcher)

    await expect(client.admitByToken('qr-token')).rejects.toMatchObject({
      status: 502,
      code: undefined,
    })
  })
})
