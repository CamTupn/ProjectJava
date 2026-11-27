import { useState, type FormEvent } from 'react'
import { X, Building2 } from 'lucide-react'
import type { Tenant, TenantRequest } from '../../../types/tenant'
import { getApiErrorMessage } from '../../../services/apiClient'

interface TenantFormProps {
  initialData?: Tenant | null
  onClose: () => void
  onSubmit: (data: TenantRequest) => Promise<void>
}

export default function TenantForm({
  initialData,
  onClose,
  onSubmit,
}: TenantFormProps) {
  const [name, setName] = useState(initialData?.name ?? '')
  const [code, setCode] = useState(initialData?.code ?? '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (saving) return

    setError('')

    const data = {
      name: name.trim(),
      code: code.trim().toUpperCase(),
    }

    if (!data.name || data.name.length > 150) {
      setError('Tên thương hiệu phải từ 1 đến 150 ký tự.')
      return
    }

    if (!/^[A-Z0-9_-]{1,50}$/.test(data.code)) {
      setError('Mã Tenant phải từ 1 đến 50 ký tự, chỉ gồm chữ, số, dấu gạch ngang hoặc gạch dưới.')
      return
    }

    setSaving(true)

    try {
      await onSubmit(data)
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="tenant-form-title"
        className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl bg-white shadow-2xl"
      >
        <div className="flex items-center justify-between border-b border-[#eee7e1] px-6 py-5">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-[#eef0ff] p-3 text-[#5865f2]">
              <Building2 size={22} />
            </div>

            <div>
              <h2 id="tenant-form-title" className="text-lg font-bold text-[#302720]">
                {initialData
                  ? 'Chỉnh sửa thương hiệu'
                  : 'Thêm thương hiệu mới'}
              </h2>
              <p className="mt-1 text-xs text-[#95877c]">
                {initialData ? 'Edit tenant' : 'Create tenant'}
              </p>
            </div>
          </div>

          <button
            type="button"
            disabled={saving}
            onClick={onClose}
            aria-label="Đóng"
            className="rounded-lg p-2 text-[#95877c] hover:bg-[#f1efff] hover:text-[#5865f2] disabled:opacity-50"
          >
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 p-6">
          <div>
            <label htmlFor="tenant-name" className="mb-2 block text-sm font-medium text-[#302720]">
              Tên thương hiệu / Brand name
              <span className="text-red-500"> *</span>
            </label>

            <input
              id="tenant-name"
              value={name}
              required
              maxLength={150}
              disabled={saving}
              onChange={(e) => {
                setName(e.target.value)
                setError('')
              }}
              placeholder="VD: Brand Aroma"
              className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2] focus:ring-2 focus:ring-[#5865f2]/10"
            />
          </div>

          <div>
            <label htmlFor="tenant-code" className="mb-2 block text-sm font-medium text-[#302720]">
              Mã Tenant / Tenant code
              <span className="text-red-500"> *</span>
            </label>

            <input
              id="tenant-code"
              value={code}
              required
              maxLength={50}
              disabled={saving}
              onChange={(e) => {
                setCode(e.target.value.toUpperCase())
                setError('')
              }}
              placeholder="VD: BRAND-AROMA"
              className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm uppercase outline-none focus:border-[#5865f2] focus:ring-2 focus:ring-[#5865f2]/10"
            />

            <p className="mt-2 text-xs text-[#95877c]">
              Mã định danh duy nhất của thương hiệu.
            </p>
          </div>

          <div className="rounded-xl border border-[#e8e7f5] bg-[#f6f5ff] p-4">
            <p className="text-xs text-[#95877c]">
              Trạng thái / Status
            </p>

            <p className="mt-1 text-sm font-semibold text-[#5865f2]">
              {initialData?.status ?? 'ACTIVE'}
            </p>

            <p className="mt-2 text-xs text-slate-500">
              {initialData
                ? 'Trạng thái hiện chỉ được xem.'
                : 'Thương hiệu mới mặc định ở trạng thái hoạt động.'}
            </p>
          </div>

          {error && (
            <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-600">
              {error}
            </div>
          )}

          <div className="flex justify-end gap-3 border-t border-[#eee7e1] pt-5">
            <button
              type="button"
              disabled={saving}
              onClick={onClose}
              className="rounded-xl border border-[#e8e5f5] px-5 py-2.5 text-sm font-medium text-[#76675c] hover:bg-[#f5f4ff] disabled:opacity-50"
            >
              Hủy / Cancel
            </button>

            <button
              type="submit"
              disabled={saving}
              className="rounded-xl bg-[#5865f2] px-5 py-2.5 text-sm font-semibold text-white hover:bg-[#4854df] disabled:cursor-not-allowed disabled:opacity-50"
            >
              {saving
                ? 'Đang lưu...'
                : initialData
                  ? 'Lưu thay đổi'
                  : 'Thêm thương hiệu'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
