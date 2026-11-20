import {
  Building2,
  Users,
  MapPin,
  Cpu,
  TrendingUp,
  Activity,
  Package,
} from 'lucide-react'

const stats = [
  {
    title: 'Tổng thương hiệu',
    value: '12',
    change: '+2 tháng này',
    icon: Building2,
    positive: true,
  },
  {
    title: 'Người dùng',
    value: '248',
    change: '+12.5%',
    icon: Users,
    positive: true,
  },
  {
    title: 'Địa điểm',
    value: '36',
    change: '+4 tháng này',
    icon: MapPin,
    positive: true,
  },
  {
    title: 'Máy hoạt động',
    value: '84',
    change: '6 máy ngoại tuyến',
    icon: Cpu,
    positive: false,
  },
]

const activities = [
  {
    name: 'ScentStation Demo',
    action: 'Thêm máy SCENT-M008',
    time: '5 phút trước',
    status: 'Thành công',
  },
  {
    name: 'Brand Aroma',
    action: 'Cập nhật sản phẩm mới',
    time: '20 phút trước',
    status: 'Thành công',
  },
  {
    name: 'ScentStation Demo',
    action: 'Máy SCENT-M003 mất kết nối',
    time: '1 giờ trước',
    status: 'Cảnh báo',
  },
  {
    name: 'Brand Luxury',
    action: 'Tạo địa điểm mới',
    time: '2 giờ trước',
    status: 'Thành công',
  },
]

export default function DashboardPage() {
  return (
    <div className="space-y-8">
      {/* Page heading */}
      <div>
        <h1 className="text-2xl font-bold text-[#202124]">
          Tổng quan hệ thống
        </h1>
        <p className="mt-1 text-sm text-[#7B8498]">
          Theo dõi hoạt động của nền tảng ScentStation
        </p>
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {stats.map((item) => {
          const Icon = item.icon

          return (
            <div
              key={item.title}
              className="rounded-2xl border border-[#E8EAF5] bg-white p-6 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-md"
            >
              <div className="flex items-center justify-between">
                <p className="text-sm text-[#7B8498]">
                  {item.title}
                </p>

                <div className="rounded-xl bg-[#EEF0FF] p-3 text-[#5364E8]">
                  <Icon size={21} />
                </div>
              </div>

              <h2 className="mt-4 text-3xl font-bold text-[#202124]">
                {item.value}
              </h2>

              <div
                className={`mt-3 flex items-center gap-1 text-xs ${
                  item.positive
                    ? 'text-emerald-600'
                    : 'text-amber-600'
                }`}
              >
                {item.positive ? (
                  <TrendingUp size={15} />
                ) : (
                  <Activity size={15} />
                )}
                {item.change}
              </div>
            </div>
          )
        })}
      </div>

      {/* Activity and device status */}
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        {/* Recent activities */}
        <div className="rounded-2xl border border-[#E8EAF5] bg-white p-6 xl:col-span-2">
          <div className="mb-6 flex items-center justify-between">
            <div>
              <h3 className="font-bold text-[#202124]">
                Hoạt động hệ thống
              </h3>
              <p className="mt-1 text-sm text-[#7B8498]">
                Các hoạt động gần đây
              </p>
            </div>

            <button className="text-sm font-semibold text-[#5364E8] transition hover:text-[#3949C8]">
              Xem tất cả
            </button>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full min-w-[600px] text-left text-sm">
              <thead>
                <tr className="border-b border-[#E8EAF5] text-[#7B8498]">
                  <th className="pb-4 font-medium">Thương hiệu</th>
                  <th className="pb-4 font-medium">Hoạt động</th>
                  <th className="pb-4 font-medium">Thời gian</th>
                  <th className="pb-4 font-medium">Trạng thái</th>
                </tr>
              </thead>

              <tbody>
                {activities.map((activity, index) => (
                  <tr
                    key={index}
                    className="border-b border-[#F0F1F8] last:border-0 transition hover:bg-[#F8F9FF]"
                  >
                    <td className="py-4 font-medium text-[#202124]">
                      {activity.name}
                    </td>

                    <td className="py-4 text-[#596174]">
                      {activity.action}
                    </td>

                    <td className="py-4 text-[#7B8498]">
                      {activity.time}
                    </td>

                    <td className="py-4">
                      <span
                        className={`rounded-full px-3 py-1 text-xs font-medium ${
                          activity.status === 'Thành công'
                            ? 'bg-emerald-50 text-emerald-700'
                            : 'bg-amber-50 text-amber-700'
                        }`}
                      >
                        {activity.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Device status */}
        <div className="rounded-2xl border border-[#E8EAF5] bg-white p-6">
          <h3 className="font-bold text-[#202124]">
            Tình trạng thiết bị
          </h3>

          <p className="mt-1 text-sm text-[#7B8498]">
            Thống kê máy nước hoa
          </p>

          <div className="mt-8 space-y-6">
            {/* Online */}
            <div>
              <div className="mb-2 flex justify-between text-sm">
                <span className="text-[#596174]">Online</span>
                <strong className="text-emerald-600">84 máy</strong>
              </div>

              <div className="h-3 rounded-full bg-[#EEF0FF]">
                <div className="h-3 w-[93%] rounded-full bg-emerald-500" />
              </div>
            </div>

            {/* Offline */}
            <div>
              <div className="mb-2 flex justify-between text-sm">
                <span className="text-[#596174]">Offline</span>
                <strong className="text-amber-600">6 máy</strong>
              </div>

              <div className="h-3 rounded-full bg-[#EEF0FF]">
                <div className="h-3 w-[7%] rounded-full bg-amber-500" />
              </div>
            </div>

            {/* Inventory */}
            <div className="rounded-xl border border-[#E8EAF5] bg-[#F5F6FF] p-4 transition hover:bg-[#EEF0FF]">
              <div className="flex items-center gap-3">
                <div className="rounded-lg bg-white p-2 text-[#5364E8]">
                  <Package size={22} />
                </div>

                <div>
                  <p className="text-sm font-semibold text-[#202124]">
                    Quản lý tồn kho
                  </p>
                  <p className="text-xs text-[#7B8498]">
                    Theo dõi lượng nước hoa trong máy
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
