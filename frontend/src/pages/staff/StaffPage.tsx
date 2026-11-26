import {
  Activity,
  Boxes,
  ClipboardList,
  Cpu,
  Package,
  ShieldCheck,
  Wrench,
} from 'lucide-react'
import type { ReactNode } from 'react'
import { useAuth, type UserRole } from '../../auth/AuthContext'

type StaffRole = Extract<
  UserRole,
  'OPERATIONS_MANAGER' | 'TECHNICIAN' | 'INVENTORY_STAFF'
>

type StaffSection =
  | 'dashboard'
  | 'machines'
  | 'orders'
  | 'maintenance'
  | 'stock'
  | 'refills'

interface StaffPageProps {
  role: StaffRole
  section: StaffSection
}

const roleContent = {
  OPERATIONS_MANAGER: {
    title: 'Operations Manager',
    description: 'Theo dõi hoạt động và hiệu suất máy nước hoa.',
    items: [
      { label: 'Máy đang hoạt động', value: '12', icon: Cpu },
      { label: 'Máy cần kiểm tra', value: '2', icon: Activity },
      { label: 'Đơn hàng hôm nay', value: '86', icon: ClipboardList },
    ],
  },
  TECHNICIAN: {
    title: 'Technician',
    description: 'Kiểm tra thiết bị và quản lý công việc bảo trì.',
    items: [
      { label: 'Máy được phân công', value: '8', icon: Cpu },
      { label: 'Cần bảo trì', value: '3', icon: Wrench },
      { label: 'Đã kiểm tra', value: '5', icon: ShieldCheck },
    ],
  },
  INVENTORY_STAFF: {
    title: 'Inventory Staff',
    description: 'Theo dõi tồn kho và lịch nạp cartridge.',
    items: [
      { label: 'Cartridge trong kho', value: '120', icon: Boxes },
      { label: 'Sắp hết hàng', value: '6', icon: Package },
      { label: 'Lịch nạp hàng', value: '4', icon: ClipboardList },
    ],
  },
}

const sectionTitles: Record<StaffSection, string> = {
  dashboard: 'Tổng quan',
  machines: 'Theo dõi thiết bị',
  orders: 'Quản lý đơn hàng',
  maintenance: 'Công việc bảo trì',
  stock: 'Quản lý tồn kho',
  refills: 'Lịch nạp hàng',
}

function InfoCard({
  label,
  value,
  icon,
}: {
  label: string
  value: string
  icon: ReactNode
}) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <div className="mb-5 flex h-11 w-11 items-center justify-center rounded-xl bg-violet-100 text-violet-700">
        {icon}
      </div>

      <p className="text-sm text-slate-500">{label}</p>
      <p className="mt-2 text-3xl font-bold text-slate-900">
        {value}
      </p>
    </div>
  )
}

export default function StaffPage({
  role,
  section,
}: StaffPageProps) {
  const { user } = useAuth()
  const content = roleContent[role]
  const isDashboard = section === 'dashboard'

  return (
    <div className="space-y-7">
      <div>
        <p className="text-sm font-medium text-violet-700">
          ScentStation / {content.title}
        </p>

        <h1 className="mt-2 text-2xl font-bold text-slate-900 sm:text-3xl">
          {isDashboard
            ? `Xin chào, ${user?.name ?? 'bạn'}!`
            : sectionTitles[section]}
        </h1>

        <p className="mt-2 text-sm text-slate-500">
          {isDashboard
            ? content.description
            : 'Không gian làm việc dành riêng cho vai trò của bạn.'}
        </p>
      </div>

      {isDashboard ? (
        <>
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            {content.items.map((item) => {
              const Icon = item.icon

              return (
                <InfoCard
                  key={item.label}
                  label={item.label}
                  value={item.value}
                  icon={<Icon size={23} />}
                />
              )
            })}
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-7 shadow-sm">
            <div className="flex items-center gap-3">
              <Activity className="text-violet-600" />
              <h2 className="text-lg font-bold text-slate-900">
                Tổng quan công việc
              </h2>
            </div>

            <p className="mt-4 text-sm leading-7 text-slate-500">
              Đây là dữ liệu minh họa dành cho giao diện
              {` ${content.title}`}. Khi tích hợp Backend,
              hệ thống sẽ hiển thị số liệu thực tế theo tài khoản
              và thương hiệu được phân quyền.
            </p>
          </div>
        </>
      ) : (
        <div className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
          <div className="mb-5 flex h-14 w-14 items-center justify-center rounded-2xl bg-violet-100 text-violet-700">
            <ClipboardList size={26} />
          </div>

          <h2 className="text-xl font-bold text-slate-900">
            {sectionTitles[section]}
          </h2>

          <p className="mt-3 max-w-xl text-sm leading-7 text-slate-500">
            Trang chức năng đã được tạo và giới hạn truy cập
            theo vai trò. Bảng dữ liệu, thao tác nghiệp vụ
            và API sẽ được bổ sung trong giai đoạn triển khai
            module tương ứng.
          </p>
        </div>
      )}
    </div>
  )
}
