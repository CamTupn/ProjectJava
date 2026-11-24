import { useRef, useState } from 'react'
import type { ReactNode, ElementType } from 'react'
import {
  Store,
  Clock,
  Droplets,
  Bell,
  Save,
  Upload,
  CheckCircle2,
  RotateCcw,
  AlertCircle,
} from 'lucide-react'

interface BrandSettings {
  name: string
  description: string
  email: string
  phone: string
  address: string
  logo: string
  openingTime: string
  closingTime: string
  sprayDuration: number
  sprayVolume: number
  lowStockAlert: boolean
  offlineAlert: boolean
  orderAlert: boolean
  completedOrderAlert: boolean
}

const STORAGE_KEY = 'scentstation_brand_settings'

const initialSettings: BrandSettings = {
  name: 'ScentStation Demo',
  description: 'Thương hiệu nước hoa trải nghiệm thông minh',
  email: 'contact@scentstation.vn',
  phone: '0901234567',
  address: 'TP. Hồ Chí Minh, Việt Nam',
  logo: '',
  openingTime: '08:00',
  closingTime: '22:00',
  sprayDuration: 2,
  sprayVolume: 0.15,
  lowStockAlert: true,
  offlineAlert: true,
  orderAlert: true,
  completedOrderAlert: false,
}

function loadSettings(): BrandSettings {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    return saved
      ? { ...initialSettings, ...JSON.parse(saved) }
      : initialSettings
  } catch {
    return initialSettings
  }
}

function InputField({
  label,
  value,
  onChange,
  placeholder,
  type = 'text',
  min,
  step,
}: {
  label: string
  value: string
  onChange: (value: string) => void
  placeholder?: string
  type?: string
  min?: number
  step?: number
}) {
  return (
    <div>
      <label className="mb-2 block text-sm font-medium text-[#514b60]">
        {label}
      </label>
      <input
        type={type}
        value={value}
        min={min}
        step={step}
        placeholder={placeholder}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded-xl border border-[#e5e4f5] bg-white px-4 py-3 text-sm outline-none transition focus:border-[#5865f2] focus:ring-2 focus:ring-[#eef0ff]"
      />
    </div>
  )
}

function SettingSection({
  title,
  description,
  icon: Icon,
  children,
}: {
  title: string
  description: string
  icon: ElementType
  children: ReactNode
}) {
  return (
    <section className="rounded-2xl border border-[#e5e4f5] bg-white p-6 shadow-sm">
      <div className="mb-6 flex items-center gap-3">
        <div className="rounded-xl bg-[#eef0ff] p-3 text-[#5865f2]">
          <Icon size={21} />
        </div>
        <div>
          <h2 className="font-bold text-[#302720]">{title}</h2>
          <p className="mt-1 text-sm text-[#95877c]">{description}</p>
        </div>
      </div>
      {children}
    </section>
  )
}

function Toggle({
  checked,
  onChange,
}: {
  checked: boolean
  onChange: (value: boolean) => void
}) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      onClick={() => onChange(!checked)}
      className={`relative h-6 w-11 shrink-0 rounded-full transition ${
        checked ? 'bg-[#5865f2]' : 'bg-gray-300'
      }`}
    >
      <span
        className={`absolute top-1 h-4 w-4 rounded-full bg-white transition ${
          checked ? 'left-6' : 'left-1'
        }`}
      />
    </button>
  )
}

export default function SettingsPage() {
  const [settings, setSettings] = useState<BrandSettings>(loadSettings)
  const [saved, setSaved] = useState(false)
  const [error, setError] = useState('')
  const fileInputRef = useRef<HTMLInputElement>(null)

  const updateField = <K extends keyof BrandSettings>(
    key: K,
    value: BrandSettings[K],
  ) => {
    setSettings((prev) => ({ ...prev, [key]: value }))
    setSaved(false)
    setError('')
  }

  const handleLogoUpload = (file?: File) => {
    if (!file) return

    if (!['image/png', 'image/jpeg', 'image/svg+xml'].includes(file.type)) {
      setError('Vui lòng chọn file PNG, JPG hoặc SVG.')
      return
    }

    if (file.size > 2 * 1024 * 1024) {
      setError('Logo không được vượt quá 2MB.')
      return
    }

    const reader = new FileReader()
    reader.onload = () => {
      updateField('logo', String(reader.result))
    }
    reader.readAsDataURL(file)
  }

  const handleSave = () => {
    if (!settings.name.trim()) {
      setError('Vui lòng nhập tên thương hiệu.')
      return
    }

    if (!settings.email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(settings.email)) {
      setError('Email liên hệ không hợp lệ.')
      return
    }

    if (settings.sprayDuration <= 0 || settings.sprayVolume <= 0) {
      setError('Thời gian xịt và lượng nước hoa phải lớn hơn 0.')
      return
    }

    if (settings.openingTime >= settings.closingTime) {
      setError('Giờ đóng cửa phải sau giờ mở cửa.')
      return
    }

    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(settings))
      setSaved(true)
      setError('')
    } catch {
      setError('Không thể lưu cấu hình. Có thể logo quá lớn.')
    }
  }

  const handleReset = () => {
    const confirmed = window.confirm(
      'Bạn có chắc muốn khôi phục toàn bộ cài đặt thương hiệu mặc định?',
    )

    if (!confirmed) return

    setSettings(initialSettings)
    localStorage.removeItem(STORAGE_KEY)
    setSaved(false)
    setError('')
  }

  const notificationItems = [
    {
      key: 'orderAlert' as const,
      title: 'Đơn hàng mới',
      description: 'Thông báo khi có đơn hàng mới được tạo.',
    },
    {
      key: 'offlineAlert' as const,
      title: 'Máy nước hoa mất kết nối',
      description: 'Thông báo khi thiết bị chuyển sang trạng thái Offline.',
    },
    {
      key: 'lowStockAlert' as const,
      title: 'Cảnh báo tồn kho thấp',
      description: 'Thông báo khi lượng nước hoa trong khe máy xuống dưới ngưỡng.',
    },
    {
      key: 'completedOrderAlert' as const,
      title: 'Đơn hàng hoàn thành',
      description: 'Thông báo khi đơn hàng được xử lý hoàn tất.',
    },
  ]

  return (
    <div className="space-y-7">
      {/* PAGE HEADER */}
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Cài đặt thương hiệu
          </h1>
          <p className="mt-1 text-sm text-[#95877c]">
            Quản lý thông tin và cấu hình hoạt động của thương hiệu
          </p>
        </div>

        <div className="flex flex-wrap gap-3">
          <button
            type="button"
            onClick={handleReset}
            className="flex items-center justify-center gap-2 rounded-xl border border-[#e5e4f5] bg-white px-5 py-3 text-sm font-semibold text-[#514b60] transition hover:bg-[#f7f7ff]"
          >
            <RotateCcw size={17} />
            Khôi phục mặc định
          </button>

          <button
            type="button"
            onClick={handleSave}
            className="flex items-center justify-center gap-2 rounded-xl bg-[#5145e5] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#4338ca]"
          >
            <Save size={17} />
            Lưu cấu hình
          </button>
        </div>
      </div>

      {/* NOTIFICATIONS */}
      {saved && (
        <div className="flex items-center gap-2 rounded-xl border border-green-200 bg-green-50 p-4 text-sm font-medium text-green-700">
          <CheckCircle2 size={18} />
          Cài đặt thương hiệu đã được lưu thành công!
        </div>
      )}

      {error && (
        <div className="flex items-center gap-2 rounded-xl border border-red-200 bg-red-50 p-4 text-sm font-medium text-red-600">
          <AlertCircle size={18} />
          {error}
        </div>
      )}

      {/* BRAND INFORMATION */}
      <SettingSection
        title="Thông tin thương hiệu"
        description="Thông tin nhận diện và liên hệ của thương hiệu"
        icon={Store}
      >
        <div className="mb-6 flex flex-wrap items-center gap-5">
          <div className="flex h-20 w-20 items-center justify-center overflow-hidden rounded-2xl bg-[#eef0ff] text-2xl font-bold text-[#5865f2]">
            {settings.logo ? (
              <img
                src={settings.logo}
                alt="Logo thương hiệu"
                className="h-full w-full object-contain"
              />
            ) : (
              'SS'
            )}
          </div>

          <div>
            <p className="font-semibold text-[#302720]">Logo thương hiệu</p>
            <p className="mt-1 text-xs text-[#95877c]">
              PNG, JPG hoặc SVG. Tối đa 2MB.
            </p>

            <input
              ref={fileInputRef}
              type="file"
              accept=".png,.jpg,.jpeg,.svg"
              className="hidden"
              onChange={(e) => handleLogoUpload(e.target.files?.[0])}
            />

            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="mt-3 flex items-center gap-2 rounded-lg border border-[#e5e4f5] px-3 py-2 text-sm font-medium text-[#5865f2] hover:bg-[#f7f7ff]"
            >
              <Upload size={15} />
              Tải logo lên
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
          <InputField
            label="Tên thương hiệu"
            value={settings.name}
            onChange={(v) => updateField('name', v)}
            placeholder="Nhập tên thương hiệu"
          />

          <InputField
            label="Email liên hệ"
            value={settings.email}
            onChange={(v) => updateField('email', v)}
            type="email"
            placeholder="example@email.com"
          />

          <InputField
            label="Số điện thoại"
            value={settings.phone}
            onChange={(v) => updateField('phone', v)}
            placeholder="Nhập số điện thoại"
          />

          <InputField
            label="Địa chỉ"
            value={settings.address}
            onChange={(v) => updateField('address', v)}
            placeholder="Nhập địa chỉ thương hiệu"
          />

          <div className="md:col-span-2">
            <label className="mb-2 block text-sm font-medium text-[#514b60]">
              Mô tả thương hiệu
            </label>
            <textarea
              value={settings.description}
              onChange={(e) => updateField('description', e.target.value)}
              rows={3}
              placeholder="Giới thiệu về thương hiệu..."
              className="w-full rounded-xl border border-[#e5e4f5] px-4 py-3 text-sm outline-none transition focus:border-[#5865f2] focus:ring-2 focus:ring-[#eef0ff]"
            />
          </div>
        </div>
      </SettingSection>

      {/* OPERATING HOURS */}
      <SettingSection
        title="Giờ hoạt động"
        description="Thiết lập khung giờ phục vụ của thương hiệu"
        icon={Clock}
      >
        <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
          <InputField
            label="Giờ mở cửa"
            type="time"
            value={settings.openingTime}
            onChange={(v) => updateField('openingTime', v)}
          />

          <InputField
            label="Giờ đóng cửa"
            type="time"
            value={settings.closingTime}
            onChange={(v) => updateField('closingTime', v)}
          />
        </div>

        <p className="mt-4 rounded-xl bg-[#f4f3ff] p-4 text-sm text-[#5145a5]">
          Khung giờ hoạt động được áp dụng cho các điểm phục vụ thuộc thương hiệu.
        </p>
      </SettingSection>

      {/* MACHINE CONFIGURATION */}
      <SettingSection
        title="Cấu hình máy nước hoa"
        description="Thiết lập thông số cho chức năng xịt thử nước hoa"
        icon={Droplets}
      >
        <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
          <div>
            <InputField
              label="Thời gian xịt thử (giây)"
              type="number"
              min={1}
              step={1}
              value={String(settings.sprayDuration)}
              onChange={(v) =>
                updateField('sprayDuration', Number(v))
              }
            />
            <p className="mt-2 text-xs text-[#95877c]">
              Thời gian máy kích hoạt vòi xịt cho mỗi lần thử.
            </p>
          </div>

          <div>
            <InputField
              label="Lượng nước hoa mỗi lần xịt (ml)"
              type="number"
              min={0.01}
              step={0.01}
              value={String(settings.sprayVolume)}
              onChange={(v) =>
                updateField('sprayVolume', Number(v))
              }
            />
            <p className="mt-2 text-xs text-[#95877c]">
              Lượng nước hoa tiêu thụ dự kiến cho mỗi lần xịt.
            </p>
          </div>
        </div>

        <div className="mt-5 rounded-xl bg-[#f4f3ff] p-4">
          <p className="text-sm font-semibold text-[#5145a5]">
            Ví dụ tính lượng nước hoa tiêu thụ
          </p>
          <p className="mt-2 text-sm text-[#514b60]">
            Nếu mỗi lần xịt sử dụng {settings.sprayVolume || 0} ml,
            sau 100 lần xịt sẽ tiêu thụ khoảng:
          </p>
          <p className="mt-2 text-xl font-bold text-[#5865f2]">
            {((settings.sprayVolume || 0) * 100).toLocaleString('vi-VN')} ml
          </p>
        </div>
      </SettingSection>

      {/* NOTIFICATION SETTINGS */}
      <SettingSection
        title="Cấu hình thông báo"
        description="Chọn những sự kiện mà thương hiệu muốn nhận thông báo"
        icon={Bell}
      >
        <div className="space-y-5">
          {notificationItems.map((item) => (
            <div
              key={item.key}
              className="flex items-center justify-between gap-4 border-b border-[#eeeef7] pb-5 last:border-0 last:pb-0"
            >
              <div>
                <p className="font-medium text-[#302720]">
                  {item.title}
                </p>
                <p className="mt-1 text-sm text-[#95877c]">
                  {item.description}
                </p>
              </div>

              <Toggle
                checked={settings[item.key]}
                onChange={(value) => updateField(item.key, value)}
              />
            </div>
          ))}
        </div>
      </SettingSection>

      {/* BOTTOM SAVE */}
      <div className="flex flex-col items-center justify-between gap-4 rounded-2xl border border-[#e5e4f5] bg-white p-5 shadow-sm sm:flex-row">
        <p className="text-sm text-[#95877c]">
          Hãy lưu thay đổi sau khi hoàn tất cấu hình.
        </p>

        <div className="flex gap-3">
          <button
            type="button"
            onClick={handleReset}
            className="rounded-xl border border-[#e5e4f5] px-5 py-3 text-sm font-semibold text-[#514b60] transition hover:bg-[#f7f7ff]"
          >
            Khôi phục mặc định
          </button>

          <button
            type="button"
            onClick={handleSave}
            className="flex items-center gap-2 rounded-xl bg-[#5145e5] px-6 py-3 text-sm font-semibold text-white transition hover:bg-[#4338ca]"
          >
            <Save size={17} />
            Lưu tất cả cài đặt
          </button>
        </div>
      </div>
    </div>
  )
}
