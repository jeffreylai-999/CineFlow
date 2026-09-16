import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useParams } from 'react-router'
import QRCode from 'qrcode'
import {
  CustomerRequestError,
  type BookingConfirmation,
  type CustomerClient,
  type SeatHold,
  type ShowtimeSeats,
  type TicketType,
} from '@/booking/api/customerClient.ts'
import { formatHoldTime } from '@/booking/formatHoldTime.ts'
import { formatMyr } from '@/booking/formatMyr.ts'
import { useSeatHoldCountdown } from '@/booking/useSeatHoldCountdown.ts'
import { Button, buttonVariants } from '@/components/ui/button.tsx'
import { Input } from '@/components/ui/input.tsx'
import { Label } from '@/components/ui/label.tsx'

export type CheckoutLocationState = {
  hold: SeatHold
  holdReceivedAt: number
  selection: Record<number, TicketType>
  map: ShowtimeSeats
}

type CheckoutPageProps = {
  client: CustomerClient
}

export function CheckoutPage({ client }: CheckoutPageProps) {
  const { showtimeId } = useParams()
  const location = useLocation()
  const parsedId = Number(showtimeId)
  const state = readCheckoutState(location.state)
  if (state === null || !Number.isInteger(parsedId) || parsedId <= 0 || state.map.showtimeId !== parsedId) {
    const fallback = Number.isInteger(parsedId) && parsedId > 0 ? `/showtimes/${parsedId}` : '/'
    return <Navigate to={fallback} replace />
  }
  return (
    <CheckoutScreen
      client={client}
      hold={state.hold}
      holdReceivedAt={state.holdReceivedAt}
      initialSelection={state.selection}
      map={state.map}
    />
  )
}

type CheckoutScreenProps = {
  client: CustomerClient
  hold: SeatHold
  holdReceivedAt: number
  initialSelection: Record<number, TicketType>
  map: ShowtimeSeats
}

function CheckoutScreen({ client, hold, holdReceivedAt, initialSelection, map }: CheckoutScreenProps) {
  const heldSeats = map.seats.filter((seat) => hold.seatIds.includes(seat.id))
  const [tickets, setTickets] = useState<Record<number, TicketType>>(() =>
    Object.fromEntries(heldSeats.map((seat) => [seat.id, initialSelection[seat.id] ?? 'ADULT'])),
  )
  const [email, setEmail] = useState('')
  const [cardNumber, setCardNumber] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [holdLost, setHoldLost] = useState<string | null>(null)
  const [confirmation, setConfirmation] = useState<BookingConfirmation | null>(null)
  const [idempotencyKey] = useState(() => crypto.randomUUID())

  const handleHoldExpired = useCallback(() => {
    setHoldLost('Your Seat Hold expired before the Payment completed.')
  }, [])

  const remainingSeconds = useSeatHoldCountdown(confirmation ? null : hold, holdReceivedAt, handleHoldExpired)

  const total = heldSeats.reduce((sum, seat) => sum + ticketPrice(tickets[seat.id] ?? 'ADULT', map), 0)

  function setTicketType(seatId: number, ticketType: TicketType) {
    setTickets((current) => ({ ...current, [seatId]: ticketType }))
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (submitting || confirmation !== null || holdLost !== null) {
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      const confirmed = await client.checkout(map.showtimeId, {
        holdId: hold.holdId,
        email: email.trim(),
        tickets: heldSeats.map((seat) => ({ seatId: seat.id, ticketType: tickets[seat.id] ?? 'ADULT' })),
        cardNumber,
        idempotencyKey,
      })
      setConfirmation(confirmed)
    } catch (failure: unknown) {
      if (failure instanceof CustomerRequestError) {
        switch (failure.code) {
          case 'booking.payment_declined':
            setError(
              'Payment was declined. No charge was made and your Seats are still held — check the card number and try again.',
            )
            break
          case 'booking.hold_expired':
          case 'booking.hold_not_found':
          case 'booking.hold_unavailable':
            setHoldLost('Your Seat Hold is no longer active.')
            break
          case 'booking.rate_limited':
            setError('Too many attempts. Wait a moment and try again.')
            break
          default:
            setError('Unable to confirm the Payment. Try again shortly.')
        }
      } else {
        setError('Unable to confirm the Payment. Retrying is safe — the same Booking is reused, never charged twice.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  if (confirmation) {
    return <TicketConfirmation confirmation={confirmation} />
  }

  const checkoutClosed = holdLost !== null || remainingSeconds === 0

  return (
    <div className="mx-auto flex w-full max-w-2xl flex-col gap-6 px-4 py-8">
      <nav aria-label="Booking steps">
        <ol className="flex flex-wrap gap-3 text-sm">
          <li>
            <Link className="underline-offset-4 hover:underline" to="/">
              Showtime
            </Link>
          </li>
          <li>
            <Link className="underline-offset-4 hover:underline" to={`/showtimes/${map.showtimeId}`}>
              Seats
            </Link>
          </li>
          <li aria-current="step" className="font-medium">
            Payment
          </li>
        </ol>
      </nav>

      <section aria-labelledby="checkout-heading" className="space-y-2">
        <h1 id="checkout-heading" className="text-2xl font-semibold">
          Checkout
        </h1>
        <p className="text-sm text-muted-foreground">
          {map.movieTitle} · {map.startsAtCinemaTime} {map.timeZone} · {map.hallName}
        </p>
        {remainingSeconds !== null && !checkoutClosed ? (
          <p className="text-sm" role="status" aria-label="Seat Hold time remaining">
            Seats held for {formatHoldTime(remainingSeconds)}.
          </p>
        ) : null}
      </section>

      {holdLost ? (
        <p role="alert">
          {holdLost}{' '}
          <Link className="underline-offset-4 hover:underline" to={`/showtimes/${map.showtimeId}`}>
            Choose Seats again
          </Link>{' '}
          to restart checkout.
        </p>
      ) : null}

      <form className="flex flex-col gap-6" onSubmit={handleSubmit}>
        <section aria-label="Seats and Ticket Types" className="space-y-3">
          <h2 className="text-lg font-medium">Seats</h2>
          <ul className="grid list-none gap-3 p-0">
            {heldSeats.map((seat) => (
              <li key={seat.id} className="grid gap-1">
                <Label htmlFor={`checkout-ticket-type-${seat.id}`}>Seat {seat.label} Ticket Type</Label>
                <select
                  id={`checkout-ticket-type-${seat.id}`}
                  className="h-9 rounded-md border border-input bg-background px-3 text-sm"
                  value={tickets[seat.id] ?? 'ADULT'}
                  disabled={checkoutClosed}
                  onChange={(event) => setTicketType(seat.id, parseTicketType(event.target.value))}
                >
                  <option value="ADULT">Adult {formatMyr(map.adultPriceMyr)}</option>
                  <option value="CHILD">Child {formatMyr(map.childPriceMyr)}</option>
                </select>
              </li>
            ))}
          </ul>
          <p className="text-base font-medium" role="status">
            Total {formatMyr(total)}
          </p>
        </section>

        <section aria-label="Contact and Payment" className="space-y-3">
          <div className="grid gap-1">
            <Label htmlFor="checkout-email">Email</Label>
            <Input
              id="checkout-email"
              type="email"
              autoComplete="email"
              required
              value={email}
              disabled={checkoutClosed}
              onChange={(event) => setEmail(event.target.value)}
            />
          </div>
          <div className="grid gap-1">
            <Label htmlFor="checkout-card-number">Card number</Label>
            <Input
              id="checkout-card-number"
              inputMode="numeric"
              autoComplete="cc-number"
              required
              value={cardNumber}
              disabled={checkoutClosed}
              onChange={(event) => setCardNumber(event.target.value)}
            />
            <p className="text-sm text-muted-foreground">
              Simulated Payment: 4242 4242 4242 4242 succeeds and 4000 0000 0000 0002 is declined. No card
              details are stored.
            </p>
          </div>
        </section>

        {error ? <p role="alert">{error}</p> : null}

        <Button type="submit" disabled={submitting || checkoutClosed}>
          {submitting ? 'Confirming Payment…' : `Pay ${formatMyr(total)}`}
        </Button>
      </form>
    </div>
  )
}

function TicketConfirmation({ confirmation }: { confirmation: BookingConfirmation }) {
  const [qrSvg, setQrSvg] = useState<string | null>(null)

  useEffect(() => {
    if (confirmation.admissionToken === null) {
      return
    }
    let cancelled = false
    QRCode.toString(confirmation.admissionToken, { type: 'svg', margin: 1 })
      .then((svg) => {
        if (!cancelled) {
          setQrSvg(svg)
        }
      })
      .catch(() => {
        if (!cancelled) {
          setQrSvg(null)
        }
      })
    return () => {
      cancelled = true
    }
  }, [confirmation.admissionToken])

  return (
    <div className="mx-auto flex w-full max-w-2xl flex-col gap-6 px-4 py-8">
      <nav aria-label="Booking steps" className="print:hidden">
        <ol className="flex flex-wrap gap-3 text-sm">
          <li>
            <Link className="underline-offset-4 hover:underline" to="/">
              Showtime
            </Link>
          </li>
          <li>Seats</li>
          <li aria-current="step" className="font-medium">
            Payment
          </li>
        </ol>
      </nav>

      <section
        aria-labelledby="booking-confirmed-heading"
        className="space-y-4 rounded-md border-2 border-dashed border-border p-6 print:border-black print:bg-white print:text-black"
      >
        <div className="space-y-1">
          <h1 id="booking-confirmed-heading" className="text-2xl font-semibold">
            Booking confirmed
          </h1>
          <p className="text-sm">
            {confirmation.movieTitle} · {confirmation.startsAtCinemaTime} {confirmation.timeZone} ·{' '}
            {confirmation.hallName}
          </p>
          <p className="text-sm">Ticket for {confirmation.email}</p>
        </div>

        <p className="text-lg">
          Booking Reference{' '}
          <strong className="font-mono text-2xl tracking-widest">{confirmation.bookingReference}</strong>
        </p>

        {qrSvg ? (
          <div
            className="h-48 w-48 [&>svg]:h-full [&>svg]:w-full"
            role="img"
            aria-label="Ticket QR code"
            dangerouslySetInnerHTML={{ __html: qrSvg }}
          />
        ) : null}
        {confirmation.admissionToken === null ? (
          <p className="text-sm">
            This Booking was already confirmed. The Ticket QR code was shown when the Payment first succeeded.
          </p>
        ) : null}

        <ul className="grid list-none gap-1 p-0">
          {confirmation.seats.map((seat) => (
            <li key={seat.seatId} className="text-sm">
              Seat {seat.label} — {seat.ticketType === 'ADULT' ? 'Adult' : 'Child'} {formatMyr(seat.priceMyr)}
            </li>
          ))}
        </ul>
        <p className="text-base font-medium" role="status">
          Total {formatMyr(confirmation.totalMyr)}
        </p>
        <p className="text-sm">
          One Ticket covers every Seat in this Booking. Present it on screen or in print at admission.
        </p>
      </section>

      <div className="flex gap-3 print:hidden">
        <Button type="button" onClick={() => window.print()}>
          Print Ticket
        </Button>
        <Link to="/" className={buttonVariants({ variant: 'outline' })}>
          Back to Showtimes
        </Link>
      </div>
    </div>
  )
}

function readCheckoutState(value: unknown): CheckoutLocationState | null {
  if (typeof value !== 'object' || value === null) {
    return null
  }
  const candidate = value as Partial<CheckoutLocationState>
  if (!candidate.hold || !candidate.map || !candidate.selection || typeof candidate.holdReceivedAt !== 'number') {
    return null
  }
  return candidate as CheckoutLocationState
}

function parseTicketType(value: string): TicketType {
  switch (value) {
    case 'ADULT':
    case 'CHILD':
      return value
    default:
      throw new Error(`Unknown Ticket Type: ${value}`)
  }
}

function ticketPrice(ticketType: TicketType, map: ShowtimeSeats): number {
  switch (ticketType) {
    case 'ADULT':
      return map.adultPriceMyr
    case 'CHILD':
      return map.childPriceMyr
    default: {
      const exhaustive: never = ticketType
      return exhaustive
    }
  }
}
