import { isCatalogAdminRequestError } from '@/catalog/api/catalogAdminClient.ts'

const SAFE_CATALOG_MESSAGE = 'Unable to complete that catalog action. Try again shortly.'

const CATALOG_ERROR_CODES = [
  'catalog.provider_unavailable',
  'catalog.provider_not_configured',
  'catalog.duplicate_import',
  'catalog.rate_limited',
  'catalog.scheduling_fields_required',
  'catalog.movie_not_found',
  'catalog.invalid_request',
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
    case 'catalog.duplicate_import':
      return 'That provider identifier is already in the catalog.'
    case 'catalog.rate_limited':
      return 'Search is temporarily limited. Wait a moment and try again.'
    case 'catalog.scheduling_fields_required':
      return 'Runtime and age rating are required before this Movie can be saved.'
    case 'catalog.movie_not_found':
      return 'That Movie is no longer available to refresh.'
    case 'catalog.invalid_request':
      return 'Enter a search query or complete the required Movie fields.'
    default: {
      const exhausted: never = error.code
      return exhausted
    }
  }
}

function isCatalogErrorCode(code: string): code is CatalogErrorCode {
  return (CATALOG_ERROR_CODES as readonly string[]).includes(code)
}
