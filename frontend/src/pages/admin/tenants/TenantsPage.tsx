import { useEffect, useMemo, useState, type ReactNode } from 'react'
import {
  Plus,
  Search,
  Eye,
  Pencil,
  Building2,
  CheckCircle2,
  Ban,
  Filter,
  X,
  RefreshCw,
} from 'lucide-react'

import TenantForm from './TenantForm'
import { tenantService } from '../../../services/tenantService'
import { getApiErrorMessage } from '../../../services/apiClient'
import type {
  Tenant,
  TenantRequest,
  TenantStatus,
} from '../../../types/tenant'

const statusLabels: Record<TenantStatus, string> = {
  ACTIVE: 'Đang hoạt động / Active',
  SUSPENDED: 'Tạm khóa / Suspended',
  INACTIVE: 'Ngừng hoạt động / Inactive',
}

const statusStyles: Record<TenantStatus, string> = {
  ACTIVE: 'bg-emerald-50 text-emerald-700',
  SUSPENDED: 'bg-amber-50 text-amber-700',
  INACTIVE: 'bg-gray-100 text-gray-600',
}

function formatDate(value: string) {
  if (!value) return '—'

  const date = new Date(value)

  return Number.isNaN(date.getTime())
    ? value
    : date.toLocaleDateString('vi-VN')
}

export default function TenantsPage() {
  const [tenants, setTenants] = useState<Tenant[]>([])
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [isFormOpen, setIsFormOpen] = useState(false)

  const [selectedTenant, setSelectedTenant] =
    useState<Tenant | null>(null)

  const [viewingTenant, setViewingTenant] =
    useState<Tenant | null>(null)

  const loadTenants = async () => {
    setLoading(true)
    setError('')

    try {
      const data = await tenantService.getAll()
      setTenants(data)
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    let cancelled = false

    tenantService.getAll()
      .then((data) => {
        if (!cancelled) setTenants(data)
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const handleAdd = () => {
    setSuccess('')
    setSelectedTenant(null)
    setIsFormOpen(true)
  }

  const handleEdit = (tenant: Tenant) => {
    setSuccess('')
    setSelectedTenant(tenant)
    setIsFormOpen(true)
  }

  const handleView = async (id: number) => {
    setError('')

    try {
      const tenant = await tenantService.getById(id)
      setViewingTenant(tenant)
    } catch (err) {
      setError(getApiErrorMessage(err))
    }
  }

  const handleSubmit = async (data: TenantRequest) => {
    if (selectedTenant) {
      const updated = await tenantService.update(
        selectedTenant.id,
        data,
      )

      setTenants((current) =>
        current.map((tenant) =>
          tenant.id === updated.id ? updated : tenant,
        ),
      )

      setSuccess('Cập nhật thương hiệu thành công.')
    } else {
      const created = await tenantService.create(data)

      setTenants((current) => [...current, created])

      setSuccess('Tạo thương hiệu thành công.')
    }

    setIsFormOpen(false)
    setSelectedTenant(null)
  }

  const filteredTenants = useMemo(() => {
    const keyword = search.trim().toLowerCase()

    return tenants.filter((tenant) => {
      const matchesSearch =
        tenant.name.toLowerCase().includes(keyword) ||
        tenant.code.toLowerCase().includes(keyword)

      const matchesStatus =
        statusFilter === 'ALL' ||
        tenant.status === statusFilter

      return matchesSearch && matchesStatus
    })
  }, [tenants, search, statusFilter])

  const activeCount = tenants.filter(
    (tenant) => tenant.status === 'ACTIVE',
  ).length

  const suspendedCount = tenants.filter(
    (tenant) => tenant.status === 'SUSPENDED',
  ).length

  const inactiveCount = tenants.filter(
    (tenant) => tenant.status === 'INACTIVE',
  ).length

  return (
    <div className="space-y-6">
      {/* PAGE HEADER */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Quản lý thương hiệu
          </h1>
          <p className="mt-1 text-sm text-[#95877c]">
            Tenant Management
          </p>
        </div>

        <button
          onClick={handleAdd}
          disabled={loading}
          className="flex items-center gap-2 rounded-xl bg-[#5865f2] px-5 py-3 text-sm font-semibold text-white shadow-sm hover:bg-[#4854df] disabled:opacity-50"
        >
          <Plus size={18} />
          Thêm thương hiệu
        </button>
      </div>

      {/* STATISTICS */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Tổng thương hiệu"
          description="Total tenants"
          value={loading || error ? '—' : tenants.length}
          icon={<Building2 size={22} />}
        />

        <StatCard
          title="Đang hoạt động"
          description="Active tenants"
          value={loading || error ? '—' : activeCount}
          icon={<CheckCircle2 size={22} />}
        />

        <StatCard
          title="Tạm khóa"
          description="Suspended tenants"
          value={loading || error ? '—' : suspendedCount}
          icon={<Ban size={22} />}
        />

        <StatCard
          title="Ngừng hoạt động"
          description="Inactive tenants"
          value={loading || error ? '—' : inactiveCount}
          icon={<Building2 size={22} />}
        />
      </div>

      {/* NOTIFICATIONS */}
      {success && (
        <div className="flex items-center justify-between rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-700">
          <span role="status">{success}</span>

          <button onClick={() => setSuccess('')} aria-label="Đóng thông báo">
            <X size={18} />
          </button>
        </div>
      )}

      {error && (
        <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-600">
          {error}

          <button
            onClick={loadTenants}
            className="ml-3 font-semibold underline"
          >
            Thử lại
          </button>
        </div>
      )}

      {/* TABLE */}
      <div className="overflow-hidden rounded-2xl border border-[#e8e7f5] bg-white shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-4 border-b border-[#eeeefa] p-5">
          <div>
            <h2 className="font-bold text-[#302720]">
              Danh sách thương hiệu
            </h2>

            <p className="mt-1 text-xs text-[#95877c]">
              Tenant list
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <div className="relative">
              <Search
                size={18}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-[#95877c]"
              />

              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Tìm tên hoặc mã Tenant..."
                className="rounded-xl border border-[#e8e7f5] py-2.5 pl-10 pr-4 text-sm outline-none focus:border-[#5865f2]"
              />
            </div>

            <div className="flex items-center gap-2 rounded-xl border border-[#e8e7f5] px-3">
              <Filter size={16} className="text-[#95877c]" />

              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="bg-white py-2.5 text-sm outline-none"
              >
                <option value="ALL">Tất cả trạng thái</option>
                <option value="ACTIVE">Đang hoạt động</option>
                <option value="SUSPENDED">Tạm khóa</option>
                <option value="INACTIVE">Ngừng hoạt động</option>
              </select>
            </div>

            <button
              onClick={loadTenants}
              disabled={loading}
              title="Làm mới dữ liệu"
              className="rounded-xl border border-[#e8e7f5] p-2.5 text-[#5865f2] hover:bg-[#efefff] disabled:opacity-50"
            >
              <RefreshCw
                size={18}
                className={loading ? 'animate-spin' : ''}
              />
            </button>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[760px] text-left text-sm">
            <thead className="bg-[#f8f7fc] text-xs text-[#77718c]">
              <tr>
                <th className="px-6 py-4">Thương hiệu</th>
                <th className="px-6 py-4">Mã Tenant</th>
                <th className="px-6 py-4">Trạng thái</th>
                <th className="px-6 py-4">Ngày tạo</th>
                <th className="px-6 py-4 text-center">
                  Thao tác
                </th>
              </tr>
            </thead>

            <tbody className="divide-y divide-[#eeeefa]">
              {!loading && filteredTenants.map((tenant) => (
                <tr
                  key={tenant.id}
                  className="transition hover:bg-[#faf9ff]"
                >
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="rounded-xl bg-[#efefff] p-3 text-[#5865f2]">
                        <Building2 size={20} />
                      </div>

                      <div>
                        <p className="font-semibold text-[#302720]">
                          {tenant.name}
                        </p>

                        <p className="mt-1 text-xs text-[#95877c]">
                          ID: {tenant.id}
                        </p>
                      </div>
                    </div>
                  </td>

                  <td className="px-6 py-4 font-medium text-[#5865f2]">
                    {tenant.code}
                  </td>

                  <td className="px-6 py-4">
                    <StatusBadge status={tenant.status} />
                  </td>

                  <td className="px-6 py-4 text-[#76675c]">
                    {formatDate(tenant.createdAt)}
                  </td>

                  <td className="px-6 py-4">
                    <div className="flex justify-center gap-2">
                      <ActionButton
                        title="Xem chi tiết"
                        onClick={() => handleView(tenant.id)}
                      >
                        <Eye size={17} />
                      </ActionButton>

                      <ActionButton
                        title="Chỉnh sửa"
                        onClick={() => handleEdit(tenant)}
                      >
                        <Pencil size={17} />
                      </ActionButton>
                    </div>
                  </td>
                </tr>
              ))}

              {loading && (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-slate-500">
                    <RefreshCw
                      size={24}
                      className="mx-auto mb-3 animate-spin text-[#5865f2]"
                    />
                    Đang tải dữ liệu...
                  </td>
                </tr>
              )}

              {!loading && !error && filteredTenants.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center">
                    <Building2 size={32} className="mx-auto text-[#b9b4cc]" />

                    <p className="mt-3 font-semibold text-[#514866]">
                      Không tìm thấy thương hiệu
                    </p>

                    <p className="mt-1 text-sm text-[#95877c]">
                      Thử thay đổi từ khóa hoặc bộ lọc.
                    </p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="flex flex-wrap justify-between gap-2 border-t border-[#eeeefa] px-6 py-4 text-xs text-[#95877c]">
          <span>
            Hiển thị {filteredTenants.length}/{tenants.length} thương hiệu
          </span>

          <span>
            Nguồn dữ liệu: ScentStation API
          </span>
        </div>
      </div>

      {/* CREATE / UPDATE FORM */}
      {isFormOpen && (
        <TenantForm
          key={selectedTenant?.id ?? 'new'}
          initialData={selectedTenant}
          onClose={() => {
            setIsFormOpen(false)
            setSelectedTenant(null)
          }}
          onSubmit={handleSubmit}
        />
      )}

      {/* DETAIL MODAL */}
      {viewingTenant && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
          <div
            role="dialog"
            aria-modal="true"
            aria-labelledby="tenant-detail-title"
            className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl bg-white shadow-2xl"
          >
            <div className="flex items-center justify-between border-b border-[#e8e7f5] px-6 py-5">
              <div className="flex items-center gap-3">
                <div className="rounded-xl bg-[#efefff] p-3 text-[#5865f2]">
                  <Building2 size={22} />
                </div>

                <div>
                  <h2 id="tenant-detail-title" className="text-lg font-bold text-[#302720]">
                    Chi tiết thương hiệu
                  </h2>

                  <p className="text-xs text-[#95877c]">
                    Tenant details
                  </p>
                </div>
              </div>

              <button
                onClick={() => setViewingTenant(null)}
                aria-label="Đóng"
                className="rounded-lg p-2 text-[#95877c] hover:bg-[#efefff]"
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

              <DetailItem
                label="Tenant ID"
                value={viewingTenant.id}
              />

              <DetailItem
                label="Ngày tạo / Created at"
                value={formatDate(viewingTenant.createdAt)}
              />

              <DetailItem
                label="Cập nhật lần cuối / Updated at"
                value={formatDate(viewingTenant.updatedAt)}
              />

              <div>
                <p className="mb-2 text-xs text-[#95877c]">
                  Trạng thái / Status
                </p>

                <StatusBadge status={viewingTenant.status} />
              </div>
            </div>

            <div className="flex justify-end border-t border-[#e8e7f5] px-6 py-4">
              <button
                onClick={() => setViewingTenant(null)}
                className="rounded-xl bg-[#5865f2] px-5 py-2.5 text-sm font-semibold text-white hover:bg-[#4854df]"
              >
                Đóng / Close
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
  value: number | string
  icon: ReactNode
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

      <p className="mt-4 text-3xl font-bold text-[#302720]">
        {value}
      </p>

      <p className="mt-2 text-xs text-[#95877c]">
        {description}
      </p>
    </div>
  )
}

function StatusBadge({ status }: { status: TenantStatus }) {
  return (
    <span className={`inline-flex rounded-full px-3 py-1.5 text-xs font-semibold ${statusStyles[status] ?? 'bg-gray-100 text-gray-600'}`}>
      {statusLabels[status] ?? status}
    </span>
  )
}

function ActionButton({
  children,
  title,
  onClick,
}: {
  children: ReactNode
  title: string
  onClick: () => void
}) {
  return (
    <button
      type="button"
      title={title}
      aria-label={title}
      onClick={onClick}
      className="rounded-lg border border-[#e8e7f5] p-2 text-[#77718c] transition hover:border-[#5865f2] hover:bg-[#efefff] hover:text-[#5865f2]"
    >
      {children}
    </button>
  )
}

function DetailItem({
  label,
  value,
}: {
  label: string
  value: string | number
}) {
  return (
    <div>
      <p className="text-xs text-[#95877c]">
        {label}
      </p>

      <p className="mt-1 text-sm font-medium text-[#302720]">
        {value}
      </p>
    </div>
  )
}
