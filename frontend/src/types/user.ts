import type { UserRole } from '../auth/AuthContext'

export type UserStatus =
  | 'ACTIVE'
  | 'LOCKED'
  | 'INACTIVE'

export interface User {
  id: number
  username: string
  email: string
  fullName: string
  status: UserStatus
  tenantId: number | null
  tenantCode: string | null
  roles: UserRole[]
  createdAt: string
  updatedAt: string
}

export interface CreateUserRequest {
  username: string
  email: string
  password: string
  fullName: string
  tenantId?: number | null
  roleCode: UserRole
}

export interface UpdateUserRequest {
  fullName: string
  email: string
}

export const USER_ROLE_LABELS: Record<UserRole, string> = {
  PLATFORM_SUPER_ADMIN: 'Super Admin',
  BRAND_ADMIN: 'Brand Admin',
  OPERATIONS_MANAGER: 'Operations Manager',
  TECHNICIAN: 'Technician',
  INVENTORY_STAFF: 'Inventory Staff',
}

export const USER_STATUS_LABELS: Record<UserStatus, string> = {
  ACTIVE: 'Đang hoạt động / Active',
  LOCKED: 'Đã khóa / Locked',
  INACTIVE: 'Ngừng hoạt động / Inactive',
}
