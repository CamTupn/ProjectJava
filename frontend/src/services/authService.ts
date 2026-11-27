import apiClient, { type ApiResponse } from './apiClient'

export interface LoginRequest {
  usernameOrEmail: string
  password: string
}

export interface LoginResponse {
  userId: number
  username: string
  email: string
  fullName: string
  tenantCode: string | null
  roles: string[]
  accessToken: string
}

export const authService = {
  async login(
    request: LoginRequest
  ): Promise<LoginResponse> {
    const response = await apiClient.post<
      ApiResponse<LoginResponse>
    >('/auth/login', request)

    if (!response.data.success || !response.data.data) {
      throw new Error(
        response.data.message || 'Đăng nhập thất bại.'
      )
    }

    return response.data.data
  },
}
