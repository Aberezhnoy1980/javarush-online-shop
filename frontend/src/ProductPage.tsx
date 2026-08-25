import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import type { ProductDetails } from './api'
import { addToCart, formatPrice } from './api'

export default function ProductPage() {
  const { productId } = useParams()
  const [product, setProduct] = useState<ProductDetails | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  useEffect(() => {
    if (!productId) {
      return
    }
    fetch(`/api/products/${productId}`)
      .then((response) => {
        if (response.status === 404) {
          throw new Error('Товар не найден')
        }
        if (!response.ok) {
          throw new Error('Не удалось загрузить товар')
        }
        return response.json()
      })
      .then(setProduct)
      .catch((reason: Error) => setError(reason.message))
  }, [productId])

  return (
    <main className="page">
      <p>
        <Link to="/">← К каталогу</Link>
      </p>
      {error && <p className="error">{error}</p>}
      {notice && <p className="notice">{notice}</p>}
      {product && (
        <>
          <h1>{product.name}</h1>
          <p className="meta">
            {product.brandName} · {product.categoryName}
          </p>
          <p className="price">{formatPrice(product.price)}</p>
          <p className="stock">В наличии: {product.stockQuantity}</p>
          <p>{product.description}</p>
          <button
            type="button"
            disabled={product.stockQuantity <= 0}
            onClick={() => {
              addToCart(product.id)
                .then(() => setNotice('Товар добавлен в корзину'))
                .catch((reason: Error) => setError(reason.message))
            }}
          >
            В корзину
          </button>
        </>
      )}
    </main>
  )
}
