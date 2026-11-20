import {
  createContext,
  useContext,
  useState,
  type ReactNode,
} from 'react'

export type UserRole = 'SUPER_ADMIN' | 'TENANT_ADMIN'

export interface AuthUser {
  id: string
  name: string
  email: string
  role: UserRole
}

interface AuthContextType {
  user: AuthUser | null
  isAuthenticated: boolean
  login: (email: string, password: string) => boolean
  logout: () => void
}

const STORAGE_KEY = 'scentstation_demo_auth'

const demoAccounts = [
  {
    id: '1',
    name: 'Super Administrator',
    email: 'admin@scentstation.com',
    password: 'Admin@123',
    role: 'SUPER_ADMIN' as UserRole,
  },
  {
    id: '2',
    name: 'Tenant Administrator',
    email: 'tenant@scentstation.com',
    password: 'Tenant@123',
    role: 'TENANT_ADMIN' as UserRole,
  },
]

const AuthContext = createContext<AuthContextType | undefined>(
  undefined
)

function getStoredUser(): AuthUser | null {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)

    if (!stored) return null

    const parsed: unknown = JSON.parse(stored)

    if (
      typeof parsed !== 'object' ||
      parsed === null ||
      !('role' in parsed) ||
      !('email' in parsed) ||
      !('id' in parsed) ||
      !('name' in parsed)
    ) {
      return null
    }

    const data = parsed as Record<string, unknown>

    if (
      typeof data.id !== 'string' ||
      typeof data.name !== 'string' ||
      typeof data.email !== 'string' ||
      (data.role !== 'SUPER_ADMIN' &&
        data.role !== 'TENANT_ADMIN')
    ) {
      return null
    }

    return {
      id: data.id,
      name: data.name,
      email: data.email,
      role: data.role,
    }
  } catch {
    return null
  }
}

export function AuthProvider({
  children,
}: {
  children: ReactNode
}) {
  const [user, setUser] = useState<AuthUser | null>(
    getStoredUser
  )

  const login = (email: string, password: string) => {
    const account = demoAccounts.find(
      (item) =>
        item.email.toLowerCase() ===
          email.trim().toLowerCase() &&
        item.password === password
    )

    if (!account) return false

    const authUser: AuthUser = {
      id: account.id,
      name: account.name,
      email: account.email,
      role: account.role,
    }

    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify(authUser)
    )

    setUser(authUser)

    return true
  }

  const logout = () => {
    localStorage.removeItem(STORAGE_KEY)
    setUser(null)
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
