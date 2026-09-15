import { isCatalogAdminRequestError } from '@/catalog/api/catalogAdminClient.ts'

const SAFE_CATALOG_MESSAGE = 'Unable to complete that catalog action. Try again shortly.'

const CATALOG_ERROR_CODES = [
  'catalog.provider_unavailable',
  'catalog.provider_not_configured',
  'catalog.provider_mismatch',
  'catalog.provider_quota',
  'catalog.duplicate_import',
  'catalog.rate_limited',
  'catalog.scheduling_fields_required',
  'catalog.movie_not_found',
  'catalog.invalid_request',
  'catalog.save_failed',
  'catalog.showtime_overlap',
] as const

type CatalogErrorCode = (typeof CATALOG_ERROR_CODES)[number]

export function catalogAdminErrorMessage(error: unknown): string {
  if (!isCatalogAdminRequestError(error) || error.code === undefined || !isCatalogErrorCode(error.code)) {
    return SAFE_CATALOG_MESSAGE
  }
  switch (error.code) {
    case 'catalog.provider_unavailable':
      return 'The movie metadata provider is unavailable. Existing Movies remain in the catalog.'
    case 'catalog.provider_not_configured':
      return 'The movie metadata provider is not configured for this deployment.'
    case 'catalog.provider_mismatch':
      return 'The active provider changed. Search again before importing.'
    case 'catalog.provider_quota':
      return withRetryAfter(
        'The movie metadata provider is temporarily limited.',
        'The movie metadata provider is temporarily limited. Wait a moment and try again.',
        error.retryAfterSeconds,
      )
    case 'catalog.duplicate_import':
      return 'That provider identifier is already in the catalog.'
    case 'catalog.rate_limited':
      return withRetryAfter(
        'Search is temporarily limited.',
        'Search is temporarily limited. Wait a moment and try again.',
        error.retryAfterSeconds,
      )
    case 'catalog.scheduling_fields_required':
      return 'Runtime and age rating are required before this Movie can be saved.'
    case 'catalog.movie_not_found':
      return 'That Movie is no longer available to refresh.'
    case 'catalog.invalid_request':
      return 'Enter a search query or complete the required Movie fields.'
    case 'catalog.save_failed':
      return 'Unable to save the Movie. Try again shortly.'
    case 'catalog.showtime_overlap':
      return 'That Hall is occupied through the movie runtime and the fifteen-minute Cleaning Buffer.'
    default: {
      const exhausted: never = error.code
      return exhausted
    }
  }
}

function withRetryAfter(prefix: string, fallback: string, retryAfterSeconds: number | undefined): string {
  if (retryAfterSeconds !== undefined && retryAfterSeconds > 0) {
    return `${prefix} Try again in ${retryAfterSeconds} seconds.`
  }
  return fallback
}

function isCatalogErrorCode(code: string): code is CatalogErrorCode {
  return (CATALOG_ERROR_CODES as readonly string[]).includes(code)
}
