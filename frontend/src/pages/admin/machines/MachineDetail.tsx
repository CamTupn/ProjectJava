import { X, Cpu, Building2, MapPin, Wifi, WifiOff, CalendarDays, Hash, Fingerprint } from 'lucide-react'
import type { Machine } from '../../../types/machine'

interface MachineDetailProps {
  machine: Machine
  tenantName: string
  locationName?: string
  onClose: () => void
}

export default function MachineDetail({
  machine,
  tenantName,
  locationName,
  onClose,
}: MachineDetailProps) {
  const connectionInfo = {
    ONLINE: {
      label: 'Đang kết nối',
      className: 'bg-emerald-50 text-emerald-700',
      icon: <Wifi size={16} />,
    },
    OFFLINE: {
      label: 'Ngoại tuyến',
      className: 'bg-slate-100 text-slate-600',
      icon: <WifiOff size={16} />,
    },
  }[machine.connectionStatus]

  const operationalInfo = {
    ACTIVE: {
      label: 'Đang hoạt động',
      className: 'bg-emerald-50 text-emerald-700',
    },
    MAINTENANCE: {
      label: 'Đang bảo trì',
      className: 'bg-amber-50 text-amber-700',
    },
    DISABLED: {
      label: 'Đã vô hiệu hóa',
      className: 'bg-slate-100 text-slate-600',
    },
    ERROR: {
      label: 'Đang gặp lỗi',
      className: 'bg-red-50 text-red-700',
    },
  }[machine.operationalStatus]

  const formatDate = (date?: string | null) => {
    if (!date) return 'Chưa có dữ liệu'

    return new Date(date).toLocaleDateString('vi-VN')
  }

  const DetailItem = ({
    icon,
    label,
    value,
  }: {
    icon: React.ReactNode
    label: string
    value?: string | number | null
  }) => (
    <div className="flex items-start gap-3">
      <div className="mt-0.5 rounded-lg bg-[#f1efff] p-2 text-[#5865f2]">
        {icon}
      </div>

      <div className="min-w-0">
        <p className="text-xs text-[#95877c]">{label}</p>
        <p className="mt-1 break-words text-sm font-medium text-[#302720]">
          {value || 'Chưa cập nhật'}
        </p>
      </div>
    </div>
  )

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div className="max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-2xl bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#eee7e1] px-6 py-5">
          <div>
            <h2 className="text-lg font-bold text-[#302720]">
              Chi tiết máy nước hoa
            </h2>
            <p className="mt-1 text-sm text-[#95877c]">
              Thông tin thiết bị trên hệ thống
            </p>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-2 text-[#95877c] transition hover:bg-[#f1efff] hover:text-[#5865f2]"
          >
            <X size={20} />
          </button>
        </div>

        <div className="space-y-6 p-6">
          <div className="flex items-center gap-4 rounded-2xl bg-[#f5f4ff] p-5">
            <div className="rounded-xl bg-white p-4 text-[#5865f2]">
              <Cpu size={30} />
            </div>

            <div className="min-w-0">
              <h3 className="text-lg font-bold text-[#302720]">
                {machine.machineCode}
              </h3>
              <p className="mt-1 text-sm text-[#5865f2]">
                {tenantName}
              </p>
            </div>
          </div>

          <div>
            <h3 className="mb-4 text-sm font-semibold text-[#302720]">
              Thông tin thiết bị
            </h3>

            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
              <DetailItem
                icon={<Hash size={17} />}
                label="Mã máy"
                value={machine.machineCode}
              />

              <DetailItem
                icon={<Fingerprint size={17} />}
                label="Serial Number"
                value={machine.serialNumber}
              />

              <DetailItem
                icon={<Cpu size={17} />}
                label="Device Identity"
                value={machine.deviceIdentity}
              />

              <DetailItem
                icon={<Building2 size={17} />}
                label="Thương hiệu"
                value={tenantName}
              />

              <DetailItem
                icon={<MapPin size={17} />}
                label="Địa điểm lắp đặt"
                value={locationName || 'Chưa gán địa điểm'}
              />

              <DetailItem
                icon={<Cpu size={17} />}
                label="Phiên bản Firmware"
                value={machine.firmwareVersion}
              />
            </div>
          </div>

          <div className="border-t border-[#eee7e1] pt-5">
            <h3 className="mb-4 text-sm font-semibold text-[#302720]">
              Trạng thái thiết bị
            </h3>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div className="rounded-xl border border-[#eee7e1] p-4">
                <p className="mb-3 text-xs text-[#95877c]">
                  Trạng thái kết nối
                </p>

                <span
                  className={`inline-flex items-center gap-2 rounded-full px-3 py-2 text-xs font-medium ${connectionInfo.className}`}
                >
                  {connectionInfo.icon}
                  {connectionInfo.label}
                </span>
              </div>

              <div className="rounded-xl border border-[#eee7e1] p-4">
                <p className="mb-3 text-xs text-[#95877c]">
                  Trạng thái vận hành
                </p>

                <span
                  className={`inline-flex rounded-full px-3 py-2 text-xs font-medium ${operationalInfo.className}`}
                >
                  {operationalInfo.label}
                </span>
              </div>
            </div>
          </div>

          <div className="border-t border-[#eee7e1] pt-5">
            <h3 className="mb-4 text-sm font-semibold text-[#302720]">
              Thời gian hoạt động
            </h3>

            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
              <DetailItem
                icon={<CalendarDays size={17} />}
                label="Ngày tạo"
                value={formatDate(machine.createdAt)}
              />

              <DetailItem
                icon={<CalendarDays size={17} />}
                label="Cập nhật lần cuối"
                value={formatDate(machine.updatedAt)}
              />

              <DetailItem
                icon={<Wifi size={17} />}
                label="Lần kết nối gần nhất"
                value={formatDate(machine.lastPingAt)}
              />
            </div>
          </div>
        </div>

        <div className="flex justify-end border-t border-[#eee7e1] px-6 py-4">
          <button
            type="button"
            onClick={onClose}
            className="rounded-xl bg-[#5865f2] px-6 py-2.5 text-sm font-semibold text-white transition hover:bg-[#4854df]"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  )
}
