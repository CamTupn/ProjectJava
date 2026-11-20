export type ProductStatus = 'ACTIVE' | 'INACTIVE'

export interface Product {
  id: number
  tenantId: number
  tenantName: string
  sku: string
  name: string
  description: string
  fragranceNotes: string
  imageUrl: string
  price: number
  volumePerSprayMl: number
  status: ProductStatus
  createdAt: string
  updatedAt: string
}

export interface ProductRequest {
  tenantId: number
  sku: string
  name: string
  description: string
  fragranceNotes: string
  imageUrl: string
  price: number
  volumePerSprayMl: number
  status: ProductStatus
}