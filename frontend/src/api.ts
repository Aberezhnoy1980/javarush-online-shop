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

export type CartItem = {
  productId: number
  name: string
  price: number
  quantity: number
  stockQuantity: number
  lineTotal: number
}

export type Cart = {
  id: number
  items: CartItem[]
  totalQuantity: number
  totalAmount: number
}

export type OrderStatus =
  | 'NEW'
  | 'PAYMENT_PENDING'
  | 'PAID'
  | 'PAYMENT_FAILED'
  | 'PAYMENT_CANCELLED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED'

export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED'

export type OrderSummary = {
  id: number
  status: OrderStatus
  totalAmount: number
  createdAt: string
}

export type TopProduct = {
  productId: number
  productName: string
  unitsSold: number
  revenue: number
  averageRating: number | null
}

export type CategorySales = {
  categoryId: number
  categoryName: string
  orderCount: number
  unitsSold: number
  revenue: number
}

export type OrderStatusCount = {
  status: OrderStatus
  orderCount: number
}

export type SalesSummary = {
  categories: CategorySales[]
  orderStatuses: OrderStatusCount[]
}

export type OrderDetails = OrderSummary & {
  items: {
    productId: number
    productName: string
    priceAtTime: number
    quantity: number
    lineTotal: number
  }[]
  payment: {
    id: number
    amount: number
    status: PaymentStatus
    paymentMethod: string
    transactionId: string | null
  } | null
}

export function formatPrice(price: number): string {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    maximumFractionDigits: 0,
  }).format(price)
}

export async function readErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const body = (await response.json()) as { message?: string }
    return body.message ?? fallback
  } catch {
    return fallback
  }
}

export async function addToCart(productId: number, quantity = 1): Promise<Cart> {
  const response = await fetch('/api/cart/items', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ productId, quantity }),
  })
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Не удалось добавить в корзину'))
  }
  const cart = (await response.json()) as Cart
  notifyCartChanged()
  return cart
}

export function notifyCartChanged() {
  window.dispatchEvent(new Event('cart-updated'))
}
