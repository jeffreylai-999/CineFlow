import { createContext, useContext } from 'react'
import type { StaffSession } from '@/identity/api/identityClient.ts'

export type StaffAuthValue = {
  session: StaffSession | null
  ready: boolean
  signIn: (username: string, password: string) => Promise<void>
  signOut: () => Promise<void>
}

export const StaffAuthContext = createContext<StaffAuthValue | null>(null)

export function useStaffAuth(): StaffAuthValue {
  const value = useContext(StaffAuthContext)
  if (!value) {
    throw new Error('useStaffAuth must be used within StaffAuthProvider')
  }
  return value
}
