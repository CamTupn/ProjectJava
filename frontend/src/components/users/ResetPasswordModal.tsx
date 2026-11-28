import { useState, type FormEvent } from 'react'

import {
  X,
  KeyRound,
  Eye,
  EyeOff,
  ShieldAlert,
  LoaderCircle,
  AlertCircle,
} from 'lucide-react'

import type { User } from '../../types/user'

import { userService } from '../../services/userService'

import {
  getApiErrorMessage,
} from '../../services/apiClient'

interface ResetPasswordModalProps {
  user: User
  onClose: () => void
  onSuccess: () => void
}

export default function ResetPasswordModal({
  user,
  onClose,
  onSuccess,
}: ResetPasswordModalProps) {
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] =
    useState('')

  const [showPassword, setShowPassword] =
    useState(false)

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault()

    if (loading) return

    setError('')

    if (password.trim().length < 8) {
      setError(
        'Mật khẩu phải có ít nhất 8 ký tự.'
      )
      return
    }

    if (
      new TextEncoder().encode(password).length > 72
    ) {
      setError(
        'Mật khẩu không được vượt quá 72 byte.'
      )
      return
    }

    if (password !== confirmPassword) {
      setError(
        'Mật khẩu xác nhận không khớp.'
      )
      return
    }

    setLoading(true)

    try {
      await userService.resetPassword(user.id, {
        temporaryPassword: password,
      })

      setPassword('')
      setConfirmPassword('')

      onSuccess()
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center bg-black/40 p-4 backdrop-blur-sm">

      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="reset-password-title"
        className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-3xl bg-white p-6 shadow-2xl"
      >

        {/* Header */}
        <div className="flex items-start justify-between gap-4">

          <div className="flex items-center gap-3">

            <div className="rounded-2xl bg-violet-100 p-3 text-violet-600">
              <KeyRound size={24} />
            </div>

            <div>
              <h2
                id="reset-password-title"
                className="text-xl font-bold text-[#302720]"
              >
                Đặt lại mật khẩu
              </h2>

              <p className="mt-1 text-sm text-[#95877c]">
                Reset Password
              </p>
            </div>
          </div>

          <button
            type="button"
            aria-label="Đóng"
            disabled={loading}
            onClick={onClose}
            className="rounded-xl p-2 text-slate-400 hover:bg-slate-100 disabled:opacity-50"
          >
            <X size={22} />
          </button>
        </div>

        {/* Target account */}
        <div className="mt-6 flex items-center gap-3 rounded-2xl bg-[#F7F6FF] p-4">

          <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-violet-100 text-lg font-bold text-violet-700">
            {user.fullName.charAt(0).toUpperCase()}
          </div>

          <div className="min-w-0">
            <p className="font-semibold text-[#302720]">
              {user.fullName}
            </p>

            <p className="mt-1 text-xs text-slate-500">
              @{user.username}
            </p>

            <p className="mt-1 break-all text-xs text-slate-500">
              {user.email}
            </p>
          </div>
        </div>

        {/* Security warning */}
        <div className="mt-5 flex items-start gap-3 rounded-2xl border border-amber-100 bg-amber-50 p-4">

          <ShieldAlert
            size={22}
            className="shrink-0 text-amber-600"
          />

          <div className="text-xs leading-relaxed text-amber-800">

            <p className="font-semibold">
              Lưu ý bảo mật
            </p>

            <p className="mt-1">
              Mật khẩu hiện tại sẽ không còn hiệu lực.
              Các phiên đăng nhập cũ sẽ bị thu hồi.
              Người dùng phải đổi mật khẩu sau khi
              đăng nhập bằng mật khẩu tạm thời.
            </p>
          </div>
        </div>

        <form
          onSubmit={handleSubmit}
          className="mt-6 space-y-5"
        >

          {error && (
            <div
              role="alert"
              className="flex items-start gap-2 rounded-xl bg-red-50 p-3 text-sm text-red-600"
            >
              <AlertCircle
                size={18}
                className="shrink-0"
              />

              {error}
            </div>
          )}

          {/* Temporary password */}
          <div>
            <label
              htmlFor="temporary-password"
              className="mb-2 block text-sm font-semibold text-[#302720]"
            >
              Mật khẩu tạm thời
            </label>

            <div className="relative">

              <input
                id="temporary-password"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
                placeholder="Nhập mật khẩu tạm thời"
                autoComplete="new-password"
                disabled={loading}
                required
                minLength={8}
                className="w-full rounded-xl border border-[#E8E5F5] px-4 py-3 pr-12 text-sm outline-none focus:border-violet-400 focus:ring-2 focus:ring-violet-100"
              />

              <button
                type="button"
                aria-label={
                  showPassword
                    ? 'Ẩn mật khẩu'
                    : 'Hiện mật khẩu'
                }
                onClick={() =>
                  setShowPassword((value) => !value)
                }
                className="absolute inset-y-0 right-4 flex items-center text-slate-400 hover:text-violet-600"
              >
                {showPassword
                  ? <EyeOff size={19} />
                  : <Eye size={19} />}
              </button>
            </div>

            <p className="mt-2 text-xs text-slate-400">
              Mật khẩu phải có ít nhất 8 ký tự.
            </p>
          </div>

          {/* Confirm password */}
          <div>
            <label
              htmlFor="confirm-temporary-password"
              className="mb-2 block text-sm font-semibold text-[#302720]"
            >
              Xác nhận mật khẩu tạm thời
            </label>

            <input
              id="confirm-temporary-password"
              type={showPassword ? 'text' : 'password'}
              value={confirmPassword}
              onChange={(event) =>
                setConfirmPassword(event.target.value)
              }
              placeholder="Nhập lại mật khẩu"
              autoComplete="new-password"
              disabled={loading}
              required
              className="w-full rounded-xl border border-[#E8E5F5] px-4 py-3 text-sm outline-none focus:border-violet-400 focus:ring-2 focus:ring-violet-100"
            />
          </div>

          <p className="text-xs leading-relaxed text-slate-500">
            Hãy cung cấp mật khẩu tạm thời cho người dùng
            qua một kênh liên lạc an toàn.
          </p>

          {/* Actions */}
          <div className="flex gap-3 border-t border-slate-100 pt-5">

            <button
              type="button"
              disabled={loading}
              onClick={onClose}
              className="flex-1 rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-600 hover:bg-slate-50 disabled:opacity-50"
            >
              Hủy / Cancel
            </button>

            <button
              type="submit"
              disabled={loading}
              className="flex flex-1 items-center justify-center gap-2 rounded-xl bg-violet-600 px-4 py-3 text-sm font-semibold text-white hover:bg-violet-700 disabled:opacity-50"
            >
              {loading ? (
                <>
                  <LoaderCircle
                    size={18}
                    className="animate-spin"
                  />
                  Đang xử lý...
                </>
              ) : (
                <>
                  <KeyRound size={18} />
                  Đặt lại mật khẩu
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}