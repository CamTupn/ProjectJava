export type OrderStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'CANCELLED'

export interface OrderItem {
  productId: number
  productName: string
  quantity: number
  unitPrice: number
}

export interface Order {
  id: number
  orderCode: string
  customerName: string
  customerPhone: string
  machineCode: string
  items: OrderItem[]
  totalAmount: number
  status: OrderStatus
  paymentMethod: string
  createdAt: string
  note?: string
}
