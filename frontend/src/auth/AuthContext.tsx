import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react'

import {
  authService,
  type LoginResponse,
  type ProfileResponse,
  type ChangePasswordRequest,
} from '../services/authService'

import {
  AUTH_STORAGE_KEY,
  AUTH_EXPIRED_EVENT,
} from '../services/apiClient'

export type UserRole =
  | 'PLATFORM_SUPER_ADMIN'
  | 'BRAND_ADMIN'
  | 'OPERATIONS_MANAGER'
  | 'TECHNICIAN'
  | 'INVENTORY_STAFF'

export interface AuthUser {
  id: string
  name: string
  username: string
  email: string
  role: UserRole
  roles: UserRole[]
  tenantId: number | null
  tenantCode: string | null
  tenantName: string | null
  mustChangePassword: boolean
}

interface AuthSession {
  user: AuthUser
  accessToken: string
}

interface AuthContextType {
  user: AuthUser | null
  isAuthenticated: boolean
  mustChangePassword: boolean

  login: (
    usernameOrEmail: string,
    password: string
  ) => Promise<AuthUser>

  logout: () => void

  refreshProfile: () => Promise<AuthUser>

  changePassword: (
    request: ChangePasswordRequest
  ) => Promise<void>
}

const ROLE_PRIORITY: UserRole[] = [
  'PLATFORM_SUPER_ADMIN',
  'BRAND_ADMIN',
  'OPERATIONS_MANAGER',
  'TECHNICIAN',
  'INVENTORY_STAFF',
]

export function getRoleHome(role: UserRole): string {
  switch (role) {
    case 'PLATFORM_SUPER_ADMIN':
      return '/admin'

    case 'BRAND_ADMIN':
      return '/tenant-admin/dashboard'

    case 'OPERATIONS_MANAGER':
      return '/operations/dashboard'

    case 'TECHNICIAN':
      return '/technician/dashboard'

    case 'INVENTORY_STAFF':
      return '/inventory/dashboard'
  }
}

export function getAuthHome(user: AuthUser): string {
  if (user.mustChangePassword) {
    return '/change-password'
  }

  return getRoleHome(user.role)
}

function getTokenExpiration(
  token: string
): number | null {
  try {
    const payload = token.split('.')[1]

    const normalized = payload
      .replace(/-/g, '+')
      .replace(/_/g, '/')

    const decoded = JSON.parse(atob(normalized))

    return typeof decoded.exp === 'number'
      ? decoded.exp * 1000
      : null
  } catch {
    return null
  }
}

function mapAuthUser(
  response: LoginResponse | ProfileResponse
): AuthUser {
  const roles = ROLE_PRIORITY.filter((role) =>
    response.roles.includes(role)
  )

  const primaryRole = roles[0]

  if (!primaryRole) {
    throw new Error(
      'Tài khoản không có vai trò hợp lệ.'
    )
  }

  return {
    id: String(response.userId),
    name: response.fullName || response.username,
    username: response.username,
    email: response.email,
    role: primaryRole,
    roles,
    tenantId: response.tenantId,
    tenantCode: response.tenantCode,
    tenantName: response.tenantName,
    mustChangePassword: response.mustChangePassword,
  }
}

function getStoredSession(): AuthSession | null {
  try {
    const stored = localStorage.getItem(
      AUTH_STORAGE_KEY
    )

    if (!stored) return null

    const session: AuthSession = JSON.parse(stored)

    if (
      !session.user ||
      !session.accessToken ||
      !ROLE_PRIORITY.includes(session.user.role) ||
      typeof session.user.mustChangePassword !== 'boolean' ||
      !('tenantId' in session.user) ||
      !('tenantName' in session.user)
    ) {
      localStorage.removeItem(AUTH_STORAGE_KEY)
      return null
    }

    const expiration = getTokenExpiration(
      session.accessToken
    )

    if (!expiration || expiration <= Date.now()) {
      localStorage.removeItem(AUTH_STORAGE_KEY)
      return null
    }

    return session
  } catch {
    localStorage.removeItem(AUTH_STORAGE_KEY)
    return null
  }
}

const AuthContext = createContext<
  AuthContextType | undefined
>(undefined)

export function AuthProvider({
  children,
}: {
  children: ReactNode
}) {
  const [session, setSession] =
    useState<AuthSession | null>(getStoredSession)

  const user = session?.user ?? null

  const saveSession = (newSession: AuthSession) => {
    localStorage.setItem(
      AUTH_STORAGE_KEY,
      JSON.stringify(newSession)
    )

    setSession(newSession)
  }

  const logout = () => {
    localStorage.removeItem(AUTH_STORAGE_KEY)
    localStorage.removeItem('scentstation_demo_auth')

    setSession(null)
  }

  useEffect(() => {
    localStorage.removeItem(
      'scentstation_demo_auth'
    )

    const handleAuthExpired = () => {
      localStorage.removeItem(AUTH_STORAGE_KEY)
      setSession(null)
    }

    const handleStorage = (event: StorageEvent) => {
      if (
        event.key === AUTH_STORAGE_KEY ||
        event.key === null
      ) {
        setSession(getStoredSession())
      }
    }

    window.addEventListener(
      AUTH_EXPIRED_EVENT,
      handleAuthExpired
    )

    window.addEventListener(
      'storage',
      handleStorage
    )

    return () => {
      window.removeEventListener(
        AUTH_EXPIRED_EVENT,
        handleAuthExpired
      )

      window.removeEventListener(
        'storage',
        handleStorage
      )
    }
  }, [])

  useEffect(() => {
    if (!session) return

    const checkExpiration = () => {
      const expiration = getTokenExpiration(
        session.accessToken
      )

      if (
        !expiration ||
        expiration <= Date.now()
      ) {
        localStorage.removeItem(AUTH_STORAGE_KEY)
        setSession(null)
      }
    }

    checkExpiration()

    const interval = window.setInterval(
      checkExpiration,
      1000
    )

    return () => window.clearInterval(interval)
  }, [session])

  const login = async (
    usernameOrEmail: string,
    password: string
  ): Promise<AuthUser> => {
    const response = await authService.login({
      usernameOrEmail: usernameOrEmail.trim(),
      password,
    })

    const expiration = getTokenExpiration(
      response.accessToken
    )

    if (!expiration || expiration <= Date.now()) {
      throw new Error(
        'Phiên đăng nhập không hợp lệ.'
      )
    }

    const authUser = mapAuthUser(response)

    saveSession({
      user: authUser,
      accessToken: response.accessToken,
    })

    return authUser
  }

  const refreshProfile = async (): Promise<AuthUser> => {
    if (!session) {
      throw new Error('Bạn chưa đăng nhập.')
    }

    const response = await authService.getProfile()

    const currentSession = getStoredSession()

    if (
      !currentSession ||
      currentSession.accessToken !== session.accessToken
    ) {
      throw new Error('Phiên đăng nhập đã thay đổi.')
    }

    const updatedUser = mapAuthUser(response)

    saveSession({
      user: updatedUser,
      accessToken: currentSession.accessToken,
    })

    return updatedUser
  }

  const changePassword = async (
    request: ChangePasswordRequest
  ): Promise<void> => {
    await authService.changePassword(request)

    // Backend revokes previously issued JWTs.
    // User must log in again with the new password.
    logout()
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: user !== null,
        mustChangePassword:
          user?.mustChangePassword ?? false,
        login,
        logout,
        refreshProfile,
        changePassword,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error(
      'useAuth must be used inside AuthProvider'
    )
  }

  return context
}