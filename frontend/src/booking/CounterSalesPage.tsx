import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { type CounterShowtime, type StaffBookingClient } from '@/booking/api/staffBookingClient.ts'
import { formatMyr } from '@/booking/formatMyr.ts'
import type { StaffSession } from '@/identity/api/identityClient.ts'
import { StaffShell } from '@/shells/staff/StaffShell.tsx'

type CounterSalesPageProps = {
  session: StaffSession
  client: StaffBookingClient
  onLogout: () => void
}

type LoadState =
  | { status: 'loading' }
  | { status: 'ready'; showtimes: CounterShowtime[] }
  | { status: 'error' }

export function CounterSalesPage({ session, client, onLogout }: CounterSalesPageProps) {
  const [state, setState] = useState<LoadState>({ status: 'loading' })

  useEffect(() => {
    let cancelled = false
    client
      .listCounterShowtimes()
      .then((showtimes) => {
        if (!cancelled) {
          setState({ status: 'ready', showtimes })
        }
      })
      .catch(() => {
        if (!cancelled) {
          setState({ status: 'error' })
        }
      })
    return () => {
      cancelled = true
    }
  }, [client])

  return (
    <StaffShell role={session.staff.role} username={session.staff.username} onLogout={onLogout}>
      <div className="px-6 py-8">
        <h1 className="text-2xl font-semibold">Counter sales</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          Sell Seats to a Walk-in Customer. Sales close fifteen minutes after the Showtime starts. No
          personal details are recorded.
        </p>

        {state.status === 'loading' ? <p role="status">Loading Showtimes…</p> : null}
        {state.status === 'error' ? (
          <p role="alert">Unable to load Showtimes. Try again shortly.</p>
        ) : null}
        {state.status === 'ready' && state.showtimes.length === 0 ? (
          <p className="mt-4 text-sm">No Showtimes are open for counter sales.</p>
        ) : null}
        {state.status === 'ready' && state.showtimes.length > 0 ? (
          <table className="mt-6 w-full border-collapse text-sm">
            <caption className="sr-only">Showtimes open for counter sales</caption>
            <thead>
              <tr className="border-b border-border text-left">
                <th scope="col" className="py-2 pr-4 font-medium">
                  Movie
                </th>
                <th scope="col" className="py-2 pr-4 font-medium">
                  Hall
                </th>
                <th scope="col" className="py-2 pr-4 font-medium">
                  Starts (Cinema Time)
                </th>
                <th scope="col" className="py-2 pr-4 font-medium">
                  Adult
                </th>
                <th scope="col" className="py-2 pr-4 font-medium">
                  Child
                </th>
                <th scope="col" className="py-2 font-medium">
                  <span className="sr-only">Sell</span>
                </th>
              </tr>
            </thead>
            <tbody>
              {state.showtimes.map((showtime) => (
                <tr key={showtime.id} className="border-b border-border/60">
                  <td className="py-2 pr-4">{showtime.movieTitle}</td>
                  <td className="py-2 pr-4">{showtime.hallName}</td>
                  <td className="py-2 pr-4">
                    {showtime.startsAtCinemaTime} {showtime.timeZone}
                  </td>
                  <td className="py-2 pr-4">{formatMyr(showtime.adultPriceMyr)}</td>
                  <td className="py-2 pr-4">{formatMyr(showtime.childPriceMyr)}</td>
                  <td className="py-2">
                    <Link
                      className="underline-offset-4 hover:underline"
                      to={`/staff/counter-sales/${showtime.id}`}
                    >
                      Sell Seats
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : null}
      </div>
    </StaffShell>
  )
}
