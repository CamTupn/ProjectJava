import {
  NavLink,
  useNavigate,
} from 'react-router-dom'

import { useAuth } from '../../auth/AuthContext'

import {
  Wind,
  LayoutDashboard,
  Users,
  MapPin,
  Package,
  Cpu,
  Layers,
  ShoppingCart,
  ChartColumn,
  Settings,
  LogOut,
  Store,
  Sparkles,
} from 'lucide-react'

const menuItems = [
  {
    label: 'Tổng quan',
    path: '/tenant-admin/dashboard',
    icon: LayoutDashboard,
    end: true,
  },
  {
    label: 'Người dùng',
    path: '/tenant-admin/users',
    icon: Users,
  },
  {
    label: 'Địa điểm',
    path: '/tenant-admin/locations',
    icon: MapPin,
  },
  {
    label: 'Sản phẩm',
    path: '/tenant-admin/products',
    icon: Package,
  },
  {
    label: 'Máy nước hoa',
    path: '/tenant-admin/machines',
    icon: Cpu,
  },
  {
    label: 'Khe máy',
    path: '/tenant-admin/slots',
    icon: Layers,
  },
  {
    label: 'Đơn hàng',
    path: '/tenant-admin/orders',
    icon: ShoppingCart,
  },
  {
    label: 'Báo cáo & Thống kê',
    path: '/tenant-admin/reports',
    icon: ChartColumn,
  },
]

const navClass = (active: boolean) =>
  `group relative flex min-h-11 items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-medium transition-all duration-200 ${
    active
      ? 'bg-white/15 text-white shadow-sm ring-1 ring-white/10'
      : 'text-violet-100/70 hover:bg-white/10 hover:text-white'
  }`

export default function TenantAdminSidebar() {
  const navigate = useNavigate()
  const { user, logout } = useAuth()

  const tenantName =
    user?.tenantName ||
    user?.tenantCode ||
    'Thương hiệu'

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <aside className="flex h-full w-64 flex-col overflow-hidden bg-[#302344] text-white">

      {/* Platform Brand */}
      <div className="flex h-[88px] shrink-0 items-center gap-3 border-b border-white/10 px-5">

        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-fuchsia-400 to-violet-600 shadow-lg shadow-purple-950/30">
          <Wind size={24} />
        </div>

        <div className="min-w-0">
          <h1 className="truncate text-lg font-bold tracking-tight">
            ScentStation
          </h1>

          <p className="text-[11px] text-violet-200/60">
            Brand Management
          </p>
        </div>
      </div>

      {/* Dynamic Tenant Information */}
      <div className="shrink-0 px-4 pt-5">

        <div className="rounded-2xl border border-white/10 bg-white/[0.07] p-3.5">

          <div className="mb-3 flex items-center gap-2">
            <Store size={15} className="text-fuchsia-300" />

            <p className="text-[10px] font-semibold uppercase tracking-widest text-violet-200/60">
              Thương hiệu của bạn
            </p>
          </div>

          <p
            title={tenantName}
            className="truncate text-sm font-bold text-white"
          >
            {tenantName}
          </p>

          {user?.tenantCode && (
            <p className="mt-1 truncate text-[11px] text-violet-200/60">
              {user.tenantCode}
            </p>
          )}

          <div className="mt-3 flex items-center gap-2">
            <span className="h-2 w-2 rounded-full bg-emerald-400" />

            <span className="text-xs text-violet-200/80">
              Không gian quản lý
            </span>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <div className="min-h-0 flex-1 overflow-y-auto px-3 pb-5 pt-7">

        <div className="mb-3 flex items-center gap-2 px-3">
          <Sparkles size={13} className="text-fuchsia-300" />

          <p className="text-[10px] font-bold uppercase tracking-[0.2em] text-violet-200/50">
            Quản lý thương hiệu
          </p>
        </div>

        <nav className="space-y-1">
          {menuItems.map((item) => {
            const Icon = item.icon

            return (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.end}
                className={({ isActive }) =>
                  navClass(isActive)
                }
              >
                {({ isActive }) => (
                  <>
                    {isActive && (
                      <span className="absolute bottom-2 left-0 top-2 w-1 rounded-r-full bg-fuchsia-300" />
                    )}

                    <Icon
                      size={19}
                      className={
                        isActive
                          ? 'shrink-0 text-fuchsia-200'
                          : 'shrink-0 text-violet-200/60 group-hover:text-white'
                      }
                    />

                    <span className="flex-1">
                      {item.label}
                    </span>

                    {isActive && (
                      <span className="h-1.5 w-1.5 rounded-full bg-fuchsia-300" />
                    )}
                  </>
                )}
              </NavLink>
            )
          })}
        </nav>
      </div>

      {/* Bottom */}
      <div className="shrink-0 border-t border-white/10 px-3 py-4">

        <NavLink
          to="/tenant-admin/settings"
          className={({ isActive }) =>
            navClass(isActive)
          }
        >
          <Settings size={19} />
          <span>Cài đặt thương hiệu</span>
        </NavLink>

        <button
          type="button"
          onClick={handleLogout}
          className="mt-1 flex min-h-11 w-full items-center gap-3 rounded-xl px-3.5 py-2.5 text-left text-sm text-violet-200/60 transition-colors hover:bg-rose-500/10 hover:text-rose-300"
        >
          <LogOut size={19} />
          <span>Đăng xuất</span>
        </button>

        <div className="mt-4 border-t border-white/10 pt-4 text-center">
          <p className="text-[11px] text-violet-200/40">
            ScentStation © 2026
          </p>
        </div>
      </div>

    </aside>
  )
}