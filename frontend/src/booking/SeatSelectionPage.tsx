import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router'
import {
  CustomerRequestError,
  type CustomerClient,
  type ShowtimeSeats,
  type TicketType,
} from '@/booking/api/customerClient.ts'
import { BOOKING_CUTOFF_MESSAGE } from '@/booking/bookingCutoffMessage.ts'
import { formatMyr } from '@/booking/formatMyr.ts'
import { Button } from '@/components/ui/button.tsx'
import { Label } from '@/components/ui/label.tsx'
import { SeatGrid, type SeatGridItem } from '@/scheduling/SeatGrid.tsx'

type SeatSelectionPageProps = {
  client: CustomerClient
}

type LoadState =
  | { status: 'loading' }
  | { status: 'ready'; map: ShowtimeSeats }
  | { status: 'cutoff' }
  | { status: 'error'; message: string }

type Selection = Record<number, TicketType>

export function SeatSelectionPage({ client }: SeatSelectionPageProps) {
  const { showtimeId } = useParams()
  const parsedId = Number(showtimeId)
  const validShowtimeId = Number.isInteger(parsedId) && parsedId > 0 ? parsedId : null
  const [state, setState] = useState<LoadState>({ status: 'loading' })
  const [selection, setSelection] = useState<Selection>({})
  const [limitMessage, setLimitMessage] = useState<string | null>(null)

  useEffect(() => {
    if (validShowtimeId === null) {
      return
    }
    let cancelled = false
    client
      .getShowtimeSeats(validShowtimeId)
      .then((map) => {
        if (!cancelled) {
          setState({ status: 'ready', map })
          setSelection({})
        }
      })
      .catch((error: unknown) => {
        if (cancelled) {
          return
        }
        if (error instanceof CustomerRequestError && error.code === 'booking.cutoff') {
          setState({ status: 'cutoff' })
          return
        }
        setState({
          status: 'error',
          message: 'Unable to load Seats. Try again shortly.',
        })
      })
    return () => {
      cancelled = true
    }
  }, [client, validShowtimeId])

  const selectedCount = Object.keys(selection).length
  const gridSeats = useMemo(
    () => (state.status === 'ready' ? state.map.seats.map((seat) => toGridSeat(seat, selection)) : []),
    [state, selection],
  )

  function handleSeatActivate(seatId: number) {
    if (state.status !== 'ready') {
      return
    }
    const seat = state.map.seats.find((item) => item.id === seatId)
    if (!seat || !seat.available) {
      return
    }
    if (selection[seatId]) {
      const next = { ...selection }
      delete next[seatId]
      setSelection(next)
      setLimitMessage(null)
      return
    }
    if (selectedCount >= state.map.bookingLimit) {
      setLimitMessage(
        `This Booking can include at most ${state.map.bookingLimit} Seats.`,
      )
      return
    }
    setSelection({ ...selection, [seatId]: 'ADULT' })
    setLimitMessage(null)
  }

  function setTicketType(seatId: number, ticketType: TicketType) {
    setSelection((current) => ({ ...current, [seatId]: ticketType }))
  }

  const total =
    state.status === 'ready'
      ? state.map.seats.reduce((sum, seat) => {
          const ticketType = selection[seat.id]
          return ticketType ? sum + ticketPrice(ticketType, state.map) : sum
        }, 0)
      : 0

  return (
    <div className="mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-8">
      <nav aria-label="Booking steps">
        <ol className="flex flex-wrap gap-3 text-sm">
          <li>
            <Link className="underline-offset-4 hover:underline" to="/">
              Showtime
            </Link>
          </li>
          <li aria-current="step" className="font-medium">
            Seats
          </li>
          <li className="text-muted-foreground">Payment</li>
        </ol>
      </nav>

      {validShowtimeId === null ? <p role="alert">Showtime not found.</p> : null}
      {validShowtimeId !== null && state.status === 'loading' ? <p role="status">Loading Seats…</p> : null}
      {state.status === 'error' ? <p role="alert">{state.message}</p> : null}
      {state.status === 'cutoff' ? (
        <p role="alert">{BOOKING_CUTOFF_MESSAGE}</p>
      ) : null}

      {state.status === 'ready' ? (
        <div className="flex flex-col gap-6 md:grid md:grid-cols-[minmax(0,1fr)_18rem] md:items-start">
          <section className="space-y-4" aria-labelledby="seat-selection-heading">
            <div>
              <h1 id="seat-selection-heading" className="text-2xl font-semibold">
                Choose Seats
              </h1>
              <p className="text-sm text-muted-foreground">
                {state.map.movieTitle} · {state.map.startsAtCinemaTime} {state.map.timeZone} ·{' '}
                {state.map.hallName}
              </p>
              <p className="mt-2 text-sm">
                A Booking can include at most {state.map.bookingLimit} Seats. Available Seats use a
                solid border and ○. Selected Seats use a solid red border and ●. Unavailable Seats
                use a dotted border and ■.
              </p>
            </div>
            {limitMessage ? <p role="alert">{limitMessage}</p> : null}
            <SeatGrid seats={gridSeats} onSeatActivate={handleSeatActivate} />
          </section>

          <aside
            className="sticky bottom-0 z-10 rounded-md border border-border/60 bg-card p-4 md:bottom-auto md:top-4"
            aria-labelledby="booking-summary-heading"
          >
            <h2 id="booking-summary-heading" className="text-lg font-medium">
              Booking summary
            </h2>
            <p className="mt-2 text-sm text-muted-foreground">
              {state.map.movieTitle}
              <br />
              {state.map.startsAtCinemaTime} {state.map.timeZone}
              <br />
              {state.map.hallName}
            </p>
            {selectedCount === 0 ? (
              <p className="mt-3 text-sm">No Seats selected yet.</p>
            ) : (
              <ul className="mt-3 grid list-none gap-3 p-0">
                {state.map.seats
                  .filter((seat) => selection[seat.id])
                  .map((seat) => {
                    const ticketType = selection[seat.id]
                    if (!ticketType) {
                      return null
                    }
                    return (
                      <li key={seat.id} className="grid gap-1">
                        <Label htmlFor={`ticket-type-${seat.id}`}>
                          Seat {seat.label} Ticket Type
                        </Label>
                        <select
                          id={`ticket-type-${seat.id}`}
                          className="h-9 rounded-md border border-input bg-background px-3 text-sm"
                          value={ticketType}
                          onChange={(event) => setTicketType(seat.id, parseTicketType(event.target.value))}
                        >
                          <option value="ADULT">Adult {formatMyr(state.map.adultPriceMyr)}</option>
                          <option value="CHILD">Child {formatMyr(state.map.childPriceMyr)}</option>
                        </select>
                      </li>
                    )
                  })}
              </ul>
            )}
            <p className="mt-4 text-base font-medium">Total {formatMyr(total)}</p>
            <p className="mt-4 text-sm text-muted-foreground">
              Payment is the next step after Seat selection.
            </p>
            <Button type="button" className="mt-3 w-full" disabled>
              Continue to Payment
            </Button>
          </aside>
        </div>
      ) : null}
    </div>
  )
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

function toGridSeat(
  seat: ShowtimeSeats['seats'][number],
  selection: Selection,
): SeatGridItem {
  if (!seat.available) {
    return {
      id: seat.id,
      label: seat.label,
      rowLabel: seat.rowLabel,
      seatNumber: seat.seatNumber,
      visualState: 'unavailable',
      pressed: false,
      accessibleName: `Seat ${seat.label}, unavailable`,
    }
  }
  if (selection[seat.id]) {
    return {
      id: seat.id,
      label: seat.label,
      rowLabel: seat.rowLabel,
      seatNumber: seat.seatNumber,
      visualState: 'selected',
      pressed: true,
      accessibleName: `Seat ${seat.label}, selected`,
    }
  }
  return {
    id: seat.id,
    label: seat.label,
    rowLabel: seat.rowLabel,
    seatNumber: seat.seatNumber,
    visualState: 'available',
    pressed: false,
    accessibleName: `Seat ${seat.label}, available`,
  }
}
