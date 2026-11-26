import {
  createContext,
  useContext,
  useState,
  type ReactNode,
} from 'react'

export type UserRole =
  | 'PLATFORM_SUPER_ADMIN'
  | 'BRAND_ADMIN'
  | 'OPERATIONS_MANAGER'
  | 'TECHNICIAN'
  | 'INVENTORY_STAFF'

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

interface DemoAccount extends AuthUser {
  password: string
}

export const demoAccounts: DemoAccount[] = [
  {
    id: '1',
    name: 'Platform Administrator',
    email: 'platform@scentstation.com',
    password: 'Admin@123',
    role: 'PLATFORM_SUPER_ADMIN',
  },
  {
    id: '2',
    name: 'Brand Administrator',
    email: 'admin@scentstation.com',
    password: 'Brand@123',
    role: 'BRAND_ADMIN',
  },
  {
    id: '3',
    name: 'Operations Manager',
    email: 'operations@scentstation.com',
    password: 'Operations@123',
    role: 'OPERATIONS_MANAGER',
  },
  {
    id: '4',
    name: 'Technician',
    email: 'tech@scentstation.com',
    password: 'Tech@123',
    role: 'TECHNICIAN',
  },
  {
    id: '5',
    name: 'Inventory Staff',
    email: 'stock@scentstation.com',
    password: 'Inventory@123',
    role: 'INVENTORY_STAFF',
  },
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

function getStoredUser(): AuthUser | null {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (!stored) return null

    const data: unknown = JSON.parse(stored)

    if (!data || typeof data !== 'object') return null

    const record = data as Record<string, unknown>

    const account = demoAccounts.find(
      (item) =>
        item.id === record.id &&
        item.email === record.email &&
        item.role === record.role
    )

    if (!account) return null

    return {
      id: account.id,
      name: account.name,
      email: account.email,
      role: account.role,
    }
  } catch {
    return null
  }
}

const AuthContext = createContext<AuthContextType | undefined>(
  undefined
)

export function AuthProvider({
  children,
}: {
  children: ReactNode
}) {
  const [user, setUser] = useState<AuthUser | null>(
    getStoredUser
  )

  const login = (email: string, password: string): boolean => {
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
