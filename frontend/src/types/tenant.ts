export type TenantStatus = 'ACTIVE' | 'SUSPENDED' | 'INACTIVE'

export interface Tenant {
  id: number
  name: string
  code: string
  status: TenantStatus
  createdAt: string
  updatedAt: string
}

export interface TenantRequest {
  name: string
  code: string
  status: TenantStatus
}