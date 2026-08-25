import { BrowserRouter, Link, Route, Routes } from 'react-router-dom'
import CatalogPage from './CatalogPage'
import ProductPage from './ProductPage'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <header className="header">
        <Link to="/">JavaRush Online Shop</Link>
      </header>
      <Routes>
        <Route path="/" element={<CatalogPage />} />
        <Route path="/products/:productId" element={<ProductPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
