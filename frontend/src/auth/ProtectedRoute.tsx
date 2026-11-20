import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth, type UserRole } from './AuthContext'

interface ProtectedRouteProps {
  allowedRoles: UserRole[]
}

export default function ProtectedRoute({
  allowedRoles,
}: ProtectedRouteProps) {
  const { user } = useAuth()
  const location = useLocation()

  if (!user) {
    return (
      <Navigate
        to="/login"
        state={{ from: location }}
        replace
      />
    )
  }

  if (!allowedRoles.includes(user.role)) {
    return (
      <Navigate
        to={
          user.role === 'SUPER_ADMIN'
            ? '/admin'
            : '/tenant-admin/dashboard'
        }
        replace
      />
    )
  }

  return <Outlet />
}
