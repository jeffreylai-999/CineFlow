import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Link, useParams } from 'react-router'
import type { BookingConfirmation, SeatHold, TicketType } from '@/booking/api/customerClient.ts'
import {
  isStaffBookingRequestError,
  type CounterPaymentMethod,
  type CounterSaleRequest,
  type StaffBookingClient,
  type StaffSeatMap,
} from '@/booking/api/staffBookingClient.ts'
import { BookingTicket } from '@/booking/BookingTicket.tsx'
import { counterHoldErrorMessage, counterSaleErrorMessage } from '@/booking/counterSaleErrorMessage.ts'
import { formatHoldTime } from '@/booking/formatHoldTime.ts'
import { formatMyr } from '@/booking/formatMyr.ts'
import { parseTicketType, ticketPrice } from '@/booking/ticketType.ts'
import { useSeatHoldCountdown } from '@/booking/useSeatHoldCountdown.ts'
import { Button } from '@/components/ui/button.tsx'
import { Label } from '@/components/ui/label.tsx'
import type { StaffSession } from '@/identity/api/identityClient.ts'
import { SeatGrid, type SeatGridItem } from '@/scheduling/SeatGrid.tsx'
import { StaffShell } from '@/shells/staff/StaffShell.tsx'
import './seat-selection.css'

type CounterSalePageProps = {
  session: StaffSession
  client: StaffBookingClient
  onLogout: () => void
}

type LoadState =
  | { status: 'loading' }
  | { status: 'ready'; map: StaffSeatMap }
  | { status: 'not-found' }
  | { status: 'error' }

type Selection = Record<number, TicketType>

type ActiveSeatHold = {
  details: SeatHold
  receivedAt: number
}

export function CounterSalePage({ session, client, onLogout }: CounterSalePageProps) {
  const { showtimeId } = useParams()
  return (
    <CounterSaleScreen
      key={showtimeId}
      session={session}
      client={client}
      onLogout={onLogout}
    />
  )
}

function CounterSaleScreen({ session, client, onLogout }: CounterSalePageProps) {
  const { showtimeId } = useParams()
  const parsedId = Number(showtimeId)
  const validShowtimeId = Number.isInteger(parsedId) && parsedId > 0 ? parsedId : null
  const [state, setState] = useState<LoadState>({ status: 'loading' })
  const [selection, setSelection] = useState<Selection>({})
  const [notice, setNotice] = useState<string | null>(null)
  const [hold, setHold] = useState<ActiveSeatHold | null>(null)
  const [holdLost, setHoldLost] = useState<string | null>(null)
  const [method, setMethod] = useState<CounterPaymentMethod>('CASH')
  const [busy, setBusy] = useState(false)
  const [confirmation, setConfirmation] = useState<BookingConfirmation | null>(null)
  const [idempotencyKey, setIdempotencyKey] = useState(() => crypto.randomUUID())
  const [submittedConfirm, setSubmittedConfirm] = useState<CounterSaleRequest | null>(null)
  const submittedConfirmRef = useRef<CounterSaleRequest | null>(null)
  const [refresh, setRefresh] = useState(0)

  const refreshAvailability = useCallback(() => {
    setRefresh((current) => current + 1)
  }, [])

  const rememberSubmittedConfirm = useCallback((request: CounterSaleRequest | null) => {
    submittedConfirmRef.current = request
    setSubmittedConfirm(request)
  }, [])

  const handleHoldExpired = useCallback(() => {
    setHold(null)
    if (submittedConfirmRef.current) {
      setHoldLost(
        'The Seat Hold expired. If the sale may already have completed, retry the same request to recover the Booking.',
      )
    } else {
      setSelection({})
      setHoldLost('The Seat Hold expired. Current availability has been refreshed.')
    }
    refreshAvailability()
  }, [refreshAvailability])

  const remainingHoldSeconds = useSeatHoldCountdown(
    confirmation ? null : (hold?.details ?? null),
    hold?.receivedAt ?? 0,
    handleHoldExpired,
  )

  useEffect(() => {
    if (validShowtimeId === null) {
      return
    }
    let cancelled = false
    client
      .getStaffSeatMap(validShowtimeId)
      .then((map) => {
        if (!cancelled) {
          setState({ status: 'ready', map })
          if (!hold && !submittedConfirmRef.current) {
            setSelection((current) =>
              Object.fromEntries(
                Object.entries(current).filter(([seatId]) =>
                  map.seats.some((seat) => seat.id === Number(seatId) && seat.state === 'AVAILABLE'),
                ),
              )
            )
          }
        }
      })
      .catch((error: unknown) => {
        if (cancelled) {
          return
        }
        if (
          isStaffBookingRequestError(error) &&
          (error.code === 'booking.showtime_not_found' || error.status === 404)
        ) {
          setState({ status: 'not-found' })
          return
        }
        // A transient refresh must not wipe an already-ready map while a Hold
        // (or recoverable confirm) is still in progress.
        setState((current) => (current.status === 'ready' ? current : { status: 'error' }))
      })
    return () => {
      cancelled = true
    }
  }, [client, hold, refresh, validShowtimeId])

  const readyMap = state.status === 'ready' && state.map.showtimeId === validShowtimeId ? state.map : null
  const selectedCount = Object.keys(selection).length
  const heldSeatIds = useMemo(() => new Set(hold?.details.seatIds), [hold])
  const gridSeats = useMemo(
    () => (readyMap ? readyMap.seats.map((seat) => toGridSeat(seat, selection, heldSeatIds)) : []),
    [heldSeatIds, readyMap, selection],
  )

  function handleSeatActivate(seatId: number) {
    if (!readyMap || !readyMap.counterSalesOpen || busy || hold || submittedConfirm || confirmation) {
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
      setNotice(null)
      return
    }
    if (seat.state !== 'AVAILABLE') {
      return
    }
    if (selectedCount >= readyMap.bookingLimit) {
      setNotice(
        readyMap.bookingLimit === 1
          ? 'This Booking can include at most 1 Seat.'
          : `This Booking can include at most ${readyMap.bookingLimit} Seats.`,
      )
      return
    }
    setSelection({ ...selection, [seatId]: 'ADULT' })
    setNotice(null)
  }

  function setTicketType(seatId: number, ticketType: TicketType) {
    setSelection((current) => ({ ...current, [seatId]: ticketType }))
  }

  async function createHold() {
    if (!readyMap || selectedCount === 0 || busy || hold || submittedConfirm) {
      return
    }
    setBusy(true)
    setNotice(null)
    setHoldLost(null)
    try {
      const created = await client.createCounterHold(readyMap.showtimeId, Object.keys(selection).map(Number))
      setHold({ details: created, receivedAt: performance.now() })
      refreshAvailability()
    } catch (error: unknown) {
      setNotice(counterHoldErrorMessage(error))
      refreshAvailability()
    } finally {
      setBusy(false)
    }
  }

  async function confirmSale() {
    if (!readyMap || busy || confirmation) {
      return
    }
    const request =
      submittedConfirm ??
      (hold
        ? {
            holdId: hold.details.holdId,
            tickets: hold.details.seatIds.map((seatId) => ({
              seatId,
              ticketType: selection[seatId] ?? 'ADULT',
            })),
            method,
            idempotencyKey,
          }
        : null)
    if (!request) {
      return
    }
    setBusy(true)
    setNotice(null)
    rememberSubmittedConfirm(request)
    try {
      const confirmed = await client.confirmCounterSale(readyMap.showtimeId, request)
      setConfirmation(confirmed)
      rememberSubmittedConfirm(null)
      refreshAvailability()
    } catch (error: unknown) {
      if (
        isStaffBookingRequestError(error) &&
        (error.code === 'booking.hold_expired' ||
          error.code === 'booking.hold_not_found' ||
          error.code === 'booking.hold_unavailable')
      ) {
        // Replay lookup already missed, so the Booking was not created — drop the
        // retained request and restart selection.
        rememberSubmittedConfirm(null)
        setHold(null)
        setSelection({})
        setHoldLost('The Seat Hold is no longer active. Choose Seats again to restart the sale.')
        refreshAvailability()
      } else {
        // Ambiguous and typed failures keep the exact request so staff can replay.
        setNotice(counterSaleErrorMessage(error))
        if (isStaffBookingRequestError(error) && error.code === 'booking.counter_sales_cutoff') {
          refreshAvailability()
        }
      }
    } finally {
      setBusy(false)
    }
  }

  function startNewSale() {
    setConfirmation(null)
    setHold(null)
    setSelection({})
    setNotice(null)
    setHoldLost(null)
    rememberSubmittedConfirm(null)
    setMethod('CASH')
    setIdempotencyKey(crypto.randomUUID())
    refreshAvailability()
  }

  const total = readyMap
    ? readyMap.seats.reduce((sum, seat) => {
        const ticketType = selection[seat.id]
        return ticketType ? sum + ticketPrice(ticketType, readyMap) : sum
      }, 0)
    : 0

  const salesClosed = readyMap !== null && !readyMap.counterSalesOpen

  return (
    <StaffShell role={session.staff.role} username={session.staff.username} onLogout={onLogout}>
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-8">
        <nav aria-label="Counter sales steps">
          <ol className="flex flex-wrap gap-3 text-sm">
            <li>
              <Link className="underline-offset-4 hover:underline" to="/staff/counter-sales">
                Counter sales
              </Link>
            </li>
            <li aria-current="step" className="font-medium">
              Seats and Payment
            </li>
          </ol>
        </nav>

        {validShowtimeId === null || state.status === 'not-found' ? (
          <p role="alert">Showtime not found.</p>
        ) : null}
        {validShowtimeId !== null && state.status === 'loading' ? <p role="status">Loading Seats…</p> : null}
        {state.status === 'error' ? <p role="alert">Unable to load Seats. Try again shortly.</p> : null}

        {readyMap && confirmation ? (
          <div className="flex flex-col gap-6">
            <BookingTicket confirmation={confirmation} />
            <div className="print:hidden">
              <Button type="button" variant="outline" onClick={startNewSale}>
                New sale for this Showtime
              </Button>
            </div>
          </div>
        ) : null}

        {readyMap && !confirmation ? (
          <div className="booking-seat-layout">
            <section className="booking-seat-map space-y-4" aria-labelledby="counter-sale-heading">
              <div>
                <h1 id="counter-sale-heading" className="text-2xl font-semibold">
                  Counter sale
                </h1>
                <p className="text-sm text-muted-foreground">
                  {readyMap.movieTitle} · {readyMap.startsAtCinemaTime} {readyMap.timeZone} ·{' '}
                  {readyMap.hallName}
                </p>
                <p className="mt-2 text-sm">
                  A Booking can include at most {readyMap.bookingLimit} Seats. Available Seats use a solid
                  border and ○. Selected Seats use a solid red border and ●. Seat Holds use a dotted border
                  and ◐. Booked Seats use a solid border and ■. Disabled Seats use a dashed border and ✕.
                </p>
              </div>
              {salesClosed ? (
                <p role="alert">Counter sales closed fifteen minutes after the Showtime started.</p>
              ) : null}
              {notice ? <p role="alert">{notice}</p> : null}
              {holdLost ? <p role="alert">{holdLost}</p> : null}
              <SeatGrid seats={gridSeats} onSeatActivate={handleSeatActivate} />
            </section>

            <aside
              className="booking-summary-dock border-t border-border/60 bg-card p-4 md:rounded-md md:border"
              aria-labelledby="counter-sale-summary-heading"
            >
              <h2 id="counter-sale-summary-heading" className="text-lg font-medium">
                Sale summary
              </h2>
              <p className="mt-2 text-sm text-muted-foreground">
                Walk-in Customer — no personal details are recorded.
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
                          <Label htmlFor={`counter-ticket-type-${seat.id}`}>Seat {seat.label} Ticket Type</Label>
                          <select
                            id={`counter-ticket-type-${seat.id}`}
                            className="h-9 rounded-md border border-input bg-background px-3 text-sm"
                            value={ticketType}
                            disabled={busy || salesClosed || submittedConfirm !== null}
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

              {hold || submittedConfirm ? (
                <fieldset className="mt-4" disabled={busy || (salesClosed && submittedConfirm === null)}>
                  <legend className="text-sm font-medium">Received Payment</legend>
                  <div className="mt-2 flex gap-4">
                    <label className="flex items-center gap-2 text-sm">
                      <input
                        type="radio"
                        name="counter-payment-method"
                        value="CASH"
                        checked={(submittedConfirm?.method ?? method) === 'CASH'}
                        disabled={submittedConfirm !== null}
                        onChange={() => setMethod('CASH')}
                      />
                      Cash
                    </label>
                    <label className="flex items-center gap-2 text-sm">
                      <input
                        type="radio"
                        name="counter-payment-method"
                        value="CARD"
                        checked={(submittedConfirm?.method ?? method) === 'CARD'}
                        disabled={submittedConfirm !== null}
                        onChange={() => setMethod('CARD')}
                      />
                      Card
                    </label>
                  </div>
                  <Button
                    type="button"
                    className="mt-3 w-full"
                    disabled={
                      busy ||
                      (salesClosed && submittedConfirm === null) ||
                      (hold !== null && remainingHoldSeconds === 0 && submittedConfirm === null)
                    }
                    onClick={confirmSale}
                  >
                    {busy
                      ? 'Confirming sale…'
                      : submittedConfirm && !hold
                        ? 'Retry previous sale'
                        : `Confirm ${formatMyr(total)} ${(submittedConfirm?.method ?? method) === 'CASH' ? 'Cash' : 'Card'} sale`}
                  </Button>
                </fieldset>
              ) : (
                <Button
                  type="button"
                  className="mt-4 w-full"
                  disabled={selectedCount === 0 || busy || salesClosed}
                  onClick={createHold}
                >
                  {busy ? 'Holding Seats…' : 'Hold selected Seats'}
                </Button>
              )}
            </aside>
          </div>
        ) : null}
      </div>
    </StaffShell>
  )
}

function toGridSeat(
  seat: StaffSeatMap['seats'][number],
  selection: Selection,
  heldSeatIds: ReadonlySet<number>,
): SeatGridItem {
  const base = {
    id: seat.id,
    label: seat.label,
    rowLabel: seat.rowLabel,
    seatNumber: seat.seatNumber,
  }
  if (heldSeatIds.has(seat.id)) {
    return { ...base, visualState: 'selected', pressed: true, ariaDisabled: true }
  }
  if (selection[seat.id]) {
    return { ...base, visualState: 'selected', pressed: true, ariaDisabled: false }
  }
  switch (seat.state) {
    case 'AVAILABLE':
      return { ...base, visualState: 'available', pressed: false, ariaDisabled: false }
    case 'HELD':
      return { ...base, visualState: 'held', pressed: false, ariaDisabled: true }
    case 'BOOKED':
      return { ...base, visualState: 'booked', pressed: false, ariaDisabled: true }
    case 'DISABLED':
      return { ...base, visualState: 'disabled', pressed: false, ariaDisabled: true }
    default: {
      const exhaustive: never = seat.state
      return exhaustive
    }
  }
}
