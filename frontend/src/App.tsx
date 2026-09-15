import { Route, Routes } from 'react-router'
import { createCatalogClient } from '@/catalog/api/catalogClient.ts'
import { CatalogPage } from '@/catalog/CatalogPage.tsx'
import { createIdentityClient } from '@/identity/api/identityClient.ts'
import { createStaffSocket } from '@/identity/api/staffSocket.ts'
import { StaffAuthProvider } from '@/identity/StaffAuthProvider.tsx'
import { StaffRoutes } from '@/identity/StaffRoutes.tsx'
import { CustomerShell } from '@/shells/customer/CustomerShell.tsx'

const catalogClient = createCatalogClient()
const identityClient = createIdentityClient()
const staffSocket = createStaffSocket()

function App() {
  return (
    <Routes>
      <Route
        path="/"
        element={
          <CustomerShell>
            <CatalogPage client={catalogClient} />
          </CustomerShell>
        }
      />
      <Route
        path="/staff/*"
        element={
          <StaffAuthProvider client={identityClient} socket={staffSocket}>
            <StaffRoutes client={identityClient} />
          </StaffAuthProvider>
        }
      />
    </Routes>
  )
}

export default App
