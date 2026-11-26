import { useEffect, useState } from 'react'
import { X, MapPin } from 'lucide-react'
import type {
  Location,
  LocationRequest,
  LocationStatus,
} from './LocationsPage'

interface LocationFormProps {
  initialData?: Location | null
  tenants: string[]
  onClose: () => void
  onSubmit: (data: LocationRequest) => void
}

export default function LocationForm({
  initialData,
  tenants,
  onClose,
  onSubmit,
}: LocationFormProps) {
  const [formData, setFormData] = useState<LocationRequest>({
    name: '',
    tenant: tenants[0] ?? '',
    address: '',
    description: '',
    status: 'ACTIVE',
  })

  const [errors, setErrors] = useState({
    name: '',
    address: '',
  })

  useEffect(() => {
    if (initialData) {
      setFormData({
        name: initialData.name,
        tenant: initialData.tenant,
        address: initialData.address,
        description: initialData.description,
        status: initialData.status,
      })
    }
  }, [initialData, tenants])

  const handleChange = (
    field: keyof LocationRequest,
    value: string,
  ) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }))

    if (field === 'name' || field === 'address') {
      setErrors((prev) => ({ ...prev, [field]: '' }))
    }
  }

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    const nextErrors = {
      name: formData.name.trim() ? '' : 'Vui lòng nhập tên địa điểm',
      address: formData.address.trim() ? '' : 'Vui lòng nhập địa chỉ',
    }

    setErrors(nextErrors)

    if (nextErrors.name || nextErrors.address) return

    onSubmit({
      ...formData,
      name: formData.name.trim(),
      address: formData.address.trim(),
      description: formData.description.trim(),
      tenant: formData.tenant,
      status: formData.status as LocationStatus,
    })
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div className="w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#e8e7f5] px-6 py-5">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-[#efefff] p-3 text-[#5865f2]">
              <MapPin size={22} />
            </div>
            <div>
              <h2 className="text-lg font-bold text-[#302720]">
                {initialData ? 'Chỉnh sửa địa điểm' : 'Thêm địa điểm mới'}
              </h2>
              <p className="mt-1 text-sm text-[#95877c]">
                Quản lý thông tin địa điểm của thương hiệu
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-2 text-[#95877c] hover:bg-[#efefff] hover:text-[#5865f2]"
          >
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 p-6">
          <div>
            <label className="mb-2 block text-sm font-medium text-[#302720]">
              Tên địa điểm <span className="text-red-500">*</span>
            </label>
            <input
              value={formData.name}
              onChange={(e) => handleChange('name', e.target.value)}
              placeholder="VD: Vincom Center"
              className="w-full rounded-xl border border-[#e8e7f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2] focus:ring-2 focus:ring-[#5865f2]/10"
            />
            {errors.name && (
              <p className="mt-1 text-xs text-red-500">{errors.name}</p>
            )}
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium text-[#302720]">
              Thương hiệu (Tenant) <span className="text-red-500">*</span>
            </label>
            <select
              value={formData.tenant}
              onChange={(e) => handleChange('tenant', e.target.value)}
              className="w-full rounded-xl border border-[#e8e7f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
            >
              {tenants.map((tenant) => (
                <option key={tenant} value={tenant}>
                  {tenant}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium text-[#302720]">
              Địa chỉ <span className="text-red-500">*</span>
            </label>
            <input
              value={formData.address}
              onChange={(e) => handleChange('address', e.target.value)}
              placeholder="Nhập địa chỉ cụ thể"
              className="w-full rounded-xl border border-[#e8e7f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
            />
            {errors.address && (
              <p className="mt-1 text-xs text-red-500">{errors.address}</p>
            )}
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium text-[#302720]">
              Mô tả
            </label>
            <textarea
              value={formData.description}
              onChange={(e) => handleChange('description', e.target.value)}
              placeholder="Mô tả về địa điểm..."
              rows={3}
              className="w-full resize-none rounded-xl border border-[#e8e7f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
            />
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium text-[#302720]">
              Trạng thái
            </label>
            <select
              value={formData.status}
              onChange={(e) => handleChange('status', e.target.value)}
              className="w-full rounded-xl border border-[#e8e7f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
            >
              <option value="ACTIVE">Đang hoạt động</option>
              <option value="INACTIVE">Ngừng hoạt động</option>
            </select>
          </div>

          <div className="flex justify-end gap-3 border-t border-[#e8e7f5] pt-5">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl border border-[#e8e7f5] px-5 py-2.5 text-sm font-medium text-[#76675c] hover:bg-[#f5f4ff]"
            >
              Hủy
            </button>
            <button
              type="submit"
              className="rounded-xl bg-[#5865f2] px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-[#4854df]"
            >
              {initialData ? 'Lưu thay đổi' : 'Thêm địa điểm'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
