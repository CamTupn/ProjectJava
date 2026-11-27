import { useState, type FormEvent } from 'react'
import { X, Save, Eye, EyeOff } from 'lucide-react'

import type { Tenant } from '../../../types/tenant'
import {
  USER_ROLE_LABELS,
  type User,
  type CreateUserRequest,
  type UpdateUserRequest,
} from '../../../types/user'

import { getApiErrorMessage } from '../../../services/apiClient'

interface UserFormProps {
  initialData?: User | null
  tenants: Tenant[]
  isSuperAdmin: boolean
  onClose: () => void
  onCreate: (data: CreateUserRequest) => Promise<void>
  onUpdate: (id: number, data: UpdateUserRequest) => Promise<void>
}

type CreateRole = CreateUserRequest['roleCode']

const employeeRoles: CreateRole[] = [
  'OPERATIONS_MANAGER',
  'TECHNICIAN',
  'INVENTORY_STAFF',
]

const inputClass =
  'w-full rounded-xl border border-[#e8e5f5] bg-white px-4 py-3 text-sm outline-none transition focus:border-[#5865f2] disabled:bg-gray-100 disabled:text-gray-500'

export default function UserForm({
  initialData,
  tenants,
  isSuperAdmin,
  onClose,
  onCreate,
  onUpdate,
}: UserFormProps) {
  const isEditing = Boolean(initialData)

  const [fullName, setFullName] = useState(
    initialData?.fullName ?? '',
  )

  const [username, setUsername] = useState(
    initialData?.username ?? '',
  )

  const [email, setEmail] = useState(
    initialData?.email ?? '',
  )

  const [password, setPassword] = useState('')

  const [tenantId, setTenantId] = useState(
    initialData?.tenantId?.toString() ?? '',
  )

  const [roleCode, setRoleCode] = useState<CreateRole>(
    isSuperAdmin ? 'BRAND_ADMIN' : 'OPERATIONS_MANAGER',
  )

  const [showPassword, setShowPassword] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const availableRoles: CreateRole[] = isSuperAdmin
    ? ['BRAND_ADMIN', ...employeeRoles]
    : employeeRoles

  const activeTenants = tenants.filter(
    (tenant) => tenant.status === 'ACTIVE',
  )

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')

    if (!fullName.trim()) {
      setError('Vui lòng nhập họ tên / Full name is required')
      return
    }

    if (!email.trim()) {
      setError('Vui lòng nhập email / Email is required')
      return
    }

    if (!isEditing) {
      if (!/^[a-zA-Z0-9_.-]{3,100}$/.test(username.trim())) {
        setError(
          'Username phải từ 3–100 ký tự, chỉ gồm chữ, số, dấu chấm, gạch dưới hoặc gạch ngang.',
        )
        return
      }

      if (
        password.length < 8 ||
        !password.trim() ||
        new TextEncoder().encode(password).length > 72
      ) {
        setError('Mật khẩu tối thiểu 8 ký tự và tối đa 72 bytes.')
        return
      }

      if (isSuperAdmin && !tenantId) {
        setError('Vui lòng chọn thương hiệu / Please select a tenant')
        return
      }
    }

    setSaving(true)

    try {
      if (initialData) {
        await onUpdate(initialData.id, {
          fullName: fullName.trim(),
          email: email.trim(),
        })
      } else {
        const request: CreateUserRequest = {
          fullName: fullName.trim(),
          username: username.trim(),
          email: email.trim(),
          password,
          roleCode,
        }

        if (isSuperAdmin) {
          request.tenantId = Number(tenantId)
        }

        await onCreate(request)
      }

      onClose()
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="user-form-title"
        className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white shadow-2xl"
      >
        <div className="flex items-center justify-between border-b border-[#eeeaf8] p-6">
          <div>
            <h2
              id="user-form-title"
              className="text-xl font-bold text-[#302720]"
            >
              {isEditing
                ? 'Chỉnh sửa người dùng'
                : 'Thêm người dùng'}
            </h2>

            <p className="mt-1 text-sm text-[#95877c]">
              {isEditing ? 'Edit user account' : 'Create new user account'}
            </p>
          </div>

          <button
            type="button"
            onClick={onClose}
            disabled={saving}
            aria-label="Close"
            className="rounded-lg p-2 text-gray-500 hover:bg-gray-100"
          >
            <X size={22} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 p-6">
          {error && (
            <div
              role="alert"
              className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700"
            >
              {error}
            </div>
          )}

          <div className="grid gap-5 sm:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-semibold text-[#302720]">
                Họ tên / Full name *
              </label>

              <input
                className={inputClass}
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                maxLength={150}
                required
                disabled={saving}
                placeholder="Nguyễn Văn A"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-semibold text-[#302720]">
                Tên đăng nhập / Username *
              </label>

              <input
                className={inputClass}
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                minLength={3}
                maxLength={100}
                required
                disabled={isEditing || saving}
                autoComplete="off"
                placeholder="nguyenvana"
              />
            </div>
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-[#302720]">
              Email *
            </label>

            <input
              type="email"
              className={inputClass}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              maxLength={255}
              required
              disabled={saving}
              placeholder="user@example.com"
            />
          </div>

          {!isEditing && (
            <div>
              <label className="mb-2 block text-sm font-semibold text-[#302720]">
                Mật khẩu / Password *
              </label>

              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  className={`${inputClass} pr-12`}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  minLength={8}
                  maxLength={72}
                  required
                  disabled={saving}
                  autoComplete="new-password"
                  placeholder="Tối thiểu 8 ký tự"
                />

                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label="Toggle password visibility"
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-500"
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>
          )}

          <div>
            <label className="mb-2 block text-sm font-semibold text-[#302720]">
              Thương hiệu / Tenant
            </label>

            {isEditing ? (
              <input
                className={inputClass}
                value={initialData?.tenantCode ?? 'Platform'}
                disabled
              />
            ) : isSuperAdmin ? (
              <select
                className={inputClass}
                value={tenantId}
                onChange={(e) => setTenantId(e.target.value)}
                required
                disabled={saving}
              >
                <option value="">Chọn thương hiệu / Select tenant</option>

                {activeTenants.map((tenant) => (
                  <option key={tenant.id} value={tenant.id}>
                    {tenant.name} ({tenant.code})
                  </option>
                ))}
              </select>
            ) : (
              <input
                className={inputClass}
                value="Thương hiệu hiện tại / Current tenant"
                disabled
              />
            )}
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-[#302720]">
              Vai trò / Role
            </label>

            {isEditing ? (
              <input
                className={inputClass}
                value={initialData?.roles
                  .map((role) => USER_ROLE_LABELS[role])
                  .join(', ') ?? ''}
                disabled
              />
            ) : (
              <select
                className={inputClass}
                value={roleCode}
                onChange={(e) => setRoleCode(e.target.value as CreateRole)}
                disabled={saving}
              >
                {availableRoles.map((role) => (
                  <option key={role} value={role}>
                    {USER_ROLE_LABELS[role]}
                  </option>
                ))}
              </select>
            )}
          </div>

          <div className="rounded-xl bg-[#f7f6ff] p-4 text-sm text-[#76675c]">
            {isEditing
              ? 'Chỉ có thể cập nhật họ tên và email. / Only full name and email can be updated.'
              : 'Tài khoản mới có trạng thái ACTIVE. / New accounts are active by default.'}
          </div>

          <div className="flex flex-wrap justify-end gap-3 border-t border-[#eeeaf8] pt-5">
            <button
              type="button"
              onClick={onClose}
              disabled={saving}
              className="rounded-xl border border-gray-200 px-5 py-3 text-sm font-semibold hover:bg-gray-50"
            >
              Hủy / Cancel
            </button>

            <button
              type="submit"
              disabled={saving}
              className="flex items-center gap-2 rounded-xl bg-[#5865f2] px-5 py-3 text-sm font-semibold text-white hover:bg-[#4854df] disabled:opacity-50"
            >
              <Save size={17} />

              {saving
                ? 'Đang lưu / Saving...'
                : 'Lưu tài khoản / Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
