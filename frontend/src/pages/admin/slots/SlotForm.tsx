import { useEffect, useState } from 'react'
import type { Slot } from './SlotDetail'

interface MachineOption {
  id: number
  machineCode: string
}

interface ProductOption {
  id: number
  name: string
}

interface SlotFormProps {
  slot: Slot | null
  machines: MachineOption[]
  products: ProductOption[]
  onClose: () => void
  onSave: (data: Partial<Slot>) => void
}

export default function SlotForm({
  slot,
  machines,
  products,
  onClose,
  onSave,
}: SlotFormProps) {
  const [machineId, setMachineId] = useState('')
  const [slotNumber, setSlotNumber] = useState('1')
  const [productId, setProductId] = useState('')
  const [capacityMl, setCapacityMl] = useState('100')
  const [currentVolumeMl, setCurrentVolumeMl] = useState('100')
  const [lowStockThresholdMl, setLowStockThresholdMl] = useState('20')
  const [status, setStatus] = useState<Slot['status']>('AVAILABLE')
  const [error, setError] = useState('')

  useEffect(() => {
    if (slot) {
      setMachineId(String(slot.machineId))
      setSlotNumber(String(slot.slotNumber))
      setProductId(slot.productId ? String(slot.productId) : '')
      setCapacityMl(String(slot.capacityMl))
      setCurrentVolumeMl(String(slot.currentVolumeMl))
      setLowStockThresholdMl(String(slot.lowStockThresholdMl))
      setStatus(slot.status)
    } else {
      setMachineId(machines[0] ? String(machines[0].id) : '')
      setSlotNumber('1')
      setProductId('')
      setCapacityMl('100')
      setCurrentVolumeMl('100')
      setLowStockThresholdMl('20')
      setStatus('AVAILABLE')
    }
  }, [slot, machines])

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')

    const capacity = Number(capacityMl)
    const currentVolume = Number(currentVolumeMl)
    const threshold = Number(lowStockThresholdMl)
    const number = Number(slotNumber)

    if (!machineId) {
      setError('Vui lòng chọn máy nước hoa.')
      return
    }

    if (!Number.isInteger(number) || number < 1) {
      setError('Số khe phải là số nguyên lớn hơn 0.')
      return
    }

    if (capacity <= 0 || currentVolume < 0 || threshold < 0) {
      setError('Dung tích và ngưỡng cảnh báo không hợp lệ.')
      return
    }

    if (currentVolume > capacity) {
      setError('Lượng nước hoa hiện tại không được vượt quá dung tích tối đa.')
      return
    }

    const selectedMachine = machines.find(
      (machine) => machine.id === Number(machineId)
    )

    const selectedProduct = products.find(
      (product) => product.id === Number(productId)
    )

    if (!selectedMachine) {
      setError('Không tìm thấy máy đã chọn.')
      return
    }

    onSave({
      ...(slot ? { id: slot.id } : {}),
      machineId: Number(machineId),
      machineCode: selectedMachine.machineCode,
      slotNumber: number,
      productId: productId ? Number(productId) : null,
      productName: selectedProduct?.name || '',
      capacityMl: capacity,
      currentVolumeMl: currentVolume,
      lowStockThresholdMl: threshold,
      status,
      createdAt: slot?.createdAt || new Date().toISOString().slice(0, 10),
      updatedAt: new Date().toISOString().slice(0, 10),
    })
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <form
        onSubmit={handleSubmit}
        className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white shadow-xl"
      >
        <div className="sticky top-0 flex items-center justify-between border-b border-gray-100 bg-white px-6 py-5">
          <div>
            <h2 className="text-xl font-bold text-gray-900">
              {slot ? 'Chỉnh sửa khe máy' : 'Thêm khe máy mới'}
            </h2>
            <p className="mt-1 text-sm text-gray-500">
              Quản lý cấu hình và lượng nước hoa trong khe
            </p>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-2 text-gray-400 hover:bg-gray-100"
          >
            ✕
          </button>
        </div>

        <div className="space-y-5 p-6">
          <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Máy nước hoa <span className="text-red-500">*</span>
              </label>
              <select
                value={machineId}
                onChange={(e) => setMachineId(e.target.value)}
                required
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              >
                <option value="">Chọn máy</option>
                {machines.map((machine) => (
                  <option key={machine.id} value={machine.id}>
                    {machine.machineCode}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Số khe <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                min="1"
                value={slotNumber}
                onChange={(e) => setSlotNumber(e.target.value)}
                required
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div className="md:col-span-2">
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Sản phẩm nước hoa
              </label>
              <select
                value={productId}
                onChange={(e) => setProductId(e.target.value)}
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              >
                <option value="">Chưa gán sản phẩm</option>
                {products.map((product) => (
                  <option key={product.id} value={product.id}>
                    {product.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Dung tích tối đa (ml)
              </label>
              <input
                type="number"
                min="1"
                value={capacityMl}
                onChange={(e) => setCapacityMl(e.target.value)}
                required
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Lượng nước hoa hiện tại (ml)
              </label>
              <input
                type="number"
                min="0"
                value={currentVolumeMl}
                onChange={(e) => setCurrentVolumeMl(e.target.value)}
                required
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Ngưỡng cảnh báo (ml)
              </label>
              <input
                type="number"
                min="0"
                value={lowStockThresholdMl}
                onChange={(e) => setLowStockThresholdMl(e.target.value)}
                required
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Trạng thái
              </label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value as Slot['status'])}
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              >
                <option value="AVAILABLE">Sẵn sàng</option>
                <option value="LOW_STOCK">Sắp hết</option>
                <option value="EMPTY">Đã hết</option>
                <option value="DISABLED">Đã vô hiệu hóa</option>
                <option value="ERROR">Đang gặp lỗi</option>
              </select>
            </div>
          </div>

          {error && (
            <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600">
              {error}
            </p>
          )}
        </div>

        <div className="sticky bottom-0 flex justify-end gap-3 border-t border-gray-100 bg-white px-6 py-4">
          <button
            type="button"
            onClick={onClose}
            className="rounded-xl border border-gray-200 px-5 py-2.5 text-sm font-semibold text-gray-600 hover:bg-gray-50"
          >
            Hủy
          </button>

          <button
            type="submit"
            className="rounded-xl bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700"
          >
            {slot ? 'Lưu thay đổi' : 'Thêm khe máy'}
          </button>
        </div>
      </form>
    </div>
  )
}
