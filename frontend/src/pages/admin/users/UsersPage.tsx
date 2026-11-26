import { useMemo, useState } from 'react'
import {
  Search,
  Users,
  UserCheck,
  UserX,
  Plus,
  Eye,
  Pencil,
  Ban,
  X,
} from 'lucide-react'

type UserStatus = 'ACTIVE' | 'LOCKED' | 'INACTIVE'

interface UserItem {
  id: number
  fullName: string
  username: string
  email: string
  tenant: string
  role: string
  status: UserStatus
  createdAt: string
}

const initialUsers: UserItem[] = [
  {
    id: 1,
    fullName: 'Nguyễn Minh Anh',
    username: 'minhanh',
    email: 'minhanh@scentstation.com',
    tenant: 'ScentStation Demo',
    role: 'BRAND_ADMIN',
    status: 'ACTIVE',
    createdAt: '10/11/2026',
  },
  {
    id: 2,
    fullName: 'Trần Quốc Bảo',
    username: 'quocbao',
    email: 'quocbao@aroma.com',
    tenant: 'Brand Aroma',
    role: 'OPERATIONS_MANAGER',
    status: 'ACTIVE',
    createdAt: '08/11/2026',
  },
  {
    id: 3,
    fullName: 'Lê Hoàng Nam',
    username: 'hoangnam',
    email: 'nam@luxury.com',
    tenant: 'Brand Luxury',
    role: 'TECHNICIAN',
    status: 'LOCKED',
    createdAt: '05/11/2026',
  },
  {
    id: 4,
    fullName: 'Phạm Thu Hà',
    username: 'thuha',
    email: 'ha@flower.com',
    tenant: 'Flower Perfume',
    role: 'INVENTORY_STAFF',
    status: 'ACTIVE',
    createdAt: '02/11/2026',
  },
  {
    id: 5,
    fullName: 'Đỗ Thành Đạt',
    username: 'thanhdat',
    email: 'dat@oriental.com',
    tenant: 'Oriental Scents',
    role: 'OPERATIONS_MANAGER',
    status: 'INACTIVE',
    createdAt: '01/11/2026',
  },
]

const statusLabels: Record<UserStatus, string> = {
  ACTIVE: 'Đang hoạt động',
  LOCKED: 'Đã khóa',
  INACTIVE: 'Ngừng hoạt động',
}

const roleLabels: Record<string, string> = {
  PLATFORM_SUPER_ADMIN: 'Super Admin',
  BRAND_ADMIN: 'Brand Admin',
  OPERATIONS_MANAGER: 'Operations Manager',
  TECHNICIAN: 'Technician',
  INVENTORY_STAFF: 'Inventory Staff',
}

export default function UsersPage() {
  const [users, setUsers] = useState(initialUsers)
  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [tenantFilter, setTenantFilter] = useState('ALL')
  const [selectedUser, setSelectedUser] = useState<UserItem | null>(null)
  const [editingUser, setEditingUser] = useState<UserItem | null>(null)
  const [showForm, setShowForm] = useState(false)

  const filteredUsers = useMemo(() => {
    return users.filter((user) => {
      const matchesKeyword = [
        user.fullName,
        user.username,
        user.email,
      ].some((value) => value.toLowerCase().includes(keyword.toLowerCase()))

      const matchesStatus =
        statusFilter === 'ALL' || user.status === statusFilter

      const matchesTenant =
        tenantFilter === 'ALL' || user.tenant === tenantFilter

      return matchesKeyword && matchesStatus && matchesTenant
    })
  }, [users, keyword, statusFilter, tenantFilter])

  const activeCount = users.filter((user) => user.status === 'ACTIVE').length
  const lockedCount = users.filter((user) => user.status === 'LOCKED').length
  const inactiveCount = users.filter((user) => user.status === 'INACTIVE').length

  const toggleLock = (id: number) => {
    setUsers((prev) =>
      prev.map((user) =>
        user.id === id
          ? {
              ...user,
              status: user.status === 'LOCKED' ? 'ACTIVE' : 'LOCKED',
            }
          : user,
      ),
    )
  }

  const handleSave = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    const form = new FormData(e.currentTarget)

    const updatedUser: UserItem = {
      id: editingUser?.id ?? Date.now(),
      fullName: String(form.get('fullName')),
      username: String(form.get('username')),
      email: String(form.get('email')),
      tenant: String(form.get('tenant')),
      role: String(form.get('role')),
      status: String(form.get('status')) as UserStatus,
      createdAt: editingUser?.createdAt ?? new Date().toLocaleDateString('vi-VN'),
    }

    if (editingUser) {
      setUsers((prev) =>
        prev.map((user) => user.id === editingUser.id ? updatedUser : user),
      )
    } else {
      setUsers((prev) => [updatedUser, ...prev])
    }

    setShowForm(false)
    setEditingUser(null)
  }

  const openCreateForm = () => {
    setEditingUser(null)
    setShowForm(true)
  }

  const openEditForm = (user: UserItem) => {
    setEditingUser(user)
    setShowForm(true)
  }

  return (
    <div className="space-y-7">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Quản lý người dùng
          </h1>
          <p className="mt-1 text-sm text-[#95877c]">
            Quản lý tài khoản và phân quyền người dùng trên hệ thống
          </p>
        </div>

        <button
          onClick={openCreateForm}
          className="flex items-center gap-2 rounded-xl bg-[#5865f2] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#4854df]"
        >
          <Plus size={18} />
          Thêm người dùng
        </button>
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {[
          { title: 'Tổng người dùng', value: users.length, icon: Users },
          { title: 'Đang hoạt động', value: activeCount, icon: UserCheck },
          { title: 'Đã khóa', value: lockedCount, icon: Ban },
          { title: 'Ngừng hoạt động', value: inactiveCount, icon: UserX },
        ].map((item) => {
          const Icon = item.icon

          return (
            <div
              key={item.title}
              className="rounded-2xl border border-[#e8e5f5] bg-white p-6 shadow-sm"
            >
              <div className="flex items-center justify-between">
                <p className="text-sm text-[#95877c]">{item.title}</p>
                <div className="rounded-xl bg-[#eef0ff] p-3 text-[#5865f2]">
                  <Icon size={21} />
                </div>
              </div>
              <h2 className="mt-4 text-3xl font-bold text-[#302720]">
                {item.value}
              </h2>
            </div>
          )
        })}
      </div>

      {/* User table */}
      <div className="overflow-hidden rounded-2xl border border-[#e8e5f5] bg-white shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-4 border-b border-[#eeeaf8] p-5">
          <div>
            <h3 className="font-bold text-[#302720]">Danh sách người dùng</h3>
            <p className="mt-1 text-sm text-[#95877c]">
              Hiển thị {filteredUsers.length} / {users.length} người dùng
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            <div className="flex items-center gap-2 rounded-xl border border-[#e8e5f5] bg-[#f7f6ff] px-3">
              <Search size={17} className="text-[#95877c]" />
              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Tìm tên, username, email..."
                className="w-56 bg-transparent py-3 text-sm outline-none"
              />
            </div>

            <select
              value={tenantFilter}
              onChange={(e) => setTenantFilter(e.target.value)}
              className="rounded-xl border border-[#e8e5f5] bg-white px-3 py-3 text-sm text-[#76675c] outline-none"
            >
              <option value="ALL">Tất cả thương hiệu</option>
              {[...new Set(users.map((user) => user.tenant))].map((tenant) => (
                <option key={tenant} value={tenant}>{tenant}</option>
              ))}
            </select>

            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="rounded-xl border border-[#e8e5f5] bg-white px-3 py-3 text-sm text-[#76675c] outline-none"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="ACTIVE">Đang hoạt động</option>
              <option value="LOCKED">Đã khóa</option>
              <option value="INACTIVE">Ngừng hoạt động</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[950px] text-left text-sm">
            <thead className="bg-[#f7f6ff] text-[#766c91]">
              <tr>
                <th className="px-6 py-4 font-medium">Người dùng</th>
                <th className="px-6 py-4 font-medium">Thương hiệu</th>
                <th className="px-6 py-4 font-medium">Vai trò</th>
                <th className="px-6 py-4 font-medium">Ngày tạo</th>
                <th className="px-6 py-4 font-medium">Trạng thái</th>
                <th className="px-6 py-4 text-center font-medium">Thao tác</th>
              </tr>
            </thead>

            <tbody>
              {filteredUsers.map((user) => (
                <tr
                  key={user.id}
                  className="border-b border-[#f0edf8] transition hover:bg-[#faf9ff]"
                >
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[#eef0ff] font-semibold text-[#5865f2]">
                        {user.fullName.charAt(0)}
                      </div>
                      <div>
                        <p className="font-semibold text-[#302720]">
                          {user.fullName}
                        </p>
                        <p className="mt-1 text-xs text-[#95877c]">
                          {user.email}
                        </p>
                        <p className="text-xs text-[#95877c]">
                          @{user.username}
                        </p>
                      </div>
                    </div>
                  </td>

                  <td className="px-6 py-4 text-[#76675c]">{user.tenant}</td>
                  <td className="px-6 py-4 text-[#76675c]">
                    {roleLabels[user.role] ?? user.role}
                  </td>
                  <td className="px-6 py-4 text-[#95877c]">{user.createdAt}</td>

                  <td className="px-6 py-4">
                    <span className={`rounded-full px-3 py-1.5 text-xs font-medium ${
                      user.status === 'ACTIVE'
                        ? 'bg-emerald-50 text-emerald-700'
                        : user.status === 'LOCKED'
                          ? 'bg-amber-50 text-amber-700'
                          : 'bg-slate-100 text-slate-600'
                    }`}>
                      {statusLabels[user.status]}
                    </span>
                  </td>

                  <td className="px-6 py-4">
                    <div className="flex justify-center gap-2">
                      <button
                        title="Xem chi tiết"
                        onClick={() => setSelectedUser(user)}
                        className="rounded-lg border border-[#e8e5f5] p-2 text-[#766c91] hover:bg-[#eef0ff] hover:text-[#5865f2]"
                      >
                        <Eye size={17} />
                      </button>
                      <button
                        title="Chỉnh sửa"
                        onClick={() => openEditForm(user)}
                        className="rounded-lg border border-[#e8e5f5] p-2 text-[#766c91] hover:bg-[#eef0ff] hover:text-[#5865f2]"
                      >
                        <Pencil size={17} />
                      </button>
                      <button
                        title={user.status === 'LOCKED' ? 'Mở khóa' : 'Khóa tài khoản'}
                        onClick={() => toggleLock(user.id)}
                        className="rounded-lg border border-[#e8e5f5] p-2 text-[#766c91] hover:bg-amber-50 hover:text-amber-600"
                      >
                        <Ban size={17} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}

              {filteredUsers.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-[#95877c]">
                    Không tìm thấy người dùng phù hợp
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="border-t border-[#eeeaf8] px-6 py-4 text-xs text-[#95877c]">
          Dữ liệu minh họa phục vụ phát triển giao diện Frontend
        </div>
      </div>

      {/* Create / Edit modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
          <div className="w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl">
            <div className="flex items-center justify-between border-b border-[#eeeaf8] px-6 py-5">
              <h2 className="text-lg font-bold text-[#302720]">
                {editingUser ? 'Chỉnh sửa người dùng' : 'Thêm người dùng mới'}
              </h2>
              <button onClick={() => setShowForm(false)}>
                <X className="text-[#95877c]" />
              </button>
            </div>

            <form onSubmit={handleSave} className="space-y-4 p-6">
              {[
                { name: 'fullName', label: 'Họ và tên', placeholder: 'Nguyễn Văn A' },
                { name: 'username', label: 'Username', placeholder: 'nguyenvana' },
                { name: 'email', label: 'Email', placeholder: 'email@example.com' },
              ].map((field) => (
                <div key={field.name}>
                  <label className="mb-2 block text-sm font-medium text-[#302720]">
                    {field.label}
                  </label>
                  <input
                    required
                    name={field.name}
                    defaultValue={editingUser?.[field.name as keyof UserItem] as string ?? ''}
                    placeholder={field.placeholder}
                    className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2] focus:ring-2 focus:ring-[#5865f2]/10"
                  />
                </div>
              ))}

              <div>
                <label className="mb-2 block text-sm font-medium text-[#302720]">
                  Thương hiệu
                </label>
                <select
                  name="tenant"
                  defaultValue={editingUser?.tenant ?? 'ScentStation Demo'}
                  className="w-full rounded-xl border border-[#e8e5f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
                >
                  {[...new Set(users.map((user) => user.tenant))].map((tenant) => (
                    <option key={tenant} value={tenant}>{tenant}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium text-[#302720]">
                  Vai trò
                </label>
                <select
                  name="role"
                  defaultValue={editingUser?.role ?? 'OPERATIONS_MANAGER'}
                  className="w-full rounded-xl border border-[#e8e5f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
                >
                  {Object.entries(roleLabels)
                    .filter(([role]) => role !== 'PLATFORM_SUPER_ADMIN')
                    .map(([role, label]) => (
                      <option key={role} value={role}>{label}</option>
                    ))}
                </select>
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium text-[#302720]">
                  Trạng thái
                </label>
                <select
                  name="status"
                  defaultValue={editingUser?.status ?? 'ACTIVE'}
                  className="w-full rounded-xl border border-[#e8e5f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
                >
                  <option value="ACTIVE">Đang hoạt động</option>
                  <option value="LOCKED">Đã khóa</option>
                  <option value="INACTIVE">Ngừng hoạt động</option>
                </select>
              </div>

              <div className="flex justify-end gap-3 border-t border-[#eeeaf8] pt-5">
                <button
                  type="button"
                  onClick={() => setShowForm(false)}
                  className="rounded-xl border border-[#e8e5f5] px-5 py-2.5 text-sm text-[#76675c] hover:bg-[#f7f6ff]"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="rounded-xl bg-[#5865f2] px-5 py-2.5 text-sm font-semibold text-white hover:bg-[#4854df]"
                >
                  {editingUser ? 'Lưu thay đổi' : 'Thêm người dùng'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Detail modal */}
      {selectedUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
          <div className="w-full max-w-md overflow-hidden rounded-2xl bg-white shadow-2xl">
            <div className="flex items-center justify-between border-b border-[#eeeaf8] px-6 py-5">
              <h2 className="text-lg font-bold text-[#302720]">
                Chi tiết người dùng
              </h2>
              <button onClick={() => setSelectedUser(null)}>
                <X className="text-[#95877c]" />
              </button>
            </div>

            <div className="space-y-5 p-6">
              <div className="flex items-center gap-4 rounded-xl bg-[#f5f4ff] p-4">
                <div className="flex h-14 w-14 items-center justify-center rounded-full bg-white text-xl font-bold text-[#5865f2]">
                  {selectedUser.fullName.charAt(0)}
                </div>
                <div>
                  <p className="font-bold text-[#302720]">{selectedUser.fullName}</p>
                  <p className="text-sm text-[#5865f2]">@{selectedUser.username}</p>
                </div>
              </div>

              {[
                ['Email', selectedUser.email],
                ['Thương hiệu', selectedUser.tenant],
                ['Vai trò', roleLabels[selectedUser.role] ?? selectedUser.role],
                ['Ngày tạo', selectedUser.createdAt],
              ].map(([label, value]) => (
                <div key={label}>
                  <p className="text-xs text-[#95877c]">{label}</p>
                  <p className="mt-1 text-sm font-medium text-[#302720]">{value}</p>
                </div>
              ))}

              <div>
                <p className="mb-2 text-xs text-[#95877c]">Trạng thái</p>
                <span className="rounded-full bg-[#eef0ff] px-3 py-1.5 text-xs font-medium text-[#5865f2]">
                  {statusLabels[selectedUser.status]}
                </span>
              </div>
            </div>

            <div className="flex justify-end border-t border-[#eeeaf8] p-5">
              <button
                onClick={() => setSelectedUser(null)}
                className="rounded-xl bg-[#5865f2] px-6 py-2.5 text-sm font-semibold text-white hover:bg-[#4854df]"
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
