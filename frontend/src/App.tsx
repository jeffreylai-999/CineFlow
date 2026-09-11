import { createCatalogClient } from '@/catalog/api/catalogClient.ts'
import { CatalogPage } from '@/catalog/CatalogPage.tsx'
import { CustomerShell } from '@/shells/customer/CustomerShell.tsx'

const catalogClient = createCatalogClient()

function App() {
  return (
    <CustomerShell>
      <CatalogPage client={catalogClient} />
    </CustomerShell>
  )
}

export default App
