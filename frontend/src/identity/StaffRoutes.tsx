import { Navigate, Route, Routes, useNavigate } from 'react-router'
import { StaffHomePage } from '@/identity/StaffHomePage.tsx'
import { StaffLoginPage } from '@/identity/StaffLoginPage.tsx'
import { useStaffAuth } from '@/identity/staffAuthContext.ts'

export function StaffRoutes() {
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
    </Routes>
  )
}
