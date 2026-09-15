import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { type CatalogClient, type Movie } from '@/catalog/api/catalogClient.ts'
import { Button } from '@/components/ui/button.tsx'
import { FieldLegend, FieldSet } from '@/components/ui/field.tsx'
import { Input } from '@/components/ui/input.tsx'
import { Label } from '@/components/ui/label.tsx'
import type { StaffSession } from '@/identity/api/identityClient.ts'
import { StaffShell } from '@/shells/staff/StaffShell.tsx'
import {
  type HallSummary,
  type SchedulingClient,
  type Showtime,
} from '@/scheduling/api/schedulingClient.ts'
import { showtimeErrorMessage } from '@/scheduling/showtimeErrorMessage.ts'

const CINEMA_TIME_ZONE = 'Asia/Kuala_Lumpur'

type ShowtimesPageProps = {
  session: StaffSession
  client: SchedulingClient
  catalogClient: CatalogClient
  onLogout: () => void
}

export function ShowtimesPage({ session, client, catalogClient, onLogout }: ShowtimesPageProps) {
  const [movies, setMovies] = useState<Movie[]>([])
  const [halls, setHalls] = useState<HallSummary[]>([])
  const [showtimes, setShowtimes] = useState<Showtime[]>([])
  const [movieId, setMovieId] = useState('')
  const [hallId, setHallId] = useState('')
  const [date, setDate] = useState('')
  const [time, setTime] = useState('')
  const [adultPrice, setAdultPrice] = useState('28.00')
  const [childPrice, setChildPrice] = useState('18.00')
  const [busy, setBusy] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  const activeHalls = useMemo(() => halls.filter((hall) => !hall.archivedAt), [halls])

  useEffect(() => {
    let cancelled = false
    Promise.all([catalogClient.listMovies(), client.listHalls(), client.listShowtimes()])
      .then(([nextMovies, nextHalls, nextShowtimes]) => {
        if (!cancelled) {
          setMovies(nextMovies)
          setHalls(nextHalls)
          setShowtimes(nextShowtimes)
        }
      })
      .catch((cause: unknown) => {
        if (!cancelled) {
          setError(showtimeErrorMessage(cause))
        }
      })
      .finally(() => {
        if (!cancelled) {
          setBusy(false)
        }
      })
    return () => {
      cancelled = true
    }
  }, [catalogClient, client])

  async function onCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const startsAtLocal = `${date}T${time}`
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      const created = await client.createShowtime({
        movieId: Number(movieId),
        hallId: Number(hallId),
        startsAtLocal,
        timeZone: CINEMA_TIME_ZONE,
        adultPriceMyr: Number(adultPrice),
        childPriceMyr: Number(childPrice),
      })
      setShowtimes((current) =>
        [...current.filter((item) => item.id !== created.id), created].sort((left, right) =>
          left.startsAt.localeCompare(right.startsAt),
        ),
      )
      setMessage(`Scheduled ${created.movieTitle} at ${created.startsAtCinemaTime}.`)
    } catch (cause) {
      setError(showtimeErrorMessage(cause))
    } finally {
      setBusy(false)
    }
  }

  async function onUpdatePrices(showtime: Showtime, nextAdultPrice: string, nextChildPrice: string) {
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      const updated = await client.updateShowtimePrices(showtime.id, {
        adultPriceMyr: Number(nextAdultPrice),
        childPriceMyr: Number(nextChildPrice),
      })
      setShowtimes((current) => current.map((item) => (item.id === updated.id ? updated : item)))
      setMessage(`Updated Ticket Prices for ${updated.movieTitle}.`)
    } catch (cause) {
      setError(showtimeErrorMessage(cause))
    } finally {
      setBusy(false)
    }
  }

  async function onRemove(showtime: Showtime) {
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      await client.removeShowtime(showtime.id)
      setShowtimes((current) => current.filter((item) => item.id !== showtime.id))
      setMessage(`Removed the Showtime for ${showtime.movieTitle}.`)
    } catch (cause) {
      setError(showtimeErrorMessage(cause))
    } finally {
      setBusy(false)
    }
  }

  return (
    <StaffShell role={session.staff.role} username={session.staff.username} onLogout={onLogout}>
      <div className="mx-auto flex w-full max-w-4xl flex-col gap-8 px-6 py-8">
        <header className="space-y-2">
          <h1 className="text-2xl font-semibold">Showtimes</h1>
          <p className="text-sm text-muted-foreground">
            Schedule an active Movie in an active Hall. Ticket Prices are tax-inclusive Malaysian
            Ringgit.
          </p>
        </header>

        <form className="grid gap-4 rounded-md border border-border/60 p-4" onSubmit={onCreate}>
          <div className="grid gap-2 sm:grid-cols-2">
            <div className="grid gap-2">
              <Label htmlFor="showtime-movie">Movie</Label>
              <select
                id="showtime-movie"
                className="h-9 rounded-md border border-input bg-background px-3 text-sm"
                value={movieId}
                onChange={(event) => setMovieId(event.target.value)}
                required
              >
                <option value="">Select a Movie</option>
                {movies.map((movie) => (
                  <option key={movie.id} value={movie.id}>
                    {movie.title}
                  </option>
                ))}
              </select>
            </div>
            <div className="grid gap-2">
              <Label htmlFor="showtime-hall">Hall</Label>
              <select
                id="showtime-hall"
                className="h-9 rounded-md border border-input bg-background px-3 text-sm"
                value={hallId}
                onChange={(event) => setHallId(event.target.value)}
                required
              >
                <option value="">Select a Hall</option>
                {activeHalls.map((hall) => (
                  <option key={hall.id} value={hall.id}>
                    {hall.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <FieldSet className="gap-3">
            <FieldLegend>Showtime in Cinema Time (Asia/Kuala_Lumpur)</FieldLegend>
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="grid gap-2">
                <Label htmlFor="showtime-date">Date</Label>
                <Input
                  id="showtime-date"
                  type="date"
                  value={date}
                  onChange={(event) => setDate(event.target.value)}
                  required
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="showtime-time">Time</Label>
                <Input
                  id="showtime-time"
                  type="time"
                  value={time}
                  onChange={(event) => setTime(event.target.value)}
                  required
                />
              </div>
            </div>
          </FieldSet>

          <div className="grid gap-3 sm:grid-cols-2">
            <div className="grid gap-2">
              <Label htmlFor="adult-price">Adult Ticket Price (MYR)</Label>
              <Input
                id="adult-price"
                inputMode="decimal"
                value={adultPrice}
                onChange={(event) => setAdultPrice(event.target.value)}
                required
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="child-price">Child Ticket Price (MYR)</Label>
              <Input
                id="child-price"
                inputMode="decimal"
                value={childPrice}
                onChange={(event) => setChildPrice(event.target.value)}
                required
              />
            </div>
          </div>

          <Button type="submit" disabled={busy || movieId === '' || hallId === '' || date === '' || time === ''}>
            Schedule Showtime
          </Button>
        </form>

        {error ? <p role="alert">{error}</p> : null}
        {message ? <p role="status">{message}</p> : null}

        <section aria-labelledby="scheduled-showtimes-heading" className="space-y-3">
          <h2 id="scheduled-showtimes-heading" className="text-lg font-medium">
            Scheduled Showtimes
          </h2>
          {showtimes.length === 0 ? (
            <p className="text-sm text-muted-foreground">No Showtimes yet.</p>
          ) : (
            <ul className="grid list-none gap-3 p-0">
              {showtimes.map((showtime) => (
                <li
                  key={showtime.id}
                  className="flex flex-col gap-3 rounded-md border border-border/60 p-4"
                >
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                      <p className="font-medium">{showtime.movieTitle}</p>
                      <p className="text-sm text-muted-foreground">
                        {showtime.hallName} · {showtime.startsAtCinemaTime} {showtime.timeZone}
                      </p>
                      <p className="text-sm">
                        Adult RM {showtime.adultPriceMyr.toFixed(2)} · Child RM{' '}
                        {showtime.childPriceMyr.toFixed(2)}
                      </p>
                    </div>
                    <Button
                      type="button"
                      variant="outline"
                      disabled={busy}
                      onClick={() => void onRemove(showtime)}
                    >
                      Remove Showtime
                    </Button>
                  </div>
                  <ShowtimeTicketPrices
                    showtime={showtime}
                    busy={busy}
                    onSave={onUpdatePrices}
                  />
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </StaffShell>
  )
}

type ShowtimeTicketPricesProps = {
  showtime: Showtime
  busy: boolean
  onSave: (showtime: Showtime, adultPrice: string, childPrice: string) => Promise<void>
}

function ShowtimeTicketPrices({ showtime, busy, onSave }: ShowtimeTicketPricesProps) {
  const [adultPrice, setAdultPrice] = useState(() => showtime.adultPriceMyr.toFixed(2))
  const [childPrice, setChildPrice] = useState(() => showtime.childPriceMyr.toFixed(2))

  useEffect(() => {
    setAdultPrice(showtime.adultPriceMyr.toFixed(2))
    setChildPrice(showtime.childPriceMyr.toFixed(2))
  }, [showtime.adultPriceMyr, showtime.childPriceMyr])

  return (
    <form
      className="grid gap-3"
      onSubmit={(event) => {
        event.preventDefault()
        void onSave(showtime, adultPrice, childPrice)
      }}
    >
      <FieldSet className="gap-3">
        <FieldLegend>
          Ticket Prices for {showtime.movieTitle} at {showtime.startsAtCinemaTime}
        </FieldLegend>
        <div className="grid gap-3 sm:grid-cols-2">
          <div className="grid gap-2">
            <Label htmlFor={`adult-price-${showtime.id}`}>Adult Ticket Price (MYR)</Label>
            <Input
              id={`adult-price-${showtime.id}`}
              inputMode="decimal"
              value={adultPrice}
              onChange={(event) => setAdultPrice(event.target.value)}
              required
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor={`child-price-${showtime.id}`}>Child Ticket Price (MYR)</Label>
            <Input
              id={`child-price-${showtime.id}`}
              inputMode="decimal"
              value={childPrice}
              onChange={(event) => setChildPrice(event.target.value)}
              required
            />
          </div>
        </div>
        <Button type="submit" variant="outline" disabled={busy}>
          Update Ticket Prices
        </Button>
      </FieldSet>
    </form>
  )
}
