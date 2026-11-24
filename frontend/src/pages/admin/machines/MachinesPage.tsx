import { useMemo, useState } from 'react'
import {
  Plus,
  Search,
  Eye,
  Pencil,
  Ban,
  Cpu,
  Wifi,
  WifiOff,
  CheckCircle2,
  AlertCircle,
} from 'lucide-react'

import type { Machine } from '../../../types/machine'
import MachineForm from './MachineForm'
import MachineDetail from './MachineDetail'

const initialMachines: Machine[] = [
  {
    id: 1,
    tenantId: 1,
    tenantName: 'ScentStation Demo',
    locationId: 1,
locationName: 'Vincom Center',
    machineCode: 'SCENT-M01',
    serialNumber: 'SN-SCENT-001',
    deviceIdentity: 'DEVICE-SCENT-001',
    connectionStatus: 'ONLINE',
    operationalStatus: 'ACTIVE',
    firmwareVersion: '1.0.2',
    lastPingAt: '2026-11-19T08:30:00',
    createdAt: '2026-11-10',
    updatedAt: '2026-11-19',
  },
  {
    id: 2,
    tenantId: 1,
    tenantName: 'ScentStation Demo',
    locationId: 2,
    locationName: 'Aeon Mall Tân Phú',
    machineCode: 'SCENT-M02',
    serialNumber: 'SN-SCENT-002',
    deviceIdentity: 'DEVICE-SCENT-002',
    connectionStatus: 'OFFLINE',
    operationalStatus: 'MAINTENANCE',
    firmwareVersion: '1.0.1',
    lastPingAt: '2026-11-18T14:20:00',
    createdAt: '2026-11-11',
    updatedAt: '2026-11-18',
  },
  {
    id: 3,
    tenantId: 2,
    tenantName: 'Brand Aroma',
    locationId: 3,
    locationName: 'Landmark 81',
    machineCode: 'AROMA-M01',
    serialNumber: 'SN-AROMA-001',
    deviceIdentity: 'DEVICE-AROMA-001',
    connectionStatus: 'ONLINE',
    operationalStatus: 'ACTIVE',
    firmwareVersion: '1.1.0',
    lastPingAt: '2026-11-19T09:00:00',
    createdAt: '2026-11-12',
    updatedAt: '2026-11-19',
  },
  {
    id: 4,
    tenantId: 3,
    tenantName: 'Brand Luxury',
    locationId: 4,
    locationName: 'Takashimaya',
    machineCode: 'LUXURY-M01',
    serialNumber: 'SN-LUXURY-001',
    deviceIdentity: 'DEVICE-LUXURY-001',
    connectionStatus: 'ONLINE',
    operationalStatus: 'ERROR',
    firmwareVersion: '1.0.0',
    lastPingAt: '2026-11-19T07:15:00',
    createdAt: '2026-11-14',
    updatedAt: '2026-11-19',
  },
]

const tenants = [
  { id: 1, name: 'ScentStation Demo' },
  { id: 2, name: 'Brand Aroma' },
  { id: 3, name: 'Brand Luxury' },
]
const locations = [
  { id: 1, tenantId: 1, name: 'Vincom Center' },
  { id: 2, tenantId: 1, name: 'Aeon Mall Tân Phú' },
  { id: 3, tenantId: 2, name: 'Landmark 81' },
  { id: 4, tenantId: 3, name: 'Takashimaya' },
]

export default function MachinesPage() {
  const [machines, setMachines] = useState<Machine[]>(initialMachines)
  const [search, setSearch] = useState('')
  const [tenantFilter, setTenantFilter] = useState('ALL')
  const [statusFilter, setStatusFilter] = useState('ALL')

  const [selectedMachine, setSelectedMachine] = useState<Machine | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [editingMachine, setEditingMachine] = useState<Machine | null>(null)

  const getTenantName = (tenantId: number) =>
    tenants.find((tenant) => tenant.id === tenantId)?.name || 'Chưa xác định'

  const getLocationName = (machine: Machine) => {
    return machine.locationName || 'Chưa gán địa điểm'
  }

  const filteredMachines = useMemo(() => {
    return machines.filter((machine) => {
      const keyword = search.toLowerCase()

      const matchesSearch =
        machine.machineCode.toLowerCase().includes(keyword) ||
        machine.serialNumber.toLowerCase().includes(keyword)

      const matchesTenant =
        tenantFilter === 'ALL' ||
        String(machine.tenantId) === tenantFilter

      const matchesStatus =
        statusFilter === 'ALL' ||
        machine.operationalStatus === statusFilter

      return matchesSearch && matchesTenant && matchesStatus
    })
  }, [machines, search, tenantFilter, statusFilter])

  const onlineCount = machines.filter(
    (machine) => machine.connectionStatus === 'ONLINE',
  ).length

  const maintenanceCount = machines.filter(
    (machine) => machine.operationalStatus === 'MAINTENANCE',
  ).length

  const handleSave = (data: Partial<Machine>) => {
    if (editingMachine) {
      setMachines((current) =>
        current.map((machine) =>
          machine.id === editingMachine.id
            ? { ...machine, ...data, updatedAt: new Date().toISOString() }
            : machine,
        ),
      )
    } else {
      const newMachine = {
        ...data,
        id: Math.max(0, ...machines.map((machine) => machine.id)) + 1,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      } as Machine

      setMachines((current) => [...current, newMachine])
    }

    setShowForm(false)
    setEditingMachine(null)
  }

  const handleDisable = (machine: Machine) => {
    const confirmed = window.confirm(
      `Bạn có chắc muốn ngừng hoạt động máy ${machine.machineCode}?`,
    )

    if (!confirmed) return

    setMachines((current) =>
      current.map((item) =>
        item.id === machine.id
          ? {
              ...item,
              operationalStatus: 'DISABLED',
              updatedAt: new Date().toISOString(),
            }
          : item,
      ),
    )
  }

  const statusInfo = (status: Machine['operationalStatus']) => {
    const statuses = {
      ACTIVE: {
        label: 'Đang hoạt động',
        className: 'bg-emerald-50 text-emerald-700',
      },
      MAINTENANCE: {
        label: 'Đang bảo trì',
        className: 'bg-amber-50 text-amber-700',
      },
      DISABLED: {
        label: 'Ngừng hoạt động',
        className: 'bg-slate-100 text-slate-600',
      },
      ERROR: {
        label: 'Đang gặp lỗi',
        className: 'bg-red-50 text-red-700',
      },
    }

    return statuses[status]
  }

  const formatDate = (date?: string | null) => {
    if (!date) return '—'
    return new Date(date).toLocaleDateString('vi-VN')
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Quản lý máy nước hoa
          </h1>
          <p className="mt-1 text-sm text-[#95877c]">
            Quản lý thiết bị và trạng thái hoạt động của các thương hiệu
          </p>
        </div>

        <button
          onClick={() => {
            setEditingMachine(null)
            setShowForm(true)
          }}
          className="flex items-center gap-2 rounded-xl bg-[#5865f2] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#4854df]"
        >
          <Plus size={18} />
          Thêm máy
        </button>
      </div>

      <div className="grid grid-cols-1 gap-5 md:grid-cols-3">
        <div className="rounded-2xl border border-[#e9e5f5] bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <p className="text-sm text-[#95877c]">Tổng số máy</p>
            <div className="rounded-xl bg-[#f1efff] p-3 text-[#5865f2]">
              <Cpu size={21} />
            </div>
          </div>
          <p className="mt-4 text-3xl font-bold text-[#302720]">
            {machines.length}
          </p>
          <p className="mt-2 text-xs text-[#95877c]">
            Tổng thiết bị trên hệ thống
          </p>
        </div>

        <div className="rounded-2xl border border-[#e9e5f5] bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <p className="text-sm text-[#95877c]">Đang kết nối</p>
            <div className="rounded-xl bg-emerald-50 p-3 text-emerald-600">
              <Wifi size={21} />
            </div>
          </div>
          <p className="mt-4 text-3xl font-bold text-[#302720]">
            {onlineCount}
          </p>
          <p className="mt-2 text-xs text-[#95877c]">
            Thiết bị đang trực tuyến
          </p>
        </div>

        <div className="rounded-2xl border border-[#e9e5f5] bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <p className="text-sm text-[#95877c]">Cần chú ý</p>
            <div className="rounded-xl bg-amber-50 p-3 text-amber-600">
              <AlertCircle size={21} />
            </div>
          </div>
          <p className="mt-4 text-3xl font-bold text-[#302720]">
            {maintenanceCount}
          </p>
          <p className="mt-2 text-xs text-[#95877c]">
            Máy đang trong quá trình bảo trì
          </p>
        </div>
      </div>

      <div className="overflow-hidden rounded-2xl border border-[#e9e5f5] bg-white shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-4 border-b border-[#eeeaf5] p-5">
          <div>
            <h2 className="font-semibold text-[#302720]">
              Danh sách máy nước hoa
            </h2>
            <p className="mt-1 text-sm text-[#95877c]">
              Hiển thị {filteredMachines.length} / {machines.length} máy
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            <div className="relative">
              <Search
                size={17}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-[#95877c]"
              />
              <input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Tìm mã máy, serial..."
                className="w-60 rounded-xl border border-[#e9e5f5] bg-[#f8f7ff] py-3 pl-10 pr-3 text-sm outline-none focus:border-[#5865f2]"
              />
            </div>

            <select
              value={tenantFilter}
              onChange={(event) => setTenantFilter(event.target.value)}
              className="rounded-xl border border-[#e9e5f5] bg-white px-4 py-3 text-sm text-[#62564d] outline-none focus:border-[#5865f2]"
            >
              <option value="ALL">Tất cả thương hiệu</option>
              {tenants.map((tenant) => (
                <option key={tenant.id} value={tenant.id}>
                  {tenant.name}
                </option>
              ))}
            </select>

            <select
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
              className="rounded-xl border border-[#e9e5f5] bg-white px-4 py-3 text-sm text-[#62564d] outline-none focus:border-[#5865f2]"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="ACTIVE">Đang hoạt động</option>
              <option value="MAINTENANCE">Đang bảo trì</option>
              <option value="DISABLED">Ngừng hoạt động</option>
              <option value="ERROR">Đang gặp lỗi</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[1050px] text-left">
            <thead className="bg-[#f7f6fc] text-sm text-[#756b85]">
              <tr>
                <th className="px-6 py-4 font-medium">Máy nước hoa</th>
                <th className="px-6 py-4 font-medium">Mã Serial</th>
                <th className="px-6 py-4 font-medium">Thương hiệu</th>
                <th className="px-6 py-4 font-medium">Địa điểm</th>
                <th className="px-6 py-4 font-medium">Kết nối</th>
                <th className="px-6 py-4 font-medium">Trạng thái</th>
                <th className="px-6 py-4 font-medium">Ngày tạo</th>
                <th className="px-6 py-4 text-center font-medium">Thao tác</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-[#eeeaf5]">
              {filteredMachines.map((machine) => {
                const status = statusInfo(machine.operationalStatus)

                return (
                  <tr
                    key={machine.id}
                    className="transition hover:bg-[#faf9ff]"
                  >
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="rounded-xl bg-[#f1efff] p-3 text-[#5865f2]">
                          <Cpu size={20} />
                        </div>
                        <div>
                          <p className="font-semibold text-[#302720]">
                            {machine.machineCode}
                          </p>
                          <p className="mt-1 text-xs text-[#95877c]">
                            {machine.firmwareVersion}
                          </p>
                        </div>
                      </div>
                    </td>

                    <td className="px-6 py-4 text-sm text-[#5865f2]">
                      {machine.serialNumber}
                    </td>

                    <td className="px-6 py-4 text-sm text-[#756b85]">
                      {getTenantName(machine.tenantId)}
                    </td>

                    <td className="px-6 py-4 text-sm text-[#756b85]">
                      {getLocationName(machine)}
                    </td>

                    <td className="px-6 py-4">
                      {machine.connectionStatus === 'ONLINE' ? (
                        <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-2 text-xs font-medium text-emerald-700">
                          <CheckCircle2 size={14} />
                          Online
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1.5 rounded-full bg-slate-100 px-3 py-2 text-xs font-medium text-slate-600">
                          <WifiOff size={14} />
                          Offline
                        </span>
                      )}
                    </td>

                    <td className="px-6 py-4">
                      <span
                        className={`rounded-full px-3 py-2 text-xs font-medium ${status.className}`}
                      >
                        {status.label}
                      </span>
                    </td>

                    <td className="px-6 py-4 text-sm text-[#95877c]">
                      {formatDate(machine.createdAt)}
                    </td>

                    <td className="px-6 py-4">
                      <div className="flex justify-center gap-2">
                        <button
                          title="Xem chi tiết"
                          onClick={() => setSelectedMachine(machine)}
                          className="rounded-lg border border-[#e9e5f5] p-2 text-[#756b85] transition hover:border-[#5865f2] hover:text-[#5865f2]"
                        >
                          <Eye size={17} />
                        </button>

                        <button
                          title="Chỉnh sửa"
                          onClick={() => {
                            setEditingMachine(machine)
                            setShowForm(true)
                          }}
                          className="rounded-lg border border-[#e9e5f5] p-2 text-[#756b85] transition hover:border-[#5865f2] hover:text-[#5865f2]"
                        >
                          <Pencil size={17} />
                        </button>

                        <button
                          title="Ngừng hoạt động"
                          onClick={() => handleDisable(machine)}
                          disabled={machine.operationalStatus === 'DISABLED'}
                          className="rounded-lg border border-[#e9e5f5] p-2 text-[#756b85] transition hover:border-red-400 hover:text-red-500 disabled:cursor-not-allowed disabled:opacity-40"
                        >
                          <Ban size={17} />
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}

              {filteredMachines.length === 0 && (
                <tr>
                  <td
                    colSpan={8}
                    className="px-6 py-12 text-center text-sm text-[#95877c]"
                  >
                    Không tìm thấy máy nước hoa phù hợp
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="border-t border-[#eeeaf5] px-6 py-4 text-xs text-[#95877c]">
          Dữ liệu minh họa phục vụ phát triển giao diện Frontend
        </div>
      </div>

      {showForm && (
        <MachineForm
          initialData={editingMachine}
          tenants={tenants}
          locations={locations}
          onClose={() => {
            setShowForm(false)
            setEditingMachine(null)
          }}
          onSubmit={handleSave}
        />
      )}

      {selectedMachine && (
        <MachineDetail
          machine={selectedMachine}
          tenantName={getTenantName(selectedMachine.tenantId)}
          locationName={getLocationName(selectedMachine)}
          onClose={() => setSelectedMachine(null)}
        />
      )}
    </div>
  )
}
