import { useState, type FormEvent } from 'react'
import { X, UserPlus, Eye, EyeOff } from 'lucide-react'

import { useAuth, type UserRole } from '../../auth/AuthContext'
import { getApiErrorMessage } from '../../services/apiClient'

import {
  USER_ROLE_LABELS,
  type User,
  type CreateUserRequest,
  type UpdateUserRequest,
} from '../../types/user'

import type { Tenant } from '../../types/tenant'

interface UserFormProps {
  initialData?: User | null
  tenants: Tenant[]
  onClose: () => void
  onCreate: (data: CreateUserRequest) => Promise<void>
  onUpdate: (
    id: number,
    data: UpdateUserRequest
  ) => Promise<void>
}

const employeeRoles: UserRole[] = [
  'OPERATIONS_MANAGER',
  'TECHNICIAN',
  'INVENTORY_STAFF',
]

export default function UserForm({
  initialData,
  tenants,
  onClose,
  onCreate,
  onUpdate,
}: UserFormProps) {
  const { user } = useAuth()

  const isSuperAdmin =
    user?.role === 'PLATFORM_SUPER_ADMIN'

  const isEditing = Boolean(initialData)

  const allowedRoles: UserRole[] = isSuperAdmin
    ? ['BRAND_ADMIN', ...employeeRoles]
    : employeeRoles

  const activeTenants = tenants.filter(
    (tenant) => tenant.status === 'ACTIVE'
  )

  const [fullName, setFullName] = useState(
    initialData?.fullName ?? ''
  )

  const [username, setUsername] = useState(
    initialData?.username ?? ''
  )

  const [email, setEmail] = useState(
    initialData?.email ?? ''
  )

  const [password, setPassword] = useState('')

  const [roleCode, setRoleCode] =
    useState<UserRole>(allowedRoles[0])

  const [tenantId, setTenantId] = useState(
    initialData?.tenantId?.toString() ?? ''
  )

  const [showPassword, setShowPassword] =
    useState(false)

  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const inputClass =
    'w-full rounded-xl border border-[#e8e5f5] ' +
    'bg-white px-4 py-3 text-sm outline-none ' +
    'focus:border-[#5865f2] ' +
    'focus:ring-2 focus:ring-[#5865f2]/10 ' +
    'disabled:bg-slate-100 disabled:text-slate-500'

  const labelClass =
    'mb-2 block text-sm font-medium text-[#302720]'

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault()

    if (saving) return

    setError('')

    const normalizedName = fullName.trim()
    const normalizedEmail = email.trim()
    const normalizedUsername =
      username.trim().toLowerCase()

    if (
      !normalizedName ||
      normalizedName.length > 150
    ) {
      setError('Họ tên phải từ 1 đến 150 ký tự.')
      return
    }

    if (
      !normalizedEmail ||
      normalizedEmail.length > 255 ||
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(
        normalizedEmail
      )
    ) {
      setError('Email không hợp lệ.')
      return
    }

    if (!isEditing) {
      if (
        !/^[a-zA-Z0-9_.-]{3,100}$/.test(
          normalizedUsername
        )
      ) {
        setError(
          'Username phải từ 3 đến 100 ký tự, chỉ gồm chữ, số, dấu chấm, gạch ngang hoặc gạch dưới.'
        )
        return
      }

      if (
        !password.trim() ||
        password.length < 8 ||
        new TextEncoder().encode(password).length > 72
      ) {
        setError(
          'Mật khẩu phải có ít nhất 8 ký tự và không vượt quá 72 byte.'
        )
        return
      }

      if (!allowedRoles.includes(roleCode)) {
        setError('Vai trò không hợp lệ.')
        return
      }

      if (
        isSuperAdmin &&
        !activeTenants.some(
          (tenant) => tenant.id === Number(tenantId)
        )
      ) {
        setError('Vui lòng chọn thương hiệu đang hoạt động.')
        return
      }
    }

    setSaving(true)

    try {
      if (initialData) {
        await onUpdate(initialData.id, {
          fullName: normalizedName,
          email: normalizedEmail,
        })
      } else {
        const data: CreateUserRequest = {
          username: normalizedUsername,
          email: normalizedEmail,
          fullName: normalizedName,
          password,
          roleCode,
        }

        if (isSuperAdmin) {
          data.tenantId = Number(tenantId)
        }

        await onCreate(data)
      }
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="user-form-title"
        className="max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-2xl bg-white shadow-2xl"
      >
        {/* Header */}
        <div className="flex items-center justify-between border-b border-[#eeeaf8] px-6 py-5">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-[#eef0ff] p-3 text-[#5865f2]">
              <UserPlus size={22} />
            </div>

            <div>
              <h2
                id="user-form-title"
                className="text-lg font-bold text-[#302720]"
              >
                {isEditing
                  ? 'Chỉnh sửa người dùng'
                  : 'Thêm người dùng'}
              </h2>

              <p className="mt-1 text-xs text-[#95877c]">
                {isEditing
                  ? 'Edit user'
                  : 'Create user'}
              </p>
            </div>
          </div>

          <button
            type="button"
            disabled={saving}
            onClick={onClose}
            aria-label="Đóng"
            className="rounded-lg p-2 text-slate-400 hover:bg-[#eef0ff] hover:text-[#5865f2]"
          >
            <X size={20} />
          </button>
        </div>

        <form
          onSubmit={handleSubmit}
          className="space-y-5 p-6"
        >
          {/* Full name */}
          <div>
            <label
              htmlFor="user-full-name"
              className={labelClass}
            >
              Họ và tên / Full name *
            </label>

            <input
              id="user-full-name"
              required
              maxLength={150}
              disabled={saving}
              value={fullName}
              onChange={(e) =>
                setFullName(e.target.value)
              }
              placeholder="Nhập họ và tên"
              className={inputClass}
            />
          </div>

          {/* Username */}
          <div>
            <label
              htmlFor="user-username"
              className={labelClass}
            >
              Tên đăng nhập / Username *
            </label>

            <input
              id="user-username"
              required
              minLength={3}
              maxLength={100}
              disabled={saving || isEditing}
              autoComplete="off"
              value={username}
              onChange={(e) =>
                setUsername(e.target.value)
              }
              placeholder="VD: luxury_admin"
              className={inputClass}
            />

            {isEditing && (
              <p className="mt-2 text-xs text-slate-400">
                Không thể thay đổi tên đăng nhập.
              </p>
            )}
          </div>

          {/* Email */}
          <div>
            <label
              htmlFor="user-email"
              className={labelClass}
            >
              Email *
            </label>

            <input
              id="user-email"
              type="email"
              required
              maxLength={255}
              disabled={saving}
              value={email}
              onChange={(e) =>
                setEmail(e.target.value)
              }
              placeholder="example@scentstation.com"
              className={inputClass}
            />
          </div>

          {/* Password - create only */}
          {!isEditing && (
            <div>
              <label
                htmlFor="user-password"
                className={labelClass}
              >
                Mật khẩu / Password *
              </label>

              <div className="relative">
                <input
                  id="user-password"
                  type={
                    showPassword
                      ? 'text'
                      : 'password'
                  }
                  required
                  minLength={8}
                  maxLength={72}
                  disabled={saving}
                  autoComplete="new-password"
                  value={password}
                  onChange={(e) =>
                    setPassword(e.target.value)
                  }
                  placeholder="Tối thiểu 8 ký tự"
                  className={`${inputClass} pr-12`}
                />

                <button
                  type="button"
                  disabled={saving}
                  aria-label={
                    showPassword
                      ? 'Ẩn mật khẩu'
                      : 'Hiện mật khẩu'
                  }
                  onClick={() =>
                    setShowPassword((prev) => !prev)
                  }
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400"
                >
                  {showPassword
                    ? <EyeOff size={18} />
                    : <Eye size={18} />}
                </button>
              </div>
            </div>
          )}

          {/* Tenant */}
          <div>
            <label
              htmlFor="user-tenant"
              className={labelClass}
            >
              Thương hiệu / Tenant
            </label>

            {isEditing ? (
              <input
                id="user-tenant"
                disabled
                value={
                  tenants.find(
                    (tenant) =>
                      tenant.id === initialData?.tenantId
                  )?.name ??
                  initialData?.tenantCode ??
                  'Platform'
                }
                className={inputClass}
              />
            ) : isSuperAdmin ? (
              <select
                id="user-tenant"
                required
                disabled={saving}
                value={tenantId}
                onChange={(e) =>
                  setTenantId(e.target.value)
                }
                className={inputClass}
              >
                <option value="">
                  Chọn thương hiệu
                </option>

                {activeTenants.map((tenant) => (
                  <option
                    key={tenant.id}
                    value={tenant.id}
                  >
                    {tenant.name} ({tenant.code})
                  </option>
                ))}
              </select>
            ) : (
              <input
                id="user-tenant"
                disabled
                value={
                  tenants.find(
                    (tenant) =>
                      tenant.code === user?.tenantCode
                  )?.name ??
                  user?.tenantCode ??
                  ''
                }
                className={inputClass}
              />
            )}
          </div>

          {/* Role */}
          <div>
            <label
              htmlFor="user-role"
              className={labelClass}
            >
              Vai trò / Role
            </label>

            {isEditing ? (
              <input
                id="user-role"
                disabled
                value={
                  initialData?.roles
                    .map(
                      (role) =>
                        USER_ROLE_LABELS[role] ?? role
                    )
                    .join(', ') ?? ''
                }
                className={inputClass}
              />
            ) : (
              <select
                id="user-role"
                disabled={saving}
                value={roleCode}
                onChange={(e) =>
                  setRoleCode(
                    e.target.value as UserRole
                  )
                }
                className={inputClass}
              >
                {allowedRoles.map((role) => (
                  <option key={role} value={role}>
                    {USER_ROLE_LABELS[role]}
                  </option>
                ))}
              </select>
            )}
          </div>

          {/* Status */}
          <div className="rounded-xl border border-[#e8e5f5] bg-[#f6f5ff] p-4">
            <p className="text-xs text-slate-500">
              Trạng thái / Status
            </p>

            <p className="mt-1 text-sm font-semibold text-[#5865f2]">
              {initialData?.status ?? 'ACTIVE'}
            </p>

            <p className="mt-2 text-xs text-slate-500">
              {isEditing
                ? 'Thay đổi trạng thái tại danh sách người dùng.'
                : 'Tài khoản mới mặc định được kích hoạt.'}
            </p>
          </div>

          {/* Error */}
          {error && (
            <div
              role="alert"
              className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-600"
            >
              {error}
            </div>
          )}

          {/* Footer */}
          <div className="flex justify-end gap-3 border-t border-[#eeeaf8] pt-5">
            <button
              type="button"
              disabled={saving}
              onClick={onClose}
              className="rounded-xl border border-[#e8e5f5] px-5 py-2.5 text-sm font-medium text-slate-600 hover:bg-[#f6f5ff] disabled:opacity-50"
            >
              Hủy / Cancel
            </button>

            <button
              type="submit"
              disabled={saving}
              className="rounded-xl bg-[#5865f2] px-5 py-2.5 text-sm font-semibold text-white hover:bg-[#4854df] disabled:opacity-50"
            >
              {saving
                ? 'Đang lưu...'
                : isEditing
                  ? 'Lưu thay đổi'
                  : 'Tạo tài khoản'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
