import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import type { CustomerClient, Movie } from '@/booking/api/customerClient.ts'
import { BOOKING_CUTOFF_MESSAGE } from '@/booking/bookingCutoffMessage.ts'
import { formatMyr } from '@/booking/formatMyr.ts'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card.tsx'

type CatalogPageProps = {
  client: CustomerClient
}

type LoadState =
  | { status: 'loading' }
  | { status: 'ready'; movies: Movie[] }
  | { status: 'error'; message: string }

export function CatalogPage({ client }: CatalogPageProps) {
  const [state, setState] = useState<LoadState>({ status: 'loading' })

  useEffect(() => {
    let cancelled = false

    client
      .listMovies()
      .then((movies) => {
        if (!cancelled) {
          setState({ status: 'ready', movies })
        }
      })
      .catch(() => {
        if (!cancelled) {
          setState({
            status: 'error',
            message: 'Unable to load the Movie catalog. Try again shortly.',
          })
        }
      })

    return () => {
      cancelled = true
    }
  }, [client])

  return (
    <div className="mx-auto flex w-full max-w-5xl flex-col gap-6 px-4 py-10">
      <header className="space-y-2">
        <p className="text-3xl font-bold tracking-[0.08em] text-primary uppercase sm:text-5xl">
          CineFlow
        </p>
        <h1 className="text-xl font-medium text-muted-foreground sm:text-2xl">Now showing</h1>
        <p className="text-sm">
          Already booked?{' '}
          <Link className="underline-offset-4 hover:underline" to="/ticket">
            Find your Ticket
          </Link>
        </p>
      </header>

      {state.status === 'loading' ? <p role="status">Loading Movies…</p> : null}
      {state.status === 'error' ? <p role="alert">{state.message}</p> : null}

      {state.status === 'ready' && state.movies.length === 0 ? (
        <p>No Movies with current or future Showtimes.</p>
      ) : null}

      {state.status === 'ready' ? (
        <ul className="grid list-none gap-6 p-0">
          {state.movies.map((movie) => (
            <li key={movie.id}>
              <Card className="overflow-hidden border-border/60 bg-card">
                <div className="grid gap-4 md:grid-cols-[180px_minmax(0,1fr)]">
                  {movie.posterUrl ? (
                    <img
                      className="aspect-[2/3] w-full max-w-[220px] object-cover"
                      src={movie.posterUrl}
                      alt={`Poster for ${movie.title}`}
                    />
                  ) : null}
                  <div>
                    <CardHeader className="px-4 pt-4 md:px-6">
                      <CardTitle className="text-2xl">
                        <h2 className="text-inherit">{movie.title}</h2>
                      </CardTitle>
                      <CardDescription>
                        {movie.genre} · {movie.runtimeMinutes} min · {movie.ageRating}
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="px-4 pb-4 md:px-6">
                      <p className="max-w-2xl text-sm leading-relaxed text-foreground/90">
                        {movie.synopsis}
                      </p>
                      <ShowtimeDates movie={movie} />
                    </CardContent>
                  </div>
                </div>
              </Card>
            </li>
          ))}
        </ul>
      ) : null}

      <footer className="mt-4 border-t border-border/60 pt-6">
        <h2 className="text-sm font-medium text-muted-foreground">Credits</h2>
        <a
          className="mt-3 inline-flex items-center"
          href="https://www.themoviedb.org"
          rel="noreferrer"
        >
          <img className="h-6 w-auto" src="/tmdb-logo.svg" alt="The Movie Database" />
        </a>
        <p className="mt-2 max-w-2xl text-xs text-muted-foreground">
          This product uses the TMDB API but is not endorsed or certified by TMDB.
        </p>
      </footer>
    </div>
  )
}

function ShowtimeDates({ movie }: { movie: Movie }) {
  return (
    <div className="mt-6 space-y-4">
      {movie.dates.map((group) => (
        <section key={group.cinemaDate} aria-labelledby={`date-${movie.id}-${group.cinemaDate}`}>
          <h3
            id={`date-${movie.id}-${group.cinemaDate}`}
            className="text-sm font-medium text-muted-foreground"
          >
            {group.cinemaDate}
          </h3>
          <ul className="mt-2 grid list-none gap-2 p-0">
            {group.showtimes.map((showtime) => (
              <li key={showtime.id}>
                {showtime.checkoutOpen ? (
                  <Link
                    className="inline-flex min-h-11 w-full flex-col rounded-md border border-border/60 bg-secondary px-3 py-2 text-left text-sm focus-visible:ring-3 focus-visible:ring-ring/50 focus-visible:outline-none sm:flex-row sm:items-center sm:justify-between"
                    to={`/showtimes/${showtime.id}`}
                  >
                    <span>
                      {showtime.startsAtCinemaTime} {showtime.timeZone} · {showtime.hallName}
                    </span>
                    <span>
                      Adult {formatMyr(showtime.adultPriceMyr)} · Child{' '}
                      {formatMyr(showtime.childPriceMyr)}
                    </span>
                  </Link>
                ) : (
                  <p>
                    <span className="block rounded-md border border-border/60 px-3 py-2 text-sm text-muted-foreground">
                      {showtime.startsAtCinemaTime} {showtime.timeZone} · {showtime.hallName} · Adult{' '}
                      {formatMyr(showtime.adultPriceMyr)} · Child {formatMyr(showtime.childPriceMyr)}
                    </span>
                    <span className="mt-1 block text-xs text-muted-foreground">
                      {BOOKING_CUTOFF_MESSAGE}
                    </span>
                  </p>
                )}
              </li>
            ))}
          </ul>
        </section>
      ))}
    </div>
  )
}
