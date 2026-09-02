import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import type { Brand, Category, PageResponse, ProductCatalogItem, ProductSort } from './api'
import { addToCart, formatPrice } from './api'

export default function CatalogPage() {
  const [query, setQuery] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [brandId, setBrandId] = useState('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [sort, setSort] = useState<ProductSort>('NAME')
  const [page, setPage] = useState(0)
  const [categories, setCategories] = useState<Category[]>([])
  const [brands, setBrands] = useState<Brand[]>([])
  const [result, setResult] = useState<PageResponse<ProductCatalogItem> | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  useEffect(() => {
    Promise.all([
      fetch('/api/categories').then((response) => response.json()),
      fetch('/api/brands').then((response) => response.json()),
    ])
      .then(([nextCategories, nextBrands]) => {
        setCategories(nextCategories)
        setBrands(nextBrands)
      })
      .catch(() => setError('Не удалось загрузить фильтры'))
  }, [])

  const searchParams = useMemo(() => {
    const params = new URLSearchParams({
      sort,
      page: String(page),
      size: '6',
    })
    if (query.trim()) {
      params.set('query', query.trim())
    }
    if (categoryId) {
      params.set('categoryId', categoryId)
    }
    if (brandId) {
      params.set('brandId', brandId)
    }
    if (minPrice) {
      params.set('minPrice', minPrice)
    }
    if (maxPrice) {
      params.set('maxPrice', maxPrice)
    }
    return params
  }, [query, categoryId, brandId, minPrice, maxPrice, sort, page])

  useEffect(() => {
    setError(null)
    fetch(`/api/products?${searchParams}`)
      .then((response) => {
        if (!response.ok) {
          throw new Error('Не удалось загрузить каталог')
        }
        return response.json()
      })
      .then(setResult)
      .catch((reason: Error) => setError(reason.message))
  }, [searchParams])

  return (
    <main className="page catalog">
      <h1>Каталог</h1>
      <form
        className="filters"
        onSubmit={(event) => {
          event.preventDefault()
          setPage(0)
        }}
      >
        <input
          value={query}
          onChange={(event) => {
            setQuery(event.target.value)
            setPage(0)
          }}
          placeholder="Поиск по названию и описанию"
        />
        <select
          value={categoryId}
          onChange={(event) => {
            setCategoryId(event.target.value)
            setPage(0)
          }}
        >
          <option value="">Все категории</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
        <select
          value={brandId}
          onChange={(event) => {
            setBrandId(event.target.value)
            setPage(0)
          }}
        >
          <option value="">Все бренды</option>
          {brands.map((brand) => (
            <option key={brand.id} value={brand.id}>
              {brand.name}
            </option>
          ))}
        </select>
        <input
          type="number"
          min="0"
          value={minPrice}
          onChange={(event) => {
            setMinPrice(event.target.value)
            setPage(0)
          }}
          placeholder="Цена от"
        />
        <input
          type="number"
          min="0"
          value={maxPrice}
          onChange={(event) => {
            setMaxPrice(event.target.value)
            setPage(0)
          }}
          placeholder="Цена до"
        />
        <select
          value={sort}
          onChange={(event) => {
            setSort(event.target.value as ProductSort)
            setPage(0)
          }}
        >
          <option value="NAME">По названию</option>
          <option value="PRICE_ASC">Сначала дешевле</option>
          <option value="PRICE_DESC">Сначала дороже</option>
        </select>
      </form>
      {error && <p className="error">{error}</p>}
      {notice && <p className="notice">{notice}</p>}
      <section className="grid">
        {result?.content.map((product) => (
          <article key={product.id} className="card">
            <h2>
              <Link to={`/products/${product.id}`}>{product.name}</Link>
            </h2>
            <p className="meta">
              {product.brandName} · {product.categoryName}
            </p>
            <p>{product.shortDescription}</p>
            <p className="price">{formatPrice(product.price)}</p>
            <p className="stock">В наличии: {product.stockQuantity}</p>
            <button
              type="button"
              disabled={product.stockQuantity <= 0}
              onClick={() => {
                addToCart(product.id)
                  .then(() => setNotice(`${product.name} добавлен в корзину`))
                  .catch((reason: Error) => setError(reason.message))
              }}
            >
              В корзину
            </button>
          </article>
        ))}
      </section>
      {result && result.totalPages > 1 && (
        <nav className="pager">
          <button type="button" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>
            Назад
          </button>
          <span>
            {page + 1} / {result.totalPages}
          </span>
          <button
            type="button"
            disabled={page + 1 >= result.totalPages}
            onClick={() => setPage((value) => value + 1)}
          >
            Вперёд
          </button>
        </nav>
      )}
    </main>
  )
}
