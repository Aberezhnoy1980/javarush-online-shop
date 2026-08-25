import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import type { OrderSummary } from './api'
import { formatPrice } from './api'

export default function OrdersPage() {
  const [orders, setOrders] = useState<OrderSummary[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetch('/api/orders')
      .then((response) => {
        if (!response.ok) {
          throw new Error('Не удалось загрузить заказы')
        }
        return response.json()
      })
      .then(setOrders)
      .catch((reason: Error) => setError(reason.message))
  }, [])

  return (
    <main className="page">
      <h1>Заказы</h1>
      {error && <p className="error">{error}</p>}
      {orders.length === 0 && !error && <p>Заказов пока нет.</p>}
      <ul className="order-list">
        {orders.map((order) => (
          <li key={order.id}>
            <Link to={`/orders/${order.id}`}>
              Заказ #{order.id} · {order.status} · {formatPrice(order.totalAmount)}
            </Link>
            <span className="meta">{new Date(order.createdAt).toLocaleString('ru-RU')}</span>
          </li>
        ))}
      </ul>
    </main>
  )
}
