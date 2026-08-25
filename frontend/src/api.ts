export type ProductCatalogItem = {
  id: number
  name: string
  shortDescription: string
  price: number
  stockQuantity: number
  brandName: string | null
  categoryName: string | null
}

export type ProductDetails = {
  id: number
  name: string
  description: string | null
  price: number
  stockQuantity: number
  brandId: number | null
  brandName: string | null
  categoryId: number | null
  categoryName: string | null
}

export type Category = {
  id: number
  name: string
  parentId: number | null
}

export type Brand = {
  id: number
  name: string
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type ProductSort = 'NAME' | 'PRICE_ASC' | 'PRICE_DESC'

export function formatPrice(price: number): string {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    maximumFractionDigits: 0,
  }).format(price)
}
