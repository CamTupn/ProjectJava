import { NavLink } from 'react-router-dom'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import {
  Wind,
  LayoutDashboard,
  Building2,
  Users,
  MapPin,
  Package,
  Cpu,
  Layers,
  ShoppingCart,
  Settings,
  LogOut,
  ShieldCheck,
  Sparkles,
} from 'lucide-react'

const menuItems = [
  { label: 'Tổng quan', path: '/admin', icon: LayoutDashboard, end: true },
  { label: 'Thương hiệu', path: '/admin/tenants', icon: Building2 },
  { label: 'Người dùng', path: '/admin/users', icon: Users },
  { label: 'Địa điểm', path: '/admin/locations', icon: MapPin },
  { label: 'Sản phẩm', path: '/admin/products', icon: Package },
  { label: 'Máy nước hoa', path: '/admin/machines', icon: Cpu },
  { label: 'Khe máy', path: '/admin/slots', icon: Layers },
  { label: 'Đơn hàng', path: '/admin/orders', icon: ShoppingCart },
]

const navClass = (active: boolean) =>
  `group relative flex min-h-11 items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-medium transition-all duration-200 ${
    active
      ? 'bg-white/15 text-white shadow-sm ring-1 ring-white/10'
      : 'text-slate-300 hover:bg-white/10 hover:text-white'
  }`

export default function Sidebar() {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }
  return (
    <aside className="flex h-full w-64 flex-col overflow-hidden bg-[#171D36] text-white">

      {/* Brand */}
      <div className="flex h-[88px] shrink-0 items-center gap-3 border-b border-white/10 px-5">

        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-400 to-violet-600 shadow-lg shadow-violet-900/30">
          <Wind size={24} />
        </div>

        <div className="min-w-0">
          <h1 className="truncate text-lg font-bold tracking-tight">
            ScentStation
          </h1>
          <p className="text-[11px] text-slate-400">
            Management Platform
          </p>
        </div>
      </div>

      {/* Role Card */}
      <div className="px-4 pt-5">
        <div className="flex items-center gap-3 rounded-2xl border border-white/10 bg-white/[0.06] p-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-400/20 text-indigo-200">
            <ShieldCheck size={21} />
          </div>

          <div className="min-w-0">
            <p className="text-xs text-slate-400">
              Vai trò quản trị
            </p>
            <p className="truncate text-sm font-semibold">
              Super Admin
            </p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <div className="min-h-0 flex-1 overflow-y-auto px-3 pb-5 pt-7">

        <div className="mb-3 flex items-center gap-2 px-3">
          <Sparkles size={13} className="text-indigo-300" />
          <p className="text-[10px] font-bold uppercase tracking-[0.2em] text-slate-500">
            Quản lý hệ thống
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
                className={({ isActive }) => navClass(isActive)}
              >
                {({ isActive }) => (
                  <>
                    {isActive && (
                      <span className="absolute bottom-2 left-0 top-2 w-1 rounded-r-full bg-indigo-300" />
                    )}

                    <Icon
                      size={19}
                      className={
                        isActive
                          ? 'shrink-0 text-indigo-200'
                          : 'shrink-0 text-slate-400 group-hover:text-white'
                      }
                    />

                    <span className="flex-1">{item.label}</span>

                    {isActive && (
                      <span className="h-1.5 w-1.5 rounded-full bg-indigo-300" />
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
          to="/admin/settings"
          className={({ isActive }) => navClass(isActive)}
        >
          <Settings size={19} />
          <span>Cài đặt</span>
        </NavLink>

        <button
          type="button"
          onClick={handleLogout}
          className="mt-1 flex min-h-11 w-full items-center gap-3 rounded-xl px-3.5 py-2.5 text-left text-sm text-slate-400 transition-colors hover:bg-rose-500/10 hover:text-rose-300"
        >
          <LogOut size={19} />
          <span>Đăng xuất</span>
        </button>

        <div className="mt-4 border-t border-white/10 pt-4 text-center">
          <p className="text-[11px] text-slate-500">
            ScentStation © 2026
          </p>
        </div>
      </div>

    </aside>
  )
}
