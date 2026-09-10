export type Movie = {
  id: number
  title: string
  synopsis: string
  genre: string
  runtimeMinutes: number
  ageRating: string
  posterUrl: string | null
}

export type CatalogClient = {
  listMovies: () => Promise<Movie[]>
}

export function createCatalogClient(fetcher: typeof fetch = fetch): CatalogClient {
  return {
    async listMovies() {
      const response = await fetcher('/api/movies', {
        headers: { Accept: 'application/json' },
      })
      if (!response.ok) {
        throw new Error(`Catalog request failed with status ${response.status}`)
      }
      return (await response.json()) as Movie[]
    },
  }
}
