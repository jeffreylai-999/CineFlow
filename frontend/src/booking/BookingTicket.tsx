import { useEffect, useState } from 'react'
import QRCode from 'qrcode'
import type { BookingConfirmation } from '@/booking/api/customerClient.ts'
import { formatMyr } from '@/booking/formatMyr.ts'
import { Button } from '@/components/ui/button.tsx'

/**
 * The printable Ticket shared by online checkout and counter sales. One Ticket
 * covers every Seat in the Booking; the QR code carries the opaque Admission
 * token. A Booking with no email belongs to a Walk-in Customer.
 */
export function BookingTicket({ confirmation }: { confirmation: BookingConfirmation }) {
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
          <p className="text-sm">
            {confirmation.email ? `Ticket for ${confirmation.email}` : 'Ticket for Walk-in Customer'}
          </p>
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
      </div>
    </>
  )
}
