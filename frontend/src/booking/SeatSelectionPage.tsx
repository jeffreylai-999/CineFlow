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
import './seat-selection.css'

type SeatSelectionPageProps = {
  client: CustomerClient
}

type LoadState =
  | { status: 'loading' }
  | { status: 'ready'; map: ShowtimeSeats }
  | { status: 'cutoff' }
  | { status: 'not-found' }
  | { status: 'error'; message: string }

type Selection = Record<number, TicketType>

export function SeatSelectionPage({ client }: SeatSelectionPageProps) {
  const { showtimeId } = useParams()
  return <SeatSelectionScreen key={showtimeId} client={client} />
}

function SeatSelectionScreen({ client }: SeatSelectionPageProps) {
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
        if (
          error instanceof CustomerRequestError &&
          (error.code === 'booking.showtime_not_found' || error.status === 404)
        ) {
          setState({ status: 'not-found' })
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

  const readyMap = state.status === 'ready' && state.map.showtimeId === validShowtimeId ? state.map : null
  const selectedCount = Object.keys(selection).length
  const gridSeats = useMemo(
    () => (readyMap ? readyMap.seats.map((seat) => toGridSeat(seat, selection)) : []),
    [readyMap, selection],
  )

  function handleSeatActivate(seatId: number) {
    if (!readyMap) {
      return
    }
    const seat = readyMap.seats.find((item) => item.id === seatId)
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
    if (selectedCount >= readyMap.bookingLimit) {
      setLimitMessage(`This Booking can include at most ${readyMap.bookingLimit} Seats.`)
      return
    }
    setSelection({ ...selection, [seatId]: 'ADULT' })
    setLimitMessage(null)
  }

  function setTicketType(seatId: number, ticketType: TicketType) {
    setSelection((current) => ({ ...current, [seatId]: ticketType }))
  }

  const total = readyMap
    ? readyMap.seats.reduce((sum, seat) => {
        const ticketType = selection[seat.id]
        return ticketType ? sum + ticketPrice(ticketType, readyMap) : sum
      }, 0)
    : 0

  const showNotFound = validShowtimeId === null || state.status === 'not-found'

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

      {showNotFound ? <p role="alert">Showtime not found.</p> : null}
      {validShowtimeId !== null && state.status === 'loading' ? <p role="status">Loading Seats…</p> : null}
      {state.status === 'error' ? <p role="alert">{state.message}</p> : null}
      {state.status === 'cutoff' ? <p role="alert">{BOOKING_CUTOFF_MESSAGE}</p> : null}

      {readyMap ? (
        <div className="booking-seat-layout">
          <section
            className="booking-seat-map space-y-4"
            aria-labelledby="seat-selection-heading"
          >
            <div>
              <h1 id="seat-selection-heading" className="text-2xl font-semibold">
                Choose Seats
              </h1>
              <p className="text-sm text-muted-foreground">
                {readyMap.movieTitle} · {readyMap.startsAtCinemaTime} {readyMap.timeZone} · {readyMap.hallName}
              </p>
              <p className="mt-2 text-sm">
                A Booking can include at most {readyMap.bookingLimit} Seats. Available Seats use a
                solid border and ○. Selected Seats use a solid red border and ●. Unavailable Seats
                use a dotted border and ■.
              </p>
            </div>
            {limitMessage ? <p role="alert">{limitMessage}</p> : null}
            <SeatGrid seats={gridSeats} onSeatActivate={handleSeatActivate} />
          </section>

          <aside
            className="booking-summary-dock border-t border-border/60 bg-card p-4 md:rounded-md md:border"
            aria-labelledby="booking-summary-heading"
          >
            <h2 id="booking-summary-heading" className="text-lg font-medium">
              Booking summary
            </h2>
            <p className="mt-2 text-sm text-muted-foreground">
              {readyMap.movieTitle}
              <br />
              {readyMap.startsAtCinemaTime} {readyMap.timeZone}
              <br />
              {readyMap.hallName}
            </p>
            {selectedCount === 0 ? (
              <p className="mt-3 text-sm">No Seats selected yet.</p>
            ) : (
              <ul className="mt-3 grid list-none gap-3 p-0">
                {readyMap.seats
                  .filter((seat) => selection[seat.id])
                  .map((seat) => {
                    const ticketType = selection[seat.id]
                    if (!ticketType) {
                      return null
                    }
                    return (
                      <li key={seat.id} className="grid gap-1">
                        <Label htmlFor={`ticket-type-${seat.id}`}>Seat {seat.label} Ticket Type</Label>
                        <select
                          id={`ticket-type-${seat.id}`}
                          className="h-9 rounded-md border border-input bg-background px-3 text-sm"
                          value={ticketType}
                          onChange={(event) => setTicketType(seat.id, parseTicketType(event.target.value))}
                        >
                          <option value="ADULT">Adult {formatMyr(readyMap.adultPriceMyr)}</option>
                          <option value="CHILD">Child {formatMyr(readyMap.childPriceMyr)}</option>
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
