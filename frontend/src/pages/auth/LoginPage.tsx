
import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import {
  ArrowRight,
  Eye,
  EyeOff,
  LockKeyhole,
  Mail,
  ShieldCheck,
  Sparkles,
  SprayCan,
} from 'lucide-react'

import {
  demoAccounts,
  getRoleHome,
  useAuth,
  type UserRole,
} from '../../auth/AuthContext'

type DemoRole =
  | 'PLATFORM_SUPER_ADMIN'
  | 'BRAND_ADMIN'
  | 'OPERATIONS_MANAGER'
  | 'TECHNICIAN'
  | 'INVENTORY_STAFF'

const demoRoleOptions: {
  role: DemoRole
  label: string
}[] = [
  {
    role: 'PLATFORM_SUPER_ADMIN',
    label: 'Super Admin',
  },
  {
    role: 'BRAND_ADMIN',
    label: 'Brand Admin',
  },
  {
    role: 'OPERATIONS_MANAGER',
    label: 'Operations Manager',
  },
  {
    role: 'TECHNICIAN',
    label: 'Technician',
  },
  {
    role: 'INVENTORY_STAFF',
    label: 'Inventory Staff',
  },
]

export default function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  // Người đã đăng nhập sẽ được chuyển về Dashboard theo role.
  if (user) {
    return <Navigate to={getRoleHome(user.role)} replace />
  }

  // Xử lý đăng nhập.
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setLoading(true)

    const normalizedEmail = email.trim().toLowerCase()

    const success = login(normalizedEmail, password)

    setLoading(false)

    if (!success) {
      setError('Email hoặc mật khẩu không chính xác.')
      return
    }

    // Xác định role từ tài khoản demo đã đăng nhập.
    const account = demoAccounts.find(
      (item) =>
        item.email.toLowerCase() === normalizedEmail
    )

    if (!account) {
      setError('Không xác định được vai trò tài khoản.')
      return
    }

    navigate(getRoleHome(account.role as UserRole), {
      replace: true,
    })
  }

  // Tự động điền tài khoản demo khi chọn vai trò.
  const fillDemo = (role: DemoRole) => {
    const account = demoAccounts.find(
      (item) => item.role === role
    )

    if (!account) return

    setEmail(account.email)
    setPassword(account.password)
    setError('')
  }

  return (
    <div className="flex min-h-screen bg-[#F7F6FC]">
      {/* LEFT PANEL - BRANDING */}
      <div className="relative hidden w-[48%] flex-col justify-between overflow-hidden bg-[#241B3B] p-12 text-white lg:flex">
        {/* Background decorations */}
        <div className="pointer-events-none absolute -right-24 top-20 h-80 w-80 rounded-full bg-fuchsia-500/20 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-24 -left-20 h-96 w-96 rounded-full bg-indigo-500/20 blur-3xl" />

        {/* Logo */}
        <div className="relative z-10 flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/15">
            <SprayCan size={25} />
          </div>

          <div>
            <h1 className="text-xl font-bold tracking-tight">
              ScentStation
            </h1>

            <p className="text-xs text-violet-200/70">
              Smart Fragrance Platform
            </p>
          </div>
        </div>

        {/* Introduction */}
        <div className="relative z-10 max-w-lg">
          <div className="mb-7 inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/10 px-4 py-2 text-sm text-violet-100">
            <Sparkles size={16} />
            Fragrance Management System
          </div>

          <h2 className="text-4xl font-bold leading-tight tracking-tight xl:text-5xl">
            Quản lý hệ thống
            <span className="mt-3 block text-fuchsia-300">
              thông minh hơn.
            </span>
          </h2>

          <p className="mt-7 max-w-md text-base leading-8 text-violet-100/70">
            Nền tảng quản lý thương hiệu, máy phân phối nước hoa,
            sản phẩm, tồn kho và giao dịch trong một không gian
            thống nhất.
          </p>

          <div className="mt-10 flex items-center gap-3 text-sm text-violet-100/80">
            <ShieldCheck
              size={20}
              className="text-fuchsia-300"
            />
            Truy cập theo vai trò quản trị và nhân viên
          </div>

          {/* Role list */}
          <div className="mt-8 flex flex-wrap gap-2">
            {[
              'Super Admin',
              'Brand Admin',
              'Operations',
              'Technician',
              'Inventory',
            ].map((role) => (
              <span
                key={role}
                className="rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-xs text-violet-100/80"
              >
                {role}
              </span>
            ))}
          </div>
        </div>

        {/* Footer */}
        <p className="relative z-10 text-sm text-violet-200/50">
          © 2026 ScentStation. All rights reserved.
        </p>
      </div>

      {/* RIGHT PANEL - LOGIN FORM */}
      <div className="flex flex-1 items-center justify-center px-5 py-12 sm:px-10">
        <div className="w-full max-w-[440px]">
          {/* Mobile logo */}
          <div className="mb-9 flex items-center gap-3 lg:hidden">
            <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-[#302344] text-white">
              <SprayCan size={23} />
            </div>

            <div>
              <h1 className="text-xl font-bold text-[#302344]">
                ScentStation
              </h1>

              <p className="text-xs text-slate-500">
                Management Platform
              </p>
            </div>
          </div>

          {/* Heading */}
          <div className="mb-8">
            <p className="mb-3 text-sm font-semibold uppercase tracking-[0.2em] text-violet-600">
              Welcome back
            </p>

            <h2 className="text-3xl font-bold tracking-tight text-slate-900">
              Đăng nhập
            </h2>

            <p className="mt-3 text-sm leading-6 text-slate-500">
              Đăng nhập vào tài khoản để tiếp tục sử dụng
              hệ thống quản lý ScentStation.
            </p>
          </div>

          {/* LOGIN FORM */}
          <form
            onSubmit={handleSubmit}
            className="space-y-5"
          >
            {/* Email */}
            <div>
              <label
                htmlFor="login-email"
                className="mb-2 block text-sm font-semibold text-slate-700"
              >
                Địa chỉ email
              </label>

              <div className="relative">
                <Mail
                  size={19}
                  className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
                />

                <input
                  id="login-email"
                  type="email"
                  required
                  autoComplete="username"
                  value={email}
                  onChange={(event) => {
                    setEmail(event.target.value)
                    setError('')
                  }}
                  placeholder="Nhập địa chỉ email"
                  className="h-13 w-full rounded-xl border border-slate-200 bg-white pl-12 pr-4 text-sm text-slate-900 outline-none transition focus:border-violet-500 focus:ring-4 focus:ring-violet-100"
                />
              </div>
            </div>

            {/* Password */}
            <div>
              <label
                htmlFor="login-password"
                className="mb-2 block text-sm font-semibold text-slate-700"
              >
                Mật khẩu
              </label>

              <div className="relative">
                <LockKeyhole
                  size={19}
                  className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
                />

                <input
                  id="login-password"
                  type={showPassword ? 'text' : 'password'}
                  required
                  autoComplete="current-password"
                  value={password}
                  onChange={(event) => {
                    setPassword(event.target.value)
                    setError('')
                  }}
                  placeholder="Nhập mật khẩu"
                  className="h-13 w-full rounded-xl border border-slate-200 bg-white pl-12 pr-12 text-sm text-slate-900 outline-none transition focus:border-violet-500 focus:ring-4 focus:ring-violet-100"
                />

                <button
                  type="button"
                  onClick={() =>
                    setShowPassword((current) => !current)
                  }
                  aria-label={
                    showPassword
                      ? 'Ẩn mật khẩu'
                      : 'Hiện mật khẩu'
                  }
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 transition hover:text-violet-600"
                >
                  {showPassword ? (
                    <EyeOff size={19} />
                  ) : (
                    <Eye size={19} />
                  )}
                </button>
              </div>
            </div>

            {/* Error message */}
            {error && (
              <div
                role="alert"
                className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600"
              >
                {error}
              </div>
            )}

            {/* Submit button */}
            <button
              type="submit"
              disabled={loading}
              className="flex h-13 w-full items-center justify-center gap-2 rounded-xl bg-[#6D45CE] font-semibold text-white shadow-lg shadow-violet-200 transition hover:bg-[#5934B4] disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loading
                ? 'Đang đăng nhập...'
                : 'Đăng nhập'}

              {!loading && <ArrowRight size={19} />}
            </button>
          </form>

          {/* DEMO ACCOUNTS */}
          <div className="mt-8 rounded-2xl border border-violet-100 bg-white p-5">
            <div className="mb-3 flex items-center gap-2 text-sm font-semibold text-slate-800">
              <ShieldCheck
                size={18}
                className="text-violet-600"
              />
              Tài khoản thử nghiệm
            </div>

            <p className="mb-4 text-xs leading-5 text-slate-500">
              Chỉ sử dụng trong giai đoạn phát triển giao diện.
              Chọn vai trò để tự động điền email và mật khẩu.
            </p>

            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              {demoRoleOptions.map((option) => (
                <button
                  key={option.role}
                  type="button"
                  onClick={() => fillDemo(option.role)}
                  className="rounded-xl border border-slate-200 px-3 py-3 text-sm font-medium text-slate-700 transition hover:border-violet-400 hover:bg-violet-50"
                >
                  {option.label}
                </button>
              ))}
            </div>
          </div>

          {/* KIOSK LINK */}
          <div className="mt-7 text-center">
            <p className="text-sm text-slate-500">
              Bạn muốn trải nghiệm máy nước hoa?
            </p>

            <Link
              to="/kiosk"
              className="mt-2 inline-flex items-center gap-2 text-sm font-semibold text-violet-700 transition hover:text-violet-900"
            >
              Truy cập Kiosk
              <ArrowRight size={16} />
            </Link>
          </div>

          {/* Bottom note */}
          <p className="mt-8 text-center text-xs leading-5 text-slate-400">
            ScentStation Management Platform
            <span className="mx-2">•</span>
            Frontend Demo
          </p>
        </div>
      </div>
    </div>
  )
}
