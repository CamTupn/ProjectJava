import { useState, type FormEvent } from 'react'

import {
  Link,
  Navigate,
  useNavigate,
  useLocation,
} from 'react-router-dom'

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
  getAuthHome,
  useAuth,
} from '../../auth/AuthContext'

import {
  getApiErrorMessage,
} from '../../services/apiClient'

export default function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const passwordChanged =
    location.state?.passwordChanged === true

  const [usernameOrEmail, setUsernameOrEmail] =
    useState('')

  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] =
    useState(false)

  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (user) {
    return (
      <Navigate
        to={getAuthHome(user)}
        replace
      />
    )
  }

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault()

    if (loading) return

    setError('')

    if (!usernameOrEmail.trim() || !password) {
      setError(
        'Vui lòng nhập đầy đủ thông tin đăng nhập.'
      )
      return
    }

    setLoading(true)

    try {
      const authenticatedUser = await login(
        usernameOrEmail,
        password
      )

      navigate(
        getAuthHome(authenticatedUser),
        { replace: true }
      )
    } catch (error) {
      setError(getApiErrorMessage(error))
    } finally {
      setLoading(false)
    }
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
            Nền tảng quản lý thương hiệu, máy phân phối
            nước hoa, sản phẩm, tồn kho và giao dịch
            trong một không gian thống nhất.
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
              Đăng nhập vào tài khoản để tiếp tục
              sử dụng hệ thống quản lý ScentStation.
            </p>
          </div>
          {passwordChanged && (
            <div
              role="status"
              className="mb-5 flex items-start gap-3 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-700"
            >
              <ShieldCheck
                size={21}
                className="shrink-0"
              />

              <div>
                <p className="font-semibold">
                  Đổi mật khẩu thành công!
                </p>

                <p className="mt-1 text-xs leading-5">
                  Vui lòng đăng nhập lại bằng mật khẩu mới.
                </p>
              </div>
            </div>
          )}
          {/* LOGIN FORM */}
          <form
            onSubmit={handleSubmit}
            className="space-y-5"
          >

            {/* Username or Email */}
            <div>
              <label
                htmlFor="login-username"
                className="mb-2 block text-sm font-semibold text-slate-700"
              >
                Tên đăng nhập hoặc email
              </label>

              <div className="relative">
                <Mail
                  size={19}
                  className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
                />

                <input
                  id="login-username"
                  type="text"
                  required
                  disabled={loading}
                  autoComplete="username"
                  value={usernameOrEmail}
                  onChange={(event) => {
                    setUsernameOrEmail(
                      event.target.value
                    )
                    setError('')
                  }}
                  placeholder="Nhập username hoặc email"
                  className="h-13 w-full rounded-xl border border-slate-200 bg-white pl-12 pr-4 text-sm text-slate-900 outline-none transition focus:border-violet-500 focus:ring-4 focus:ring-violet-100 disabled:opacity-60"
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
                  type={
                    showPassword
                      ? 'text'
                      : 'password'
                  }
                  required
                  disabled={loading}
                  autoComplete="current-password"
                  value={password}
                  onChange={(event) => {
                    setPassword(event.target.value)
                    setError('')
                  }}
                  placeholder="Nhập mật khẩu"
                  className="h-13 w-full rounded-xl border border-slate-200 bg-white pl-12 pr-12 text-sm text-slate-900 outline-none transition focus:border-violet-500 focus:ring-4 focus:ring-violet-100 disabled:opacity-60"
                />

                <button
                  type="button"
                  onClick={() =>
                    setShowPassword(
                      (current) => !current
                    )
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

          {/* Account information */}
          <div className="mt-8 rounded-2xl border border-violet-100 bg-white p-5">
            <div className="mb-3 flex items-center gap-2 text-sm font-semibold text-slate-800">
              <ShieldCheck
                size={18}
                className="text-violet-600"
              />

              Truy cập hệ thống
            </div>

            <p className="text-xs leading-6 text-slate-500">
              Tài khoản được cấp bởi quản trị viên.
              Nếu bạn chưa có tài khoản hoặc không thể
              đăng nhập, vui lòng liên hệ người quản lý
              của thương hiệu.
            </p>
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

            Secure Access
          </p>
        </div>
      </div>
    </div>
  )
}
