import { X, Package } from 'lucide-react'
import type { Product } from '../../../types/product'

interface ProductDetailProps {
  product: Product
  onClose: () => void
}

const formatPrice = (price: number) =>
  `${price.toLocaleString('vi-VN')} đ`

export default function ProductDetail({
  product,
  onClose,
}: ProductDetailProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-slate-900/40 p-4">
      <div className="my-auto w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#eee7e1] px-6 py-5">
          <div>
            <h2 className="text-lg font-bold text-[#302720]">
              Chi tiết sản phẩm
            </h2>
            <p className="mt-1 text-sm text-[#95877c]">
              Thông tin sản phẩm trên hệ thống
            </p>
          </div>

          <button
            onClick={onClose}
            className="rounded-lg p-2 text-[#95877c] hover:bg-[#f1efff]"
          >
            <X size={20} />
          </button>
        </div>

        <div className="space-y-5 p-6">
          <div className="flex items-center gap-4 rounded-2xl bg-[#f5f4ff] p-5">
            <div className="flex h-16 w-16 items-center justify-center rounded-xl bg-white text-[#5865f2]">
              <Package size={30} />
            </div>

            <div>
              <h3 className="font-bold text-[#302720]">{product.name}</h3>
              <p className="mt-1 text-sm text-[#5865f2]">{product.sku}</p>
            </div>
          </div>

          <DetailRow label="Thương hiệu" value={product.tenantName} />
          <DetailRow label="Giá mỗi lần xịt" value={formatPrice(product.price)} />
          <DetailRow
            label="Lượng nước hoa mỗi lần xịt"
            value={`${product.volumePerSprayMl} ml`}
          />
          <DetailRow
            label="Ghi chú hương thơm"
            value={product.fragranceNotes || 'Chưa có thông tin'}
          />
          <DetailRow
            label="Mô tả"
            value={product.description || 'Chưa có mô tả'}
          />
          <DetailRow label="Ngày tạo" value={product.createdAt} />

          <div>
            <p className="mb-2 text-sm text-[#95877c]">Trạng thái</p>
            <span
              className={`rounded-full px-3 py-1.5 text-xs font-medium ${
                product.status === 'ACTIVE'
                  ? 'bg-emerald-50 text-emerald-700'
                  : 'bg-slate-100 text-slate-600'
              }`}
            >
              {product.status === 'ACTIVE'
                ? 'Đang kinh doanh'
                : 'Ngừng kinh doanh'}
            </span>
          </div>
        </div>

        <div className="flex justify-end border-t border-[#eee7e1] px-6 py-4">
          <button
            onClick={onClose}
            className="rounded-xl bg-[#5865f2] px-6 py-2.5 text-sm font-semibold text-white hover:bg-[#4854df]"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  )
}

function DetailRow({
  label,
  value,
}: {
  label: string
  value: string
}) {
  return (
    <div>
      <p className="mb-1 text-sm text-[#95877c]">{label}</p>
      <p className="text-sm font-medium text-[#302720]">{value}</p>
    </div>
  )
}
