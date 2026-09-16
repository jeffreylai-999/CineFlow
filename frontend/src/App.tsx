import { Route, Routes } from 'react-router'
import { createCustomerClient } from '@/booking/api/customerClient.ts'
import { SeatSelectionPage } from '@/booking/SeatSelectionPage.tsx'
import { createCatalogAdminClient } from '@/catalog/api/catalogAdminClient.ts'
import { CatalogPage } from '@/catalog/CatalogPage.tsx'
import { createIdentityClient } from '@/identity/api/identityClient.ts'
import { createStaffSocket } from '@/identity/api/staffSocket.ts'
import { StaffAuthProvider } from '@/identity/StaffAuthProvider.tsx'
import { StaffRoutes } from '@/identity/StaffRoutes.tsx'
import { CustomerShell } from '@/shells/customer/CustomerShell.tsx'

const customerClient = createCustomerClient()
const catalogAdminClient = createCatalogAdminClient()
const identityClient = createIdentityClient()
const staffSocket = createStaffSocket()

function App() {
  return (
    <Routes>
      <Route
        path="/"
        element={
          <CustomerShell>
            <CatalogPage client={customerClient} />
          </CustomerShell>
        }
      />
      <Route
        path="/showtimes/:showtimeId"
        element={
          <CustomerShell>
            <SeatSelectionPage client={customerClient} />
          </CustomerShell>
        }
      />
      <Route
        path="/staff/*"
        element={
          <StaffAuthProvider client={identityClient} socket={staffSocket}>
            <StaffRoutes catalogAdminClient={catalogAdminClient} client={identityClient} />
          </StaffAuthProvider>
        }
      />
    </Routes>
  )
}

export default App
