import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ChevronDown,
  UserRound,
  LockKeyhole,
  LogOut,
} from 'lucide-react'

import { useAuth } from '../../auth/AuthContext'

const ROLE_LABELS = {
  PLATFORM_SUPER_ADMIN: 'Platform Super Admin',
  BRAND_ADMIN: 'Brand Admin',
  OPERATIONS_MANAGER: 'Operations Manager',
  TECHNICIAN: 'Technician',
  INVENTORY_STAFF: 'Inventory Staff',
}

export default function AccountMenu() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const [open, setOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return

    const handleOutsideClick = (event: MouseEvent) => {
      if (
        menuRef.current &&
        !menuRef.current.contains(event.target as Node)
      ) {
        setOpen(false)
      }
    }

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpen(false)
      }
    }

    document.addEventListener('mousedown', handleOutsideClick)
    document.addEventListener('keydown', handleEscape)

    return () => {
      document.removeEventListener('mousedown', handleOutsideClick)
      document.removeEventListener('keydown', handleEscape)
    }
  }, [open])

  if (!user) return null

  const initials =
    user.name
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((word) => word.charAt(0))
      .join('')
      .toUpperCase() || 'U'

  const roleLabel = ROLE_LABELS[user.role]

  const tenantName =
    user.tenantName ||
    user.tenantCode ||
    'ScentStation Platform'

  const handleNavigate = (path: string) => {
    setOpen(false)
    navigate(path)
  }

  const handleLogout = () => {
    setOpen(false)
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div ref={menuRef} className="relative">
      <button
        type="button"
        aria-label="Menu tài khoản"
        aria-expanded={open}
        onClick={() => setOpen(!open)}
        className="flex items-center gap-3 rounded-xl p-1.5 transition hover:bg-violet-50"
      >
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#EEF0FF] font-semibold text-[#4F5FE8]">
          {initials}
        </div>

        <div className="hidden max-w-44 text-left xl:block">
          <p className="truncate text-sm font-semibold text-[#202124]">
            {user.name}
          </p>

          <p className="truncate text-xs text-[#95877C]">
            {roleLabel}
          </p>
        </div>

        <ChevronDown
          size={16}
          className={`text-slate-400 transition-transform ${
            open ? 'rotate-180' : ''
          }`}
        />
      </button>

      {open && (
        <div className="absolute right-0 top-full z-50 mt-3 w-72 max-w-[calc(100vw-2rem)] overflow-hidden rounded-2xl border border-violet-100 bg-white shadow-xl">
          <div className="border-b border-slate-100 bg-[#FAF8FD] p-4">
            <p className="break-words font-semibold text-slate-900">
              {user.name}
            </p>

            <p className="mt-1 break-all text-xs text-slate-500">
              {user.email}
            </p>

            <p className="mt-2 text-xs font-semibold text-violet-600">
              {tenantName}
            </p>
          </div>

          <div className="space-y-1 p-2">
            <button
              type="button"
              onClick={() => handleNavigate('/profile')}
              className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-left hover:bg-violet-50"
            >
              <UserRound size={19} className="text-violet-600" />

              <div>
                <p className="text-sm font-medium text-slate-800">
                  Thông tin cá nhân
                </p>
                <p className="text-xs text-slate-400">
                  My Profile
                </p>
              </div>
            </button>

            <button
              type="button"
              onClick={() => handleNavigate('/change-password')}
              className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-left hover:bg-violet-50"
            >
              <LockKeyhole size={19} className="text-violet-600" />

              <div>
                <p className="text-sm font-medium text-slate-800">
                  Đổi mật khẩu
                </p>
                <p className="text-xs text-slate-400">
                  Change Password
                </p>
              </div>
            </button>
          </div>

          <div className="border-t border-slate-100 p-2">
            <button
              type="button"
              onClick={handleLogout}
              className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-left text-red-600 hover:bg-red-50"
            >
              <LogOut size={19} />

              <div>
                <p className="text-sm font-medium">
                  Đăng xuất
                </p>
                <p className="text-xs text-red-400">
                  Logout
                </p>
              </div>
            </button>
          </div>
        </div>
      )}
    </div>
  )
}