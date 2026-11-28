import {
  Navigate,
  Outlet,
  useLocation,
} from 'react-router-dom'

import {
  getRoleHome,
  useAuth,
  type UserRole,
} from './AuthContext'

interface ProtectedRouteProps {
  allowedRoles: UserRole[]
}

export default function ProtectedRoute({
  allowedRoles,
}: ProtectedRouteProps) {
  const { user } = useAuth()
  const location = useLocation()

  // 1. Authentication
  if (!user) {
    return (
      <Navigate
        to="/login"
        state={{ from: location }}
        replace
      />
    )
  }

  // 2. Mandatory password change
  if (user.mustChangePassword) {
    return (
      <Navigate
        to="/change-password"
        replace
      />
    )
  }

  // 3. Role-based authorization
  if (!allowedRoles.includes(user.role)) {
    return (
      <Navigate
        to={getRoleHome(user.role)}
        replace
      />
    )
  }

  // 4. Authorized access
  return <Outlet />
}