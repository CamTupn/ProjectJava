import apiClient from './apiClient'

import type {
  Tenant,
  CreateTenantRequest,
  UpdateTenantRequest,
} from '../types/tenant'

export const tenantService = {
  // GET /api/v1/tenants
  async getAll(): Promise<Tenant[]> {
    const response = await apiClient.get<Tenant[]>(
      '/tenants'
    )

    return response.data
  },

  // GET /api/v1/tenants/{id}
  async getById(id: number): Promise<Tenant> {
    const response = await apiClient.get<Tenant>(
      `/tenants/${id}`
    )

    return response.data
  },

  // POST /api/v1/tenants
  async create(
    data: CreateTenantRequest
  ): Promise<Tenant> {
    const response = await apiClient.post<Tenant>(
      '/tenants',
      data
    )

    return response.data
  },

  // PUT /api/v1/tenants/{id}
  async update(
    id: number,
    data: UpdateTenantRequest
  ): Promise<Tenant> {
    const response = await apiClient.put<Tenant>(
      `/tenants/${id}`,
      data
    )

    return response.data
  },
}
