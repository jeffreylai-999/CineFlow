import { createCatalogClient } from './api/catalogClient.ts'
import { CatalogPage } from './CatalogPage.tsx'
import './App.css'

const catalogClient = createCatalogClient()

function App() {
  return <CatalogPage client={catalogClient} />
}

export default App
