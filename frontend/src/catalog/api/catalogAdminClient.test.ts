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
})
