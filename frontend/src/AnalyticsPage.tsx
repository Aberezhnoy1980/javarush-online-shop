import { useEffect, useState } from 'react'
import type { SalesSummary, TopProduct } from './api'
import { formatPrice, readErrorMessage } from './api'

const DEFAULT_FROM = '2020-01-01'

function todayIsoDate(): string {
  return new Date().toISOString().slice(0, 10)
}

export default function AnalyticsPage() {
  const [from, setFrom] = useState(DEFAULT_FROM)
  const [to, setTo] = useState(todayIsoDate)
  const [topProducts, setTopProducts] = useState<TopProduct[]>([])
  const [summary, setSummary] = useState<SalesSummary | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const params = new URLSearchParams({ from, to, limit: '10' })
    setError(null)
    Promise.all([
      fetch(`/api/analytics/top-products?${params}`).then(async (response) => {
        if (!response.ok) {
          throw new Error(await readErrorMessage(response, 'Не удалось загрузить топ товаров'))
        }
        return response.json() as Promise<TopProduct[]>
      }),
      fetch(`/api/analytics/sales-summary?from=${from}&to=${to}`).then(async (response) => {
        if (!response.ok) {
          throw new Error(await readErrorMessage(response, 'Не удалось загрузить сводку продаж'))
        }
        return response.json() as Promise<SalesSummary>
      }),
    ])
      .then(([products, sales]) => {
        setTopProducts(products)
        setSummary(sales)
      })
      .catch((reason: Error) => setError(reason.message))
  }, [from, to])

  return (
    <main className="page">
      <h1>Аналитика продаж</h1>
      <p className="meta">
        В выручку входят заказы в статусах PAID, PROCESSING, SHIPPED и DELIVERED. NEW и неуспешная
        оплата не считаются.
      </p>
      <div className="filters">
        <label>
          С
          <input type="date" value={from} onChange={(event) => setFrom(event.target.value)} />
        </label>
        <label>
          По
          <input type="date" value={to} onChange={(event) => setTo(event.target.value)} />
        </label>
      </div>
      {error && <p className="error">{error}</p>}
      <section className="analytics-block">
        <h2>Топ товаров</h2>
        <table className="cart-table">
          <thead>
            <tr>
              <th>Товар</th>
              <th>Продано</th>
              <th>Выручка</th>
              <th>Средний рейтинг</th>
            </tr>
          </thead>
          <tbody>
            {topProducts.map((product) => (
              <tr key={product.productId}>
                <td>{product.productName}</td>
                <td>{product.unitsSold}</td>
                <td>{formatPrice(product.revenue)}</td>
                <td>{product.averageRating == null ? '—' : product.averageRating.toFixed(1)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {topProducts.length === 0 && !error && <p>Нет продаж за выбранный период.</p>}
      </section>
      {summary && (
        <div className="analytics-grid">
          <section className="analytics-block">
            <h2>Выручка по категориям</h2>
            <table className="cart-table">
              <thead>
                <tr>
                  <th>Категория</th>
                  <th>Заказов</th>
                  <th>Единиц</th>
                  <th>Выручка</th>
                </tr>
              </thead>
              <tbody>
                {summary.categories.map((category) => (
                  <tr key={category.categoryId}>
                    <td>{category.categoryName}</td>
                    <td>{category.orderCount}</td>
                    <td>{category.unitsSold}</td>
                    <td>{formatPrice(category.revenue)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
          <section className="analytics-block">
            <h2>Заказы по статусам</h2>
            <table className="cart-table">
              <thead>
                <tr>
                  <th>Статус</th>
                  <th>Количество</th>
                </tr>
              </thead>
              <tbody>
                {summary.orderStatuses.map((row) => (
                  <tr key={row.status}>
                    <td>{row.status}</td>
                    <td>{row.orderCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
        </div>
      )}
    </main>
  )
}
