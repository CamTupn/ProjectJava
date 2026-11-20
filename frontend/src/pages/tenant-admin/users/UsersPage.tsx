import { useMemo, useState } from 'react'
import {
  Search,
  Plus,
  Eye,
  Pencil,
  Users,
  ShieldCheck,
  UserCheck,
  UserX,
  X,
  Lock,
  Unlock,
} from 'lucide-react'

type Role = 'BRAND_ADMIN' | 'STAFF'
type Status = 'ACTIVE' | 'LOCKED'

interface User {
  id: number
  fullName: string
  email: string
  phone: string
  role: Role
  status: Status
  createdAt: string
}

const initialUsers: User[] = [
  {
    id: 1,
    fullName: 'Nguyễn Minh Anh',
    email: 'minhanh@sc entstation.vn'.replace(' ', ''),
    phone: '0901234567',
    role: 'BRAND_ADMIN',
    status: 'ACTIVE',
    createdAt: '01/11/2026',
  },
  {
    id: 2,
    fullName: 'Trần Quốc Bảo',
    email: 'quocbao@scentstation.vn',
    phone: '0912345678',
    role: 'STAFF',
    status: 'ACTIVE',
    createdAt: '03/11/2026',
  },
  {
    id: 3,
    fullName: 'Lê Hoàng Yến',
    email: 'hoangyen@scentstation.vn',
    phone: '0934567890',
    role: 'STAFF',
    status: 'ACTIVE',
    createdAt: '05/11/2026',
  },
  {
    id: 4,
    fullName: 'Phạm Gia Huy',
    email: 'giahuy@scentstation.vn',
    phone: '0945678901',
    role: 'STAFF',
    status: 'LOCKED',
    createdAt: '08/11/2026',
  },
]

const emptyForm = {
  fullName: '',
  email: '',
  phone: '',
  role: 'STAFF' as Role,
}

function RoleBadge({ role }: { role: Role }) {
  return (
    <span className={`rounded-full px-3 py-1 text-xs font-medium ${
      role === 'BRAND_ADMIN'
        ? 'bg-purple-50 text-purple-700'
        : 'bg-blue-50 text-blue-700'
    }`}>
      {role === 'BRAND_ADMIN' ? 'Quản trị viên' : 'Nhân viên'}
    </span>
  )
}

function StatusBadge({ status }: { status: Status }) {
  return (
    <span className={`rounded-full px-3 py-1 text-xs font-medium ${
      status === 'ACTIVE'
        ? 'bg-green-50 text-green-700'
        : 'bg-red-50 text-red-600'
    }`}>
      {status === 'ACTIVE' ? 'Đang hoạt động' : 'Đã khóa'}
    </span>
  )
}

export default function UsersPage() {
  const [users, setUsers] = useState<User[]>(initialUsers)
  const [search, setSearch] = useState('')
  const [roleFilter, setRoleFilter] = useState('ALL')
  const [statusFilter, setStatusFilter] = useState('ALL')

  const [showForm, setShowForm] = useState(false)
  const [selectedUser, setSelectedUser] = useState<User | null>(null)
  const [editingUser, setEditingUser] = useState<User | null>(null)
  const [form, setForm] = useState(emptyForm)

  const filteredUsers = useMemo(() => {
    return users.filter((user) => {
      const keyword = search.toLowerCase()

      const matchesSearch =
        user.fullName.toLowerCase().includes(keyword) ||
        user.email.toLowerCase().includes(keyword) ||
        user.phone.includes(keyword)

      const matchesRole =
        roleFilter === 'ALL' || user.role === roleFilter

      const matchesStatus =
        statusFilter === 'ALL' || user.status === statusFilter

      return matchesSearch && matchesRole && matchesStatus
    })
  }, [users, search, roleFilter, statusFilter])

  const activeCount = users.filter((u) => u.status === 'ACTIVE').length
  const lockedCount = users.filter((u) => u.status === 'LOCKED').length
  const adminCount = users.filter((u) => u.role === 'BRAND_ADMIN').length

  const openAddForm = () => {
    setEditingUser(null)
    setForm(emptyForm)
    setShowForm(true)
  }

  const openEditForm = (user: User) => {
    setEditingUser(user)
    setForm({
      fullName: user.fullName,
      email: user.email,
      phone: user.phone,
      role: user.role,
    })
    setShowForm(true)
  }

  const handleSave = () => {
    if (!form.fullName.trim() || !form.email.trim() || !form.phone.trim()) {
      alert('Vui lòng nhập đầy đủ thông tin.')
      return
    }

    const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)
    if (!emailValid) {
      alert('Email không hợp lệ.')
      return
    }

    if (!/^0\d{9}$/.test(form.phone)) {
      alert('Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.')
      return
    }

    const duplicateEmail = users.some(
      (u) =>
        u.email.toLowerCase() === form.email.trim().toLowerCase() &&
        u.id !== editingUser?.id
    )

    if (duplicateEmail) {
      alert('Email này đã được sử dụng.')
      return
    }

    if (editingUser) {
      setUsers((prev) =>
        prev.map((u) =>
          u.id === editingUser.id
            ? { ...u, ...form, fullName: form.fullName.trim(), email: form.email.trim() }
            : u
        )
      )
    } else {
      const newUser: User = {
        id: Math.max(0, ...users.map((u) => u.id)) + 1,
        ...form,
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        status: 'ACTIVE',
        createdAt: new Date().toLocaleDateString('vi-VN'),
      }
      setUsers((prev) => [newUser, ...prev])
    }

    setShowForm(false)
    setForm(emptyForm)
    setEditingUser(null)
  }

  const toggleStatus = (user: User) => {
    const nextStatus: Status =
      user.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE'

    const action = nextStatus === 'LOCKED' ? 'khóa' : 'mở khóa'

    if (!window.confirm(`Bạn có chắc muốn ${action} tài khoản ${user.fullName}?`)) {
      return
    }

    setUsers((prev) =>
      prev.map((u) =>
        u.id === user.id ? { ...u, status: nextStatus } : u
      )
    )
  }

  return (
    <div className="min-h-full space-y-6 bg-[#f0efff] p-6">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Quản lý người dùng
          </h1>
          <p className="mt-1 text-sm text-[#95877c]">
            Quản lý tài khoản và quyền truy cập của nhân sự thuộc thương hiệu
          </p>
        </div>

        <button
          onClick={openAddForm}
          className="flex items-center gap-2 rounded-xl bg-[#5145e5] px-5 py-3 text-sm font-semibold text-white shadow-sm hover:bg-[#4338ca]"
        >
          <Plus size={18} />
          Thêm người dùng
        </button>
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Tổng người dùng"
          value={users.length}
          icon={<Users size={22} />}
          color="text-indigo-600"
          bg="bg-indigo-50"
        />
        <StatCard
          title="Đang hoạt động"
          value={activeCount}
          icon={<UserCheck size={22} />}
          color="text-green-600"
          bg="bg-green-50"
        />
        <StatCard
          title="Đã khóa"
          value={lockedCount}
          icon={<UserX size={22} />}
          color="text-red-600"
          bg="bg-red-50"
        />
        <StatCard
          title="Quản trị viên"
          value={adminCount}
          icon={<ShieldCheck size={22} />}
          color="text-purple-600"
          bg="bg-purple-50"
        />
      </div>

      {/* User list */}
      <section className="overflow-hidden rounded-2xl border border-[#e5e4f5] bg-white shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-4 border-b border-[#eeedf6] p-5">
          <div>
            <h2 className="font-bold text-[#302720]">Danh sách người dùng</h2>
            <p className="mt-1 text-sm text-[#95877c]">
              Hiển thị {filteredUsers.length} / {users.length} tài khoản
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            <div className="flex items-center gap-2 rounded-xl border border-[#e5e4f5] bg-[#f8f7ff] px-3">
              <Search size={17} className="text-[#95877c]" />
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Tìm tên, email, số điện thoại..."
                className="w-52 bg-transparent py-3 text-sm outline-none"
              />
            </div>

            <select
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
              className="rounded-xl border border-[#e5e4f5] px-3 py-3 text-sm outline-none"
            >
              <option value="ALL">Tất cả vai trò</option>
              <option value="BRAND_ADMIN">Quản trị viên</option>
              <option value="STAFF">Nhân viên</option>
            </select>

            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="rounded-xl border border-[#e5e4f5] px-3 py-3 text-sm outline-none"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="ACTIVE">Đang hoạt động</option>
              <option value="LOCKED">Đã khóa</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[950px] text-left">
            <thead className="bg-[#f5f4ff] text-sm text-[#5d5791]">
              <tr>
                <th className="px-5 py-4 font-semibold">Người dùng</th>
                <th className="px-5 py-4 font-semibold">Email</th>
                <th className="px-5 py-4 font-semibold">Số điện thoại</th>
                <th className="px-5 py-4 font-semibold">Vai trò</th>
                <th className="px-5 py-4 font-semibold">Trạng thái</th>
                <th className="px-5 py-4 font-semibold">Ngày tạo</th>
                <th className="px-5 py-4 text-center font-semibold">Thao tác</th>
              </tr>
            </thead>

            <tbody>
              {filteredUsers.map((user) => (
                <tr
                  key={user.id}
                  className="border-t border-[#eeedf6] hover:bg-[#faf9ff]"
                >
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[#eef0ff] font-semibold text-[#5865f2]">
                        {user.fullName.charAt(0)}
                      </div>
                      <div>
                        <p className="font-semibold text-[#302720]">
                          {user.fullName}
                        </p>
                        <p className="text-xs text-[#95877c]">ID: {user.id}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-4 text-sm">{user.email}</td>
                  <td className="px-5 py-4 text-sm">{user.phone}</td>
                  <td className="px-5 py-4">
                    <RoleBadge role={user.role} />
                  </td>
                  <td className="px-5 py-4">
                    <StatusBadge status={user.status} />
                  </td>
                  <td className="px-5 py-4 text-sm text-[#95877c]">
                    {user.createdAt}
                  </td>
                  <td className="px-5 py-4">
                    <div className="flex items-center justify-center gap-2">
                      <IconButton
                        title="Xem chi tiết"
                        onClick={() => setSelectedUser(user)}
                      >
                        <Eye size={17} />
                      </IconButton>
                      <IconButton
                        title="Chỉnh sửa"
                        onClick={() => openEditForm(user)}
                      >
                        <Pencil size={16} />
                      </IconButton>
                      <IconButton
                        title={user.status === 'ACTIVE' ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}
                        danger={user.status === 'ACTIVE'}
                        onClick={() => toggleStatus(user)}
                      >
                        {user.status === 'ACTIVE'
                          ? <Lock size={16} />
                          : <Unlock size={16} />}
                      </IconButton>
                    </div>
                  </td>
                </tr>
              ))}

              {filteredUsers.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-5 py-12 text-center text-sm text-[#95877c]">
                    Không tìm thấy người dùng phù hợp.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      {/* Add/Edit Modal */}
      {showForm && (
        <Modal onClose={() => setShowForm(false)}>
          <div className="mb-5 flex items-center justify-between">
            <div>
              <h2 className="text-xl font-bold text-[#302720]">
                {editingUser ? 'Chỉnh sửa người dùng' : 'Thêm người dùng'}
              </h2>
              <p className="mt-1 text-sm text-[#95877c]">
                Nhập thông tin tài khoản thuộc thương hiệu
              </p>
            </div>
            <button onClick={() => setShowForm(false)}>
              <X className="text-gray-500" />
            </button>
          </div>

          <div className="space-y-4">
            <FormField label="Họ và tên">
              <input
                value={form.fullName}
                onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                placeholder="Nhập họ và tên"
                className="form-input"
              />
            </FormField>

            <FormField label="Email">
              <input
                type="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                placeholder="example@email.com"
                className="form-input"
              />
            </FormField>

            <FormField label="Số điện thoại">
              <input
                value={form.phone}
                onChange={(e) => setForm({ ...form, phone: e.target.value })}
                placeholder="0901234567"
                className="form-input"
              />
            </FormField>

            <FormField label="Vai trò">
              <select
                value={form.role}
                onChange={(e) => setForm({ ...form, role: e.target.value as Role })}
                className="form-input"
              >
                <option value="STAFF">Nhân viên</option>
                <option value="BRAND_ADMIN">Quản trị viên</option>
              </select>
            </FormField>
          </div>

          <div className="mt-6 flex justify-end gap-3">
            <button
              onClick={() => setShowForm(false)}
              className="rounded-xl border border-[#e5e4f5] px-5 py-2.5 text-sm"
            >
              Hủy
            </button>
            <button
              onClick={handleSave}
              className="rounded-xl bg-[#5145e5] px-5 py-2.5 text-sm font-semibold text-white"
            >
              {editingUser ? 'Lưu thay đổi' : 'Tạo tài khoản'}
            </button>
          </div>
        </Modal>
      )}

      {/* Detail Modal */}
      {selectedUser && (
        <Modal onClose={() => setSelectedUser(null)}>
          <div className="mb-5 flex items-center justify-between">
            <h2 className="text-xl font-bold text-[#302720]">
              Chi tiết người dùng
            </h2>
            <button onClick={() => setSelectedUser(null)}>
              <X className="text-gray-500" />
            </button>
          </div>

          <div className="mb-6 flex items-center gap-4 rounded-xl bg-[#f6f5ff] p-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-full bg-[#e9eaff] text-xl font-bold text-[#5865f2]">
              {selectedUser.fullName.charAt(0)}
            </div>
            <div>
              <h3 className="font-bold text-[#302720]">
                {selectedUser.fullName}
              </h3>
              <p className="mt-1 text-sm text-[#95877c]">
                Mã người dùng: #{selectedUser.id}
              </p>
            </div>
          </div>

          <div className="space-y-4">
            <DetailRow label="Email" value={selectedUser.email} />
            <DetailRow label="Số điện thoại" value={selectedUser.phone} />
            <div className="flex justify-between gap-4">
              <span className="text-sm text-[#95877c]">Vai trò</span>
              <RoleBadge role={selectedUser.role} />
            </div>
            <div className="flex justify-between gap-4">
              <span className="text-sm text-[#95877c]">Trạng thái</span>
              <StatusBadge status={selectedUser.status} />
            </div>
            <DetailRow label="Ngày tạo" value={selectedUser.createdAt} />
          </div>

          <button
            onClick={() => setSelectedUser(null)}
            className="mt-6 w-full rounded-xl bg-[#5145e5] py-3 text-sm font-semibold text-white"
          >
            Đóng
          </button>
        </Modal>
      )}
    </div>
  )
}

function StatCard({
  title,
  value,
  icon,
  color,
  bg,
}: {
  title: string
  value: number
  icon: React.ReactNode
  color: string
  bg: string
}) {
  return (
    <div className="rounded-2xl border border-[#e5e4f5] bg-white p-6 shadow-sm">
      <div className="flex items-center justify-between">
        <p className="text-sm text-[#95877c]">{title}</p>
        <div className={`rounded-xl p-3 ${bg} ${color}`}>{icon}</div>
      </div>
      <p className={`mt-4 text-3xl font-bold ${color}`}>{value}</p>
    </div>
  )
}

function IconButton({
  children,
  title,
  onClick,
  danger = false,
}: {
  children: React.ReactNode
  title: string
  onClick: () => void
  danger?: boolean
}) {
  return (
    <button
      title={title}
      onClick={onClick}
      className={`rounded-lg border p-2 transition ${
        danger
          ? 'border-red-100 text-red-500 hover:bg-red-50'
          : 'border-[#e5e4f5] text-[#5865f2] hover:bg-[#f0efff]'
      }`}
    >
      {children}
    </button>
  )
}

function Modal({
  children,
  onClose,
}: {
  children: React.ReactNode
  onClose: () => void
}) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onClose()
      }}
    >
      <div className="max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-2xl bg-white p-6 shadow-xl">
        {children}
      </div>
    </div>
  )
}

function FormField({
  label,
  children,
}: {
  label: string
  children: React.ReactNode
}) {
  return (
    <label className="block space-y-2">
      <span className="text-sm font-medium text-[#302720]">{label}</span>
      {children}
    </label>
  )
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-4 border-b border-[#f0eff7] pb-3">
      <span className="text-sm text-[#95877c]">{label}</span>
      <span className="break-all text-right text-sm font-medium text-[#302720]">
        {value}
      </span>
    </div>
  )
}
