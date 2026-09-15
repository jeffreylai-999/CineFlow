import { describe, expect, it } from 'vitest'
import { CatalogAdminRequestError } from '@/catalog/api/catalogAdminClient.ts'
import { catalogAdminErrorMessage } from '@/catalog/catalogAdminErrorMessage.ts'

describe('catalogAdminErrorMessage', () => {
  it('includes Retry-After seconds for search and provider quota limits', () => {
    expect(catalogAdminErrorMessage(new CatalogAdminRequestError(429, 'catalog.rate_limited', 12))).toBe(
      'Search is temporarily limited. Try again in 12 seconds.',
    )
    expect(catalogAdminErrorMessage(new CatalogAdminRequestError(429, 'catalog.provider_quota', 8))).toBe(
      'The movie metadata provider is temporarily limited. Try again in 8 seconds.',
    )
  })

  it('explains a Hall overlap when lengthening runtime', () => {
    expect(catalogAdminErrorMessage(new CatalogAdminRequestError(409, 'catalog.showtime_overlap'))).toBe(
      'That Hall is occupied through the movie runtime and the fifteen-minute Cleaning Buffer.',
    )
  })

  it('keeps the fallback wording when Retry-After is absent', () => {
    expect(catalogAdminErrorMessage(new CatalogAdminRequestError(429, 'catalog.rate_limited'))).toBe(
      'Search is temporarily limited. Wait a moment and try again.',
    )
  })

  it('tells the Administrator to search again when the import provider is stale', () => {
    expect(catalogAdminErrorMessage(new CatalogAdminRequestError(409, 'catalog.provider_mismatch'))).toBe(
      'The active provider changed. Search again before importing.',
    )
  })
})
