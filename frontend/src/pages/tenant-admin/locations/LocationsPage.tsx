import { useMemo, useState } from 'react'
import {
  Search,
  Plus,
  Eye,
  Pencil,
  Trash2,
  X,
  MapPin,
  MapPinned,
  Store,
  Phone,
  Mail,
  Clock,
  CheckCircle2,
  XCircle,
  Building2,
  Users,
} from 'lucide-react'

type LocationStatus = 'ACTIVE' | 'INACTIVE'

interface Location {
  id: number
  name: string
  code: string
  address: string
  city: string
  contactName: string
  phone: string
  email: string
  openingTime: string
  closingTime: string
  machineCount: number
  status: LocationStatus
  description: string
  createdAt: string
}

const initialLocations: Location[] = [
  {
    id: 1,
    name: 'Vincom Đồng Khởi',
    code: 'LOC-001',
    address: '72 Lê Thánh Tôn, Quận 1',
    city: 'TP. Hồ Chí Minh',
    contactName: 'Nguyễn Minh Anh',
    phone: '0901234567',
    email: 'vincom@scentstation.vn',
    openingTime: '09:00',
    closingTime: '22:00',
    machineCount: 2,
    status: 'ACTIVE',
    description: 'Địa điểm trải nghiệm nước hoa tại trung tâm thương mại.',
    createdAt: '01/11/2026',
  },
  {
    id: 2,
    name: 'Landmark 81',
    code: 'LOC-002',
    address: '720A Điện Biên Phủ, Bình Thạnh',
    city: 'TP. Hồ Chí Minh',
    contactName: 'Trần Quốc Bảo',
    phone: '0912345678',
    email: 'landmark@scentstation.vn',
    openingTime: '10:00',
    closingTime: '22:00',
    machineCount: 2,
    status: 'ACTIVE',
    description: 'Khu vực đặt máy trải nghiệm nước hoa tại Landmark 81.',
    createdAt: '03/11/2026',
  },
  {
    id: 3,
    name: 'Aeon Mall Tân Phú',
    code: 'LOC-003',
    address: '30 Bờ Bao Tân Thắng, Tân Phú',
    city: 'TP. Hồ Chí Minh',
    contactName: 'Lê Hoàng Yến',
    phone: '0934567890',
    email: 'aeon@scentstation.vn',
    openingTime: '10:00',
    closingTime: '22:00',
    machineCount: 1,
    status: 'INACTIVE',
    description: 'Địa điểm đang tạm ngừng hoạt động.',
    createdAt: '05/11/2026',
  },
  {
    id: 4,
    name: 'Gigamall Thủ Đức',
    code: 'LOC-004',
    address: '240 - 242 Phạm Văn Đồng, Thủ Đức',
    city: 'TP. Hồ Chí Minh',
    contactName: 'Phạm Tuấn Kiệt',
    phone: '0987654321',
    email: 'gigamall@scentstation.vn',
    openingTime: '09:30',
    closingTime: '22:00',
    machineCount: 3,
    status: 'ACTIVE',
    description: 'Địa điểm trải nghiệm và mua nước hoa tự động.',
    createdAt: '08/11/2026',
  },
]

const emptyForm = {
  name: '',
  address: '',
  city: 'TP. Hồ Chí Minh',
  contactName: '',
  phone: '',
  email: '',
  openingTime: '09:00',
  closingTime: '22:00',
  description: '',
}

function StatusBadge({ status }: { status: LocationStatus }) {
  const active = status === 'ACTIVE'

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-medium ${
        active
          ? 'bg-green-50 text-green-700'
          : 'bg-gray-100 text-gray-600'
      }`}
    >
      {active ? <CheckCircle2 size={13} /> : <XCircle size={13} />}
      {active ? 'Đang hoạt động' : 'Ngừng hoạt động'}
    </span>
  )
}

function StatCard({
  title,
  value,
  icon: Icon,
  color,
}: {
  title: string
  value: number
  icon: typeof MapPin
  color: string
}) {
  return (
    <div className="rounded-2xl border border-[#e5e4f5] bg-white p-6 shadow-sm">
      <div className="flex items-center justify-between">
        <p className="text-sm text-[#95877c]">{title}</p>
        <div className={`rounded-xl p-3 ${color}`}>
          <Icon size={23} />
        </div>
      </div>
      <p className="mt-4 text-3xl font-bold text-[#5865f2]">{value}</p>
    </div>
  )
}

export default function LocationsPage() {
  const [locations, setLocations] = useState<Location[]>(initialLocations)
  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')

  const [showForm, setShowForm] = useState(false)
  const [editingLocation, setEditingLocation] = useState<Location | null>(null)
  const [viewingLocation, setViewingLocation] = useState<Location | null>(null)
  const [deletingLocation, setDeletingLocation] = useState<Location | null>(null)

  const [form, setForm] = useState(emptyForm)
  const [formError, setFormError] = useState('')

  const activeCount = locations.filter(
    (item) => item.status === 'ACTIVE',
  ).length

  const inactiveCount = locations.length - activeCount

  const totalMachines = locations.reduce(
    (total, item) => total + item.machineCount,
    0,
  )

  const filteredLocations = useMemo(() => {
    const normalizedKeyword = keyword.trim().toLowerCase()

    return locations.filter((item) => {
      const matchesKeyword =
        !normalizedKeyword ||
        item.name.toLowerCase().includes(normalizedKeyword) ||
        item.code.toLowerCase().includes(normalizedKeyword) ||
        item.address.toLowerCase().includes(normalizedKeyword) ||
        item.contactName.toLowerCase().includes(normalizedKeyword)

      const matchesStatus =
        statusFilter === 'ALL' || item.status === statusFilter

      return matchesKeyword && matchesStatus
    })
  }, [locations, keyword, statusFilter])

  const openAddForm = () => {
    setEditingLocation(null)
    setForm(emptyForm)
    setFormError('')
    setShowForm(true)
  }

  const openEditForm = (location: Location) => {
    setEditingLocation(location)
    setForm({
      name: location.name,
      address: location.address,
      city: location.city,
      contactName: location.contactName,
      phone: location.phone,
      email: location.email,
      openingTime: location.openingTime,
      closingTime: location.closingTime,
      description: location.description,
    })
    setFormError('')
    setShowForm(true)
  }

  const handleSave = () => {
    if (!form.name.trim() || !form.address.trim()) {
      setFormError('Vui lòng nhập tên địa điểm và địa chỉ.')
      return
    }

    if (!form.contactName.trim() || !form.phone.trim()) {
      setFormError('Vui lòng nhập thông tin người liên hệ và số điện thoại.')
      return
    }

    if (!/^[0-9]{9,11}$/.test(form.phone.trim())) {
      setFormError('Số điện thoại phải có từ 9 đến 11 chữ số.')
      return
    }

    if (
      form.email.trim() &&
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())
    ) {
      setFormError('Email không đúng định dạng.')
      return
    }

    if (form.openingTime >= form.closingTime) {
      setFormError('Giờ mở cửa phải sớm hơn giờ đóng cửa.')
      return
    }

    if (editingLocation) {
      setLocations((previous) =>
        previous.map((item) =>
          item.id === editingLocation.id
            ? {
                ...item,
                ...form,
              }
            : item,
        ),
      )
    } else {
      const newId =
        locations.length > 0
          ? Math.max(...locations.map((item) => item.id)) + 1
          : 1

      const newLocation: Location = {
        id: newId,
        code: `LOC-${String(newId).padStart(3, '0')}`,
        ...form,
        machineCount: 0,
        status: 'ACTIVE',
        createdAt: new Date().toLocaleDateString('vi-VN'),
      }

      setLocations((previous) => [newLocation, ...previous])
    }

    setShowForm(false)
    setEditingLocation(null)
    setForm(emptyForm)
    setFormError('')
  }

  const handleDelete = () => {
    if (!deletingLocation) return

    if (deletingLocation.machineCount > 0) {
      alert(
        'Không thể xóa địa điểm đang có máy nước hoa. Vui lòng chuyển máy sang địa điểm khác trước.',
      )
      setDeletingLocation(null)
      return
    }

    setLocations((previous) =>
      previous.filter((item) => item.id !== deletingLocation.id),
    )

    setDeletingLocation(null)
  }

  const toggleStatus = (location: Location) => {
    setLocations((previous) =>
      previous.map((item) =>
        item.id === location.id
          ? {
              ...item,
              status:
                item.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE',
            }
          : item,
      ),
    )
  }

  return (
    <div className="min-h-full bg-[#f0efff] p-6 md:p-8">
      {/* Page header */}
      <div className="mb-7 flex flex-col justify-between gap-4 md:flex-row md:items-center">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Quản lý địa điểm
          </h1>
          <p className="mt-1 text-sm text-[#95877c]">
            Quản lý các địa điểm đặt máy nước hoa thuộc thương hiệu
          </p>
        </div>

        <button
          onClick={openAddForm}
          className="inline-flex items-center justify-center gap-2 rounded-xl bg-[#5041e8] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#4033d5]"
        >
          <Plus size={19} />
          Thêm địa điểm
        </button>
      </div>

      {/* Statistics */}
      <div className="mb-7 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Tổng địa điểm"
          value={locations.length}
          icon={MapPin}
          color="bg-[#eef0ff] text-[#5865f2]"
        />
        <StatCard
          title="Đang hoạt động"
          value={activeCount}
          icon={CheckCircle2}
          color="bg-green-50 text-green-600"
        />
        <StatCard
          title="Ngừng hoạt động"
          value={inactiveCount}
          icon={XCircle}
          color="bg-red-50 text-red-500"
        />
        <StatCard
          title="Tổng máy nước hoa"
          value={totalMachines}
          icon={Store}
          color="bg-purple-50 text-purple-600"
        />
      </div>

      {/* Location list */}
      <section className="overflow-hidden rounded-2xl border border-[#e5e4f5] bg-white shadow-sm">
        <div className="flex flex-col justify-between gap-4 border-b border-[#eeeef5] p-5 xl:flex-row xl:items-center">
          <div>
            <h2 className="font-bold text-[#302720]">Danh sách địa điểm</h2>
            <p className="mt-1 text-sm text-[#95877c]">
              Hiển thị {filteredLocations.length} / {locations.length} địa điểm
            </p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row">
            <div className="relative">
              <Search
                size={18}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-[#95877c]"
              />
              <input
                value={keyword}
                onChange={(event) => setKeyword(event.target.value)}
                placeholder="Tìm tên, mã, địa chỉ..."
                className="w-full rounded-xl border border-[#e5e4f5] bg-[#f9f8ff] py-3 pl-10 pr-4 text-sm outline-none transition focus:border-[#5865f2] sm:w-64"
              />
            </div>

            <select
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
              className="rounded-xl border border-[#e5e4f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="ACTIVE">Đang hoạt động</option>
              <option value="INACTIVE">Ngừng hoạt động</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[1050px] text-left">
            <thead className="bg-[#f5f4ff] text-sm text-[#6255a5]">
              <tr>
                <th className="px-5 py-4 font-semibold">Địa điểm</th>
                <th className="px-5 py-4 font-semibold">Mã địa điểm</th>
                <th className="px-5 py-4 font-semibold">Địa chỉ</th>
                <th className="px-5 py-4 font-semibold">Người liên hệ</th>
                <th className="px-5 py-4 font-semibold">Số máy</th>
                <th className="px-5 py-4 font-semibold">Trạng thái</th>
                <th className="px-5 py-4 text-center font-semibold">Thao tác</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-[#eeeef5]">
              {filteredLocations.map((location) => (
                <tr
                  key={location.id}
                  className="transition hover:bg-[#faf9ff]"
                >
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <div className="rounded-xl bg-[#eef0ff] p-3 text-[#5865f2]">
                        <MapPin size={20} />
                      </div>
                      <div>
                        <p className="font-semibold text-[#302720]">
                          {location.name}
                        </p>
                        <p className="mt-1 text-xs text-[#95877c]">
                          {location.city}
                        </p>
                      </div>
                    </div>
                  </td>

                  <td className="px-5 py-4 text-sm font-medium text-[#5865f2]">
                    {location.code}
                  </td>

                  <td className="max-w-[230px] px-5 py-4 text-sm text-[#62564d]">
                    {location.address}
                  </td>

                  <td className="px-5 py-4">
                    <p className="text-sm font-medium text-[#302720]">
                      {location.contactName}
                    </p>
                    <p className="mt-1 text-xs text-[#95877c]">
                      {location.phone}
                    </p>
                  </td>

                  <td className="px-5 py-4">
                    <span className="inline-flex items-center gap-2 rounded-lg bg-[#f5f4ff] px-3 py-2 text-sm font-semibold text-[#5865f2]">
                      <Store size={15} />
                      {location.machineCount}
                    </span>
                  </td>

                  <td className="px-5 py-4">
                    <StatusBadge status={location.status} />
                  </td>

                  <td className="px-5 py-4">
                    <div className="flex items-center justify-center gap-2">
                      <button
                        title="Xem chi tiết"
                        onClick={() => setViewingLocation(location)}
                        className="rounded-lg border border-[#e5e4f5] p-2 text-[#5865f2] hover:bg-[#eef0ff]"
                      >
                        <Eye size={17} />
                      </button>

                      <button
                        title="Chỉnh sửa"
                        onClick={() => openEditForm(location)}
                        className="rounded-lg border border-[#e5e4f5] p-2 text-[#5865f2] hover:bg-[#eef0ff]"
                      >
                        <Pencil size={17} />
                      </button>

                      <button
                        title={
                          location.status === 'ACTIVE'
                            ? 'Ngừng hoạt động'
                            : 'Kích hoạt'
                        }
                        onClick={() => toggleStatus(location)}
                        className="rounded-lg border border-[#e5e4f5] p-2 text-orange-500 hover:bg-orange-50"
                      >
                        {location.status === 'ACTIVE' ? (
                          <XCircle size={17} />
                        ) : (
                          <CheckCircle2 size={17} />
                        )}
                      </button>

                      <button
                        title="Xóa"
                        onClick={() => setDeletingLocation(location)}
                        className="rounded-lg border border-red-100 p-2 text-red-500 hover:bg-red-50"
                      >
                        <Trash2 size={17} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}

              {filteredLocations.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-5 py-14 text-center">
                    <MapPinned
                      size={36}
                      className="mx-auto mb-3 text-[#b5b0d9]"
                    />
                    <p className="font-medium text-[#302720]">
                      Không tìm thấy địa điểm
                    </p>
                    <p className="mt-1 text-sm text-[#95877c]">
                      Thử thay đổi từ khóa hoặc bộ lọc của bạn.
                    </p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      {/* Add / Edit modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-black/40 p-4">
          <div className="my-auto w-full max-w-2xl rounded-2xl bg-white shadow-xl">
            <div className="flex items-center justify-between border-b border-[#eeeef5] p-5">
              <div>
                <h2 className="text-lg font-bold text-[#302720]">
                  {editingLocation ? 'Chỉnh sửa địa điểm' : 'Thêm địa điểm mới'}
                </h2>
                <p className="mt-1 text-sm text-[#95877c]">
                  Nhập thông tin địa điểm đặt máy nước hoa
                </p>
              </div>
              <button
                onClick={() => setShowForm(false)}
                className="rounded-lg p-2 text-gray-500 hover:bg-gray-100"
              >
                <X size={21} />
              </button>
            </div>

            <div className="grid grid-cols-1 gap-4 p-5 md:grid-cols-2">
              <div className="md:col-span-2">
                <label className="mb-2 block text-sm font-medium">
                  Tên địa điểm *
                </label>
                <input
                  value={form.name}
                  onChange={(event) =>
                    setForm({ ...form, name: event.target.value })
                  }
                  placeholder="Ví dụ: Vincom Đồng Khởi"
                  className="w-full rounded-xl border border-[#e5e4f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
                />
              </div>

              <div className="md:col-span-2">
                <label className="mb-2 block text-sm font-medium">
                  Địa chỉ *
                </label>
                <input
                  value={form.address}
                  onChange={(event) =>
                    setForm({ ...form, address: event.target.value })
                  }
                  placeholder="Số nhà, đường, quận/huyện"
                  className="w-full rounded-xl border border-[#e5e4f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium">
                  Thành phố
                </label>
                <input
                  value={form.city}
                  onChange={(event) =>
                    setForm({ ...form, city: event.target.value })
                  }
                  className="w-full rounded-xl border border-[#e5e4f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium">
                  Người liên hệ *
                </label>
                <input
                  value={form.contactName}
                  onChange={(event) =>
                    setForm({ ...form, contactName: event.target.value })
                  }
                  placeholder="Họ và tên"
                  className="w-full rounded-xl border border-[#e5e4f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium">
                  Số điện thoại *
                </label>
                <input
                  value={form.phone}
                  onChange={(event) =>
                    setForm({ ...form, phone: event.target.value })
                  }
                  placeholder="0901234567"
                  className="w-full rounded-xl border border-[#e5e4f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium">
                  Email liên hệ
                </label>
                <input
                  value={form.email}
                  onChange={(event) =>
                    setForm({ ...form, email: event.target.value })
                  }
                  placeholder="location@example.com"
                  className="w-full rounded-xl border border-[#e5e4f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium">
                  Giờ mở cửa
                </label>
                <input
                  type="time"
                  value={form.openingTime}
                  onChange={(event) =>
                    setForm({ ...form, openingTime: event.target.value })
                  }
                  className="w-full rounded-xl border border-[#e5e4f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium">
                  Giờ đóng cửa
                </label>
                <input
                  type="time"
                  value={form.closingTime}
                  onChange={(event) =>
                    setForm({ ...form, closingTime: event.target.value })
                  }
                  className="w-full rounded-xl border border-[#e5e4f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
                />
              </div>

              <div className="md:col-span-2">
                <label className="mb-2 block text-sm font-medium">
                  Mô tả
                </label>
                <textarea
                  rows={3}
                  value={form.description}
                  onChange={(event) =>
                    setForm({ ...form, description: event.target.value })
                  }
                  placeholder="Thông tin bổ sung về địa điểm..."
                  className="w-full resize-none rounded-xl border border-[#e5e4f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
                />
              </div>

              {formError && (
                <p className="md:col-span-2 rounded-lg bg-red-50 p-3 text-sm text-red-600">
                  {formError}
                </p>
              )}
            </div>

            <div className="flex justify-end gap-3 border-t border-[#eeeef5] p-5">
              <button
                onClick={() => setShowForm(false)}
                className="rounded-xl border border-[#e5e4f5] px-5 py-3 text-sm font-medium text-[#62564d] hover:bg-gray-50"
              >
                Hủy
              </button>
              <button
                onClick={handleSave}
                className="rounded-xl bg-[#5041e8] px-6 py-3 text-sm font-semibold text-white hover:bg-[#4033d5]"
              >
                {editingLocation ? 'Lưu thay đổi' : 'Thêm địa điểm'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Detail modal */}
      {viewingLocation && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-xl rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-5 flex items-center justify-between">
              <h2 className="text-lg font-bold text-[#302720]">
                Chi tiết địa điểm
              </h2>
              <button
                onClick={() => setViewingLocation(null)}
                className="rounded-lg p-2 text-gray-500 hover:bg-gray-100"
              >
                <X size={21} />
              </button>
            </div>

            <div className="mb-5 flex items-center gap-4 rounded-xl bg-[#f5f4ff] p-4">
              <div className="rounded-xl bg-[#e9eaff] p-3 text-[#5865f2]">
                <Building2 size={25} />
              </div>
              <div>
                <h3 className="font-bold text-[#302720]">
                  {viewingLocation.name}
                </h3>
                <p className="mt-1 text-sm text-[#95877c]">
                  {viewingLocation.code}
                </p>
              </div>
            </div>

            <div className="space-y-4 text-sm">
              <div className="flex items-start gap-3">
                <MapPin size={18} className="mt-0.5 text-[#5865f2]" />
                <div>
                  <p className="text-[#95877c]">Địa chỉ</p>
                  <p className="mt-1 font-medium text-[#302720]">
                    {viewingLocation.address}, {viewingLocation.city}
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <Users size={18} className="mt-0.5 text-[#5865f2]" />
                <div>
                  <p className="text-[#95877c]">Người liên hệ</p>
                  <p className="mt-1 font-medium text-[#302720]">
                    {viewingLocation.contactName}
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <Phone size={18} className="mt-0.5 text-[#5865f2]" />
                <div>
                  <p className="text-[#95877c]">Số điện thoại</p>
                  <p className="mt-1 font-medium text-[#302720]">
                    {viewingLocation.phone}
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <Mail size={18} className="mt-0.5 text-[#5865f2]" />
                <div>
                  <p className="text-[#95877c]">Email</p>
                  <p className="mt-1 font-medium text-[#302720]">
                    {viewingLocation.email || 'Chưa cập nhật'}
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <Clock size={18} className="mt-0.5 text-[#5865f2]" />
                <div>
                  <p className="text-[#95877c]">Thời gian hoạt động</p>
                  <p className="mt-1 font-medium text-[#302720]">
                    {viewingLocation.openingTime} - {viewingLocation.closingTime}
                  </p>
                </div>
              </div>

              <div className="flex items-center justify-between border-t border-[#eeeef5] pt-4">
                <span className="text-[#95877c]">Số máy nước hoa</span>
                <span className="font-semibold text-[#5865f2]">
                  {viewingLocation.machineCount} máy
                </span>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-[#95877c]">Trạng thái</span>
                <StatusBadge status={viewingLocation.status} />
              </div>

              <div>
                <p className="text-[#95877c]">Mô tả</p>
                <p className="mt-1 text-[#302720]">
                  {viewingLocation.description || 'Chưa có mô tả.'}
                </p>
              </div>
            </div>

            <button
              onClick={() => setViewingLocation(null)}
              className="mt-6 w-full rounded-xl bg-[#5041e8] py-3 text-sm font-semibold text-white hover:bg-[#4033d5]"
            >
              Đóng
            </button>
          </div>
        </div>
      )}

      {/* Delete confirmation */}
      {deletingLocation && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
            <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-red-50 text-red-500">
              <Trash2 size={23} />
            </div>

            <h2 className="text-lg font-bold text-[#302720]">
              Xác nhận xóa địa điểm
            </h2>

            <p className="mt-2 text-sm leading-6 text-[#95877c]">
              Bạn có chắc chắn muốn xóa địa điểm{' '}
              <strong className="text-[#302720]">
                {deletingLocation.name}
              </strong>
              ? Thao tác này không thể hoàn tác.
            </p>

            {deletingLocation.machineCount > 0 && (
              <p className="mt-3 rounded-lg bg-orange-50 p-3 text-sm text-orange-700">
                Địa điểm hiện có {deletingLocation.machineCount} máy nước hoa.
                Bạn cần chuyển máy đi trước khi xóa.
              </p>
            )}

            <div className="mt-6 flex justify-end gap-3">
              <button
                onClick={() => setDeletingLocation(null)}
                className="rounded-xl border border-[#e5e4f5] px-5 py-3 text-sm font-medium"
              >
                Hủy
              </button>
              <button
                onClick={handleDelete}
                className="rounded-xl bg-red-500 px-5 py-3 text-sm font-semibold text-white hover:bg-red-600"
              >
                Xác nhận xóa
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
