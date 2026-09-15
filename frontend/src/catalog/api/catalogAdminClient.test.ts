import { describe, expect, it, vi } from 'vitest'
import { createCatalogAdminClient } from '@/catalog/api/catalogAdminClient.ts'

describe('createCatalogAdminClient', () => {
  it('surfaces Retry-After seconds on a failed catalog request', async () => {
    const fetcher = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ code: 'catalog.rate_limited' }), {
        status: 429,
        headers: {
          'Content-Type': 'application/json',
          'Retry-After': '12',
        },
      }),
    )
    const client = createCatalogAdminClient(fetcher)

    await expect(client.search('courier gate', 'admin-token')).rejects.toMatchObject({
      status: 429,
      code: 'catalog.rate_limited',
      retryAfterSeconds: 12,
    })
  })

  it('selects the active provider through PUT', async () => {
    const fetcher = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          activeProviderId: 'omdb',
          providers: [
            { id: 'tmdb', displayName: 'TMDB' },
            { id: 'omdb', displayName: 'OMDb' },
          ],
        }),
        { status: 200, headers: { 'Content-Type': 'application/json' } },
      ),
    )
    const client = createCatalogAdminClient(fetcher)

    await expect(client.selectProvider('omdb', 'admin-token')).resolves.toMatchObject({
      activeProviderId: 'omdb',
    })
    expect(fetcher).toHaveBeenCalledWith('/api/admin/movie-providers/active', {
      method: 'PUT',
      headers: {
        Accept: 'application/json',
        Authorization: 'Bearer admin-token',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ providerId: 'omdb' }),
    })
  })
})
