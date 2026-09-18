import { useEffect, useState, type FormEvent } from 'react'
import type {
  CatalogAdminClient,
  ManagedMovie,
  MovieProviderSettings,
  MovieSearchHit,
} from '@/catalog/api/catalogAdminClient.ts'
import { catalogAdminErrorMessage } from '@/catalog/catalogAdminErrorMessage.ts'
import { Button } from '@/components/ui/button.tsx'
import { FieldLegend, FieldSet } from '@/components/ui/field.tsx'
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
  const [providers, setProviders] = useState<MovieProviderSettings | null>(null)
  const [drafts, setDrafts] = useState<Record<number, SchedulingDraft>>({})
  const [importRuntimeMinutes, setImportRuntimeMinutes] = useState('')
  const [importAgeRating, setImportAgeRating] = useState('')
  const [busy, setBusy] = useState(true)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    let remaining = 2
    function settle() {
      remaining -= 1
      if (!cancelled && remaining === 0) {
        setBusy(false)
      }
    }
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
      .finally(settle)
    client
      .listProviders(session.accessToken)
      .then((next) => {
        if (!cancelled) {
          setProviders(next)
        }
      })
      .catch((cause: unknown) => {
        if (!cancelled) {
          setError(catalogAdminErrorMessage(cause))
        }
      })
      .finally(settle)
    return () => {
      cancelled = true
    }
  }, [client, session.accessToken])

  async function onSelectProvider(providerId: string) {
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      const next = await client.selectProvider(providerId, session.accessToken)
      setProviders(next)
      setHits([])
      const selected = next.providers.find((provider) => provider.id === next.activeProviderId)
      setMessage(`Now searching ${selected?.displayName ?? next.activeProviderId}.`)
    } catch (cause) {
      setError(catalogAdminErrorMessage(cause))
    } finally {
      setBusy(false)
    }
  }

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

  async function onImport(hit: MovieSearchHit) {
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
        importInput(hit, runtimeMinutes, importAgeRating),
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

  async function onArchive(movieId: number) {
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      const archived = await client.archive(movieId, session.accessToken)
      upsertMovie(archived)
      setMessage(`Archived ${archived.title}.`)
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
            Search the selected provider, import a Movie, then keep runtime and age rating locally.
            Existing Movies keep their source and refresh from that provider only.
          </p>
        </header>

        {providers ? (
          <FieldSet className="gap-3 rounded-md border border-border/60 p-4">
            <FieldLegend>Active metadata provider</FieldLegend>
            <p className="text-sm text-muted-foreground">
              The stored selection stays selected even if that provider has no credentials. CineFlow
              does not fail over automatically.
            </p>
            <div className="flex flex-col gap-2">
              {providers.providers.map((provider) => (
                <label key={provider.id} className="flex items-center gap-2 text-sm">
                  <input
                    type="radio"
                    name="active-provider"
                    value={provider.id}
                    checked={providers.activeProviderId === provider.id}
                    disabled={busy}
                    onChange={() => void onSelectProvider(provider.id)}
                  />
                  {provider.displayName}
                </label>
              ))}
            </div>
          </FieldSet>
        ) : null}

        <form className="flex flex-col gap-3 sm:flex-row sm:items-end" onSubmit={onSearch}>
          <div className="grid flex-1 gap-2">
            <Label htmlFor="movie-search">{searchLabel(providers)}</Label>
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
                  <Button type="button" disabled={busy} onClick={() => void onImport(hit)}>
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
              const archived = movie.archivedAt != null
              return (
                <li key={movie.id} className="space-y-4 rounded-md border border-border/60 p-4">
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                    <div>
                      <h3 className="text-xl font-semibold">
                        {movie.title}
                        {archived ? ' (archived)' : ''}
                      </h3>
                      <p className="text-sm text-muted-foreground">
                        {movie.genre} · {movie.sourceProvider} {movie.externalId}
                      </p>
                      <p className="mt-2 max-w-2xl text-sm">{movie.synopsis}</p>
                      {movie.providerRetentionWarning ? (
                        <p role="status" className="mt-2 text-sm text-amber-800 dark:text-amber-200">
                          Provider metadata approaches the retention limit
                          {movie.providerRetentionExpiresAt
                            ? ` on ${formatRetentionDate(movie.providerRetentionExpiresAt)}`
                            : ''}
                          . Refresh from the original source or archive this Movie.
                        </p>
                      ) : null}
                      {archived ? (
                        <p className="mt-2 text-sm text-muted-foreground">
                          Archived — excluded from the public catalog and new Showtimes.
                        </p>
                      ) : null}
                    </div>
                    <div className="flex flex-col gap-2 sm:items-end">
                      {canRefreshFromSource(movie.sourceProvider) ? (
                        <Button
                          type="button"
                          variant="outline"
                          disabled={busy}
                          onClick={() => void onRefresh(movie.id)}
                        >
                          Refresh metadata
                        </Button>
                      ) : null}
                      {archived ? null : (
                        <Button
                          type="button"
                          variant="outline"
                          disabled={busy}
                          onClick={() => void onArchive(movie.id)}
                        >
                          Archive Movie
                        </Button>
                      )}
                    </div>
                  </div>
                  {archived ? null : (
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
                  )}
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
  hit: MovieSearchHit,
  runtimeMinutes: number | undefined,
  ageRating: string,
): {
  providerId: string
  externalId: string
  runtimeMinutes?: number
  ageRating?: string
} {
  return {
    providerId: hit.providerId,
    externalId: hit.externalId,
    runtimeMinutes,
    ageRating: ageRating.trim() === '' ? undefined : ageRating.trim(),
  }
}

function canRefreshFromSource(sourceProvider: string): boolean {
  return sourceProvider === 'tmdb' || sourceProvider === 'omdb'
}

function formatRetentionDate(iso: string): string {
  const parsed = Date.parse(iso)
  if (Number.isNaN(parsed)) {
    return iso
  }
  return new Intl.DateTimeFormat('en-GB', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    timeZone: 'Asia/Kuala_Lumpur',
  }).format(new Date(parsed))
}

function searchLabel(providers: MovieProviderSettings | null): string {
  if (providers == null) {
    return 'Search movies'
  }
  const active = providers.providers.find((provider) => provider.id === providers.activeProviderId)
  return `Search ${active?.displayName ?? providers.activeProviderId}`
}

function toDrafts(movies: ManagedMovie[]): Record<number, SchedulingDraft> {
  return Object.fromEntries(
    movies.map((movie) => [
      movie.id,
      { runtimeMinutes: String(movie.runtimeMinutes), ageRating: movie.ageRating },
    ]),
  )
}
