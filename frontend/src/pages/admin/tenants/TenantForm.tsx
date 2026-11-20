import { useEffect, useState } from 'react'
import { X, Building2 } from 'lucide-react'
import type { Tenant, TenantRequest } from '../../../types/tenant'

interface TenantFormProps {
  initialData?: Pick<Tenant, 'name' | 'code' | 'status'> | null
  onClose: () => void
  onSubmit: (data: TenantRequest) => void
}

export default function TenantForm({
  initialData,
  onClose,
  onSubmit,
}: TenantFormProps) {
  const [formData, setFormData] = useState<TenantRequest>({
    name: '',
    code: '',
    status: 'ACTIVE',
  })

  const [errors, setErrors] = useState({
    name: '',
    code: '',
  })

  useEffect(() => {
    if (initialData) {
      setFormData({
        name: initialData.name,
        code: initialData.code,
        status: initialData.status,
      })
    }
  }, [initialData])

  const handleChange = (
    field: keyof TenantRequest,
    value: string,
  ) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }))

    if (field === 'name' || field === 'code') {
      setErrors((prev) => ({ ...prev, [field]: '' }))
    }
  }

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    const nextErrors = {
      name: formData.name.trim() ? '' : 'Vui lòng nhập tên thương hiệu',
      code: formData.code.trim() ? '' : 'Vui lòng nhập mã Tenant',
    }

    setErrors(nextErrors)

    if (nextErrors.name || nextErrors.code) return

    onSubmit({
      name: formData.name.trim(),
      code: formData.code.trim().toUpperCase(),
      status: formData.status,
    })
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div className="w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#eee7e1] px-6 py-5">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-[#eef0ff] p-3 text-[#5865f2]">
              <Building2 size={22} />
            </div>

            <div>
              <h2 className="text-lg font-bold text-[#302720]">
                {initialData ? 'Chỉnh sửa thương hiệu' : 'Thêm thương hiệu mới'}
              </h2>
              <p className="mt-1 text-sm text-[#95877c]">
                Quản lý thông tin thương hiệu trên hệ thống
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-2 text-[#95877c] hover:bg-[#f1efff] hover:text-[#5865f2]"
          >
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 p-6">
          <div>
            <label className="mb-2 block text-sm font-medium text-[#302720]">
              Tên thương hiệu <span className="text-red-500">*</span>
            </label>

            <input
              value={formData.name}
              onChange={(e) => handleChange('name', e.target.value)}
              placeholder="VD: Brand Aroma"
              className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm outline-none transition focus:border-[#5865f2] focus:ring-2 focus:ring-[#5865f2]/10"
            />

            {errors.name && (
              <p className="mt-1 text-xs text-red-500">{errors.name}</p>
            )}
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium text-[#302720]">
              Mã Tenant <span className="text-red-500">*</span>
            </label>

            <input
              value={formData.code}
              onChange={(e) => handleChange('code', e.target.value)}
              placeholder="VD: BRAND-AROMA"
              className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm uppercase outline-none transition focus:border-[#5865f2] focus:ring-2 focus:ring-[#5865f2]/10"
            />

            {errors.code && (
              <p className="mt-1 text-xs text-red-500">{errors.code}</p>
            )}

            <p className="mt-1 text-xs text-[#95877c]">
              Mã định danh duy nhất của thương hiệu
            </p>
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium text-[#302720]">
              Trạng thái
            </label>

            <select
              value={formData.status}
              onChange={(e) => handleChange('status', e.target.value)}
              className="w-full rounded-xl border border-[#e8e5f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
            >
              <option value="ACTIVE">Đang hoạt động</option>
              <option value="SUSPENDED">Tạm khóa</option>
              <option value="INACTIVE">Ngừng hoạt động</option>
            </select>
          </div>

          <div className="flex justify-end gap-3 border-t border-[#eee7e1] pt-5">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl border border-[#e8e5f5] px-5 py-2.5 text-sm font-medium text-[#76675c] transition hover:bg-[#f5f4ff]"
            >
              Hủy
            </button>

            <button
              type="submit"
              className="rounded-xl bg-[#5865f2] px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-[#4854df]"
            >
              {initialData ? 'Lưu thay đổi' : 'Thêm thương hiệu'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
