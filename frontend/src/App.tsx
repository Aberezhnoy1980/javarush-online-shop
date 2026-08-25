import { useEffect, useState } from 'react'
import { BrowserRouter, Link, NavLink, Route, Routes, useLocation } from 'react-router-dom'
import CatalogPage from './CatalogPage'
import ProductPage from './ProductPage'
import CartPage from './CartPage'
import OrdersPage from './OrdersPage'
import OrderPage from './OrderPage'
import AnalyticsPage from './AnalyticsPage'
import type { Cart } from './api'
import './App.css'

function Header() {
  const location = useLocation()
  const [cartCount, setCartCount] = useState(0)

  useEffect(() => {
    const loadCartCount = () => {
      fetch('/api/cart')
        .then((response) => (response.ok ? response.json() : null))
        .then((cart: Cart | null) => {
          if (cart) {
            setCartCount(cart.totalQuantity)
          }
        })
        .catch(() => setCartCount(0))
    }
    loadCartCount()
    window.addEventListener('cart-updated', loadCartCount)
    return () => window.removeEventListener('cart-updated', loadCartCount)
  }, [location.pathname])

  return (
    <header className="header">
      <Link to="/" className="logo">
        JavaRush Online Shop
      </Link>
      <nav className="nav">
        <NavLink to="/" end>
          Каталог
        </NavLink>
        <NavLink to="/cart">Корзина{cartCount > 0 ? ` (${cartCount})` : ''}</NavLink>
        <NavLink to="/orders">Заказы</NavLink>
        <NavLink to="/analytics">Аналитика</NavLink>
      </nav>
    </header>
  )
}

function App() {
  return (
    <BrowserRouter>
      <Header />
      <Routes>
        <Route path="/" element={<CatalogPage />} />
        <Route path="/products/:productId" element={<ProductPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/orders/:orderId" element={<OrderPage />} />
        <Route path="/analytics" element={<AnalyticsPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
