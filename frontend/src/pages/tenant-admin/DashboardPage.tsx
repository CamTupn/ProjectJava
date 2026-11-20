import {
  ShoppingCart,
  Clock,
  CheckCircle2,
  Cpu,
  Wifi,
  WifiOff,
  AlertTriangle,
  ArrowRight,
  Package,
} from 'lucide-react'

const stats = [
  {
    title: 'Tổng đơn hàng',
    value: '128',
    description: 'Tổng đơn hàng của thương hiệu',
    icon: ShoppingCart,
    color: 'text-[#5865F2]',
    bg: 'bg-[#EEF0FF]',
  },
  {
    title: 'Chờ xử lý',
    value: '12',
    description: 'Đơn hàng cần xác nhận',
    icon: Clock,
    color: 'text-amber-600',
    bg: 'bg-amber-50',
  },
  {
    title: 'Đơn hoàn thành',
    value: '96',
    description: 'Đơn đã xử lý thành công',
    icon: CheckCircle2,
    color: 'text-green-600',
    bg: 'bg-green-50',
  },
  {
    title: 'Máy nước hoa',
    value: '8',
    description: 'Tổng máy thuộc thương hiệu',
    icon: Cpu,
    color: 'text-purple-600',
    bg: 'bg-purple-50',
  },
]

const machines = [
  {
    code: 'SCENT-M01',
    location: 'Vincom Đồng Khởi',
    status: 'ONLINE',
    label: 'Đang hoạt động',
  },
  {
    code: 'SCENT-M02',
    location: 'Landmark 81',
    status: 'ONLINE',
    label: 'Đang hoạt động',
  },
  {
    code: 'SCENT-M03',
    location: 'Aeon Mall Tân Phú',
    status: 'OFFLINE',
    label: 'Mất kết nối',
  },
  {
    code: 'SCENT-M04',
    location: 'Gigamall Thủ Đức',
    status: 'ONLINE',
    label: 'Đang hoạt động',
  },
]

const recentOrders = [
  {
    code: 'ORD-202611-128',
    customer: 'Nguyễn Minh Anh',
    product: 'Rose Elegance',
    total: '27.000 ₫',
    status: 'PENDING',
    label: 'Chờ xử lý',
  },
  {
    code: 'ORD-202611-127',
    customer: 'Trần Quốc Bảo',
    product: 'Royal Oud',
    total: '40.000 ₫',
    status: 'PROCESSING',
    label: 'Đang xử lý',
  },
  {
    code: 'ORD-202611-126',
    customer: 'Lê Hoàng Yến',
    product: 'Ocean Breeze',
    total: '18.000 ₫',
    status: 'COMPLETED',
    label: 'Hoàn thành',
  },
  {
    code: 'ORD-202611-125',
    customer: 'Phạm Gia Huy',
    product: 'Vanilla Bloom',
    total: '30.000 ₫',
    status: 'COMPLETED',
    label: 'Hoàn thành',
  },
]

const lowStock = [
  {
    machine: 'SCENT-M01',
    slot: 'SLOT-02',
    product: 'Royal Oud',
    remaining: 15,
    capacity: 100,
  },
  {
    machine: 'SCENT-M02',
    slot: 'SLOT-03',
    product: 'White Musk',
    remaining: 8,
    capacity: 100,
  },
]

function StatCard({ item }: { item: (typeof stats)[number] }) {
  const Icon = item.icon

  return (
    <div className="rounded-2xl border border-[#E5E5F5] bg-white p-6 shadow-sm">
      <div className="flex items-center justify-between">
        <p className="text-sm text-[#95877C]">{item.title}</p>
        <div className={`flex h-12 w-12 items-center justify-center rounded-xl ${item.bg}`}>
          <Icon size={22} className={item.color} />
        </div>
      </div>

      <h2 className={`mt-5 text-3xl font-bold ${item.color}`}>
        {item.value}
      </h2>

      <p className="mt-2 text-xs text-[#95877C]">
        {item.description}
      </p>
    </div>
  )
}

export default function TenantDashboardPage() {
  return (
    <div className="space-y-7">
      {/* Page title */}
      <div>
        <h1 className="text-2xl font-bold text-[#302720]">
          Tổng quan thương hiệu
        </h1>
        <p className="mt-2 text-sm text-[#95877C]">
          Theo dõi tình hình kinh doanh và hoạt động của thương hiệu ScentStation Demo.
        </p>
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {stats.map((item) => (
          <StatCard key={item.title} item={item} />
        ))}
      </div>

      {/* Machine overview */}
      <section className="rounded-2xl border border-[#E5E5F5] bg-white p-6 shadow-sm">
        <div className="mb-5 flex items-center justify-between">
          <div>
            <h2 className="font-bold text-[#302720]">
              Tình trạng máy nước hoa
            </h2>
            <p className="mt-1 text-sm text-[#95877C]">
              Theo dõi trạng thái kết nối của các máy thuộc thương hiệu
            </p>
          </div>

          <Cpu size={23} className="text-[#5865F2]" />
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {machines.map((machine) => (
            <div
              key={machine.code}
              className="rounded-xl border border-[#E5E5F5] p-4"
            >
              <div className="flex items-center justify-between">
                <span className="font-semibold text-[#5865F2]">
                  {machine.code}
                </span>

                {machine.status === 'ONLINE' ? (
                  <Wifi size={19} className="text-green-600" />
                ) : (
                  <WifiOff size={19} className="text-red-500" />
                )}
              </div>

              <p className="mt-3 text-sm text-[#475569]">
                {machine.location}
              </p>

              <span
                className={`mt-3 inline-flex rounded-full px-3 py-1 text-xs font-medium ${
                  machine.status === 'ONLINE'
                    ? 'bg-green-50 text-green-700'
                    : 'bg-red-50 text-red-700'
                }`}
              >
                {machine.label}
              </span>
            </div>
          ))}
        </div>
      </section>

      {/* Recent orders and low stock */}
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        {/* Recent orders */}
        <section className="overflow-hidden rounded-2xl border border-[#E5E5F5] bg-white shadow-sm xl:col-span-2">
          <div className="flex items-center justify-between border-b border-[#F0EEF5] p-6">
            <div>
              <h2 className="font-bold text-[#302720]">
                Đơn hàng gần đây
              </h2>
              <p className="mt-1 text-sm text-[#95877C]">
                Những đơn hàng mới nhất của thương hiệu
              </p>
            </div>

            <span className="text-sm font-medium text-[#5865F2]">
              Xem tất cả <ArrowRight size={15} className="ml-1 inline" />
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full min-w-[650px] text-left text-sm">
              <thead className="bg-[#F6F5FF] text-[#64748B]">
                <tr>
                  <th className="px-5 py-4 font-medium">Mã đơn</th>
                  <th className="px-5 py-4 font-medium">Khách hàng</th>
                  <th className="px-5 py-4 font-medium">Sản phẩm</th>
                  <th className="px-5 py-4 font-medium">Tổng tiền</th>
                  <th className="px-5 py-4 font-medium">Trạng thái</th>
                </tr>
              </thead>

              <tbody>
                {recentOrders.map((order) => (
                  <tr
                    key={order.code}
                    className="border-t border-[#F0EEF5]"
                  >
                    <td className="px-5 py-4 font-medium text-[#5865F2]">
                      {order.code}
                    </td>
                    <td className="px-5 py-4 text-[#302720]">
                      {order.customer}
                    </td>
                    <td className="px-5 py-4 text-[#475569]">
                      {order.product}
                    </td>
                    <td className="px-5 py-4 font-semibold text-[#302720]">
                      {order.total}
                    </td>
                    <td className="px-5 py-4">
                      <span
                        className={`whitespace-nowrap rounded-full px-3 py-1 text-xs font-medium ${
                          order.status === 'PENDING'
                            ? 'bg-amber-50 text-amber-700'
                            : order.status === 'PROCESSING'
                              ? 'bg-blue-50 text-blue-700'
                              : 'bg-green-50 text-green-700'
                        }`}
                      >
                        {order.label}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        {/* Low stock */}
        <section className="rounded-2xl border border-[#E5E5F5] bg-white p-6 shadow-sm">
          <div className="mb-5 flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50">
              <AlertTriangle size={21} className="text-amber-600" />
            </div>

            <div>
              <h2 className="font-bold text-[#302720]">
                Cảnh báo tồn kho
              </h2>
              <p className="text-xs text-[#95877C]">
                Các khe sắp hết nước hoa
              </p>
            </div>
          </div>

          <div className="space-y-4">
            {lowStock.map((item) => (
              <div
                key={item.slot}
                className="rounded-xl border border-[#F0EEF5] p-4"
              >
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-[#302720]">
                    {item.product}
                  </span>
                  <Package size={17} className="text-[#95877C]" />
                </div>

                <p className="mt-2 text-xs text-[#95877C]">
                  {item.machine} · {item.slot}
                </p>

                <div className="mt-4 flex justify-between text-xs">
                  <span className="text-[#95877C]">
                    Lượng còn lại
                  </span>
                  <span className="font-semibold text-red-600">
                    {item.remaining} / {item.capacity} ml
                  </span>
                </div>

                <div className="mt-2 h-2 overflow-hidden rounded-full bg-[#F0EEF5]">
                  <div
                    className="h-full rounded-full bg-red-500"
                    style={{
                      width: `${(item.remaining / item.capacity) * 100}%`,
                    }}
                  />
                </div>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  )
}
