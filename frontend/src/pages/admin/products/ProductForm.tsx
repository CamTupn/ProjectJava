import { useEffect, useState } from 'react'
import { X, Package } from 'lucide-react'
import type { Product, ProductRequest } from '../../../types/product'

interface ProductFormProps {
  initialData?: Product | null
  onClose: () => void
  onSubmit: (data: ProductRequest) => void
}

const defaultForm: ProductRequest = {
  tenantId: 1,
  sku: '',
  name: '',
  description: '',
  fragranceNotes: '',
  imageUrl: '',
  price: 15000,
  volumePerSprayMl: 0.15,
  status: 'ACTIVE',
}

export default function ProductForm({
  initialData,
  onClose,
  onSubmit,
}: ProductFormProps) {
  const [formData, setFormData] = useState<ProductRequest>(defaultForm)

  const [errors, setErrors] = useState({
    sku: '',
    name: '',
    price: '',
    volumePerSprayMl: '',
  })

  useEffect(() => {
    if (initialData) {
      setFormData({
        tenantId: initialData.tenantId,
        sku: initialData.sku,
        name: initialData.name,
        description: initialData.description ?? '',
        fragranceNotes: initialData.fragranceNotes ?? '',
        imageUrl: initialData.imageUrl ?? '',
        price: initialData.price,
        volumePerSprayMl: initialData.volumePerSprayMl,
        status: initialData.status,
      })
    } else {
      setFormData(defaultForm)
    }
  }, [initialData])

  const handleChange = (
    field: keyof ProductRequest,
    value: string | number,
  ) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }))

    if (field in errors) {
      setErrors((prev) => ({
        ...prev,
        [field]: '',
      }))
    }
  }

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    const nextErrors = {
      sku: formData.sku.trim() ? '' : 'Vui lòng nhập mã SKU',
      name: formData.name.trim() ? '' : 'Vui lòng nhập tên sản phẩm',
      price:
        formData.price > 0 ? '' : 'Giá sản phẩm phải lớn hơn 0',
      volumePerSprayMl:
        formData.volumePerSprayMl > 0
          ? ''
          : 'Lượng nước hoa mỗi lần xịt phải lớn hơn 0',
    }

    setErrors(nextErrors)

    if (Object.values(nextErrors).some(Boolean)) return

    onSubmit({
      ...formData,
      sku: formData.sku.trim().toUpperCase(),
      name: formData.name.trim(),
      description: formData.description.trim(),
      fragranceNotes: formData.fragranceNotes.trim(),
    })
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-slate-900/40 p-4">
      <div className="my-auto w-full max-w-2xl overflow-hidden rounded-2xl bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#eee7e1] px-6 py-5">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-[#eef0ff] p-3 text-[#5865f2]">
              <Package size={22} />
            </div>

            <div>
              <h2 className="text-lg font-bold text-[#302720]">
                {initialData ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới'}
              </h2>
              <p className="mt-1 text-sm text-[#95877c]">
                Quản lý thông tin sản phẩm nước hoa
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-2 text-[#95877c] transition hover:bg-[#f1efff] hover:text-[#5865f2]"
          >
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 p-6">
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Thương hiệu <span className="text-red-500">*</span>
              </label>

              <select
                value={formData.tenantId}
                onChange={(e) =>
                  handleChange('tenantId', Number(e.target.value))
                }
                className="w-full rounded-xl border border-[#e8e5f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              >
                <option value={1}>ScentStation Demo</option>
                <option value={2}>Brand Aroma</option>
                <option value={3}>Brand Luxury</option>
                <option value={4}>Flower Perfume</option>
                <option value={5}>Oriental Scents</option>
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Mã SKU <span className="text-red-500">*</span>
              </label>

              <input
                value={formData.sku}
                onChange={(e) => handleChange('sku', e.target.value)}
                placeholder="VD: SCENT-ROSE-001"
                className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm uppercase outline-none focus:border-[#5865f2]"
              />

              {errors.sku && (
                <p className="mt-1 text-xs text-red-500">{errors.sku}</p>
              )}
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Tên sản phẩm <span className="text-red-500">*</span>
              </label>

              <input
                value={formData.name}
                onChange={(e) => handleChange('name', e.target.value)}
                placeholder="VD: Rose Elegance"
                className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              />

              {errors.name && (
                <p className="mt-1 text-xs text-red-500">{errors.name}</p>
              )}
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Giá mỗi lần xịt (VNĐ)
              </label>

              <input
                type="number"
                min="1"
                value={formData.price}
                onChange={(e) =>
                  handleChange('price', Number(e.target.value))
                }
                className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              />

              {errors.price && (
                <p className="mt-1 text-xs text-red-500">{errors.price}</p>
              )}
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Lượng nước hoa mỗi lần xịt (ml)
              </label>

              <input
                type="number"
                min="0.01"
                step="0.01"
                value={formData.volumePerSprayMl}
                onChange={(e) =>
                  handleChange(
                    'volumePerSprayMl',
                    Number(e.target.value),
                  )
                }
                className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              />

              {errors.volumePerSprayMl && (
                <p className="mt-1 text-xs text-red-500">
                  {errors.volumePerSprayMl}
                </p>
              )}
            </div>

            <div className="sm:col-span-2">
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Ghi chú hương thơm
              </label>

              <input
                value={formData.fragranceNotes}
                onChange={(e) =>
                  handleChange('fragranceNotes', e.target.value)
                }
                placeholder="VD: Hoa hồng, hoa nhài, xạ hương"
                className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              />
            </div>

            <div className="sm:col-span-2">
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Mô tả sản phẩm
              </label>

              <textarea
                rows={3}
                value={formData.description}
                onChange={(e) =>
                  handleChange('description', e.target.value)
                }
                placeholder="Nhập mô tả sản phẩm..."
                className="w-full resize-none rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              />
            </div>

            <div className="sm:col-span-2">
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Đường dẫn hình ảnh
              </label>

              <input
                value={formData.imageUrl}
                onChange={(e) =>
                  handleChange('imageUrl', e.target.value)
                }
                placeholder="https://example.com/product.jpg"
                className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              />
            </div>

            <div className="sm:col-span-2">
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Trạng thái
              </label>

              <select
                value={formData.status}
                onChange={(e) => handleChange('status', e.target.value)}
                className="w-full rounded-xl border border-[#e8e5f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              >
                <option value="ACTIVE">Đang kinh doanh</option>
                <option value="INACTIVE">Ngừng kinh doanh</option>
              </select>
            </div>
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
              {initialData ? 'Lưu thay đổi' : 'Thêm sản phẩm'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
