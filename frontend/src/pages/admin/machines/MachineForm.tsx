import { useEffect, useMemo, useState } from 'react'
import { X, Cpu } from 'lucide-react'
import type {
  Machine,
  MachineRequest,
  MachineConnectionStatus,
  MachineOperationalStatus,
} from '../../../types/machine'

interface MachineFormProps {
  initialData?: Machine | null
  tenants: { id: number; name: string }[]
  locations: { id: number; tenantId: number; name: string }[]
  onClose: () => void
  onSubmit: (data: MachineRequest) => void
}

export default function MachineForm({
  initialData,
  tenants,
  locations,
  onClose,
  onSubmit,
}: MachineFormProps) {
  const [formData, setFormData] = useState<MachineRequest>({
    tenantId: tenants[0]?.id ?? 0,
    locationId: null,
    machineCode: '',
    serialNumber: '',
    deviceIdentity: '',
    connectionStatus: 'OFFLINE',
    operationalStatus: 'ACTIVE',
    firmwareVersion: '',
  })

  const [errors, setErrors] = useState({
    tenantId: '',
    machineCode: '',
    serialNumber: '',
    deviceIdentity: '',
    firmwareVersion: '',
  })

  useEffect(() => {
    if (initialData) {
      setFormData({
        tenantId: initialData.tenantId,
        locationId: initialData.locationId,
        machineCode: initialData.machineCode,
        serialNumber: initialData.serialNumber,
        deviceIdentity: initialData.deviceIdentity,
        connectionStatus: initialData.connectionStatus,
        operationalStatus: initialData.operationalStatus,
        firmwareVersion: initialData.firmwareVersion,
      })
    } else {
      setFormData({
        tenantId: tenants[0]?.id ?? 0,
        locationId: null,
        machineCode: '',
        serialNumber: '',
        deviceIdentity: '',
        connectionStatus: 'OFFLINE',
        operationalStatus: 'ACTIVE',
        firmwareVersion: '',
      })
    }
  }, [initialData, tenants])

  const filteredLocations = useMemo(
    () => locations.filter(
      (location) => location.tenantId === formData.tenantId,
    ),
    [locations, formData.tenantId],
  )

  const handleChange = (
    field: keyof MachineRequest,
    value: string | number | null,
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

  const handleTenantChange = (tenantId: number) => {
    setFormData((prev) => ({
      ...prev,
      tenantId,
      locationId: null,
    }))

    setErrors((prev) => ({
      ...prev,
      tenantId: '',
    }))
  }

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    const nextErrors = {
      tenantId: formData.tenantId ? '' : 'Vui lòng chọn thương hiệu',
      machineCode: formData.machineCode.trim()
        ? ''
        : 'Vui lòng nhập mã máy',
      serialNumber: formData.serialNumber.trim()
        ? ''
        : 'Vui lòng nhập số serial',
      deviceIdentity: formData.deviceIdentity.trim()
        ? ''
        : 'Vui lòng nhập định danh thiết bị',
      firmwareVersion: formData.firmwareVersion.trim()
        ? ''
        : 'Vui lòng nhập phiên bản firmware',
    }

    setErrors(nextErrors)

    if (Object.values(nextErrors).some(Boolean)) return

    onSubmit({
      ...formData,
      machineCode: formData.machineCode.trim().toUpperCase(),
      serialNumber: formData.serialNumber.trim(),
      deviceIdentity: formData.deviceIdentity.trim(),
      firmwareVersion: formData.firmwareVersion.trim(),
    })
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#eee7e1] px-6 py-5">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-[#eef0ff] p-3 text-[#5865f2]">
              <Cpu size={22} />
            </div>

            <div>
              <h2 className="text-lg font-bold text-[#302720]">
                {initialData ? 'Chỉnh sửa máy nước hoa' : 'Thêm máy nước hoa mới'}
              </h2>
              <p className="mt-1 text-sm text-[#95877c]">
                Quản lý thông tin và cấu hình thiết bị
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
          <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Thương hiệu <span className="text-red-500">*</span>
              </label>

              <select
                value={formData.tenantId}
                onChange={(e) => handleTenantChange(Number(e.target.value))}
                className="w-full rounded-xl border border-[#e8e5f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              >
                <option value={0}>Chọn thương hiệu</option>
                {tenants.map((tenant) => (
                  <option key={tenant.id} value={tenant.id}>
                    {tenant.name}
                  </option>
                ))}
              </select>

              {errors.tenantId && (
                <p className="mt-1 text-xs text-red-500">{errors.tenantId}</p>
              )}
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Địa điểm lắp đặt
              </label>

              <select
                value={formData.locationId ?? ''}
                onChange={(e) =>
                  handleChange(
                    'locationId',
                    e.target.value ? Number(e.target.value) : null,
                  )
                }
                className="w-full rounded-xl border border-[#e8e5f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              >
                <option value="">Chưa gán địa điểm</option>
                {filteredLocations.map((location) => (
                  <option key={location.id} value={location.id}>
                    {location.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Mã máy <span className="text-red-500">*</span>
              </label>

              <input
                value={formData.machineCode}
                onChange={(e) => handleChange('machineCode', e.target.value)}
                placeholder="VD: MACHINE-001"
                className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm uppercase outline-none focus:border-[#5865f2]"
              />

              {errors.machineCode && (
                <p className="mt-1 text-xs text-red-500">{errors.machineCode}</p>
              )}
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Serial Number <span className="text-red-500">*</span>
              </label>

              <input
                value={formData.serialNumber}
                onChange={(e) => handleChange('serialNumber', e.target.value)}
                placeholder="VD: SN-2026-0001"
                className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              />

              {errors.serialNumber && (
                <p className="mt-1 text-xs text-red-500">{errors.serialNumber}</p>
              )}
            </div>

            <div className="md:col-span-2">
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Device Identity <span className="text-red-500">*</span>
              </label>

              <input
                value={formData.deviceIdentity}
                onChange={(e) => handleChange('deviceIdentity', e.target.value)}
                placeholder="VD: scentstation-device-001"
                className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              />

              {errors.deviceIdentity && (
                <p className="mt-1 text-xs text-red-500">{errors.deviceIdentity}</p>
              )}
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Phiên bản Firmware <span className="text-red-500">*</span>
              </label>

              <input
                value={formData.firmwareVersion}
                onChange={(e) => handleChange('firmwareVersion', e.target.value)}
                placeholder="VD: v1.0.0"
                className="w-full rounded-xl border border-[#e8e5f5] px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              />

              {errors.firmwareVersion && (
                <p className="mt-1 text-xs text-red-500">{errors.firmwareVersion}</p>
              )}
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Trạng thái kết nối
              </label>

              <select
                value={formData.connectionStatus}
                onChange={(e) =>
                  handleChange(
                    'connectionStatus',
                    e.target.value as MachineConnectionStatus,
                  )
                }
                className="w-full rounded-xl border border-[#e8e5f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              >
                <option value="ONLINE">Đang kết nối</option>
                <option value="OFFLINE">Ngoại tuyến</option>
              </select>
            </div>

            <div className="md:col-span-2">
              <label className="mb-2 block text-sm font-medium text-[#302720]">
                Trạng thái vận hành
              </label>

              <select
                value={formData.operationalStatus}
                onChange={(e) =>
                  handleChange(
                    'operationalStatus',
                    e.target.value as MachineOperationalStatus,
                  )
                }
                className="w-full rounded-xl border border-[#e8e5f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              >
                <option value="ACTIVE">Đang hoạt động</option>
                <option value="MAINTENANCE">Đang bảo trì</option>
                <option value="DISABLED">Đã vô hiệu hóa</option>
                <option value="ERROR">Đang gặp lỗi</option>
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
              {initialData ? 'Lưu thay đổi' : 'Thêm máy'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
