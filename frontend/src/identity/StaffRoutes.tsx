import { useMemo } from 'react'
import { Navigate, Route, Routes, useNavigate } from 'react-router'
import { AdminMoviesPage } from '@/catalog/AdminMoviesPage.tsx'
import type { CatalogAdminClient } from '@/catalog/api/catalogAdminClient.ts'
import type { IdentityClient } from '@/identity/api/identityClient.ts'
import { StaffAccountsPage } from '@/identity/StaffAccountsPage.tsx'
import { StaffHomePage } from '@/identity/StaffHomePage.tsx'
import { StaffLoginPage } from '@/identity/StaffLoginPage.tsx'
import { useStaffAuth } from '@/identity/staffAuthContext.ts'
import { HallsPage } from '@/scheduling/HallsPage.tsx'
import { ShowtimesPage } from '@/scheduling/ShowtimesPage.tsx'
import { createSchedulingClient } from '@/scheduling/api/schedulingClient.ts'

type StaffRoutesProps = {
  catalogAdminClient: CatalogAdminClient
  client: IdentityClient
}

export function StaffRoutes({ catalogAdminClient, client }: StaffRoutesProps) {
  const auth = useStaffAuth()
  const navigate = useNavigate()
  const accessToken = auth.session?.accessToken
  const hallsClient = useMemo(
    () => (accessToken ? createSchedulingClient(accessToken) : null),
    [accessToken],
  )

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
          ) : auth.session ? (
            <Navigate to="/staff" replace />
          ) : (
            <Navigate to="/staff/login" replace />
          )
        }
      />
      <Route
        path="showtimes"
        element={
          auth.session?.staff.role === 'ADMINISTRATOR' && hallsClient ? (
            <ShowtimesPage
              session={auth.session}
              client={hallsClient}
              catalogAdminClient={catalogAdminClient}
              onLogout={() => void auth.signOut()}
            />
          ) : auth.session ? (
            <Navigate to="/staff" replace />
          ) : (
            <Navigate to="/staff/login" replace />
          )
        }
      />
      <Route
        path="halls"
        element={
          auth.session?.staff.role === 'ADMINISTRATOR' && hallsClient ? (
            <HallsPage
              session={auth.session}
              client={hallsClient}
              onLogout={() => void auth.signOut()}
            />
          ) : auth.session ? (
            <Navigate to="/staff" replace />
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
