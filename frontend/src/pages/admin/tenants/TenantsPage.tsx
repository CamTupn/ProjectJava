import { useMemo, useState } from 'react'
import {
  Plus,
  Search,
  Eye,
  Pencil,
  Ban,
  CheckCircle2,
  Building2,
  Users,
  MapPin,
  Filter,
  X,
} from 'lucide-react'
import TenantForm from './TenantForm'
import type { TenantRequest } from '../../../types/tenant'

type TenantStatus = 'ACTIVE' | 'SUSPENDED' | 'INACTIVE'

interface Tenant {
  id: number
  name: string
  code: string
  email: string
  users: number
  locations: number
  status: TenantStatus
  createdAt: string
}

const initialTenants: Tenant[] = [
  {
    id: 1,
    name: 'ScentStation Demo',
    code: 'SCENT-DEMO',
    email: 'admin@scentstation.com',
    users: 12,
    locations: 4,
    status: 'ACTIVE',
    createdAt: '10/11/2026',
  },
  {
    id: 2,
    name: 'Brand Aroma',
    code: 'BRAND-AROMA',
    email: 'admin@aroma.com',
    users: 8,
    locations: 3,
    status: 'ACTIVE',
    createdAt: '08/11/2026',
  },
  {
    id: 3,
    name: 'Brand Luxury',
    code: 'BRAND-LUXURY',
    email: 'admin@luxury.com',
    users: 15,
    locations: 6,
    status: 'ACTIVE',
    createdAt: '05/11/2026',
  },
  {
    id: 4,
    name: 'Flower Perfume',
    code: 'FLOWER-PERFUME',
    email: 'contact@flower.com',
    users: 5,
    locations: 2,
    status: 'SUSPENDED',
    createdAt: '02/11/2026',
  },
  {
    id: 5,
    name: 'Oriental Scents',
    code: 'ORIENTAL-SCENTS',
    email: 'admin@oriental.com',
    users: 0,
    locations: 0,
    status: 'INACTIVE',
    createdAt: '01/11/2026',
  },
]

const statusLabels: Record<TenantStatus, string> = {
  ACTIVE: 'Đang hoạt động',
  SUSPENDED: 'Tạm khóa',
  INACTIVE: 'Ngừng hoạt động',
}

const statusStyles: Record<TenantStatus, string> = {
  ACTIVE: 'bg-emerald-50 text-emerald-700',
  SUSPENDED: 'bg-amber-50 text-amber-700',
  INACTIVE: 'bg-gray-100 text-gray-600',
}

export default function TenantsPage() {
  const [tenants, setTenants] = useState(initialTenants)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [selectedTenant, setSelectedTenant] = useState<Tenant | null>(null)
  const [viewingTenant, setViewingTenant] = useState<Tenant | null>(null)

  const handleAdd = () => {
    setSelectedTenant(null)
    setIsFormOpen(true)
  }

  const handleEdit = (tenant: Tenant) => {
    setSelectedTenant(tenant)
    setIsFormOpen(true)
  }
  const handleSubmit = (data: TenantRequest) => {
    const duplicateCode = tenants.some(
        (tenant) =>
            tenant.code.toLowerCase() === data.code.toLowerCase() &&
            tenant.id !== selectedTenant?.id,
    )
    if (duplicateCode) {
        alert('Mã Tenant đã tồn tại')
        return
    }

    if (selectedTenant) {
        setTenants((current) =>
            current.map((tenant) =>
                tenant.id === selectedTenant.id
                    ?{
                        ...tenant,
                        name: data.name,
                        code: data.code,
                        status: data.status,
                    }
                    : tenant,
            ),
        )
    } else {
        const newTenant: Tenant = {
            id: Math.max(0, ...tenants.map((tenant) => tenant.id)) + 1,
            name: data.name,
            code: data.code,
            email: 'Chưa thiết lập tài khoản quản trị',
            users: 0,
            locations: 0,
            status: data.status,
            createdAt: new Date().toLocaleDateString('vi-VN'),
        }
        setTenants((current) => [...current, newTenant])
    }
    setIsFormOpen(false)
    setSelectedTenant(null)
  }

  const filteredTenants = useMemo(() => {
    return tenants.filter((tenant) => {
      const keyword = search.toLowerCase()

      const matchesSearch =
        tenant.name.toLowerCase().includes(keyword) ||
        tenant.code.toLowerCase().includes(keyword) ||
        tenant.email.toLowerCase().includes(keyword)

      const matchesStatus =
        statusFilter === 'ALL' || tenant.status === statusFilter

      return matchesSearch && matchesStatus
    })
  }, [tenants, search, statusFilter])

  const activeCount = tenants.filter((t) => t.status === 'ACTIVE').length
  const suspendedCount = tenants.filter((t) => t.status === 'SUSPENDED').length

  const toggleStatus = (id: number) => {
    setTenants((current) =>
      current.map((tenant) => {
        if (tenant.id !== id) return tenant

        return {
          ...tenant,
          status: tenant.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE',
        }
      }),
    )
  }

  return (
    <div className="space-y-7">
      {/* Page heading */}
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Quản lý thương hiệu
          </h1>
          <p className="mt-1 text-sm text-[#95877c]">
            Quản lý các thương hiệu đang sử dụng nền tảng ScentStation
          </p>
        </div>

        <button
          onClick={handleAdd}
          className="flex items-center justify-center gap-2 rounded-xl bg-[#5865f2] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#4653df]"
        >
          <Plus size={18} />
          Thêm thương hiệu
        </button>
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-3">
        <StatCard
          title="Tổng thương hiệu"
          value={tenants.length}
          icon={<Building2 size={21} />}
          description="Tất cả thương hiệu trên hệ thống"
        />
        <StatCard
          title="Đang hoạt động"
          value={activeCount}
          icon={<CheckCircle2 size={21} />}
          description="Có thể sử dụng nền tảng"
        />
        <StatCard
          title="Tạm khóa"
          value={suspendedCount}
          icon={<Ban size={21} />}
          description="Đang bị hạn chế hoạt động"
        />
      </div>

      {/* Main table */}
      <div className="overflow-hidden rounded-2xl border border-[#e8e7f5] bg-white shadow-sm">
        {/* Search and filter */}
        <div className="flex flex-col gap-4 border-b border-[#eeeefa] p-5 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <h2 className="font-bold text-[#302720]">
              Danh sách thương hiệu
            </h2>
            <p className="mt-1 text-sm text-[#95877c]">
              Hiển thị {filteredTenants.length} / {tenants.length} thương hiệu
            </p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row">
            <div className="flex items-center gap-2 rounded-xl border border-[#e8e7f5] bg-[#f6f5ff] px-3 py-2.5 sm:w-72">
              <Search size={18} className="text-[#95877c]" />
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Tìm tên, mã, email..."
                className="w-full bg-transparent text-sm outline-none placeholder:text-[#aaa3b8]"
              />
              {search && (
                <button onClick={() => setSearch('')}>
                  <X size={16} className="text-[#95877c]" />
                </button>
              )}
            </div>

            <div className="flex items-center gap-2 rounded-xl border border-[#e8e7f5] bg-white px-3">
              <Filter size={17} className="text-[#5865f2]" />
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="bg-transparent py-2.5 text-sm text-[#514866] outline-none"
              >
                <option value="ALL">Tất cả trạng thái</option>
                <option value="ACTIVE">Đang hoạt động</option>
                <option value="SUSPENDED">Tạm khóa</option>
                <option value="INACTIVE">Ngừng hoạt động</option>
              </select>
            </div>
          </div>
        </div>

        {/* Table */}
        <div className="overflow-x-auto">
          <table className="w-full min-w-[900px] text-left text-sm">
            <thead className="bg-[#f8f7ff] text-[#817994]">
              <tr>
                <th className="px-6 py-4 font-semibold">Thương hiệu</th>
                <th className="px-6 py-4 font-semibold">Mã Tenant</th>
                <th className="px-6 py-4 font-semibold">Quy mô</th>
                <th className="px-6 py-4 font-semibold">Ngày tạo</th>
                <th className="px-6 py-4 font-semibold">Trạng thái</th>
                <th className="px-6 py-4 text-center font-semibold">Thao tác</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-[#f0eff8]">
              {filteredTenants.map((tenant) => (
                <tr
                  key={tenant.id}
                  className="transition hover:bg-[#fafaff]"
                >
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#efefff] text-[#5865f2]">
                        <Building2 size={20} />
                      </div>
                      <div>
                        <p className="font-semibold text-[#302720]">
                          {tenant.name}
                        </p>
                        <p className="mt-1 text-xs text-[#95877c]">
                          {tenant.email}
                        </p>
                      </div>
                    </div>
                  </td>

                  <td className="px-6 py-4">
                    <span className="rounded-lg bg-[#f5f4ff] px-2.5 py-1.5 text-xs font-semibold text-[#5865f2]">
                      {tenant.code}
                    </span>
                  </td>

                  <td className="px-6 py-4">
                    <div className="flex flex-col gap-1.5 text-xs text-[#766b82]">
                      <span className="flex items-center gap-2">
                        <Users size={14} />
                        {tenant.users} người dùng
                      </span>
                      <span className="flex items-center gap-2">
                        <MapPin size={14} />
                        {tenant.locations} địa điểm
                      </span>
                    </div>
                  </td>

                  <td className="px-6 py-4 text-[#766b82]">
                    {tenant.createdAt}
                  </td>

                  <td className="px-6 py-4">
                    <span className={`inline-flex rounded-full px-3 py-1.5 text-xs font-semibold ${statusStyles[tenant.status]}`}>
                      {statusLabels[tenant.status]}
                    </span>
                  </td>

                  <td className="px-6 py-4">
                    <div className="flex items-center justify-center gap-2">
                      <ActionButton
                        title="Xem chi tiết"
                        onClick={() => setViewingTenant(tenant)}
                      >
                        <Eye size={17} />
                      </ActionButton>
                      <ActionButton
                        title="Chỉnh sửa"
                        onClick={() => handleEdit(tenant)}
                      >
                        <Pencil size={17} />
                      </ActionButton>
                      <ActionButton
                        title={tenant.status === 'ACTIVE' ? 'Tạm khóa' : 'Kích hoạt'}
                        onClick={() => toggleStatus(tenant.id)}
                      >
                        <Ban size={17} />
                      </ActionButton>
                    </div>
                  </td>
                </tr>
              ))}

              {filteredTenants.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center">
                    <Building2 size={32} className="mx-auto text-[#b9b4cc]" />
                    <p className="mt-3 font-semibold text-[#514866]">
                      Không tìm thấy thương hiệu
                    </p>
                    <p className="mt-1 text-sm text-[#95877c]">
                      Thử thay đổi từ khóa hoặc bộ lọc trạng thái.
                    </p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="border-t border-[#eeeefa] px-6 py-4 text-xs text-[#95877c]">
          Dữ liệu minh họa phục vụ phát triển giao diện Frontend
        </div>
      </div>
      {isFormOpen && (
        <TenantForm
          initialData={selectedTenant}
          onClose={() => {
            setIsFormOpen(false)
            setSelectedTenant(null)
          }}
          onSubmit={handleSubmit}
        />
      )}

      {viewingTenant && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
            <div className="w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl">

                <div className="flex items-center justify-between border-b border-[#e8e7f5] px-6 py-5">
                    <div className="flex items-center gap-3">
                        <div className="rounded-xl bg-[#efefff] p-3 text-[#5865f2]">
                            <Building2 size={22} />
                        </div>
                        <div>
                            <h2 className="text-lg font-bold text-[#302720]">
                                Chi tiết thương hiệu
                            </h2>
                            <p className="text-sm text-[#95877c]">
                                Thông tin thương hiệu trên hệ thống
                            </p>
                        </div>
                    </div>

                    <button
                        onClick={() => setViewingTenant(null)}
                        className="rounded-lg p-2 text-[#95877c] hover:bg-[#efefff] hover:text-[#5865f2]"
                    >
                        <X size={20} />
                    </button>
                </div>

                <div className="space-y-5 p-6">
                    <div className="flex items-center gap-4 rounded-xl bg-[#f6f5ff] p-4">
                        <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-white text-[#5865f2]">
                            <Building2 size={28} />
                        </div>
                        <div>
                            <h3 className="font-bold text-[#302720]">
                                {viewingTenant.name}
                            </h3>
                            <p className="mt-1 text-sm text-[#5865f2]">
                                {viewingTenant.code}
                            </p>
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <div className="rounded-xl border border-[#e8e7f5] p-4">
                            <Users size={20} className="mb-2 text-[#5865f2]" />
                            <p className="text-xs text-[#95877c]">Người dùng</p>
                            <p className="mt-1 text-xl font-bold text-[#302720]">
                                {viewingTenant.users}
                            </p>
                        </div>
                        <div className="rounded-xl border border-[#e8e7f5] p-4">
                            <MapPin size={20} className="mb-2 text-[#5865f2]" />
                            <p className="text-xs text-[#95877c]">Địa điểm</p>
                            <p className="mt-1 text-xl font-bold text-[#302720]">
                                {viewingTenant.locations}
                            </p>
                        </div>
                    </div>

                    <div className="space-y-4">
                        <div>
                            <p className="text-xs text-[#95877c]">Email quản trị</p>
                            <p className="mt-1 text-sm font-medium text-[#302720]">
                                {viewingTenant.email}
                            </p>
                        </div>
                        <div>
                            <p className="text-xs text-[#95877c]">Ngày tạo</p>
                            <p className="mt-1 text-sm font-medium text-[#302720]">
                                {viewingTenant.createdAt}
                            </p>
                        </div>
                        <div>
                            <p className="text-xs text-[#95877c]">Trạng thái</p>
                            <span className={`mt-2 inline-flex rounded-full px-3 py-1.5 text-xs font-semibold ${statusStyles[viewingTenant.status]}`}>
                                {statusLabels[viewingTenant.status]}
                            </span>
                        </div>
                    </div>
                </div>

                <div className="flex justify-end border-t border-[#e8e7f5] px-6 py-4">
                    <button
                        onClick={() => setViewingTenant(null)}
                        className="rounded-xl bg-[#5865f2] px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-[#4854df]"
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

function StatCard({
  title,
  value,
  icon,
  description,
}: {
  title: string
  value: number
  icon: React.ReactNode
  description: string
}) {
  return (
    <div className="rounded-2xl border border-[#e8e7f5] bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between">
        <p className="text-sm text-[#95877c]">{title}</p>
        <div className="rounded-xl bg-[#efefff] p-3 text-[#5865f2]">
          {icon}
        </div>
      </div>
      <p className="mt-4 text-3xl font-bold text-[#302720]">{value}</p>
      <p className="mt-2 text-xs text-[#95877c]">{description}</p>
    </div>
  )
}

function ActionButton({
  children,
  title,
  onClick,
}: {
  children: React.ReactNode
  title: string
  onClick: () => void
}) {
  return (
    <button
      title={title}
      onClick={onClick}
      className="rounded-lg border border-[#e8e7f5] p-2 text-[#77718c] transition hover:border-[#5865f2] hover:bg-[#efefff] hover:text-[#5865f2]"
    >
      {children}
    </button>
  )
}
