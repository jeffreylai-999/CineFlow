import { useEffect, useState, type FormEvent } from 'react'
import type {
  CatalogAdminClient,
  ManagedMovie,
  MovieSearchHit,
} from '@/catalog/api/catalogAdminClient.ts'
import { catalogAdminErrorMessage } from '@/catalog/catalogAdminErrorMessage.ts'
import { Button } from '@/components/ui/button.tsx'
import { Input } from '@/components/ui/input.tsx'
import { Label } from '@/components/ui/label.tsx'
import { StaffShell } from '@/shells/staff/StaffShell.tsx'
import type { StaffSession } from '@/identity/api/identityClient.ts'

type AdminMoviesPageProps = {
  session: StaffSession
  client: CatalogAdminClient
  onLogout: () => void
}

type SchedulingDraft = {
  runtimeMinutes: string
  ageRating: string
}

export function AdminMoviesPage({ session, client, onLogout }: AdminMoviesPageProps) {
  const [query, setQuery] = useState('')
  const [hits, setHits] = useState<MovieSearchHit[]>([])
  const [movies, setMovies] = useState<ManagedMovie[]>([])
  const [drafts, setDrafts] = useState<Record<number, SchedulingDraft>>({})
  const [importRuntimeMinutes, setImportRuntimeMinutes] = useState('')
  const [importAgeRating, setImportAgeRating] = useState('')
  const [busy, setBusy] = useState(true)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    client
      .listMovies(session.accessToken)
      .then((next) => {
        if (!cancelled) {
          setMovies(next)
          setDrafts(toDrafts(next))
        }
      })
      .catch((cause: unknown) => {
        if (!cancelled) {
          setError(catalogAdminErrorMessage(cause))
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
  }, [client, session.accessToken])

  async function onSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setBusy(true)
    setError(null)
    setMessage(null)
    setHits([])
    try {
      setHits(await client.search(query, session.accessToken))
    } catch (cause) {
      setError(catalogAdminErrorMessage(cause))
    } finally {
      setBusy(false)
    }
  }

  async function onImport(externalId: string) {
    const runtimeMinutes = parseOptionalRuntime(importRuntimeMinutes)
    if (runtimeMinutes === 'invalid') {
      setMessage(null)
      setError(
        'Enter a whole number of minutes for import runtime, or leave it blank to use the provider value.',
      )
      return
    }
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      const imported = await client.importMovie(
        importInput(externalId, runtimeMinutes, importAgeRating),
        session.accessToken,
      )
      upsertMovie(imported)
      setMessage(`Imported ${imported.title}.`)
      try {
        const next = await client.listMovies(session.accessToken)
        setMovies(next)
        setDrafts(toDrafts(next))
      } catch {
        // Import already committed; keep the returned Movie in local state.
      }
    } catch (cause) {
      setError(catalogAdminErrorMessage(cause))
    } finally {
      setBusy(false)
    }
  }

  async function onRefresh(movieId: number) {
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      const refreshed = await client.refresh(movieId, session.accessToken)
      upsertMovie(refreshed)
      setMessage(`Refreshed ${refreshed.title}.`)
    } catch (cause) {
      setError(catalogAdminErrorMessage(cause))
    } finally {
      setBusy(false)
    }
  }

  async function onSaveScheduling(movie: ManagedMovie) {
    const draft = drafts[movie.id]
    const runtimeMinutes = Number(draft?.runtimeMinutes)
    const ageRating = draft?.ageRating.trim() ?? ''
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      const updated = await client.updateSchedulingFields(
        movie.id,
        { runtimeMinutes, ageRating },
        session.accessToken,
      )
      upsertMovie(updated)
      setMessage(`Updated runtime and age rating for ${updated.title}.`)
    } catch (cause) {
      setError(catalogAdminErrorMessage(cause))
    } finally {
      setBusy(false)
    }
  }

  function upsertMovie(next: ManagedMovie) {
    setMovies((current) => {
      if (current.some((movie) => movie.id === next.id)) {
        return current.map((movie) => (movie.id === next.id ? next : movie))
      }
      return [...current, next]
    })
    setDrafts((current) => ({
      ...current,
      [next.id]: { runtimeMinutes: String(next.runtimeMinutes), ageRating: next.ageRating },
    }))
  }

  return (
    <StaffShell role={session.staff.role} username={session.staff.username} onLogout={onLogout}>
      <div className="mx-auto flex w-full max-w-4xl flex-col gap-8 px-6 py-8">
        <header className="space-y-2">
          <h1 className="text-2xl font-semibold">Movies</h1>
          <p className="text-sm text-muted-foreground">
            Search TMDB, import a Movie, then keep runtime and age rating locally.
          </p>
        </header>

        <form className="flex flex-col gap-3 sm:flex-row sm:items-end" onSubmit={onSearch}>
          <div className="grid flex-1 gap-2">
            <Label htmlFor="movie-search">Search TMDB</Label>
            <Input
              id="movie-search"
              name="query"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              autoComplete="off"
            />
          </div>
          <div className="grid w-full gap-2 sm:w-36">
            <Label htmlFor="import-runtime">Import runtime</Label>
            <Input
              id="import-runtime"
              inputMode="numeric"
              value={importRuntimeMinutes}
              onChange={(event) => setImportRuntimeMinutes(event.target.value)}
            />
          </div>
          <div className="grid w-full gap-2 sm:w-36">
            <Label htmlFor="import-age-rating">Import age rating</Label>
            <Input
              id="import-age-rating"
              value={importAgeRating}
              onChange={(event) => setImportAgeRating(event.target.value)}
            />
          </div>
          <Button type="submit" disabled={busy || query.trim() === ''}>
            Search
          </Button>
        </form>

        {error ? <p role="alert">{error}</p> : null}
        {message ? <p role="status">{message}</p> : null}

        {hits.length > 0 ? (
          <section aria-labelledby="search-results-heading" className="space-y-3">
            <h2 id="search-results-heading" className="text-lg font-medium">
              Search results
            </h2>
            <ul className="grid list-none gap-3 p-0">
              {hits.map((hit) => (
                <li
                  key={hit.externalId}
                  className="flex flex-col gap-3 rounded-md border border-border/60 p-4 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div>
                    <p className="font-medium">{hit.title}</p>
                    <p className="text-sm text-muted-foreground">
                      {hit.year ?? 'Year unknown'} · {hit.externalId}
                    </p>
                  </div>
                  <Button type="button" disabled={busy} onClick={() => void onImport(hit.externalId)}>
                    Import
                  </Button>
                </li>
              ))}
            </ul>
          </section>
        ) : null}

        <section aria-labelledby="managed-movies-heading" className="space-y-3">
          <h2 id="managed-movies-heading" className="text-lg font-medium">
            Catalog Movies
          </h2>
          <ul className="grid list-none gap-4 p-0">
            {movies.map((movie) => {
              const draft = drafts[movie.id] ?? {
                runtimeMinutes: String(movie.runtimeMinutes),
                ageRating: movie.ageRating,
              }
              return (
                <li key={movie.id} className="space-y-4 rounded-md border border-border/60 p-4">
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                    <div>
                      <h3 className="text-xl font-semibold">{movie.title}</h3>
                      <p className="text-sm text-muted-foreground">
                        {movie.genre} · {movie.sourceProvider} {movie.externalId}
                      </p>
                      <p className="mt-2 max-w-2xl text-sm">{movie.synopsis}</p>
                    </div>
                    <Button
                      type="button"
                      variant="outline"
                      disabled={busy || movie.sourceProvider !== 'tmdb'}
                      onClick={() => void onRefresh(movie.id)}
                    >
                      Refresh metadata
                    </Button>
                  </div>
                  <div className="grid gap-3 sm:grid-cols-[8rem_8rem_auto] sm:items-end">
                    <div className="grid gap-2">
                      <Label htmlFor={`runtime-${movie.id}`}>Runtime (minutes)</Label>
                      <Input
                        id={`runtime-${movie.id}`}
                        inputMode="numeric"
                        value={draft.runtimeMinutes}
                        onChange={(event) =>
                          setDrafts((current) => ({
                            ...current,
                            [movie.id]: { ...draft, runtimeMinutes: event.target.value },
                          }))
                        }
                      />
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor={`age-rating-${movie.id}`}>Age rating</Label>
                      <Input
                        id={`age-rating-${movie.id}`}
                        value={draft.ageRating}
                        onChange={(event) =>
                          setDrafts((current) => ({
                            ...current,
                            [movie.id]: { ...draft, ageRating: event.target.value },
                          }))
                        }
                      />
                    </div>
                    <Button type="button" disabled={busy} onClick={() => void onSaveScheduling(movie)}>
                      Save scheduling fields
                    </Button>
                  </div>
                </li>
              )
            })}
          </ul>
        </section>
      </div>
    </StaffShell>
  )
}

function parseOptionalRuntime(raw: string): number | undefined | 'invalid' {
  const trimmed = raw.trim()
  if (trimmed === '') {
    return undefined
  }
  if (!/^[1-9]\d*$/.test(trimmed)) {
    return 'invalid'
  }
  return Number(trimmed)
}

function importInput(
  externalId: string,
  runtimeMinutes: number | undefined,
  ageRating: string,
): {
  externalId: string
  runtimeMinutes?: number
  ageRating?: string
} {
  return {
    externalId,
    runtimeMinutes,
    ageRating: ageRating.trim() === '' ? undefined : ageRating.trim(),
  }
}

function toDrafts(movies: ManagedMovie[]): Record<number, SchedulingDraft> {
  return Object.fromEntries(
    movies.map((movie) => [
      movie.id,
      { runtimeMinutes: String(movie.runtimeMinutes), ageRating: movie.ageRating },
    ]),
  )
}
