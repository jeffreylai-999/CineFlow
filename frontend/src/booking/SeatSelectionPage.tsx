import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import {
  CustomerRequestError,
  type CustomerClient,
  type SeatHold,
  type ShowtimeSeats,
  type TicketType,
} from '@/booking/api/customerClient.ts'
import { type SeatAvailabilitySocket } from '@/booking/api/seatAvailabilitySocket.ts'
import { BOOKING_CUTOFF_MESSAGE } from '@/booking/bookingCutoffMessage.ts'
import { type CheckoutLocationState } from '@/booking/CheckoutPage.tsx'
import { formatHoldTime } from '@/booking/formatHoldTime.ts'
import { formatMyr } from '@/booking/formatMyr.ts'
import { parseTicketType, ticketPrice } from '@/booking/ticketType.ts'
import { useSeatHoldCountdown } from '@/booking/useSeatHoldCountdown.ts'
import { Button } from '@/components/ui/button.tsx'
import { Label } from '@/components/ui/label.tsx'
import { SeatGrid, type SeatGridItem } from '@/scheduling/SeatGrid.tsx'
import './seat-selection.css'

type SeatSelectionPageProps = {
  client: CustomerClient
  socket?: SeatAvailabilitySocket
}

type LoadState =
  | { status: 'loading' }
  | { status: 'ready'; map: ShowtimeSeats }
  | { status: 'cutoff' }
  | { status: 'not-found' }
  | { status: 'error'; message: string }

type Selection = Record<number, TicketType>
type ActiveSeatHold = {
  details: SeatHold
  receivedAt: number
}

export function SeatSelectionPage({ client, socket }: SeatSelectionPageProps) {
  const { showtimeId } = useParams()
  return <SeatSelectionScreen key={showtimeId} client={client} socket={socket} />
}

function SeatSelectionScreen({ client, socket }: SeatSelectionPageProps) {
  const { showtimeId } = useParams()
  const navigate = useNavigate()
  const parsedId = Number(showtimeId)
  const validShowtimeId = Number.isInteger(parsedId) && parsedId > 0 ? parsedId : null
  const [state, setState] = useState<LoadState>({ status: 'loading' })
  const [selection, setSelection] = useState<Selection>({})
  const [limitMessage, setLimitMessage] = useState<string | null>(null)
  const [hold, setHold] = useState<ActiveSeatHold | null>(null)
  const [holdError, setHoldError] = useState<string | null>(null)
  const [creatingHold, setCreatingHold] = useState(false)
  const [refresh, setRefresh] = useState(0)

  const refreshAvailability = useCallback(() => {
    setRefresh((current) => current + 1)
  }, [])

  const handleHoldExpired = useCallback(() => {
    setHold(null)
    setSelection({})
    setHoldError('Your Seat Hold expired. Current availability has been refreshed.')
    refreshAvailability()
  }, [refreshAvailability])

  const remainingHoldSeconds = useSeatHoldCountdown(
    hold?.details ?? null,
    hold?.receivedAt ?? 0,
    handleHoldExpired,
  )

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
          if (!hold) {
            setSelection((current) =>
              Object.fromEntries(
                Object.entries(current).filter(([seatId]) =>
                  map.seats.some((seat) => seat.id === Number(seatId) && seat.available),
                ),
              ),
            )
          }
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
  }, [client, hold, refresh, validShowtimeId])

  useEffect(() => {
    if (validShowtimeId === null || !socket) {
      return
    }
    socket.connect(validShowtimeId, refreshAvailability)
    return () => {
      void socket.disconnect()
    }
  }, [refreshAvailability, socket, validShowtimeId])

  const readyMap = state.status === 'ready' && state.map.showtimeId === validShowtimeId ? state.map : null
  const selectedCount = Object.keys(selection).length
  const heldSeatIds = useMemo(() => new Set(hold?.details.seatIds), [hold])
  const gridSeats = useMemo(
    () => (readyMap ? readyMap.seats.map((seat) => toGridSeat(seat, selection, heldSeatIds)) : []),
    [heldSeatIds, readyMap, selection],
  )

  function handleSeatActivate(seatId: number) {
    if (!readyMap || creatingHold || hold) {
      return
    }
    const seat = readyMap.seats.find((item) => item.id === seatId)
    if (!seat) {
      return
    }
    if (selection[seatId]) {
      const next = { ...selection }
      delete next[seatId]
      setSelection(next)
      setLimitMessage(null)
      return
    }
    if (!seat.available) {
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

  async function createSeatHold() {
    if (!readyMap || selectedCount === 0 || creatingHold || hold) {
      return
    }
    setCreatingHold(true)
    setHoldError(null)
    try {
      const created = await client.createSeatHold(readyMap.showtimeId, Object.keys(selection).map(Number))
      setHold({ details: created, receivedAt: performance.now() })
      refreshAvailability()
    } catch (error: unknown) {
      if (error instanceof CustomerRequestError && error.code === 'booking.seats_unavailable') {
        setHoldError('One or more selected Seats are no longer available. Current availability has been refreshed.')
      } else {
        setHoldError('Unable to hold Seats. Try again shortly.')
      }
      refreshAvailability()
    } finally {
      setCreatingHold(false)
    }
  }

  const total = readyMap
    ? readyMap.seats.reduce((sum, seat) => {
        const ticketType = selection[seat.id]
        return ticketType ? sum + ticketPrice(ticketType, readyMap) : sum
      }, 0)
    : 0

  function continueToPayment() {
    if (!readyMap || !hold) {
      return
    }
    const state: CheckoutLocationState = {
      hold: hold.details,
      holdReceivedAt: hold.receivedAt,
      selection,
      map: readyMap,
    }
    navigate(`/showtimes/${readyMap.showtimeId}/checkout`, { state })
  }

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
            {holdError ? <p role="alert">{holdError}</p> : null}
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
            <p className="mt-4 text-base font-medium" role="status">
              Total {formatMyr(total)}
            </p>
            {hold && remainingHoldSeconds !== null ? (
              <>
                <p className="sr-only" role="status">
                  Seat Hold created.
                </p>
                <p className="mt-2 text-sm" aria-label="Seat Hold time remaining">
                Seats held for {formatHoldTime(remainingHoldSeconds)}.
                </p>
              </>
            ) : null}
            <p className="mt-4 text-sm text-muted-foreground">
              {hold
                ? 'Your Seats are held. Continue to Payment to confirm the Booking.'
                : 'Payment is the next step after Seat selection.'}
            </p>
            {hold ? (
              <Button type="button" className="mt-3 w-full" onClick={continueToPayment}>
                Continue to Payment
              </Button>
            ) : (
              <Button
                type="button"
                className="mt-3 w-full"
                disabled={selectedCount === 0 || creatingHold}
                onClick={createSeatHold}
              >
                {creatingHold ? 'Holding Seats…' : 'Hold selected Seats'}
              </Button>
            )}
          </aside>
        </div>
      ) : null}
    </div>
  )
}

function toGridSeat(
  seat: ShowtimeSeats['seats'][number],
  selection: Selection,
  heldSeatIds: ReadonlySet<number>,
): SeatGridItem {
  if (heldSeatIds.has(seat.id)) {
    return {
      id: seat.id,
      label: seat.label,
      rowLabel: seat.rowLabel,
      seatNumber: seat.seatNumber,
      visualState: 'selected',
      pressed: true,
    }
  }
  if (!seat.available) {
    return {
      id: seat.id,
      label: seat.label,
      rowLabel: seat.rowLabel,
      seatNumber: seat.seatNumber,
      visualState: 'unavailable',
      pressed: false,
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
    }
  }
  return {
    id: seat.id,
    label: seat.label,
    rowLabel: seat.rowLabel,
    seatNumber: seat.seatNumber,
    visualState: 'available',
    pressed: false,
  }
}
