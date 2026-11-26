import { useEffect, useState } from 'react'

interface SystemSettings {
  platformName: string
  supportEmail: string
  supportPhone: string
  timezone: string
  currency: string
  language: string

  notifications: {
    newOrder: boolean
    machineOffline: boolean
    lowStock: boolean
    orderCompleted: boolean
  }

  payments: {
    cash: boolean
    bankTransfer: boolean
    eWallet: boolean
    card: boolean
  }

  inventory: {
    lowStockThresholdMl: number
    criticalStockThresholdMl: number
  }
}

const defaultSettings: SystemSettings = {
  platformName: 'ScentStation',
  supportEmail: 'support@scentstation.vn',
  supportPhone: '1900 1234',
  timezone: 'Asia/Ho_Chi_Minh',
  currency: 'VND',
  language: 'vi',

  notifications: {
    newOrder: true,
    machineOffline: true,
    lowStock: true,
    orderCompleted: false,
  },

  payments: {
    cash: true,
    bankTransfer: true,
    eWallet: true,
    card: false,
  },

  inventory: {
    lowStockThresholdMl: 20,
    criticalStockThresholdMl: 5,
  },
}

const STORAGE_KEY = 'scentstation_system_settings'

function SectionTitle({
  title,
  description,
}: {
  title: string
  description: string
}) {
  return (
    <div className="mb-5">
      <h2 className="text-lg font-bold text-[#302720]">{title}</h2>
      <p className="mt-1 text-sm text-[#95877c]">{description}</p>
    </div>
  )
}

function SettingCard({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <section className="rounded-2xl border border-[#e8e6f5] bg-white p-6 shadow-sm">
      {children}
    </section>
  )
}

function Field({
  label,
  value,
  onChange,
  placeholder,
  type = 'text',
}: {
  label: string
  value: string | number
  onChange: (value: string) => void
  placeholder?: string
  type?: string
}) {
  return (
    <div>
      <label className="mb-2 block text-sm font-medium text-gray-700">
        {label}
      </label>

      <input
        type={type}
        value={value}
        placeholder={placeholder}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded-xl border border-[#e8e6f5] bg-white px-4 py-3 text-sm outline-none transition focus:border-[#5865f2] focus:ring-2 focus:ring-[#5865f2]/10"
      />
    </div>
  )
}

function Toggle({
  label,
  description,
  checked,
  onChange,
}: {
  label: string
  description: string
  checked: boolean
  onChange: (checked: boolean) => void
}) {
  return (
    <div className="flex items-center justify-between gap-4 border-b border-gray-100 py-4 last:border-0 last:pb-0">
      <div>
        <p className="text-sm font-medium text-gray-800">{label}</p>
        <p className="mt-1 text-xs text-gray-500">{description}</p>
      </div>

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
          className={`absolute top-1 h-4 w-4 rounded-full bg-white shadow transition ${
            checked ? 'left-6' : 'left-1'
          }`}
        />
      </button>
    </div>
  )
}

export default function SettingsPage() {
  const [settings, setSettings] =
    useState<SystemSettings>(defaultSettings)

  const [saved, setSaved] = useState(false)

  // Load settings from localStorage
  useEffect(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY)

      if (stored) {
        const parsed = JSON.parse(stored)

        setSettings({
          ...defaultSettings,
          ...parsed,
          notifications: {
            ...defaultSettings.notifications,
            ...parsed.notifications,
          },
          payments: {
            ...defaultSettings.payments,
            ...parsed.payments,
          },
          inventory: {
            ...defaultSettings.inventory,
            ...parsed.inventory,
          },
        })
      }
    } catch {
      console.error('Không thể đọc cấu hình hệ thống.')
    }
  }, [])

  const updateGeneral = (
    field: keyof Pick<
      SystemSettings,
      'platformName' | 'supportEmail' | 'supportPhone' | 'timezone' | 'currency' | 'language'
    >,
    value: string,
  ) => {
    setSettings((current) => ({
      ...current,
      [field]: value,
    }))
    setSaved(false)
  }

  const updateNotification = (
    field: keyof SystemSettings['notifications'],
    value: boolean,
  ) => {
    setSettings((current) => ({
      ...current,
      notifications: {
        ...current.notifications,
        [field]: value,
      },
    }))
    setSaved(false)
  }

  const updatePayment = (
    field: keyof SystemSettings['payments'],
    value: boolean,
  ) => {
    setSettings((current) => ({
      ...current,
      payments: {
        ...current.payments,
        [field]: value,
      },
    }))
    setSaved(false)
  }

  const updateInventory = (
    field: keyof SystemSettings['inventory'],
    value: number,
  ) => {
    setSettings((current) => ({
      ...current,
      inventory: {
        ...current.inventory,
        [field]: value,
      },
    }))
    setSaved(false)
  }

  const handleSave = () => {
    if (!settings.platformName.trim()) {
      alert('Tên nền tảng không được để trống.')
      return
    }

    if (
      settings.inventory.lowStockThresholdMl < 0 ||
      settings.inventory.criticalStockThresholdMl < 0
    ) {
      alert('Ngưỡng cảnh báo không được nhỏ hơn 0.')
      return
    }

    if (
      settings.inventory.criticalStockThresholdMl >=
      settings.inventory.lowStockThresholdMl
    ) {
      alert('Ngưỡng cảnh báo nghiêm trọng phải nhỏ hơn ngưỡng sắp hết.')
      return
    }

    try {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify(settings),
      )

      setSaved(true)
      alert('Đã lưu cấu hình hệ thống thành công!')
    } catch {
      alert('Không thể lưu cấu hình. Vui lòng thử lại.')
    }
  }

  const handleReset = () => {
    const confirmed = window.confirm(
      'Bạn có chắc muốn khôi phục toàn bộ cấu hình mặc định?',
    )

    if (!confirmed) return

    setSettings(defaultSettings)
    localStorage.removeItem(STORAGE_KEY)
    setSaved(false)
  }

  return (
    <div className="min-h-screen bg-[#f0efff] p-6">
      {/* Page header */}
      <div className="mb-7 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Cài đặt hệ thống
          </h1>

          <p className="mt-1 text-sm text-[#95877c]">
            Quản lý cấu hình chung của nền tảng ScentStation
          </p>
        </div>

        <div className="flex gap-3">
          <button
            onClick={handleReset}
            className="rounded-xl border border-[#e1def3] bg-white px-5 py-3 text-sm font-semibold text-gray-600 transition hover:bg-gray-50"
          >
            Khôi phục mặc định
          </button>

          <button
            onClick={handleSave}
            className="rounded-xl bg-[#4935df] px-6 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#3f2bc7]"
          >
            Lưu cấu hình
          </button>
        </div>
      </div>

      {saved && (
        <div className="mb-5 rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">
          Cấu hình đã được lưu trên trình duyệt này.
        </div>
      )}

      <div className="space-y-6">
        {/* Platform information */}
        <SettingCard>
          <SectionTitle
            title="Thông tin nền tảng"
            description="Thông tin nhận diện và liên hệ hỗ trợ của ScentStation."
          />

          <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
            <Field
              label="Tên nền tảng"
              value={settings.platformName}
              onChange={(value) =>
                updateGeneral('platformName', value)
              }
              placeholder="Nhập tên nền tảng"
            />

            <Field
              label="Email hỗ trợ"
              value={settings.supportEmail}
              onChange={(value) =>
                updateGeneral('supportEmail', value)
              }
              type="email"
              placeholder="support@example.com"
            />

            <Field
              label="Số điện thoại hỗ trợ"
              value={settings.supportPhone}
              onChange={(value) =>
                updateGeneral('supportPhone', value)
              }
              placeholder="Nhập số điện thoại"
            />
          </div>
        </SettingCard>

        {/* General configuration */}
        <SettingCard>
          <SectionTitle
            title="Cấu hình chung"
            description="Thiết lập ngôn ngữ, múi giờ và đơn vị tiền tệ của nền tảng."
          />

          <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Múi giờ
              </label>

              <select
                value={settings.timezone}
                onChange={(e) =>
                  updateGeneral('timezone', e.target.value)
                }
                className="w-full rounded-xl border border-[#e8e6f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              >
                <option value="Asia/Ho_Chi_Minh">
                  Việt Nam (GMT+7)
                </option>
                <option value="Asia/Singapore">
                  Singapore (GMT+8)
                </option>
                <option value="Asia/Tokyo">
                  Tokyo (GMT+9)
                </option>
                <option value="UTC">
                  UTC (GMT+0)
                </option>
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Đơn vị tiền tệ
              </label>

              <select
                value={settings.currency}
                onChange={(e) =>
                  updateGeneral('currency', e.target.value)
                }
                className="w-full rounded-xl border border-[#e8e6f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              >
                <option value="VND">VND - Việt Nam đồng</option>
                <option value="USD">USD - US Dollar</option>
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Ngôn ngữ mặc định
              </label>

              <select
                value={settings.language}
                onChange={(e) =>
                  updateGeneral('language', e.target.value)
                }
                className="w-full rounded-xl border border-[#e8e6f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
              >
                <option value="vi">Tiếng Việt</option>
                <option value="en">English</option>
              </select>
            </div>
          </div>
        </SettingCard>

        {/* Notifications */}
        <SettingCard>
          <SectionTitle
            title="Cấu hình thông báo"
            description="Chọn những sự kiện mà hệ thống cần gửi thông báo cho quản trị viên."
          />

          <Toggle
            label="Đơn hàng mới"
            description="Thông báo khi có đơn hàng mới được tạo."
            checked={settings.notifications.newOrder}
            onChange={(value) =>
              updateNotification('newOrder', value)
            }
          />

          <Toggle
            label="Máy nước hoa mất kết nối"
            description="Thông báo khi thiết bị chuyển sang trạng thái Offline."
            checked={settings.notifications.machineOffline}
            onChange={(value) =>
              updateNotification('machineOffline', value)
            }
          />

          <Toggle
            label="Cảnh báo tồn kho thấp"
            description="Thông báo khi lượng nước hoa trong khe xuống dưới ngưỡng."
            checked={settings.notifications.lowStock}
            onChange={(value) =>
              updateNotification('lowStock', value)
            }
          />

          <Toggle
            label="Đơn hàng hoàn thành"
            description="Thông báo khi đơn hàng được xử lý hoàn tất."
            checked={settings.notifications.orderCompleted}
            onChange={(value) =>
              updateNotification('orderCompleted', value)
            }
          />
        </SettingCard>

        {/* Payment methods */}
        <SettingCard>
          <SectionTitle
            title="Phương thức thanh toán"
            description="Thiết lập các phương thức thanh toán được nền tảng cho phép."
          />

          <Toggle
            label="Tiền mặt"
            description="Cho phép thanh toán bằng tiền mặt tại điểm phục vụ."
            checked={settings.payments.cash}
            onChange={(value) =>
              updatePayment('cash', value)
            }
          />

          <Toggle
            label="Chuyển khoản ngân hàng"
            description="Cho phép thanh toán qua tài khoản ngân hàng."
            checked={settings.payments.bankTransfer}
            onChange={(value) =>
              updatePayment('bankTransfer', value)
            }
          />

          <Toggle
            label="Ví điện tử"
            description="Cho phép thanh toán qua các ví điện tử được tích hợp."
            checked={settings.payments.eWallet}
            onChange={(value) =>
              updatePayment('eWallet', value)
            }
          />

          <Toggle
            label="Thẻ ngân hàng"
            description="Cho phép thanh toán bằng thẻ ngân hàng."
            checked={settings.payments.card}
            onChange={(value) =>
              updatePayment('card', value)
            }
          />
        </SettingCard>

        {/* Inventory configuration */}
        <SettingCard>
          <SectionTitle
            title="Cấu hình cảnh báo tồn kho"
            description="Thiết lập ngưỡng cảnh báo lượng nước hoa còn lại trong mỗi khe máy."
          />

          <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
            <div>
              <Field
                label="Ngưỡng sắp hết nước hoa (ml)"
                type="number"
                value={settings.inventory.lowStockThresholdMl}
                onChange={(value) =>
                  updateInventory(
                    'lowStockThresholdMl',
                    Number(value),
                  )
                }
              />

              <p className="mt-2 text-xs text-gray-500">
                Cảnh báo khi lượng nước hoa còn lại bằng hoặc thấp hơn giá trị này.
              </p>
            </div>

            <div>
              <Field
                label="Ngưỡng tồn kho nghiêm trọng (ml)"
                type="number"
                value={settings.inventory.criticalStockThresholdMl}
                onChange={(value) =>
                  updateInventory(
                    'criticalStockThresholdMl',
                    Number(value),
                  )
                }
              />

              <p className="mt-2 text-xs text-gray-500">
                Cảnh báo mức nghiêm trọng khi lượng nước hoa xuống thấp hơn ngưỡng này.
              </p>
            </div>
          </div>

          <div className="mt-5 rounded-xl bg-[#f7f6ff] p-4">
            <p className="text-sm font-semibold text-gray-800">
              Ví dụ cấu hình hiện tại
            </p>

            <div className="mt-3 flex flex-wrap gap-3 text-sm">
              <span className="rounded-lg bg-yellow-100 px-3 py-2 text-yellow-800">
                Sắp hết: ≤ {settings.inventory.lowStockThresholdMl} ml
              </span>

              <span className="rounded-lg bg-red-100 px-3 py-2 text-red-700">
                Nghiêm trọng: dưới {settings.inventory.criticalStockThresholdMl} ml
              </span>
            </div>
          </div>
        </SettingCard>

        {/* Bottom actions */}
        <div className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-[#e8e6f5] bg-white p-5 shadow-sm">
          <p className="text-sm text-gray-500">
            Hãy lưu thay đổi sau khi hoàn tất cấu hình.
          </p>

          <div className="flex gap-3">
            <button
              onClick={handleReset}
              className="rounded-xl border border-[#e1def3] px-5 py-3 text-sm font-semibold text-gray-600 hover:bg-gray-50"
            >
              Khôi phục mặc định
            </button>

            <button
              onClick={handleSave}
              className="rounded-xl bg-[#4935df] px-6 py-3 text-sm font-semibold text-white hover:bg-[#3f2bc7]"
            >
              Lưu cấu hình
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
