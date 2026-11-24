import SlotStatusBadge from './SlotStatusBadge'

export interface Slot {
  id: number
  machineId: number
  machineCode: string
  slotNumber: number
  productId: number | null
  productName: string
  capacityMl: number
  currentVolumeMl: number
  lowStockThresholdMl: number
  status:
    | 'AVAILABLE'
    | 'LOW_STOCK'
    | 'EMPTY'
    | 'DISABLED'
    | 'ERROR'
  createdAt: string
  updatedAt?: string
}

interface SlotDetailProps {
  slot: Slot | null
  onClose: () => void
}

export default function SlotDetail({
  slot,
  onClose,
}: SlotDetailProps) {
  if (!slot) return null

  const remainingPercent =
    slot.capacityMl > 0
      ? Math.min(
          100,
          Math.max(0, (slot.currentVolumeMl / slot.capacityMl) * 100)
        )
      : 0

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-xl overflow-hidden rounded-2xl bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-gray-100 px-6 py-5">
          <div>
            <h2 className="text-xl font-bold text-gray-900">
              Chi tiết khe máy
            </h2>
            <p className="mt-1 text-sm text-gray-500">
              Thông tin khe chứa nước hoa trên thiết bị
            </p>
          </div>

          <button
            onClick={onClose}
            className="rounded-lg p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
            aria-label="Đóng"
          >
            ✕
          </button>
        </div>

        <div className="space-y-5 p-6">
          <div className="rounded-xl bg-indigo-50 p-5">
            <p className="text-sm text-gray-500">Khe số</p>
            <h3 className="mt-1 text-2xl font-bold text-indigo-700">
              SLOT-{String(slot.slotNumber).padStart(2, '0')}
            </h3>
            <p className="mt-1 text-sm text-gray-600">
              Máy: {slot.machineCode}
            </p>
          </div>

          <div className="grid grid-cols-2 gap-5">
            <div>
              <p className="text-sm text-gray-500">Sản phẩm</p>
              <p className="mt-1 font-semibold text-gray-900">
                {slot.productName || 'Chưa gán sản phẩm'}
              </p>
            </div>

            <div>
              <p className="text-sm text-gray-500">Trạng thái</p>
              <div className="mt-2">
                <SlotStatusBadge status={slot.status} />
              </div>
            </div>

            <div>
              <p className="text-sm text-gray-500">Dung tích tối đa</p>
              <p className="mt-1 font-semibold text-gray-900">
                {slot.capacityMl} ml
              </p>
            </div>

            <div>
              <p className="text-sm text-gray-500">Lượng nước hoa hiện tại</p>
              <p className="mt-1 font-semibold text-gray-900">
                {slot.currentVolumeMl} ml
              </p>
            </div>
          </div>

          <div className="rounded-xl border border-gray-100 p-4">
            <div className="mb-2 flex justify-between text-sm">
              <span className="text-gray-500">Mức nước hoa</span>
              <span className="font-semibold text-gray-900">
                {remainingPercent.toFixed(0)}%
              </span>
            </div>

            <div className="h-3 overflow-hidden rounded-full bg-gray-100">
              <div
                className={`h-full rounded-full ${
                  remainingPercent <= 20
                    ? 'bg-red-500'
                    : remainingPercent <= 40
                      ? 'bg-yellow-500'
                      : 'bg-indigo-500'
                }`}
                style={{ width: `${remainingPercent}%` }}
              />
            </div>

            <p className="mt-3 text-sm text-gray-500">
              Ngưỡng cảnh báo: {slot.lowStockThresholdMl} ml
            </p>
          </div>

          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p className="text-gray-500">Ngày tạo</p>
              <p className="mt-1 font-medium text-gray-800">
                {slot.createdAt}
              </p>
            </div>

            <div>
              <p className="text-gray-500">Cập nhật lần cuối</p>
              <p className="mt-1 font-medium text-gray-800">
                {slot.updatedAt || 'Chưa cập nhật'}
              </p>
            </div>
          </div>
        </div>

        <div className="flex justify-end border-t border-gray-100 px-6 py-4">
          <button
            onClick={onClose}
            className="rounded-xl bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  )
}
