import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { Cart } from './api'
import { formatPrice, notifyCartChanged, readErrorMessage } from './api'

export default function CartPage() {
  const navigate = useNavigate()
  const [cart, setCart] = useState<Cart | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const loadCart = () => {
    fetch('/api/cart')
      .then((response) => {
        if (!response.ok) {
          throw new Error('Не удалось загрузить корзину')
        }
        return response.json()
      })
      .then(setCart)
      .catch((reason: Error) => setError(reason.message))
  }

  useEffect(loadCart, [])

  const updateQuantity = async (productId: number, quantity: number) => {
    setBusy(true)
    setError(null)
    const response = await fetch(`/api/cart/items/${productId}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ quantity }),
    })
    if (!response.ok) {
      setError(await readErrorMessage(response, 'Не удалось изменить количество'))
      setBusy(false)
      return
    }
    setCart(await response.json())
    notifyCartChanged()
    setBusy(false)
  }

  const removeItem = async (productId: number) => {
    setBusy(true)
    setError(null)
    const response = await fetch(`/api/cart/items/${productId}`, { method: 'DELETE' })
    if (!response.ok) {
      setError(await readErrorMessage(response, 'Не удалось удалить товар'))
      setBusy(false)
      return
    }
    setCart(await response.json())
    notifyCartChanged()
    setBusy(false)
  }

  const checkout = async () => {
    setBusy(true)
    setError(null)
    const response = await fetch('/api/orders', { method: 'POST' })
    if (!response.ok) {
      setError(await readErrorMessage(response, 'Не удалось оформить заказ'))
      setBusy(false)
      return
    }
    const order = await response.json()
    notifyCartChanged()
    navigate(`/orders/${order.id}`)
  }

  return (
    <main className="page">
      <h1>Корзина</h1>
      {error && <p className="error">{error}</p>}
      {cart && cart.items.length === 0 && <p>Корзина пуста. Выберите товары в каталоге.</p>}
      {cart && cart.items.length > 0 && (
        <>
          <table className="cart-table">
            <thead>
              <tr>
                <th>Товар</th>
                <th>Цена</th>
                <th>Кол-во</th>
                <th>Сумма</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {cart.items.map((item) => (
                <tr key={item.productId}>
                  <td>
                    <Link to={`/products/${item.productId}`}>{item.name}</Link>
                  </td>
                  <td>{formatPrice(item.price)}</td>
                  <td>
                    <input
                      type="number"
                      min="1"
                      max={item.stockQuantity}
                      value={item.quantity}
                      disabled={busy}
                      onChange={(event) => {
                        const quantity = Number(event.target.value)
                        if (quantity >= 1) {
                          void updateQuantity(item.productId, quantity)
                        }
                      }}
                    />
                  </td>
                  <td>{formatPrice(item.lineTotal)}</td>
                  <td>
                    <button type="button" disabled={busy} onClick={() => void removeItem(item.productId)}>
                      Удалить
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className="price">Итого: {formatPrice(cart.totalAmount)}</p>
          <button type="button" disabled={busy} onClick={() => void checkout()}>
            Оформить заказ
          </button>
        </>
      )}
    </main>
  )
}
