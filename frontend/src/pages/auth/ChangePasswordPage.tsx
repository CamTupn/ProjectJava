import { useState, type FormEvent } from 'react'

import {
  Navigate,
  useNavigate,
} from 'react-router-dom'

import {
  ArrowLeft,
  Eye,
  EyeOff,
  LockKeyhole,
  ShieldAlert,
  ShieldCheck,
  LogOut,
} from 'lucide-react'

import {
  getRoleHome,
  useAuth,
} from '../../auth/AuthContext'

import {
  getApiErrorMessage,
} from '../../services/apiClient'

export default function ChangePasswordPage() {
  const {
    user,
    changePassword,
    logout,
  } = useAuth()

  const navigate = useNavigate()

  const [currentPassword, setCurrentPassword] =
    useState('')

  const [newPassword, setNewPassword] =
    useState('')

  const [confirmPassword, setConfirmPassword] =
    useState('')

  const [showPasswords, setShowPasswords] =
    useState(false)

  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (!user) {
    return <Navigate to="/login" replace />
  }

  const required = user.mustChangePassword

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault()

    if (loading) return

    setError('')

    if (
      !currentPassword ||
      !newPassword ||
      !confirmPassword
    ) {
      setError('Vui lòng nhập đầy đủ thông tin.')
      return
    }

    if (
      newPassword.length < 8 ||
      new TextEncoder().encode(newPassword).length > 72
    ) {
      setError(
        'Mật khẩu mới phải có ít nhất 8 ký tự và không vượt quá 72 byte.'
      )
      return
    }

    if (currentPassword === newPassword) {
      setError(
        'Mật khẩu mới phải khác mật khẩu hiện tại.'
      )
      return
    }

    if (newPassword !== confirmPassword) {
      setError(
        'Mật khẩu xác nhận không khớp.'
      )
      return
    }

    setLoading(true)

    try {
      await changePassword({
        currentPassword,
        newPassword,
      })

      navigate('/login', {
        replace: true,
        state: {
          passwordChanged: true,
        },
      })
    } catch (error) {
      setError(getApiErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const inputClass =
    'h-12 w-full rounded-xl border border-slate-200 bg-white px-4 text-sm text-slate-900 outline-none transition focus:border-violet-500 focus:ring-4 focus:ring-violet-100 disabled:opacity-60'

  return (
    <div className="flex min-h-screen items-center justify-center bg-[#F7F6FC] px-5 py-10">
      <div className="w-full max-w-[460px] rounded-3xl border border-violet-100 bg-white p-7 shadow-xl shadow-violet-100/60 sm:p-9">

        {/* Branding */}
        <div className="mb-7 flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-violet-100 text-violet-700">
            <LockKeyhole size={24} />
          </div>

          <div>
            <h1 className="text-lg font-bold text-[#302344]">
              ScentStation
            </h1>

            <p className="text-xs text-slate-500">
              Account Security
            </p>
          </div>
        </div>

        {/* Heading */}
        <h2 className="text-2xl font-bold text-slate-900">
          Đổi mật khẩu
        </h2>

        <p className="mt-2 text-sm text-slate-500">
          Bảo vệ tài khoản của bạn
        </p>

        {/* Account information */}
        <div className="mt-5 rounded-xl bg-slate-50 p-4">
          <p className="text-sm font-semibold text-slate-800">
            {user.name}
          </p>

          <p className="mt-1 text-xs text-slate-500">
            {user.email}
          </p>

          {user.tenantName && (
            <p className="mt-2 text-xs font-medium text-violet-700">
              {user.tenantName}
            </p>
          )}
        </div>

        {/* Mandatory password change */}
        {required ? (
          <div className="mt-5 flex items-start gap-3 rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
            <ShieldAlert
              size={21}
              className="mt-0.5 shrink-0"
            />

            <div>
              <p className="font-semibold">
                Yêu cầu đổi mật khẩu
              </p>

              <p className="mt-1 text-xs leading-5">
                Bạn cần đổi mật khẩu tạm thời
                trước khi tiếp tục sử dụng hệ thống.
              </p>
            </div>
          </div>
        ) : (
          <div className="mt-5 flex items-start gap-3 rounded-xl bg-violet-50 p-4 text-sm text-violet-800">
            <ShieldCheck
              size={20}
              className="shrink-0"
            />

            <p className="text-xs leading-5">
              Sau khi đổi mật khẩu, bạn cần đăng nhập
              lại bằng mật khẩu mới.
            </p>
          </div>
        )}

        {/* Password form */}
        <form
          onSubmit={handleSubmit}
          className="mt-6 space-y-5"
        >
          <div>
            <label
              htmlFor="current-password"
              className="mb-2 block text-sm font-semibold text-slate-700"
            >
              Mật khẩu hiện tại
            </label>

            <input
              id="current-password"
              type={showPasswords ? 'text' : 'password'}
              autoComplete="current-password"
              value={currentPassword}
              onChange={(event) => {
                setCurrentPassword(event.target.value)
                setError('')
              }}
              required
              disabled={loading}
              placeholder="Nhập mật khẩu hiện tại"
              className={inputClass}
            />
          </div>

          <div>
            <label
              htmlFor="new-password"
              className="mb-2 block text-sm font-semibold text-slate-700"
            >
              Mật khẩu mới
            </label>

            <input
              id="new-password"
              type={showPasswords ? 'text' : 'password'}
              autoComplete="new-password"
              value={newPassword}
              onChange={(event) => {
                setNewPassword(event.target.value)
                setError('')
              }}
              required
              minLength={8}
              maxLength={72}
              disabled={loading}
              placeholder="Nhập mật khẩu mới"
              className={inputClass}
            />

            <p className="mt-2 text-xs text-slate-500">
              Mật khẩu phải có ít nhất 8 ký tự.
            </p>
          </div>

          <div>
            <label
              htmlFor="confirm-password"
              className="mb-2 block text-sm font-semibold text-slate-700"
            >
              Xác nhận mật khẩu mới
            </label>

            <input
              id="confirm-password"
              type={showPasswords ? 'text' : 'password'}
              autoComplete="new-password"
              value={confirmPassword}
              onChange={(event) => {
                setConfirmPassword(event.target.value)
                setError('')
              }}
              required
              disabled={loading}
              placeholder="Nhập lại mật khẩu mới"
              className={inputClass}
            />
          </div>

          <button
            type="button"
            onClick={() =>
              setShowPasswords((current) => !current)
            }
            className="flex items-center gap-2 text-xs font-medium text-violet-700"
          >
            {showPasswords ? (
              <EyeOff size={16} />
            ) : (
              <Eye size={16} />
            )}

            {showPasswords
              ? 'Ẩn mật khẩu'
              : 'Hiện mật khẩu'}
          </button>

          {error && (
            <div
              role="alert"
              className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-600"
            >
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-[#6D45CE] font-semibold text-white transition hover:bg-[#5934B4] disabled:cursor-not-allowed disabled:opacity-60"
          >
            <LockKeyhole size={18} />

            {loading
              ? 'Đang cập nhật...'
              : 'Đổi mật khẩu'}
          </button>
        </form>

        {/* Navigation */}
        <div className="mt-6 flex items-center justify-center gap-5">
          {!required && (
            <button
              type="button"
              disabled={loading}
              onClick={() =>
                navigate(getRoleHome(user.role))
              }
              className="flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-violet-700"
            >
              <ArrowLeft size={16} />
              Quay lại
            </button>
          )}

          <button
            type="button"
            disabled={loading}
            onClick={handleLogout}
            className="flex items-center gap-2 text-sm font-medium text-violet-700"
          >
            <LogOut size={16} />
            Đăng xuất
          </button>
        </div>
      </div>
    </div>
  )
}