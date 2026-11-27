export type TenantStatus =
  | 'ACTIVE'
  | 'SUSPENDED'
  | 'INACTIVE'

export interface Tenant {
  id: number
  name: string
  code: string
  status: TenantStatus
  createdAt: string
  updatedAt: string
}

export interface CreateTenantRequest {
  name: string
  code: string
}

export interface UpdateTenantRequest {
  name: string
  code: string
}

export type TenantRequest = CreateTenantRequest
