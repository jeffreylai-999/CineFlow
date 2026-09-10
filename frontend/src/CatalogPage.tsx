import { useEffect, useState } from 'react'
import type { CatalogClient, Movie } from './api/catalogClient.ts'

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
    <main className="catalog">
      <p className="brand">CineFlow</p>
      <h1>Now showing</h1>
      {state.status === 'loading' ? <p role="status">Loading Movies…</p> : null}
      {state.status === 'error' ? (
        <p role="alert">{state.message}</p>
      ) : null}
      {state.status === 'ready' ? (
        <ul className="movie-list">
          {state.movies.map((movie) => (
            <li key={movie.id} className="movie">
              {movie.posterUrl ? (
                <img
                  className="movie-poster"
                  src={movie.posterUrl}
                  alt={`Poster for ${movie.title}`}
                />
              ) : null}
              <div className="movie-copy">
                <h2>{movie.title}</h2>
                <p className="movie-meta">
                  {movie.genre} · {movie.runtimeMinutes} min · {movie.ageRating}
                </p>
                <p>{movie.synopsis}</p>
              </div>
            </li>
          ))}
        </ul>
      ) : null}
    </main>
  )
}
