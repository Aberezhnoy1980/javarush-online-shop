import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import type { OrderDetails } from './api'
import { formatPrice, readErrorMessage } from './api'

export default function OrderPage() {
  const { orderId } = useParams()
  const [order, setOrder] = useState<OrderDetails | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const loadOrder = () => {
    if (!orderId) {
      return
    }
    fetch(`/api/orders/${orderId}`)
      .then((response) => {
        if (response.status === 404) {
          throw new Error('Заказ не найден')
        }
        if (!response.ok) {
          throw new Error('Не удалось загрузить заказ')
        }
        return response.json()
      })
      .then(setOrder)
      .catch((reason: Error) => setError(reason.message))
  }

  useEffect(loadOrder, [orderId])

  const pay = async () => {
    if (!orderId) {
      return
    }
    setBusy(true)
    setError(null)
    const response = await fetch(`/api/orders/${orderId}/payment`, { method: 'POST' })
    if (response.status === 503) {
      setError(await readErrorMessage(response, 'Платёжный сервис временно недоступен. Можно повторить оплату.'))
      loadOrder()
      setBusy(false)
      return
    }
    if (!response.ok) {
      setError(await readErrorMessage(response, 'Не удалось оплатить заказ'))
      setBusy(false)
      return
    }
    setOrder(await response.json())
    setBusy(false)
  }

  const cancelPayment = async () => {
    if (!orderId) {
      return
    }
    setBusy(true)
    setError(null)
    const response = await fetch(`/api/orders/${orderId}/payment/cancel`, { method: 'POST' })
    if (!response.ok) {
      setError(await readErrorMessage(response, 'Не удалось отменить оплату'))
      setBusy(false)
      return
    }
    setOrder(await response.json())
    setBusy(false)
  }

  const canPay =
    order &&
    (order.status === 'NEW' || order.status === 'PAYMENT_FAILED' || order.status === 'PAYMENT_PENDING')
  const canCancel = order?.status === 'PAID'

  return (
    <main className="page">
      <p>
        <Link to="/orders">← К заказам</Link>
      </p>
      {error && <p className="error">{error}</p>}
      {order && (
        <>
          <h1>Заказ #{order.id}</h1>
          <p className="meta">
            {order.status} · {new Date(order.createdAt).toLocaleString('ru-RU')}
          </p>
          <table className="cart-table">
            <thead>
              <tr>
                <th>Товар</th>
                <th>Цена на момент заказа</th>
                <th>Кол-во</th>
                <th>Сумма</th>
              </tr>
            </thead>
            <tbody>
              {order.items.map((item) => (
                <tr key={`${item.productId}-${item.productName}`}>
                  <td>{item.productName}</td>
                  <td>{formatPrice(item.priceAtTime)}</td>
                  <td>{item.quantity}</td>
                  <td>{formatPrice(item.lineTotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className="price">Итого: {formatPrice(order.totalAmount)}</p>
          {order.payment && (
            <p className="meta">
              Платёж: {order.payment.status} · {order.payment.paymentMethod} ·{' '}
              {formatPrice(order.payment.amount)}
              {order.payment.transactionId ? ` · ${order.payment.transactionId}` : ''}
            </p>
          )}
          {canPay && (
            <button type="button" disabled={busy} onClick={() => void pay()}>
              Оплатить
            </button>
          )}
          {canCancel && (
            <button type="button" disabled={busy} onClick={() => void cancelPayment()}>
              Отменить оплату
            </button>
          )}
        </>
      )}
    </main>
  )
}
