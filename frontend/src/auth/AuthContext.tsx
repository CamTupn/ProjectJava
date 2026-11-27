import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react'

import { authService } from '../services/authService'

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
  tenantCode: string | null
}

interface AuthSession {
  user: AuthUser
  accessToken: string
}

interface AuthContextType {
  user: AuthUser | null
  isAuthenticated: boolean
  login: (
    usernameOrEmail: string,
    password: string
  ) => Promise<AuthUser>
  logout: () => void
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
      !ROLE_PRIORITY.includes(session.user.role)
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

    const roles = ROLE_PRIORITY.filter((role) =>
      response.roles.includes(role)
    )

    const primaryRole = roles[0]

    if (!primaryRole) {
      throw new Error(
        'Tài khoản không có vai trò hợp lệ.'
      )
    }

    const expiration = getTokenExpiration(
      response.accessToken
    )

    if (!expiration || expiration <= Date.now()) {
      throw new Error(
        'Phiên đăng nhập không hợp lệ.'
      )
    }

    const authUser: AuthUser = {
      id: String(response.userId),
      name: response.fullName || response.username,
      username: response.username,
      email: response.email,
      role: primaryRole,
      roles,
      tenantCode: response.tenantCode,
    }

    const authSession: AuthSession = {
      user: authUser,
      accessToken: response.accessToken,
    }

    localStorage.setItem(
      AUTH_STORAGE_KEY,
      JSON.stringify(authSession)
    )

    setSession(authSession)

    return authUser
  }

  const logout = () => {
    localStorage.removeItem(AUTH_STORAGE_KEY)
    localStorage.removeItem(
      'scentstation_demo_auth'
    )

    setSession(null)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: user !== null,
        login,
        logout,
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
