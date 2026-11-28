import apiClient from './apiClient'

import type {
  User,
  UserStatus,
  CreateUserRequest,
  UpdateUserRequest,
  ResetPasswordRequest,
} from '../types/user'

export const userService = {
  // GET /api/v1/users
  async getAll(): Promise<User[]> {
    const response = await apiClient.get<User[]>(
      '/users'
    )

    return response.data
  },

  // GET /api/v1/users/{id}
  async getById(id: number): Promise<User> {
    const response = await apiClient.get<User>(
      `/users/${id}`
    )

    return response.data
  },

  // POST /api/v1/users
  async create(
    data: CreateUserRequest
  ): Promise<User> {
    const response = await apiClient.post<User>(
      '/users',
      data
    )

    return response.data
  },

  // PUT /api/v1/users/{id}
  async update(
    id: number,
    data: UpdateUserRequest
  ): Promise<User> {
    const response = await apiClient.put<User>(
      `/users/${id}`,
      data
    )

    return response.data
  },

  // PATCH /api/v1/users/{id}/status
  async updateStatus(
    id: number,
    status: UserStatus
  ): Promise<User> {
    const response = await apiClient.patch<User>(
      `/users/${id}/status`,
      null,
      {
        params: { status },
      }
    )

    return response.data
  },

  // POST /api/v1/users/{id}/reset-password
  async resetPassword(
    id: number,
    data: ResetPasswordRequest
  ): Promise<void> {
    await apiClient.post(
      `/users/${id}/reset-password`,
      data
    )
  },
}