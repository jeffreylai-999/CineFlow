import { Navigate, Route, Routes, useNavigate } from 'react-router'
import type { IdentityClient } from '@/identity/api/identityClient.ts'
import { StaffAccountsPage } from '@/identity/StaffAccountsPage.tsx'
import { StaffHomePage } from '@/identity/StaffHomePage.tsx'
import { StaffLoginPage } from '@/identity/StaffLoginPage.tsx'
import { useStaffAuth } from '@/identity/staffAuthContext.ts'

type StaffRoutesProps = {
  client: IdentityClient
}

export function StaffRoutes({ client }: StaffRoutesProps) {
  const auth = useStaffAuth()
  const navigate = useNavigate()

  if (!auth.ready) {
    return <p role="status">Restoring session…</p>
  }

  return (
    <Routes>
      <Route
        path="login"
        element={
          auth.session ? (
            <Navigate to="/staff" replace />
          ) : (
            <StaffLoginPage onSignedIn={() => navigate('/staff')} />
          )
        }
      />
      <Route
        path=""
        element={
          auth.session ? (
            <StaffHomePage session={auth.session} onLogout={() => void auth.signOut()} />
          ) : (
            <Navigate to="/staff/login" replace />
          )
        }
      />
      <Route
        path="accounts"
        element={
          auth.session?.staff.role === 'ADMINISTRATOR' ? (
            <StaffAccountsPage
              session={auth.session}
              client={client}
              onLogout={() => void auth.signOut()}
            />
          ) : auth.session ? (
            <Navigate to="/staff" replace />
          ) : (
            <Navigate to="/staff/login" replace />
          )
        }
      />
    </Routes>
  )
}
