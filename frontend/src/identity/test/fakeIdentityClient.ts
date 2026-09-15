import { vi } from 'vitest'
import type { IdentityClient } from '@/identity/api/identityClient.ts'

export function fakeIdentityClient(overrides: Partial<IdentityClient> = {}): IdentityClient {
  return {
    login: vi.fn(),
    refresh: vi.fn().mockRejectedValue(new Error('no cookie')),
    logout: vi.fn().mockResolvedValue(undefined),
    listStaffAccounts: vi.fn().mockResolvedValue([]),
    createStaffAccount: vi.fn(),
    deactivateStaffAccount: vi.fn().mockResolvedValue(undefined),
    resetStaffPassword: vi.fn().mockResolvedValue(undefined),
    ...overrides,
  }
}
