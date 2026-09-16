export type MovieSearchHit = {
  externalId: string
  title: string
  year: string | null
  posterUrl: string | null
  providerId: string
}

export type ManagedMovie = {
  id: number
  title: string
  synopsis: string
  genre: string
  runtimeMinutes: number
  ageRating: string
  posterUrl: string | null
  sourceProvider: string
  externalId: string
  sourceRefreshedAt: string
}

export type ImportMovieInput = {
  providerId: string
  externalId: string
  runtimeMinutes?: number
  ageRating?: string
}

export type MovieProviderOption = {
  id: string
  displayName: string
}

export type MovieProviderSettings = {
  activeProviderId: string
  providers: MovieProviderOption[]
}

export type CatalogAdminClient = {
  listMovies: (accessToken: string) => Promise<ManagedMovie[]>
  listProviders: (accessToken: string) => Promise<MovieProviderSettings>
  selectProvider: (providerId: string, accessToken: string) => Promise<MovieProviderSettings>
  search: (query: string, accessToken: string) => Promise<MovieSearchHit[]>
  importMovie: (input: ImportMovieInput, accessToken: string) => Promise<ManagedMovie>
  refresh: (movieId: number, accessToken: string) => Promise<ManagedMovie>
  updateSchedulingFields: (
    movieId: number,
    input: { runtimeMinutes: number; ageRating: string },
    accessToken: string,
  ) => Promise<ManagedMovie>
}

export class CatalogAdminRequestError extends Error {
  readonly status: number
  readonly code: string | undefined
  readonly retryAfterSeconds: number | undefined

  constructor(status: number, code: string | undefined, retryAfterSeconds?: number) {
    super(`Catalog administration request failed with status ${status}`)
    this.status = status
    this.code = code
    this.retryAfterSeconds = retryAfterSeconds
  }
}

export function createCatalogAdminClient(fetcher: typeof fetch = fetch): CatalogAdminClient {
  return {
    listMovies(accessToken) {
      return readJson(fetcher('/api/admin/movies', { headers: headers(accessToken) }))
    },
    listProviders(accessToken) {
      return readJson(fetcher('/api/admin/movie-providers', { headers: headers(accessToken) }))
    },
    selectProvider(providerId, accessToken) {
      return readJson(
        fetcher('/api/admin/movie-providers/active', {
          method: 'PUT',
          headers: jsonHeaders(accessToken),
          body: JSON.stringify({ providerId }),
        }),
      )
    },
    search(query, accessToken) {
      return readJson(
        fetcher(`/api/admin/movies/search?query=${encodeURIComponent(query)}`, {
          headers: headers(accessToken),
        }),
      )
    },
    importMovie(input, accessToken) {
      return readJson(
        fetcher('/api/admin/movies/import', {
          method: 'POST',
          headers: jsonHeaders(accessToken),
          body: JSON.stringify(input),
        }),
      )
    },
    refresh(movieId, accessToken) {
      return readJson(
        fetcher(`/api/admin/movies/${movieId}/refresh`, {
          method: 'POST',
          headers: headers(accessToken),
        }),
      )
    },
    updateSchedulingFields(movieId, input, accessToken) {
      return readJson(
        fetcher(`/api/admin/movies/${movieId}`, {
          method: 'PATCH',
          headers: jsonHeaders(accessToken),
          body: JSON.stringify(input),
        }),
      )
    },
  }
}

function headers(accessToken: string): HeadersInit {
  return {
    Accept: 'application/json',
    Authorization: `Bearer ${accessToken}`,
  }
}

function jsonHeaders(accessToken: string): HeadersInit {
  return {
    ...headers(accessToken),
    'Content-Type': 'application/json',
  }
}

async function readJson<T>(responsePromise: Promise<Response>): Promise<T> {
  const response = await responsePromise
  if (!response.ok) {
    let code: string | undefined
    try {
      const body = (await response.json()) as { code?: string }
      code = body.code
    } catch {
      code = undefined
    }
    throw new CatalogAdminRequestError(response.status, code, retryAfterSeconds(response))
  }
  return (await response.json()) as T
}

function retryAfterSeconds(response: Response): number | undefined {
  const raw = response.headers.get('Retry-After')
  if (raw == null) {
    return undefined
  }
  const seconds = Number(raw)
  if (!Number.isInteger(seconds) || seconds <= 0) {
    return undefined
  }
  return seconds
}

export function isCatalogAdminRequestError(error: unknown): error is CatalogAdminRequestError {
  return error instanceof CatalogAdminRequestError
}
