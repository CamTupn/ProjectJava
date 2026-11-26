import { useState } from 'react'
import {
  NavLink,
  Outlet,
  useNavigate,
} from 'react-router-dom'
import {
  Activity,
  Boxes,
  ClipboardList,
  Cpu,
  LayoutDashboard,
  LogOut,
  Menu,
  Package,
  Settings2,
  Wrench,
  X,
  Wind,
} from 'lucide-react'

import {
  getRoleHome,
  useAuth,
  type UserRole,
} from '../auth/AuthContext'

const staffConfigs = {
  OPERATIONS_MANAGER: {
    title: 'Operations Manager',
    subtitle: 'Quản lý vận hành',
    links: [
      {
        label: 'Tổng quan',
        path: '/operations/dashboard',
        icon: LayoutDashboard,
      },
      {
        label: 'Theo dõi máy',
        path: '/operations/machines',
        icon: Cpu,
      },
      {
        label: 'Đơn hàng',
        path: '/operations/orders',
        icon: ClipboardList,
      },
    ],
  },
  TECHNICIAN: {
    title: 'Technician',
    subtitle: 'Kỹ thuật viên',
    links: [
      {
        label: 'Tổng quan',
        path: '/technician/dashboard',
        icon: LayoutDashboard,
      },
      {
        label: 'Thiết bị',
        path: '/technician/machines',
        icon: Cpu,
      },
      {
        label: 'Bảo trì',
        path: '/technician/maintenance',
        icon: Wrench,
      },
    ],
  },
  INVENTORY_STAFF: {
    title: 'Inventory Staff',
    subtitle: 'Nhân viên kho',
    links: [
      {
        label: 'Tổng quan',
        path: '/inventory/dashboard',
        icon: LayoutDashboard,
      },
      {
        label: 'Tồn kho',
        path: '/inventory/stock',
        icon: Boxes,
      },
      {
        label: 'Nạp hàng',
        path: '/inventory/refills',
        icon: Package,
      },
    ],
  },
}

type StaffRole = Extract<
  UserRole,
  'OPERATIONS_MANAGER' | 'TECHNICIAN' | 'INVENTORY_STAFF'
>

interface StaffLayoutProps {
  role: StaffRole
}

export default function StaffLayout({
  role,
}: StaffLayoutProps) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [mobileOpen, setMobileOpen] = useState(false)

  const config = staffConfigs[role]

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const sidebar = (
    <div className="flex h-full flex-col bg-[#211B38] text-white">
      <div className="flex h-20 items-center gap-3 border-b border-white/10 px-5">
        <div className="rounded-xl bg-violet-500 p-2.5">
          <Wind size={23} />
        </div>
        <div>
          <h1 className="font-bold">ScentStation</h1>
          <p className="text-xs text-violet-200/60">
            Staff Workspace
          </p>
        </div>
      </div>

      <div className="mx-4 mt-5 rounded-2xl border border-white/10 bg-white/5 p-4">
        <p className="text-xs text-violet-200/60">
          Vai trò
        </p>
        <p className="mt-1 font-semibold">
          {config.title}
        </p>
        <p className="mt-1 text-xs text-violet-200/70">
          {user?.name}
        </p>
      </div>

      <nav className="mt-7 flex-1 space-y-1 px-3">
        <p className="mb-3 px-3 text-xs font-semibold uppercase tracking-widest text-violet-200/50">
          Chức năng
        </p>

        {config.links.map((link) => {
          const Icon = link.icon

          return (
            <NavLink
              key={link.path}
              to={link.path}
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-xl px-4 py-3 text-sm transition ${
                  isActive
                    ? 'bg-violet-500/30 font-semibold text-white'
                    : 'text-violet-100/70 hover:bg-white/10 hover:text-white'
                }`
              }
            >
              <Icon size={19} />
              {link.label}
            </NavLink>
          )
        })}
      </nav>

      <div className="border-t border-white/10 p-4">
        <button
          onClick={handleLogout}
          className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm text-rose-200 hover:bg-rose-500/10"
        >
          <LogOut size={19} />
          Đăng xuất
        </button>
      </div>
    </div>
  )

  return (
    <div className="min-h-screen bg-[#F8F7FC]">
      <aside className="fixed inset-y-0 left-0 hidden w-64 lg:block">
        {sidebar}
      </aside>

      {mobileOpen && (
        <>
          <button
            type="button"
            aria-label="Đóng menu"
            onClick={() => setMobileOpen(false)}
            className="fixed inset-0 z-40 bg-black/50 lg:hidden"
          />
          <aside className="fixed inset-y-0 left-0 z-50 w-64 lg:hidden">
            {sidebar}
            <button
              type="button"
              aria-label="Đóng menu"
              onClick={() => setMobileOpen(false)}
              className="absolute right-3 top-5 rounded-lg p-2 text-white"
            >
              <X size={20} />
            </button>
          </aside>
        </>
      )}

      <div className="lg:ml-64">
        <header className="sticky top-0 z-30 flex h-20 items-center justify-between border-b border-slate-200 bg-white px-5 sm:px-8">
          <div className="flex items-center gap-3">
            <button
              type="button"
              aria-label="Mở menu"
              onClick={() => setMobileOpen(true)}
              className="rounded-xl border border-slate-200 p-2 lg:hidden"
            >
              <Menu size={22} />
            </button>

            <div>
              <h2 className="font-bold text-slate-900">
                {config.subtitle}
              </h2>
              <p className="text-xs text-slate-500">
                {config.title}
              </p>
            </div>
          </div>

          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-violet-100 font-bold text-violet-700">
            {user?.name.charAt(0).toUpperCase() ?? 'U'}
          </div>
        </header>

        <main className="mx-auto max-w-7xl p-5 sm:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export {
  Activity,
  Cpu,
  Wrench,
  Boxes,
  Package,
  Settings2,
  getRoleHome,
}
