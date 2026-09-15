import { Navigate, Route, Routes, useNavigate } from 'react-router'
import { AdminMoviesPage } from '@/catalog/AdminMoviesPage.tsx'
import type { CatalogAdminClient } from '@/catalog/api/catalogAdminClient.ts'
import { StaffHomePage } from '@/identity/StaffHomePage.tsx'
import { StaffLoginPage } from '@/identity/StaffLoginPage.tsx'
import { useStaffAuth } from '@/identity/staffAuthContext.ts'

type StaffRoutesProps = {
  catalogAdminClient: CatalogAdminClient
}

export function StaffRoutes({ catalogAdminClient }: StaffRoutesProps) {
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
        path="movies"
        element={
          auth.session?.staff.role === 'ADMINISTRATOR' ? (
            <AdminMoviesPage
              session={auth.session}
              client={catalogAdminClient}
              onLogout={() => void auth.signOut()}
            />
          ) : (
            <Navigate to={auth.session ? '/staff' : '/staff/login'} replace />
          )
        }
      />
    </Routes>
  )
}
