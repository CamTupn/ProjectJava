import axios from 'axios'

export const AUTH_STORAGE_KEY = 'scentstation_auth'

export const AUTH_EXPIRED_EVENT = 'scentstation:auth-expired'

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  errors: Record<string, string>
  timestamp: string
}

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',

  timeout: 15000,

  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
})

// Automatically attach JWT to protected requests.
apiClient.interceptors.request.use((config) => {
  try {
    const stored = localStorage.getItem(AUTH_STORAGE_KEY)

    if (stored && config.url !== '/auth/login') {
      const session = JSON.parse(stored)

      if (session.accessToken) {
        config.headers.Authorization =
          `Bearer ${session.accessToken}`
      }
    }
  } catch {
    localStorage.removeItem(AUTH_STORAGE_KEY)
  }

  return config
})

// Handle expired or invalid authentication.
apiClient.interceptors.response.use(
  (response) => response,

  (error) => {
    if (
      error.response?.status === 401 &&
      error.config?.url !== '/auth/login'
    ) {
      localStorage.removeItem(AUTH_STORAGE_KEY)

      window.dispatchEvent(
        new Event(AUTH_EXPIRED_EVENT)
      )
    }

    return Promise.reject(error)
  }
)

export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return 'Không thể kết nối đến máy chủ.'
    }

    const data = error.response.data

    if (data?.message) {
      return data.message
    }

    switch (error.response.status) {
      case 400:
        return 'Dữ liệu không hợp lệ.'
      case 401:
        return 'Thông tin xác thực không hợp lệ.'
      case 403:
        return 'Bạn không có quyền thực hiện thao tác này.'
      case 404:
        return 'Không tìm thấy dữ liệu.'
      case 409:
        return 'Dữ liệu bị trùng hoặc xảy ra xung đột.'
      default:
        return 'Đã xảy ra lỗi từ máy chủ.'
    }
  }

  if (error instanceof Error) {
    return error.message
  }

  return 'Đã xảy ra lỗi không xác định.'
}

export default apiClient
