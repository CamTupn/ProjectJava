import { useMemo, useState } from 'react'
import {
  Plus,
  Search,
  Eye,
  Pencil,
  Ban,
  CheckCircle2,
  MapPin,
  Filter,
  X,
} from 'lucide-react'
import LocationForm from './LocationForm'
import LocationDetail from './LocationDetail'

export type LocationStatus = 'ACTIVE' | 'INACTIVE'

export interface Location {
  id: number
  name: string
  tenant: string
  address: string
  description: string
  status: LocationStatus
  createdAt: string
}

export interface LocationRequest {
  name: string
  tenant: string
  address: string
  description: string
  status: LocationStatus
}

const initialLocations: Location[] = [
  {
    id: 1,
    name: 'Vincom Center',
    tenant: 'ScentStation Demo',
    address: '72 Lê Thánh Tôn, Quận 1, TP. Hồ Chí Minh',
    description: 'Khu vực đặt máy nước hoa tại trung tâm thương mại',
    status: 'ACTIVE',
    createdAt: '10/11/2026',
  },
  {
    id: 2,
    name: 'Aeon Mall Tân Phú',
    tenant: 'ScentStation Demo',
    address: '30 Bờ Bao Tân Thắng, Tân Phú, TP. Hồ Chí Minh',
    description: 'Địa điểm kinh doanh và trải nghiệm nước hoa',
    status: 'ACTIVE',
    createdAt: '11/11/2026',
  },
  {
    id: 3,
    name: 'Landmark 81',
    tenant: 'Brand Aroma',
    address: '720A Điện Biên Phủ, Bình Thạnh, TP. Hồ Chí Minh',
    description: 'Khu vực trải nghiệm sản phẩm Aroma',
    status: 'ACTIVE',
    createdAt: '12/11/2026',
  },
  {
    id: 4,
    name: 'Crescent Mall',
    tenant: 'Brand Aroma',
    address: '101 Tôn Dật Tiên, Quận 7, TP. Hồ Chí Minh',
    description: 'Điểm đặt máy tại trung tâm thương mại',
    status: 'INACTIVE',
    createdAt: '13/11/2026',
  },
  {
    id: 5,
    name: 'Takashimaya',
    tenant: 'Brand Luxury',
    address: '92-94 Nam Kỳ Khởi Nghĩa, Quận 1, TP. Hồ Chí Minh',
    description: 'Địa điểm trưng bày dòng nước hoa cao cấp',
    status: 'ACTIVE',
    createdAt: '14/11/2026',
  },
  {
    id: 6,
    name: 'Gigamall',
    tenant: 'Flower Perfume',
    address: '240-242 Phạm Văn Đồng, TP. Thủ Đức, TP. Hồ Chí Minh',
    description: 'Khu vực phục vụ khách hàng Flower Perfume',
    status: 'ACTIVE',
    createdAt: '15/11/2026',
  },
]

const statusLabels: Record<LocationStatus, string> = {
  ACTIVE: 'Đang hoạt động',
  INACTIVE: 'Ngừng hoạt động',
}

const statusStyles: Record<LocationStatus, string> = {
  ACTIVE: 'bg-emerald-50 text-emerald-700',
  INACTIVE: 'bg-gray-100 text-gray-600',
}

const tenants = [
  'ScentStation Demo',
  'Brand Aroma',
  'Brand Luxury',
  'Flower Perfume',
  'Oriental Scents',
  'CPNT Perfume',
]

export default function LocationsPage() {
  const [locations, setLocations] = useState(initialLocations)
  const [search, setSearch] = useState('')
  const [tenantFilter, setTenantFilter] = useState('ALL')
  const [statusFilter, setStatusFilter] = useState('ALL')

  const [isFormOpen, setIsFormOpen] = useState(false)
  const [selectedLocation, setSelectedLocation] =
    useState<Location | null>(null)
  const [viewingLocation, setViewingLocation] =
    useState<Location | null>(null)

  const handleAdd = () => {
    setSelectedLocation(null)
    setIsFormOpen(true)
  }

  const handleEdit = (location: Location) => {
    setSelectedLocation(location)
    setIsFormOpen(true)
  }

  const handleSubmit = (data: LocationRequest) => {
    if (selectedLocation) {
      setLocations((current) =>
        current.map((location) =>
          location.id === selectedLocation.id
            ? { ...location, ...data }
            : location,
        ),
      )
    } else {
      const newLocation: Location = {
        id: Math.max(0, ...locations.map((item) => item.id)) + 1,
        ...data,
        createdAt: new Date().toLocaleDateString('vi-VN'),
      }

      setLocations((current) => [...current, newLocation])
    }

    setIsFormOpen(false)
    setSelectedLocation(null)
  }

  const toggleStatus = (id: number) => {
    setLocations((current) =>
      current.map((location) =>
        location.id === id
          ? {
              ...location,
              status:
                location.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE',
            }
          : location,
      ),
    )
  }

  const filteredLocations = useMemo(() => {
    return locations.filter((location) => {
      const keyword = search.toLowerCase()

      const matchesSearch =
        location.name.toLowerCase().includes(keyword) ||
        location.address.toLowerCase().includes(keyword) ||
        location.tenant.toLowerCase().includes(keyword)

      const matchesTenant =
        tenantFilter === 'ALL' || location.tenant === tenantFilter

      const matchesStatus =
        statusFilter === 'ALL' || location.status === statusFilter

      return matchesSearch && matchesTenant && matchesStatus
    })
  }, [locations, search, tenantFilter, statusFilter])

  const activeCount = locations.filter(
    (location) => location.status === 'ACTIVE',
  ).length

  const inactiveCount = locations.filter(
    (location) => location.status === 'INACTIVE',
  ).length

  return (
    <div className="space-y-7">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Quản lý địa điểm
          </h1>
          <p className="mt-1 text-sm text-[#95877c]">
            Quản lý các địa điểm đặt máy nước hoa của từng thương hiệu
          </p>
        </div>

        <button
          onClick={handleAdd}
          className="flex items-center justify-center gap-2 rounded-xl bg-[#5865f2] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#4653df]"
        >
          <Plus size={18} />
          Thêm địa điểm
        </button>
      </div>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-3">
        <StatCard
          title="Tổng địa điểm"
          value={locations.length}
          icon={<MapPin size={21} />}
          description="Tất cả địa điểm trên hệ thống"
        />
        <StatCard
          title="Đang hoạt động"
          value={activeCount}
          icon={<CheckCircle2 size={21} />}
          description="Các địa điểm đang sử dụng"
        />
        <StatCard
          title="Ngừng hoạt động"
          value={inactiveCount}
          icon={<Ban size={21} />}
          description="Các địa điểm đã tạm ngừng"
        />
      </div>

      <div className="overflow-hidden rounded-2xl border border-[#e8e7f5] bg-white shadow-sm">
        <div className="flex flex-col gap-4 border-b border-[#eeeefa] p-5 xl:flex-row xl:items-center xl:justify-between">
          <div>
            <h2 className="font-bold text-[#302720]">
              Danh sách địa điểm
            </h2>
            <p className="mt-1 text-sm text-[#95877c]">
              Hiển thị {filteredLocations.length} / {locations.length} địa điểm
            </p>
          </div>

          <div className="flex flex-col gap-3 md:flex-row">
            <div className="flex items-center gap-2 rounded-xl border border-[#e8e7f5] bg-[#f6f5ff] px-3 py-2.5 md:w-64">
              <Search size={18} className="text-[#95877c]" />
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Tìm tên, địa chỉ..."
                className="w-full bg-transparent text-sm outline-none placeholder:text-[#aaa3b8]"
              />
              {search && (
                <button onClick={() => setSearch('')}>
                  <X size={16} className="text-[#95877c]" />
                </button>
              )}
            </div>

            <select
              value={tenantFilter}
              onChange={(e) => setTenantFilter(e.target.value)}
              className="rounded-xl border border-[#e8e7f5] bg-white px-3 py-2.5 text-sm text-[#514866] outline-none focus:border-[#5865f2]"
            >
              <option value="ALL">Tất cả thương hiệu</option>
              {tenants.map((tenant) => (
                <option key={tenant} value={tenant}>
                  {tenant}
                </option>
              ))}
            </select>

            <div className="flex items-center gap-2 rounded-xl border border-[#e8e7f5] bg-white px-3">
              <Filter size={17} className="text-[#5865f2]" />
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="bg-transparent py-2.5 text-sm text-[#514866] outline-none"
              >
                <option value="ALL">Tất cả trạng thái</option>
                <option value="ACTIVE">Đang hoạt động</option>
                <option value="INACTIVE">Ngừng hoạt động</option>
              </select>
            </div>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[1050px] text-left text-sm">
            <thead className="bg-[#f8f7ff] text-[#817994]">
              <tr>
                <th className="px-6 py-4 font-semibold">Địa điểm</th>
                <th className="px-6 py-4 font-semibold">Thương hiệu</th>
                <th className="px-6 py-4 font-semibold">Địa chỉ</th>
                <th className="px-6 py-4 font-semibold">Ngày tạo</th>
                <th className="px-6 py-4 font-semibold">Trạng thái</th>
                <th className="px-6 py-4 text-center font-semibold">Thao tác</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-[#f0eff8]">
              {filteredLocations.map((location) => (
                <tr
                  key={location.id}
                  className="transition hover:bg-[#fafaff]"
                >
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#efefff] text-[#5865f2]">
                        <MapPin size={20} />
                      </div>
                      <span className="font-semibold text-[#302720]">
                        {location.name}
                      </span>
                    </div>
                  </td>

                  <td className="px-6 py-4 text-[#766b82]">
                    {location.tenant}
                  </td>

                  <td className="max-w-xs px-6 py-4 text-[#766b82]">
                    {location.address}
                  </td>

                  <td className="px-6 py-4 text-[#766b82]">
                    {location.createdAt}
                  </td>

                  <td className="px-6 py-4">
                    <span
                      className={`inline-flex rounded-full px-3 py-1.5 text-xs font-semibold ${statusStyles[location.status]}`}
                    >
                      {statusLabels[location.status]}
                    </span>
                  </td>

                  <td className="px-6 py-4">
                    <div className="flex items-center justify-center gap-2">
                      <ActionButton
                        title="Xem chi tiết"
                        onClick={() => setViewingLocation(location)}
                      >
                        <Eye size={17} />
                      </ActionButton>

                      <ActionButton
                        title="Chỉnh sửa"
                        onClick={() => handleEdit(location)}
                      >
                        <Pencil size={17} />
                      </ActionButton>

                      <ActionButton
                        title={
                          location.status === 'ACTIVE'
                            ? 'Ngừng hoạt động'
                            : 'Kích hoạt'
                        }
                        onClick={() => toggleStatus(location.id)}
                      >
                        <Ban size={17} />
                      </ActionButton>
                    </div>
                  </td>
                </tr>
              ))}

              {filteredLocations.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center">
                    <MapPin size={32} className="mx-auto text-[#b9b4cc]" />
                    <p className="mt-3 font-semibold text-[#514866]">
                      Không tìm thấy địa điểm
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

        <div className="border-t border-[#eeeefa] px-6 py-4 text-xs text-[#95877c]">
          Dữ liệu minh họa phục vụ phát triển giao diện Frontend
        </div>
      </div>

      {isFormOpen && (
        <LocationForm
          initialData={selectedLocation}
          tenants={tenants}
          onClose={() => {
            setIsFormOpen(false)
            setSelectedLocation(null)
          }}
          onSubmit={handleSubmit}
        />
      )}

      {viewingLocation && (
        <LocationDetail
          location={viewingLocation}
          onClose={() => setViewingLocation(null)}
        />
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
