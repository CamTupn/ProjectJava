import { useEffect, useMemo, useState } from 'react'

import {
  Search,
  Users,
  UserCheck,
  UserX,
  Plus,
  Eye,
  Pencil,
  Lock,
  RefreshCw,
  X,
} from 'lucide-react'

import { useAuth } from '../../../auth/AuthContext'
import { userService } from '../../../services/userService'
import { tenantService } from '../../../services/tenantService'
import { getApiErrorMessage } from '../../../services/apiClient'

import type { Tenant } from '../../../types/tenant'

import {
  USER_ROLE_LABELS,
  USER_STATUS_LABELS,
  type User,
  type UserStatus,
  type CreateUserRequest,
  type UpdateUserRequest,
} from '../../../types/user'

import UserForm from './UserForm'

const statuses: UserStatus[] = [
  'ACTIVE',
  'LOCKED',
  'INACTIVE',
]

const selectClass =
  'rounded-xl border border-[#e8e5f5] bg-white px-3 py-3 text-sm outline-none'

function formatDate(value: string) {
  if (!value) return '—'

  const date = new Date(value)

  return Number.isNaN(date.getTime())
    ? value
    : date.toLocaleDateString('vi-VN')
}

function StatusBadge({ status }: { status: UserStatus }) {
  const colors: Record<UserStatus, string> = {
    ACTIVE: 'bg-green-50 text-green-700',
    LOCKED: 'bg-amber-50 text-amber-700',
    INACTIVE: 'bg-red-50 text-red-700',
  }

  return (
    <span
      className={`inline-flex whitespace-nowrap rounded-full px-3 py-1 text-xs font-semibold ${colors[status]}`}
    >
      {USER_STATUS_LABELS[status]}
    </span>
  )
}

export default function UsersPage() {
  const { user: currentUser } = useAuth()

  const isSuperAdmin =
    currentUser?.role === 'PLATFORM_SUPER_ADMIN'

  const [users, setUsers] = useState<User[]>([])
  const [tenants, setTenants] = useState<Tenant[]>([])

  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [roleFilter, setRoleFilter] = useState('ALL')
  const [tenantFilter, setTenantFilter] = useState('ALL')

  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [showForm, setShowForm] = useState(false)
  const [editingUser, setEditingUser] = useState<User | null>(null)
  const [selectedUser, setSelectedUser] = useState<User | null>(null)

  const loadData = async () => {
    setLoading(true)
    setError('')

    try {
      const [userData, tenantData] = await Promise.all([
        userService.getAll(),
        isSuperAdmin
          ? tenantService.getAll()
          : Promise.resolve([] as Tenant[]),
      ])

      setUsers(userData)
      setTenants(tenantData)
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    let cancelled = false

    async function initialize() {
      setLoading(true)
      setError('')
      setUsers([])
      setTenants([])

      try {
        const [userData, tenantData] = await Promise.all([
          userService.getAll(),
          isSuperAdmin
            ? tenantService.getAll()
            : Promise.resolve([] as Tenant[]),
        ])

        if (!cancelled) {
          setUsers(userData)
          setTenants(tenantData)
        }
      } catch (err) {
        if (!cancelled) {
          setError(getApiErrorMessage(err))
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void initialize()

    return () => {
      cancelled = true
    }
  }, [isSuperAdmin, currentUser?.id])

  const canManageUser = (target: User) => {
    if (isSuperAdmin) return true

    if (currentUser?.role !== 'BRAND_ADMIN') {
      return false
    }

    return !target.roles.some(
      (role) =>
        role === 'PLATFORM_SUPER_ADMIN' ||
        role === 'BRAND_ADMIN',
    )
  }

  const isCurrentUser = (target: User) =>
    String(target.id) === String(currentUser?.id)

  const filteredUsers = useMemo(() => {
    const search = keyword.trim().toLowerCase()

    return users.filter((item) => {
      const matchesKeyword = [
        item.fullName,
        item.username,
        item.email,
        item.tenantCode ?? '',
      ].some((value) => value.toLowerCase().includes(search))

      const matchesStatus =
        statusFilter === 'ALL' ||
        item.status === statusFilter

      const matchesRole =
        roleFilter === 'ALL' ||
        item.roles.some((role) => role === roleFilter)

      const matchesTenant =
        tenantFilter === 'ALL' ||
        (tenantFilter === 'PLATFORM'
          ? item.tenantId === null
          : String(item.tenantId) === tenantFilter)

      return (
        matchesKeyword &&
        matchesStatus &&
        matchesRole &&
        matchesTenant
      )
    })
  }, [users, keyword, statusFilter, roleFilter, tenantFilter])

  const statistics = [
    {
      title: 'Tổng người dùng',
      subtitle: 'Total users',
      value: users.length,
      icon: Users,
      color: 'bg-indigo-50 text-indigo-600',
    },
    {
      title: 'Đang hoạt động',
      subtitle: 'Active',
      value: users.filter((u) => u.status === 'ACTIVE').length,
      icon: UserCheck,
      color: 'bg-green-50 text-green-600',
    },
    {
      title: 'Đã khóa',
      subtitle: 'Locked',
      value: users.filter((u) => u.status === 'LOCKED').length,
      icon: Lock,
      color: 'bg-amber-50 text-amber-600',
    },
    {
      title: 'Vô hiệu hóa',
      subtitle: 'Inactive',
      value: users.filter((u) => u.status === 'INACTIVE').length,
      icon: UserX,
      color: 'bg-red-50 text-red-600',
    },
  ]

  const openCreateForm = () => {
    setError('')
    setSuccess('')
    setEditingUser(null)
    setShowForm(true)
  }

  const openUser = async (
    target: User,
    mode: 'view' | 'edit',
  ) => {
    if (mode === 'edit' && !canManageUser(target)) {
      return
    }

    setBusy(true)
    setError('')
    setSuccess('')

    try {
      const detail = await userService.getById(target.id)

      if (mode === 'view') {
        setSelectedUser(detail)
      } else {
        setEditingUser(detail)
        setShowForm(true)
      }
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  const handleCreate = async (data: CreateUserRequest) => {
    const createdUser = await userService.create(data)

    setUsers((prev) => [createdUser, ...prev])

    setSuccess('Tạo tài khoản thành công / User created successfully')
  }

  const handleUpdate = async (
    id: number,
    data: UpdateUserRequest,
  ) => {
    const updatedUser = await userService.update(id, data)

    setUsers((prev) =>
      prev.map((item) =>
        item.id === id ? updatedUser : item,
      ),
    )

    setSuccess('Cập nhật thành công / User updated successfully')
  }

  const handleStatusChange = async (
    target: User,
    status: UserStatus,
  ) => {
    if (
      !canManageUser(target) ||
      target.status === status ||
      (isCurrentUser(target) && status !== 'ACTIVE')
    ) {
      return
    }

    const confirmed = window.confirm(
      `Thay đổi trạng thái tài khoản ${target.username} thành ${USER_STATUS_LABELS[status]}?`,
    )

    if (!confirmed) return

    setBusy(true)
    setError('')
    setSuccess('')

    try {
      const updatedUser = await userService.updateStatus(
        target.id,
        status,
      )

      setUsers((prev) =>
        prev.map((item) =>
          item.id === updatedUser.id ? updatedUser : item,
        ),
      )

      setSuccess(
        'Cập nhật trạng thái thành công / Status updated successfully',
      )
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="space-y-7">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Quản lý người dùng
          </h1>

          <p className="mt-1 text-sm text-[#95877c]">
            User Management
          </p>

          <p className="mt-2 text-sm text-[#76675c]">
            {isSuperAdmin
              ? 'Quản lý tài khoản trên toàn hệ thống'
              : 'Quản lý nhân sự thuộc thương hiệu của bạn'}
          </p>
        </div>

        <div className="flex flex-wrap gap-3">
          <button
            onClick={() => {
              setSuccess('')
              void loadData()
            }}
            disabled={loading || busy}
            className="flex items-center gap-2 rounded-xl border border-[#e8e5f5] bg-white px-4 py-3 text-sm font-semibold disabled:opacity-50"
          >
            <RefreshCw
              size={17}
              className={loading ? 'animate-spin' : ''}
            />
            Làm mới / Refresh
          </button>

          <button
            onClick={openCreateForm}
            disabled={loading || busy}
            className="flex items-center gap-2 rounded-xl bg-[#5865f2] px-5 py-3 text-sm font-semibold text-white shadow-sm hover:bg-[#4854df] disabled:opacity-50"
          >
            <Plus size={18} />
            Thêm người dùng / Add user
          </button>
        </div>
      </div>

      {/* Messages */}
      {error && (
        <div
          role="alert"
          className="flex items-center justify-between gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700"
        >
          {error}

          <button onClick={() => setError('')} aria-label="Close error">
            <X size={18} />
          </button>
        </div>
      )}

      {success && (
        <div
          role="status"
          className="flex items-center justify-between gap-3 rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-700"
        >
          {success}

          <button onClick={() => setSuccess('')} aria-label="Close message">
            <X size={18} />
          </button>
        </div>
      )}

      {/* Statistics */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {statistics.map((item) => {
          const Icon = item.icon

          return (
            <div
              key={item.title}
              className="rounded-2xl border border-[#e8e5f5] bg-white p-6 shadow-sm"
            >
              <div className="flex items-center justify-between gap-2">
                <div>
                  <p className="text-sm font-medium text-[#76675c]">
                    {item.title}
                  </p>

                  <p className="mt-1 text-xs text-[#95877c]">
                    {item.subtitle}
                  </p>
                </div>

                <div className={`rounded-xl p-3 ${item.color}`}>
                  <Icon size={22} />
                </div>
              </div>

              <h2 className="mt-4 text-3xl font-bold text-[#302720]">
                {loading ? '—' : item.value}
              </h2>
            </div>
          )
        })}
      </div>

      {/* User list */}
      <section className="overflow-hidden rounded-2xl border border-[#e8e5f5] bg-white shadow-sm">
        <div className="space-y-4 border-b border-[#eeeaf8] p-5">
          <div>
            <h2 className="font-bold text-[#302720]">
              Danh sách người dùng / User list
            </h2>

            <p className="mt-1 text-sm text-[#95877c]">
              Hiển thị / Showing {filteredUsers.length} / {users.length}
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            <div className="flex items-center gap-2 rounded-xl border border-[#e8e5f5] bg-[#f7f6ff] px-3">
              <Search size={17} className="text-[#95877c]" />

              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Tìm kiếm / Search..."
                className="w-48 bg-transparent py-3 text-sm outline-none"
              />
            </div>

            {isSuperAdmin && (
              <select
                className={selectClass}
                value={tenantFilter}
                onChange={(e) => setTenantFilter(e.target.value)}
              >
                <option value="ALL">Tất cả thương hiệu / All tenants</option>
                <option value="PLATFORM">Platform</option>

                {tenants.map((tenant) => (
                  <option key={tenant.id} value={tenant.id}>
                    {tenant.name}
                  </option>
                ))}
              </select>
            )}

            <select
              className={selectClass}
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
            >
              <option value="ALL">Tất cả vai trò / All roles</option>

              {Object.entries(USER_ROLE_LABELS).map(([code, label]) => (
                <option key={code} value={code}>
                  {label}
                </option>
              ))}
            </select>

            <select
              className={selectClass}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="ALL">Tất cả trạng thái / All statuses</option>

              {statuses.map((status) => (
                <option key={status} value={status}>
                  {USER_STATUS_LABELS[status]}
                </option>
              ))}
            </select>
          </div>
        </div>

        {loading ? (
          <div className="flex items-center justify-center gap-3 p-12 text-sm text-gray-500">
            <RefreshCw size={20} className="animate-spin" />
            Đang tải dữ liệu / Loading...
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[1000px] text-left text-sm">
              <thead className="bg-[#f8f7ff] text-xs font-semibold text-[#76675c]">
                <tr>
                  <th className="px-5 py-4">Người dùng / User</th>
                  <th className="px-5 py-4">Thương hiệu / Tenant</th>
                  <th className="px-5 py-4">Vai trò / Role</th>
                  <th className="px-5 py-4">Trạng thái / Status</th>
                  <th className="px-5 py-4">Ngày tạo / Created</th>
                  <th className="px-5 py-4">Thao tác / Actions</th>
                </tr>
              </thead>

              <tbody className="divide-y divide-[#eeeaf8]">
                {filteredUsers.map((item) => (
                  <tr
                    key={item.id}
                    className="transition hover:bg-[#faf9ff]"
                  >
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-3">
                        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-[#eef0ff] font-bold text-[#5865f2]">
                          {item.fullName.charAt(0).toUpperCase()}
                        </div>

                        <div>
                          <p className="font-semibold text-[#302720]">
                            {item.fullName}
                          </p>

                          <p className="mt-1 text-xs text-[#76675c]">
                            @{item.username}
                          </p>

                          <p className="mt-1 text-xs text-[#95877c]">
                            {item.email}
                          </p>
                        </div>
                      </div>
                    </td>

                    <td className="px-5 py-4">
                      {tenants.find((t) => t.id === item.tenantId)?.name
                        ?? item.tenantCode
                        ?? 'Platform'}
                    </td>

                    <td className="px-5 py-4">
                      <div className="flex flex-wrap gap-1">
                        {item.roles.map((role) => (
                          <span
                            key={role}
                            className="rounded-lg bg-indigo-50 px-2 py-1 text-xs font-medium text-indigo-700"
                          >
                            {USER_ROLE_LABELS[role] ?? role}
                          </span>
                        ))}
                      </div>
                    </td>

                    <td className="px-5 py-4">
                      <StatusBadge status={item.status} />
                    </td>

                    <td className="whitespace-nowrap px-5 py-4 text-[#76675c]">
                      {formatDate(item.createdAt)}
                    </td>

                    <td className="px-5 py-4">
                      <div className="flex flex-wrap items-center gap-2">
                        <button
                          title="Xem chi tiết / View"
                          aria-label="View user"
                          disabled={busy}
                          onClick={() => void openUser(item, 'view')}
                          className="rounded-lg bg-blue-50 p-2 text-blue-600 hover:bg-blue-100 disabled:opacity-50"
                        >
                          <Eye size={17} />
                        </button>

                        {canManageUser(item) && (
                          <>
                            <button
                              title="Chỉnh sửa / Edit"
                              aria-label="Edit user"
                              disabled={busy}
                              onClick={() => void openUser(item, 'edit')}
                              className="rounded-lg bg-indigo-50 p-2 text-indigo-600 hover:bg-indigo-100 disabled:opacity-50"
                            >
                              <Pencil size={17} />
                            </button>

                            <select
                              aria-label="Change user status"
                              value={item.status}
                              disabled={busy || isCurrentUser(item)}
                              onChange={(e) =>
                                void handleStatusChange(
                                  item,
                                  e.target.value as UserStatus,
                                )
                              }
                              className="max-w-[140px] rounded-lg border border-[#e8e5f5] px-2 py-2 text-xs outline-none disabled:opacity-50"
                            >
                              {statuses.map((status) => (
                                <option key={status} value={status}>
                                  {status}
                                </option>
                              ))}
                            </select>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}

                {filteredUsers.length === 0 && (
                  <tr>
                    <td
                      colSpan={6}
                      className="p-12 text-center text-gray-500"
                    >
                      {error
                        ? 'Không thể tải dữ liệu / Unable to load data'
                        : 'Không tìm thấy người dùng / No users found'}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* Create / Edit modal */}
      {showForm && (
        <UserForm
          initialData={editingUser}
          tenants={tenants}
          isSuperAdmin={isSuperAdmin}
          onCreate={handleCreate}
          onUpdate={handleUpdate}
          onClose={() => {
            setShowForm(false)
            setEditingUser(null)
          }}
        />
      )}

      {/* User detail modal */}
      {selectedUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div
            role="dialog"
            aria-modal="true"
            aria-labelledby="user-detail-title"
            className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl bg-white p-6 shadow-xl"
          >
            <div className="mb-6 flex items-center justify-between">
              <div>
                <h2
                  id="user-detail-title"
                  className="text-xl font-bold text-[#302720]"
                >
                  Chi tiết người dùng
                </h2>

                <p className="mt-1 text-sm text-[#95877c]">
                  User details
                </p>
              </div>

              <button
                onClick={() => setSelectedUser(null)}
                aria-label="Close details"
                className="rounded-lg p-2 hover:bg-gray-100"
              >
                <X size={22} />
              </button>
            </div>

            <div className="mb-5 flex items-center gap-4 rounded-xl bg-[#f7f6ff] p-4">
              <div className="flex h-14 w-14 items-center justify-center rounded-full bg-[#5865f2] text-xl font-bold text-white">
                {selectedUser.fullName.charAt(0).toUpperCase()}
              </div>

              <div>
                <p className="font-bold text-[#302720]">
                  {selectedUser.fullName}
                </p>

                <p className="mt-1 text-sm text-[#95877c]">
                  @{selectedUser.username}
                </p>
              </div>
            </div>

            <div className="divide-y divide-[#eeeaf8] text-sm">
              {[
                ['User ID', selectedUser.id],
                ['Họ tên / Full name', selectedUser.fullName],
                ['Username', selectedUser.username],
                ['Email', selectedUser.email],
                ['Tenant ID', selectedUser.tenantId ?? '—'],
                ['Thương hiệu / Tenant', selectedUser.tenantCode ?? 'Platform'],
                [
                  'Vai trò / Role',
                  selectedUser.roles
                    .map((role) => USER_ROLE_LABELS[role] ?? role)
                    .join(', '),
                ],
                ['Ngày tạo / Created', formatDate(selectedUser.createdAt)],
                ['Cập nhật / Updated', formatDate(selectedUser.updatedAt)],
              ].map(([label, value]) => (
                <div
                  key={label}
                  className="flex justify-between gap-4 py-3"
                >
                  <span className="text-[#95877c]">{label}</span>

                  <span className="break-all text-right font-medium text-[#302720]">
                    {value}
                  </span>
                </div>
              ))}

              <div className="flex items-center justify-between py-3">
                <span className="text-[#95877c]">
                  Trạng thái / Status
                </span>

                <StatusBadge status={selectedUser.status} />
              </div>
            </div>

            <button
              onClick={() => setSelectedUser(null)}
              className="mt-5 w-full rounded-xl bg-[#5865f2] py-3 text-sm font-semibold text-white hover:bg-[#4854df]"
            >
              Đóng / Close
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
