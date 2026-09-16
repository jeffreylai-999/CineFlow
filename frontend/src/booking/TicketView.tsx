import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import QRCode from 'qrcode'
import type { BookingConfirmation } from '@/booking/api/customerClient.ts'
import { formatMyr } from '@/booking/formatMyr.ts'
import { Button, buttonVariants } from '@/components/ui/button.tsx'

const REPLAY_QR_NOTE =
  'This Booking was already confirmed. The Ticket QR code was shown when the Payment first succeeded.'

type TicketViewProps = {
  confirmation: BookingConfirmation
  heading?: string
  qrNote?: string
}

/** Printable Ticket card shared by checkout confirmation and Booking retrieval. */
export function TicketView({ confirmation, heading = 'Booking confirmed', qrNote = REPLAY_QR_NOTE }: TicketViewProps) {
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
    <>
      <section
        aria-labelledby="ticket-view-heading"
        className="space-y-4 rounded-md border-2 border-dashed border-border p-6 print:border-black print:bg-white print:text-black"
      >
      <div className="space-y-1">
        <h1 id="ticket-view-heading" className="text-2xl font-semibold">
          {heading}
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
      {confirmation.admissionToken === null ? <p className="text-sm">{qrNote}</p> : null}

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
    </>
  )
}
