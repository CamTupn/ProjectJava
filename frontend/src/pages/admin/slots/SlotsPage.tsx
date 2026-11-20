import { useMemo, useState } from 'react'
import {
  Plus,
  Search,
  Eye,
  Pencil,
  Trash2,
  Package,
  Droplets,
  AlertTriangle,
  CheckCircle2,
  Cpu,
  X,
} from 'lucide-react'

import type { Slot } from './SlotDetail'
import SlotForm from './SlotForm'
import SlotDetail from './SlotDetail'
import SlotStatusBadge from './SlotStatusBadge'

const machines = [
  { id: 1, machineCode: 'SCENT-M01' },
  { id: 2, machineCode: 'SCENT-M02' },
  { id: 3, machineCode: 'AROMA-M01' },
  { id: 4, machineCode: 'LUXURY-M01' },
]

const products = [
  { id: 1, name: 'Rose Elegance' },
  { id: 2, name: 'Royal Oud' },
  { id: 3, name: 'Ocean Breeze' },
  { id: 4, name: 'Floral Dream' },
  { id: 5, name: 'White Musk' },
  { id: 6, name: 'Vanilla Bloom' },
]

const initialSlots: Slot[] = [
  {
    id: 1,
    machineId: 1,
    machineCode: 'SCENT-M01',
    slotNumber: 1,
    productId: 1,
    productName: 'Rose Elegance',
    capacityMl: 100,
    currentVolumeMl: 85,
    lowStockThresholdMl: 20,
    status: 'AVAILABLE',
    createdAt: '10/11/2026',
    updatedAt: '19/11/2026',
  },
  {
    id: 2,
    machineId: 1,
    machineCode: 'SCENT-M01',
    slotNumber: 2,
    productId: 2,
    productName: 'Royal Oud',
    capacityMl: 100,
    currentVolumeMl: 15,
    lowStockThresholdMl: 20,
    status: 'LOW_STOCK',
    createdAt: '10/11/2026',
    updatedAt: '19/11/2026',
  },
  {
    id: 3,
    machineId: 1,
    machineCode: 'SCENT-M01',
    slotNumber: 3,
    productId: 3,
    productName: 'Ocean Breeze',
    capacityMl: 150,
    currentVolumeMl: 0,
    lowStockThresholdMl: 25,
    status: 'EMPTY',
    createdAt: '11/11/2026',
    updatedAt: '19/11/2026',
  },
  {
    id: 4,
    machineId: 2,
    machineCode: 'SCENT-M02',
    slotNumber: 1,
    productId: 4,
    productName: 'Floral Dream',
    capacityMl: 100,
    currentVolumeMl: 70,
    lowStockThresholdMl: 20,
    status: 'AVAILABLE',
    createdAt: '12/11/2026',
    updatedAt: '18/11/2026',
  },
  {
    id: 5,
    machineId: 3,
    machineCode: 'AROMA-M01',
    slotNumber: 1,
    productId: 5,
    productName: 'White Musk',
    capacityMl: 120,
    currentVolumeMl: 10,
    lowStockThresholdMl: 25,
    status: 'LOW_STOCK',
    createdAt: '13/11/2026',
    updatedAt: '19/11/2026',
  },
  {
    id: 6,
    machineId: 4,
    machineCode: 'LUXURY-M01',
    slotNumber: 1,
    productId: 6,
    productName: 'Vanilla Bloom',
    capacityMl: 100,
    currentVolumeMl: 0,
    lowStockThresholdMl: 20,
    status: 'DISABLED',
    createdAt: '14/11/2026',
    updatedAt: '19/11/2026',
  },
]

export default function SlotsPage() {
  const [slots, setSlots] = useState<Slot[]>(initialSlots)
  const [keyword, setKeyword] = useState('')
  const [machineFilter, setMachineFilter] = useState('ALL')
  const [statusFilter, setStatusFilter] = useState('ALL')

  const [showForm, setShowForm] = useState(false)
  const [editingSlot, setEditingSlot] = useState<Slot | null>(null)
  const [viewingSlot, setViewingSlot] = useState<Slot | null>(null)

  const filteredSlots = useMemo(() => {
    const search = keyword.toLowerCase().trim()

    return slots.filter((slot) => {
      const matchesKeyword =
        slot.machineCode.toLowerCase().includes(search) ||
        slot.productName.toLowerCase().includes(search) ||
        `slot-${String(slot.slotNumber).padStart(2, '0')}`.includes(search)

      const matchesMachine =
        machineFilter === 'ALL' ||
        String(slot.machineId) === machineFilter

      const matchesStatus =
        statusFilter === 'ALL' || slot.status === statusFilter

      return matchesKeyword && matchesMachine && matchesStatus
    })
  }, [slots, keyword, machineFilter, statusFilter])

  const availableCount = slots.filter(
    (slot) => slot.status === 'AVAILABLE',
  ).length

  const lowStockCount = slots.filter(
    (slot) => slot.status === 'LOW_STOCK',
  ).length

  const emptyCount = slots.filter(
    (slot) => slot.status === 'EMPTY',
  ).length

  const handleSave = (data: Partial<Slot>) => {
    if (!data.machineId || !data.slotNumber) return

    const duplicatedSlot = slots.some(
      (slot) =>
        slot.machineId === data.machineId &&
        slot.slotNumber === data.slotNumber &&
        slot.id !== editingSlot?.id,
    )

    if (duplicatedSlot) {
      window.alert('Số khe này đã tồn tại trên máy được chọn.')
      return
    }

    if (editingSlot) {
      setSlots((prev) =>
        prev.map((slot) =>
          slot.id === editingSlot.id
            ? { ...slot, ...data }
            : slot,
        ),
      )
    } else {
      const newSlot = {
        ...data,
        id: Math.max(0, ...slots.map((slot) => slot.id)) + 1,
      } as Slot

      setSlots((prev) => [newSlot, ...prev])
    }

    setShowForm(false)
    setEditingSlot(null)
  }

  const handleDelete = (slot: Slot) => {
    const confirmed = window.confirm(
      `Bạn có chắc muốn xóa khe SLOT-${String(slot.slotNumber).padStart(2, '0')} của máy ${slot.machineCode}?`,
    )

    if (!confirmed) return

    setSlots((prev) =>
      prev.filter((item) => item.id !== slot.id),
    )

    if (viewingSlot?.id === slot.id) {
      setViewingSlot(null)
    }
  }

  const handleEdit = (slot: Slot) => {
    setEditingSlot(slot)
    setShowForm(true)
  }

  const formatPercent = (slot: Slot) => {
    if (slot.capacityMl <= 0) return 0

    return Math.min(
      100,
      Math.max(0, (slot.currentVolumeMl / slot.capacityMl) * 100),
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Quản lý khe máy
          </h1>
          <p className="mt-1 text-sm text-[#95877c]">
            Quản lý sản phẩm và dung tích nước hoa trong từng khe máy
          </p>
        </div>

        <button
          onClick={() => {
            setEditingSlot(null)
            setShowForm(true)
          }}
          className="flex items-center justify-center gap-2 rounded-xl bg-[#5865f2] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#4854df]"
        >
          <Plus size={18} />
          Thêm khe máy
        </button>
      </div>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Tổng số khe"
          value={slots.length}
          icon={<Package size={21} />}
        />
        <StatCard
          title="Sẵn sàng"
          value={availableCount}
          icon={<CheckCircle2 size={21} />}
        />
        <StatCard
          title="Sắp hết nước hoa"
          value={lowStockCount}
          icon={<AlertTriangle size={21} />}
        />
        <StatCard
          title="Đã hết nước hoa"
          value={emptyCount}
          icon={<Droplets size={21} />}
        />
      </div>

      <div className="overflow-hidden rounded-2xl border border-[#e8e5f5] bg-white shadow-sm">
        <div className="flex flex-col gap-4 border-b border-[#eeeaf8] p-5 xl:flex-row xl:items-center xl:justify-between">
          <div>
            <h2 className="font-bold text-[#302720]">
              Danh sách khe máy
            </h2>
            <p className="mt-1 text-sm text-[#95877c]">
              Hiển thị {filteredSlots.length} / {slots.length} khe máy
            </p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row">
            <div className="flex items-center gap-2 rounded-xl border border-[#e8e5f5] bg-[#f7f6ff] px-3">
              <Search size={18} className="text-[#95877c]" />
              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Tìm mã máy, sản phẩm..."
                className="w-full bg-transparent py-3 text-sm outline-none sm:w-52"
              />
              {keyword && (
                <button
                  onClick={() => setKeyword('')}
                  aria-label="Xóa tìm kiếm"
                >
                  <X size={16} className="text-[#95877c]" />
                </button>
              )}
            </div>

            <select
              value={machineFilter}
              onChange={(e) => setMachineFilter(e.target.value)}
              className="rounded-xl border border-[#e8e5f5] bg-white px-3 py-3 text-sm text-[#76675c] outline-none focus:border-[#5865f2]"
            >
              <option value="ALL">Tất cả máy</option>
              {machines.map((machine) => (
                <option key={machine.id} value={machine.id}>
                  {machine.machineCode}
                </option>
              ))}
            </select>

            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="rounded-xl border border-[#e8e5f5] bg-white px-3 py-3 text-sm text-[#76675c] outline-none focus:border-[#5865f2]"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="AVAILABLE">Sẵn sàng</option>
              <option value="LOW_STOCK">Sắp hết</option>
              <option value="EMPTY">Đã hết</option>
              <option value="DISABLED">Đã vô hiệu hóa</option>
              <option value="ERROR">Đang gặp lỗi</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[1000px] text-left text-sm">
            <thead className="bg-[#f7f6ff] text-[#766b91]">
              <tr>
                <th className="px-6 py-4 font-medium">Khe máy</th>
                <th className="px-6 py-4 font-medium">Máy nước hoa</th>
                <th className="px-6 py-4 font-medium">Sản phẩm</th>
                <th className="px-6 py-4 font-medium">Dung tích</th>
                <th className="px-6 py-4 font-medium">Mức nước hoa</th>
                <th className="px-6 py-4 font-medium">Trạng thái</th>
                <th className="px-6 py-4 text-center font-medium">Thao tác</th>
              </tr>
            </thead>

            <tbody>
              {filteredSlots.map((slot) => {
                const percent = formatPercent(slot)

                return (
                  <tr
                    key={slot.id}
                    className="border-b border-[#f0edf8] transition hover:bg-[#faf9ff] last:border-0"
                  >
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="rounded-xl bg-[#eef0ff] p-3 text-[#5865f2]">
                          <Package size={20} />
                        </div>
                        <div>
                          <p className="font-semibold text-[#302720]">
                            SLOT-{String(slot.slotNumber).padStart(2, '0')}
                          </p>
                          <p className="mt-1 text-xs text-[#95877c]">
                            ID: {slot.id}
                          </p>
                        </div>
                      </div>
                    </td>

                    <td className="px-6 py-4 font-medium text-[#5865f2]">
                      <div className="flex items-center gap-2">
                        <Cpu size={16} />
                        {slot.machineCode}
                      </div>
                    </td>

                    <td className="px-6 py-4 text-[#76675c]">
                      {slot.productName || 'Chưa gán sản phẩm'}
                    </td>

                    <td className="px-6 py-4 text-[#76675c]">
                      {slot.currentVolumeMl} / {slot.capacityMl} ml
                    </td>

                    <td className="px-6 py-4">
                      <div className="w-36">
                        <div className="mb-2 flex justify-between text-xs">
                          <span className="font-semibold text-[#302720]">
                            {percent.toFixed(0)}%
                          </span>
                          <span className="text-[#95877c]">
                            {slot.currentVolumeMl} ml
                          </span>
                        </div>
                        <div className="h-2 overflow-hidden rounded-full bg-[#f1ece7]">
                          <div
                            className={`h-full rounded-full ${
                              percent <= 20
                                ? 'bg-red-500'
                                : percent <= 40
                                  ? 'bg-amber-500'
                                  : 'bg-[#5865f2]'
                            }`}
                            style={{ width: `${percent}%` }}
                          />
                        </div>
                      </div>
                    </td>

                    <td className="px-6 py-4">
                      <SlotStatusBadge status={slot.status} />
                    </td>

                    <td className="px-6 py-4">
                      <div className="flex justify-center gap-2">
                        <ActionButton
                          title="Xem chi tiết"
                          onClick={() => setViewingSlot(slot)}
                        >
                          <Eye size={17} />
                        </ActionButton>

                        <ActionButton
                          title="Chỉnh sửa"
                          onClick={() => handleEdit(slot)}
                        >
                          <Pencil size={17} />
                        </ActionButton>

                        <ActionButton
                          title="Xóa khe máy"
                          onClick={() => handleDelete(slot)}
                          danger
                        >
                          <Trash2 size={17} />
                        </ActionButton>
                      </div>
                    </td>
                  </tr>
                )
              })}

              {filteredSlots.length === 0 && (
                <tr>
                  <td
                    colSpan={7}
                    className="px-6 py-12 text-center text-[#95877c]"
                  >
                    Không tìm thấy khe máy phù hợp.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="border-t border-[#eeeaf8] px-6 py-4 text-xs text-[#95877c]">
          Dữ liệu minh họa phục vụ phát triển giao diện Frontend
        </div>
      </div>

      {showForm && (
        <SlotForm
          slot={editingSlot}
          machines={machines}
          products={products}
          onClose={() => {
            setShowForm(false)
            setEditingSlot(null)
          }}
          onSave={handleSave}
        />
      )}

      {viewingSlot && (
        <SlotDetail
          slot={viewingSlot}
          onClose={() => setViewingSlot(null)}
        />
      )}
    </div>
  )
}

function StatCard({
  title,
  value,
  icon,
}: {
  title: string
  value: number
  icon: React.ReactNode
}) {
  return (
    <div className="rounded-2xl border border-[#e8e5f5] bg-white p-6 shadow-sm">
      <div className="flex items-center justify-between">
        <p className="text-sm text-[#95877c]">{title}</p>
        <div className="rounded-xl bg-[#eef0ff] p-3 text-[#5865f2]">
          {icon}
        </div>
      </div>
      <h2 className="mt-4 text-3xl font-bold text-[#302720]">{value}</h2>
    </div>
  )
}

function ActionButton({
  children,
  title,
  onClick,
  danger = false,
}: {
  children: React.ReactNode
  title: string
  onClick: () => void
  danger?: boolean
}) {
  return (
    <button
      type="button"
      title={title}
      onClick={onClick}
      className={`rounded-lg border p-2 transition ${
        danger
          ? 'border-[#f5dddd] text-red-500 hover:bg-red-50'
          : 'border-[#e8e5f5] text-[#766b91] hover:border-[#5865f2] hover:bg-[#eef0ff] hover:text-[#5865f2]'
      }`}
    >
      {children}
    </button>
  )
}
