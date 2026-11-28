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
  tenantId: number | null
  tenantCode: string | null
  tenantName: string | null
  roles: string[]
  mustChangePassword: boolean
  accessToken: string
}

export interface ProfileResponse {
  userId: number
  username: string
  email: string
  fullName: string
  tenantId: number | null
  tenantCode: string | null
  tenantName: string | null
  roles: string[]
  status: string
  mustChangePassword: boolean
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
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

  async getProfile(): Promise<ProfileResponse> {
    const response = await apiClient.get<
      ApiResponse<ProfileResponse>
    >('/auth/me')

    if (!response.data.success || !response.data.data) {
      throw new Error(
        response.data.message ||
          'Không thể tải thông tin tài khoản.'
      )
    }

    return response.data.data
  },

  async changePassword(
    request: ChangePasswordRequest
  ): Promise<void> {
    const response = await apiClient.post<
      ApiResponse<null>
    >('/auth/change-password', request)

    if (!response.data.success) {
      throw new Error(
        response.data.message ||
          'Đổi mật khẩu thất bại.'
      )
    }
  },
}