import { useCallback, useEffect, useRef, useState, type FormEvent, type RefObject } from 'react'
import { admissionFeedback } from '@/admission/admissionFeedback.ts'
import {
  type AdmissionClient,
  type AdmissionConfirmation,
} from '@/admission/api/admissionClient.ts'
import { createTicketScanner, type TicketScanner } from '@/admission/api/ticketScanner.ts'
import { ticketTypeLabel } from '@/booking/ticketType.ts'
import { Button } from '@/components/ui/button.tsx'
import { Input } from '@/components/ui/input.tsx'
import { Label } from '@/components/ui/label.tsx'
import type { StaffSession } from '@/identity/api/identityClient.ts'
import { StaffShell } from '@/shells/staff/StaffShell.tsx'

type AdmissionPageProps = {
  session: StaffSession
  client: AdmissionClient
  onLogout: () => void
  scanner?: TicketScanner
}

type Outcome =
  | { kind: 'admitted'; confirmation: AdmissionConfirmation }
  | { kind: 'already-admitted' }

const DUPLICATE_SCAN_WINDOW_MS = 3_000

export function AdmissionPage({ session, client, onLogout, scanner }: AdmissionPageProps) {
  const [scannerInstance] = useState(() => scanner ?? createTicketScanner())
  const [reference, setReference] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [scanNote, setScanNote] = useState<string | null>(null)
  const [outcome, setOutcome] = useState<Outcome | null>(null)

  const busyRef = useRef(false)
  const clientRef = useRef(client)
  const lastScanRef = useRef<{ token: string; at: number } | null>(null)
  const resultRef = useRef<HTMLHeadingElement>(null)
  const referenceInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    clientRef.current = client
  }, [client])

  const previousOutcomeRef = useRef<Outcome | null>(null)
  useEffect(() => {
    if (outcome !== null) {
      resultRef.current?.focus()
    } else if (previousOutcomeRef.current !== null) {
      referenceInputRef.current?.focus()
    }
    previousOutcomeRef.current = outcome
  }, [outcome])

  const submit = useCallback(async (request: () => Promise<AdmissionConfirmation>) => {
    if (busyRef.current) {
      return
    }
    busyRef.current = true
    setSubmitting(true)
    setError(null)
    try {
      const confirmation = await request()
      setOutcome({ kind: 'admitted', confirmation })
    } catch (failure) {
      const feedback = admissionFeedback(failure)
      if (feedback.kind === 'already-admitted') {
        setOutcome({ kind: 'already-admitted' })
      } else {
        setError(feedback.message)
      }
    } finally {
      busyRef.current = false
      setSubmitting(false)
      setScanNote(null)
    }
  }, [])

  const handleScannedToken = useCallback(
    (token: string) => {
      if (busyRef.current) {
        if (lastScanRef.current?.token !== token) {
          setScanNote(
            'Still admitting the previous Booking. Wait for the result before scanning the next Ticket.',
          )
        }
        return
      }
      const now = Date.now()
      const last = lastScanRef.current
      if (last !== null && last.token === token && now - last.at < DUPLICATE_SCAN_WINDOW_MS) {
        return
      }
      lastScanRef.current = { token, at: now }
      setScanNote(null)
      void submit(() => clientRef.current.admitByToken(token))
    },
    [submit],
  )

  function handleReferenceSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const normalized = reference.trim().toUpperCase()
    void submit(() => clientRef.current.admitByReference(normalized))
  }

  function reset() {
    setOutcome(null)
    setError(null)
    setReference('')
  }

  return (
    <StaffShell role={session.staff.role} username={session.staff.username} onLogout={onLogout}>
      <div className="px-6 py-8">
        <h1 className="text-2xl font-semibold">Admission</h1>
        <p className="mt-2 max-w-2xl text-sm text-muted-foreground">
          Scan the Ticket QR code or enter the Booking Reference. Each Booking is admitted once as a whole
          group.
        </p>

        {outcome === null ? (
          <div className="mt-8 grid max-w-3xl gap-10">
            {error ? (
              <p role="alert" className="max-w-2xl">
                {error}
              </p>
            ) : null}

            <section aria-labelledby="scan-heading" className="space-y-3">
              <h2 id="scan-heading" className="text-lg font-medium">
                Scan Ticket QR
              </h2>
              <TicketScannerPanel scanner={scannerInstance} onToken={handleScannedToken} />
              {scanNote ? (
                <p role="status" className="text-sm">
                  {scanNote}
                </p>
              ) : null}
            </section>

            <section aria-labelledby="reference-heading" className="space-y-3">
              <h2 id="reference-heading" className="text-lg font-medium">
                Enter Booking Reference
              </h2>
              <p id="reference-hint" className="text-sm text-muted-foreground">
                Use the ten-character Booking Reference from the Ticket when scanning fails.
              </p>
              <form className="flex flex-wrap items-end gap-2" onSubmit={handleReferenceSubmit}>
                <div className="grid gap-1">
                  <Label htmlFor="booking-reference">Booking Reference</Label>
                  <Input
                    id="booking-reference"
                    ref={referenceInputRef}
                    className="w-52 font-mono uppercase"
                    autoComplete="off"
                    spellCheck={false}
                    required
                    minLength={10}
                    maxLength={10}
                    aria-describedby="reference-hint"
                    value={reference}
                    disabled={submitting}
                    onChange={(event) => setReference(event.target.value.toUpperCase())}
                  />
                </div>
                <Button type="submit" disabled={submitting}>
                  {submitting ? 'Admitting…' : 'Admit Booking'}
                </Button>
              </form>
            </section>
          </div>
        ) : (
          <AdmissionOutcome outcome={outcome} resultRef={resultRef} onNext={reset} />
        )}
      </div>
    </StaffShell>
  )
}

type TicketScannerPanelProps = {
  scanner: TicketScanner
  onToken: (token: string) => void
}

function TicketScannerPanel({ scanner, onToken }: TicketScannerPanelProps) {
  const videoRef = useRef<HTMLVideoElement>(null)
  const [state, setState] = useState<'idle' | 'starting' | 'scanning' | 'unavailable'>('idle')

  useEffect(() => () => scanner.stop(), [scanner])

  async function start() {
    const video = videoRef.current
    if (!video) {
      return
    }
    setState('starting')
    try {
      await scanner.start(video, onToken)
      setState('scanning')
    } catch {
      setState('unavailable')
    }
  }

  function stop() {
    scanner.stop()
    setState('idle')
  }

  const previewVisible = state === 'starting' || state === 'scanning'

  return (
    <div className="space-y-3">
      <video
        ref={videoRef}
        className={
          previewVisible
            ? 'aspect-video w-full max-w-md rounded-md border border-border bg-black'
            : 'hidden'
        }
        muted
        playsInline
        aria-label="Camera preview for Ticket scanning"
      />
      {state === 'idle' ? (
        <Button type="button" variant="outline" onClick={() => void start()}>
          Start camera
        </Button>
      ) : null}
      {state === 'starting' ? <p role="status">Starting camera…</p> : null}
      {state === 'scanning' ? (
        <>
          <p role="status">Camera on. Point it at the Ticket QR code.</p>
          <Button type="button" variant="outline" onClick={stop}>
            Stop camera
          </Button>
        </>
      ) : null}
      {state === 'unavailable' ? (
        <p role="alert">Camera scanning is unavailable. Enter the Booking Reference instead.</p>
      ) : null}
    </div>
  )
}

type AdmissionOutcomeProps = {
  outcome: Outcome
  resultRef: RefObject<HTMLHeadingElement | null>
  onNext: () => void
}

function AdmissionOutcome({ outcome, resultRef, onNext }: AdmissionOutcomeProps) {
  if (outcome.kind === 'already-admitted') {
    return (
      <section aria-labelledby="admission-result-heading" className="mt-8 max-w-2xl space-y-4">
        <h2 id="admission-result-heading" ref={resultRef} tabIndex={-1} className="text-xl font-semibold">
          Already admitted
        </h2>
        <p role="alert">
          This Booking was already admitted. Its Ticket has been used before — do not admit the group again
          without checking.
        </p>
        <Button type="button" onClick={onNext}>
          Admit another Booking
        </Button>
      </section>
    )
  }

  const { confirmation } = outcome
  const seatCount = confirmation.seats.length
  return (
    <section aria-labelledby="admission-result-heading" className="mt-8 max-w-2xl space-y-4">
      <h2 id="admission-result-heading" ref={resultRef} tabIndex={-1} className="text-xl font-semibold">
        Booking admitted
      </h2>
      <p className="text-sm">
        {confirmation.movieTitle} · {confirmation.startsAtCinemaTime} {confirmation.timeZone} ·{' '}
        {confirmation.hallName}
      </p>
      <p>
        Booking Reference{' '}
        <strong className="font-mono text-lg tracking-widest">{confirmation.bookingReference}</strong>
      </p>
      <ul className="grid list-none gap-1 p-0">
        {confirmation.seats.map((seat) => (
          <li key={seat.label} className="text-sm">
            Seat {seat.label} — {ticketTypeLabel(seat.ticketType)}
          </li>
        ))}
      </ul>
      <p role="status" className="text-sm">
        Admit {seatCount} {seatCount === 1 ? 'person' : 'people'} for this Booking.
      </p>
      <Button type="button" onClick={onNext}>
        Admit another Booking
      </Button>
    </section>
  )
}
