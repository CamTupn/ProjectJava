import { X, MapPin, Building2 } from 'lucide-react'
import type { Location } from './LocationsPage'

interface LocationDetailProps {
  location: Location
  onClose: () => void
}

export default function LocationDetail({
  location,
  onClose,
}: LocationDetailProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div className="w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#e8e7f5] px-6 py-5">
          <div>
            <h2 className="text-lg font-bold text-[#302720]">
              Chi tiết địa điểm
            </h2>
            <p className="mt-1 text-sm text-[#95877c]">
              Thông tin địa điểm trên hệ thống
            </p>
          </div>
          <button
            onClick={onClose}
            className="rounded-lg p-2 text-[#95877c] hover:bg-[#efefff] hover:text-[#5865f2]"
          >
            <X size={20} />
          </button>
        </div>

        <div className="space-y-5 p-6">
          <div className="flex items-center gap-4 rounded-xl bg-[#f6f5ff] p-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-white text-[#5865f2]">
              <MapPin size={28} />
            </div>
            <div>
              <h3 className="font-bold text-[#302720]">{location.name}</h3>
              <p className="mt-1 text-sm text-[#5865f2]">
                {location.tenant}
              </p>
            </div>
          </div>

          <div className="space-y-4">
            <div>
              <div className="mb-1 flex items-center gap-2 text-xs text-[#95877c]">
                <Building2 size={15} />
                Thương hiệu sở hữu
              </div>
              <p className="text-sm font-medium text-[#302720]">
                {location.tenant}
              </p>
            </div>

            <div>
              <div className="mb-1 flex items-center gap-2 text-xs text-[#95877c]">
                <MapPin size={15} />
                Địa chỉ
              </div>
              <p className="text-sm font-medium text-[#302720]">
                {location.address}
              </p>
            </div>

            <div>
              <p className="text-xs text-[#95877c]">Mô tả</p>
              <p className="mt-1 text-sm font-medium text-[#302720]">
                {location.description || 'Chưa có mô tả'}
              </p>
            </div>

            <div>
              <p className="text-xs text-[#95877c]">Ngày tạo</p>
              <p className="mt-1 text-sm font-medium text-[#302720]">
                {location.createdAt}
              </p>
            </div>

            <div>
              <p className="text-xs text-[#95877c]">Trạng thái</p>
              <span
                className={`mt-2 inline-flex rounded-full px-3 py-1.5 text-xs font-semibold ${
                  location.status === 'ACTIVE'
                    ? 'bg-emerald-50 text-emerald-700'
                    : 'bg-gray-100 text-gray-600'
                }`}
              >
                {location.status === 'ACTIVE'
                  ? 'Đang hoạt động'
                  : 'Ngừng hoạt động'}
              </span>
            </div>
          </div>
        </div>

        <div className="flex justify-end border-t border-[#e8e7f5] px-6 py-4">
          <button
            onClick={onClose}
            className="rounded-xl bg-[#5865f2] px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-[#4854df]"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  )
}
