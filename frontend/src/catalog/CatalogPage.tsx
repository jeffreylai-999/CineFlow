import { useEffect, useState } from 'react'
import type { CatalogClient, Movie } from '@/catalog/api/catalogClient.ts'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card.tsx'

type CatalogPageProps = {
  client: CatalogClient
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
      </header>

      {state.status === 'loading' ? <p role="status">Loading Movies…</p> : null}
      {state.status === 'error' ? <p role="alert">{state.message}</p> : null}

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
