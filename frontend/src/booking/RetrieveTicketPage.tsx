import { useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import {
  CustomerRequestError,
  type BookingConfirmation,
  type CustomerClient,
} from '@/booking/api/customerClient.ts'
import { TicketView } from '@/booking/TicketView.tsx'
import { Button } from '@/components/ui/button.tsx'
import { Input } from '@/components/ui/input.tsx'
import { Label } from '@/components/ui/label.tsx'

const RETRIEVED_QR_NOTE =
  'The Ticket QR code was shown once when the Payment succeeded. Present this Booking Reference at admission and Staff will find the Booking with it.'

type RetrieveTicketPageProps = {
  client: CustomerClient
}

export function RetrieveTicketPage({ client }: RetrieveTicketPageProps) {
  const [email, setEmail] = useState('')
  const [bookingReference, setBookingReference] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [ticket, setTicket] = useState<BookingConfirmation | null>(null)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (submitting || ticket !== null) {
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      const found = await client.retrieveTicket({
        email: email.trim(),
        bookingReference: bookingReference.trim(),
      })
      setTicket(found)
    } catch (failure: unknown) {
      if (failure instanceof CustomerRequestError) {
        switch (failure.code) {
          case 'booking.retrieval_failed':
            setError(
              'No Booking matches that email and Booking Reference. Check both values — retrieval stays open until seven days after the Showtime.',
            )
            break
          case 'booking.rate_limited':
            setError('Too many attempts. Wait a moment and try again.')
            break
          default:
            setError('Unable to retrieve the Booking. Try again shortly.')
        }
      } else {
        setError('Unable to retrieve the Booking. Try again shortly.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  if (ticket) {
    return (
      <div className="mx-auto flex w-full max-w-2xl flex-col gap-6 px-4 py-8">
        <TicketView confirmation={ticket} heading="Your Ticket" qrNote={RETRIEVED_QR_NOTE} />
      </div>
    )
  }

  return (
    <div className="mx-auto flex w-full max-w-md flex-col gap-6 px-4 py-8">
      <section aria-labelledby="retrieve-ticket-heading" className="space-y-2">
        <h1 id="retrieve-ticket-heading" className="text-2xl font-semibold">
          Find your Ticket
        </h1>
        <p className="text-sm text-muted-foreground">
          Enter the email and Booking Reference from your confirmation. Retrieval stays open until seven days
          after the Showtime.
        </p>
      </section>

      <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
        <div className="grid gap-1">
          <Label htmlFor="retrieve-email">Email</Label>
          <Input
            id="retrieve-email"
            type="email"
            autoComplete="email"
            required
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
        </div>
        <div className="grid gap-1">
          <Label htmlFor="retrieve-booking-reference">Booking Reference</Label>
          <Input
            id="retrieve-booking-reference"
            autoComplete="off"
            required
            value={bookingReference}
            onChange={(event) => setBookingReference(event.target.value)}
          />
        </div>

        {error ? <p role="alert">{error}</p> : null}

        <Button type="submit" disabled={submitting}>
          {submitting ? 'Finding Ticket…' : 'Find Ticket'}
        </Button>
      </form>

      <Link to="/" className="text-sm underline-offset-4 hover:underline">
        Back to Showtimes
      </Link>
    </div>
  )
}
