import { useMemo, useState } from 'react'
import {
  CalendarDays,
  TrendingUp,
  TrendingDown,
  ShoppingCart,
  Wallet,
  Package,
  Cpu,
  Download,
  CheckCircle2,
  XCircle,
  FileSpreadsheet,
  Droplets,
} from 'lucide-react'

type Period = '7days' | '30days' | '12months'

interface DailyRecord {
  date: string
  orders: number
  revenue: number
  completed: number
  processing: number
  pending: number
  cancelled: number
  consumedMl: number
}

interface ProductStat {
  name: string
  consumedMl: number
  revenue: number
}

interface MachineStat {
  code: string
  location: string
  orders: number
  status: 'ONLINE' | 'OFFLINE'
}

const dailyRecords: DailyRecord[] = [
  {
    date: '2026-11-14',
    orders: 38,
    revenue: 7850000,
    completed: 30,
    processing: 4,
    pending: 3,
    cancelled: 1,
    consumedMl: 285,
  },
  {
    date: '2026-11-15',
    orders: 42,
    revenue: 9200000,
    completed: 34,
    processing: 5,
    pending: 2,
    cancelled: 1,
    consumedMl: 320,
  },
  {
    date: '2026-11-16',
    orders: 35,
    revenue: 7100000,
    completed: 27,
    processing: 4,
    pending: 2,
    cancelled: 2,
    consumedMl: 260,
  },
  {
    date: '2026-11-17',
    orders: 46,
    revenue: 10500000,
    completed: 37,
    processing: 5,
    pending: 2,
    cancelled: 2,
    consumedMl: 355,
  },
  {
    date: '2026-11-18',
    orders: 51,
    revenue: 11800000,
    completed: 40,
    processing: 6,
    pending: 3,
    cancelled: 2,
    consumedMl: 390,
  },
  {
    date: '2026-11-19',
    orders: 48,
    revenue: 10200000,
    completed: 38,
    processing: 5,
    pending: 3,
    cancelled: 2,
    consumedMl: 365,
  },
  {
    date: '2026-11-20',
    orders: 52,
    revenue: 16200000,
    completed: 42,
    processing: 6,
    pending: 2,
    cancelled: 2,
    consumedMl: 475,
  },
]

const products: ProductStat[] = [
  { name: 'Rose Elegance', consumedMl: 2450, revenue: 3675000 },
  { name: 'Royal Oud', consumedMl: 1980, revenue: 3960000 },
  { name: 'Ocean Breeze', consumedMl: 1560, revenue: 2808000 },
  { name: 'Vanilla Bloom', consumedMl: 1320, revenue: 3960000 },
  { name: 'White Musk', consumedMl: 980, revenue: 1176000 },
]

const machines: MachineStat[] = [
  {
    code: 'SCENT-M01',
    location: 'Vincom Đồng Khởi',
    orders: 38,
    status: 'ONLINE',
  },
  {
    code: 'SCENT-M02',
    location: 'Landmark 81',
    orders: 32,
    status: 'ONLINE',
  },
  {
    code: 'SCENT-M03',
    location: 'Aeon Mall Tân Phú',
    orders: 18,
    status: 'OFFLINE',
  },
  {
    code: 'SCENT-M04',
    location: 'Gigamall Thủ Đức',
    orders: 26,
    status: 'ONLINE',
  },
]

const formatCurrency = (value: number) =>
  `${value.toLocaleString('vi-VN')} đ`

const formatNumber = (value: number) =>
  value.toLocaleString('vi-VN')

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(`${date}T00:00:00`).toLocaleDateString('vi-VN')
}

const getToday = () => {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function StatCard({
  title,
  value,
  description,
  icon: Icon,
  color,
  trend,
}: {
  title: string
  value: string
  description: string
  icon: React.ElementType
  color: string
  trend?: 'up' | 'down'
}) {
  return (
    <div className="rounded-2xl border border-[#e5e4f5] bg-white p-6 shadow-sm">
      <div className="flex items-center justify-between">
        <p className="text-sm text-[#95877c]">{title}</p>
        <div className={`rounded-xl p-3 ${color}`}>
          <Icon size={22} />
        </div>
      </div>

      <h3 className="mt-5 text-3xl font-bold text-[#302720]">
        {value}
      </h3>

      <div className="mt-2 flex items-center gap-1 text-xs text-[#95877c]">
        {trend === 'up' && (
          <TrendingUp size={14} className="text-green-600" />
        )}
        {trend === 'down' && (
          <TrendingDown size={14} className="text-red-500" />
        )}
        {description}
      </div>
    </div>
  )
}

function SectionCard({
  title,
  description,
  children,
}: {
  title: string
  description?: string
  children: React.ReactNode
}) {
  return (
    <section className="rounded-2xl border border-[#e5e4f5] bg-white p-6 shadow-sm">
      <div className="mb-6">
        <h3 className="text-lg font-bold text-[#302720]">{title}</h3>
        {description && (
          <p className="mt-1 text-sm text-[#95877c]">{description}</p>
        )}
      </div>
      {children}
    </section>
  )
}

export default function ReportsPage() {
  const [period, setPeriod] = useState<Period>('30days')

  const [dailyStartDate, setDailyStartDate] = useState('2026-11-14')
  const [dailyEndDate, setDailyEndDate] = useState('2026-11-20')

  const filteredDailyRecords = useMemo(() => {
    return dailyRecords.filter(
      (item) =>
        item.date >= dailyStartDate &&
        item.date <= dailyEndDate,
    )
  }, [dailyStartDate, dailyEndDate])

  const dailySummary = useMemo(() => {
    return filteredDailyRecords.reduce(
      (total, item) => ({
        orders: total.orders + item.orders,
        revenue: total.revenue + item.revenue,
        consumedMl: total.consumedMl + item.consumedMl,
        completed: total.completed + item.completed,
        processing: total.processing + item.processing,
        pending: total.pending + item.pending,
        cancelled: total.cancelled + item.cancelled,
      }),
      {
        orders: 0,
        revenue: 0,
        consumedMl: 0,
        completed: 0,
        processing: 0,
        pending: 0,
        cancelled: 0,
      },
    )
  }, [filteredDailyRecords])

  const reportRecords = useMemo(() => {
    if (period === '7days') {
      return dailyRecords.slice(-7)
    }

    if (period === '12months') {
      return [
        { label: 'T1', value: 65 },
        { label: 'T2', value: 72 },
        { label: 'T3', value: 58 },
        { label: 'T4', value: 80 },
        { label: 'T5', value: 68 },
        { label: 'T6', value: 85 },
      ]
    }

    return [
      { label: 'Tuần 1', value: 48 },
      { label: 'Tuần 2', value: 65 },
      { label: 'Tuần 3', value: 60 },
      { label: 'Tuần 4', value: 90 },
    ]
  }, [period])

  const totalRevenue = 72850000
  const totalOrders = 342
  const averageOrderValue = Math.round(totalRevenue / totalOrders)

  const exportDailyReport = () => {
    const headers = [
      'Ngày',
      'Tổng đơn hàng',
      'Doanh thu',
      'Đơn hoàn thành',
      'Đang xử lý',
      'Chờ xử lý',
      'Đã hủy',
      'Lượng nước hoa tiêu thụ (ml)',
    ]

    const rows = filteredDailyRecords.map((item) => [
      item.date,
      item.orders,
      item.revenue,
      item.completed,
      item.processing,
      item.pending,
      item.cancelled,
      item.consumedMl,
    ])

    rows.push([
      'TỔNG CỘNG',
      dailySummary.orders,
      dailySummary.revenue,
      dailySummary.completed,
      dailySummary.processing,
      dailySummary.pending,
      dailySummary.cancelled,
      dailySummary.consumedMl,
    ])

    const csvContent = [
      headers,
      ...rows,
    ]
      .map((row) =>
        row
          .map((value) => `"${String(value).replace(/"/g, '""')}"`)
          .join(','),
      )
      .join('\n')

    const blob = new Blob(
      ['\uFEFF' + csvContent],
      { type: 'text/csv;charset=utf-8;' },
    )

    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')

    link.href = url
    link.download = `bao-cao-ngay-${dailyStartDate}-${dailyEndDate}.csv`

    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    URL.revokeObjectURL(url)
  }

  const exportGeneralReport = () => {
    const rows = [
      ['Chỉ số', 'Giá trị'],
      ['Tổng doanh thu', totalRevenue],
      ['Tổng đơn hàng', totalOrders],
      ['Giá trị đơn trung bình', averageOrderValue],
      ['Tổng lượng nước hoa tiêu thụ (ml)', products.reduce((sum, item) => sum + item.consumedMl, 0)],
    ]

    const csvContent = rows
      .map((row) =>
        row
          .map((value) => `"${String(value).replace(/"/g, '""')}"`)
          .join(','),
      )
      .join('\n')

    const blob = new Blob(
      ['\uFEFF' + csvContent],
      { type: 'text/csv;charset=utf-8;' },
    )

    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')

    link.href = url
    link.download = 'bao-cao-thuong-hieu.csv'

    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    URL.revokeObjectURL(url)
  }

  const maxRevenue = Math.max(
    ...reportRecords.map((item) => 'value' in item ? item.value : item.revenue),
  )

  const totalProductMl = products.reduce(
    (sum, item) => sum + item.consumedMl,
    0,
  )

  const orderStatus = [
    { label: 'Hoàn thành', value: 96, color: 'bg-green-500' },
    { label: 'Đang xử lý', value: 20, color: 'bg-blue-500' },
    { label: 'Chờ xử lý', value: 12, color: 'bg-orange-500' },
    { label: 'Đã hủy', value: 8, color: 'bg-red-400' },
  ]

  return (
    <div className="space-y-7">
      {/* PAGE HEADER */}
      <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Báo cáo & Thống kê
          </h1>
          <p className="mt-1 text-sm text-[#95877c]">
            Theo dõi doanh thu và hiệu quả kinh doanh của thương hiệu ScentStation Demo.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-2 rounded-xl border border-[#e5e4f5] bg-white px-4 py-3 text-sm text-[#64748b]">
            <CalendarDays size={17} />
            {formatDate(getToday())}
          </div>

          <button
            onClick={exportGeneralReport}
            className="flex items-center gap-2 rounded-xl bg-[#5145e5] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#4338ca]"
          >
            <Download size={17} />
            Xuất báo cáo
          </button>
        </div>
      </div>

      {/* PERIOD FILTER */}
      <div className="flex flex-wrap gap-2">
        {[
          { value: '7days', label: '7 ngày qua' },
          { value: '30days', label: '30 ngày qua' },
          { value: '12months', label: '12 tháng qua' },
        ].map((item) => (
          <button
            key={item.value}
            onClick={() => setPeriod(item.value as Period)}
            className={`rounded-xl px-5 py-3 text-sm font-semibold transition ${
              period === item.value
                ? 'bg-[#5145e5] text-white shadow-sm'
                : 'border border-[#e5e4f5] bg-white text-[#64748b] hover:bg-[#f4f3ff]'
            }`}
          >
            {item.label}
          </button>
        ))}
      </div>

      {/* OVERVIEW CARDS */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Tổng doanh thu"
          value={formatCurrency(totalRevenue)}
          description="18.2% so với kỳ trước"
          icon={Wallet}
          color="bg-indigo-50 text-indigo-500"
          trend="up"
        />

        <StatCard
          title="Tổng đơn hàng"
          value={formatNumber(totalOrders)}
          description="10.8% so với kỳ trước"
          icon={ShoppingCart}
          color="bg-orange-50 text-orange-500"
          trend="up"
        />

        <StatCard
          title="Giá trị đơn trung bình"
          value={formatCurrency(averageOrderValue)}
          description="Trung bình mỗi đơn hàng"
          icon={TrendingUp}
          color="bg-green-50 text-green-600"
        />

        <StatCard
          title="Sản phẩm đang kinh doanh"
          value="24"
          description="Thuộc thương hiệu"
          icon={Package}
          color="bg-purple-50 text-purple-600"
        />
      </div>

      {/* REVENUE CHART */}
      <SectionCard
        title="Biểu đồ doanh thu"
        description="Doanh thu theo khoảng thời gian đã chọn"
      >
        <div className="mb-5 flex justify-end">
          <span className="rounded-lg bg-[#eef0ff] px-3 py-2 text-xs font-medium text-[#5145e5]">
            Đơn vị: Triệu đồng
          </span>
        </div>

        <div className="flex h-64 items-end justify-around gap-4 border-b border-l border-[#e5e4f5] px-3 pb-0">
          {reportRecords.map((item, index) => {
            const value = 'value' in item ? item.value : item.revenue / 1000000
            const label = 'label' in item ? item.label : formatDate(item.date)

            return (
              <div
                key={index}
                className="flex h-full flex-1 flex-col items-center justify-end"
              >
                <div className="mb-2 text-xs font-medium text-[#64748b]">
                  {value.toFixed(1)}
                </div>

                <div
                  className="w-full max-w-[120px] rounded-t-lg bg-gradient-to-t from-[#5865f2] to-[#a5a9ff] transition-all hover:opacity-80"
                  style={{
                    height: `${Math.max((value / maxRevenue) * 75, 5)}%`,
                  }}
                  title={`${value.toFixed(1)} triệu đồng`}
                />

                <span className="mt-3 text-xs text-[#95877c]">
                  {label}
                </span>
              </div>
            )
          })}
        </div>
      </SectionCard>

      {/* DAILY REPORT */}
      <SectionCard
        title="Báo cáo hoạt động hàng ngày"
        description="Tổng hợp doanh thu, đơn hàng và lượng nước hoa tiêu thụ theo ngày"
      >
        <div className="mb-6 flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
          <div className="flex flex-wrap items-end gap-3">
            <div>
              <label className="mb-2 block text-xs font-medium text-[#64748b]">
                Từ ngày
              </label>
              <input
                type="date"
                value={dailyStartDate}
                max={dailyEndDate}
                onChange={(e) => setDailyStartDate(e.target.value)}
                className="rounded-xl border border-[#e5e4f5] bg-white px-3 py-3 text-sm outline-none focus:border-[#5865f2]"
              />
            </div>

            <div>
              <label className="mb-2 block text-xs font-medium text-[#64748b]">
                Đến ngày
              </label>
              <input
                type="date"
                value={dailyEndDate}
                min={dailyStartDate}
                max={getToday()}
                onChange={(e) => setDailyEndDate(e.target.value)}
                className="rounded-xl border border-[#e5e4f5] bg-white px-3 py-3 text-sm outline-none focus:border-[#5865f2]"
              />
            </div>
          </div>

          <button
            onClick={exportDailyReport}
            disabled={filteredDailyRecords.length === 0}
            className="flex items-center justify-center gap-2 rounded-xl bg-[#5145e5] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#4338ca] disabled:cursor-not-allowed disabled:opacity-50"
          >
            <FileSpreadsheet size={17} />
            Xuất báo cáo ngày
          </button>
        </div>

        <div className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-3">
          <div className="rounded-xl border border-[#e5e4f5] bg-[#fafaff] p-4">
            <div className="flex items-center gap-2 text-sm text-[#95877c]">
              <ShoppingCart size={17} className="text-[#5865f2]" />
              Tổng đơn hàng
            </div>
            <p className="mt-3 text-2xl font-bold text-[#302720]">
              {formatNumber(dailySummary.orders)}
            </p>
            <p className="mt-1 text-xs text-[#95877c]">
              Đơn trong khoảng thời gian
            </p>
          </div>

          <div className="rounded-xl border border-[#e5e4f5] bg-[#fafaff] p-4">
            <div className="flex items-center gap-2 text-sm text-[#95877c]">
              <Wallet size={17} className="text-green-600" />
              Tổng doanh thu
            </div>
            <p className="mt-3 text-2xl font-bold text-[#302720]">
              {formatCurrency(dailySummary.revenue)}
            </p>
            <p className="mt-1 text-xs text-[#95877c]">
              Doanh thu trong khoảng thời gian
            </p>
          </div>

          <div className="rounded-xl border border-[#e5e4f5] bg-[#fafaff] p-4">
            <div className="flex items-center gap-2 text-sm text-[#95877c]">
              <Droplets size={17} className="text-blue-500" />
              Nước hoa tiêu thụ
            </div>
            <p className="mt-3 text-2xl font-bold text-[#302720]">
              {formatNumber(dailySummary.consumedMl)} ml
            </p>
            <p className="mt-1 text-xs text-[#95877c]">
              Tổng lượng nước hoa đã sử dụng
            </p>
          </div>
        </div>

        <div className="mb-4 flex items-center justify-between">
          <h4 className="font-semibold text-[#302720]">
            Chi tiết theo từng ngày
          </h4>
          <span className="text-xs text-[#95877c]">
            {filteredDailyRecords.length} ngày có dữ liệu
          </span>
        </div>

        {filteredDailyRecords.length > 0 ? (
          <div className="overflow-x-auto rounded-xl border border-[#e5e4f5]">
            <table className="w-full min-w-[850px] text-left text-sm">
              <thead className="bg-[#f4f3ff] text-[#5145a5]">
                <tr>
                  <th className="px-4 py-4 font-semibold">Ngày</th>
                  <th className="px-4 py-4 font-semibold">Đơn hàng</th>
                  <th className="px-4 py-4 font-semibold">Doanh thu</th>
                  <th className="px-4 py-4 font-semibold">Hoàn thành</th>
                  <th className="px-4 py-4 font-semibold">Đang xử lý</th>
                  <th className="px-4 py-4 font-semibold">Đã hủy</th>
                  <th className="px-4 py-4 font-semibold">Tiêu thụ</th>
                </tr>
              </thead>

              <tbody className="divide-y divide-[#eeeef7]">
                {filteredDailyRecords.map((item) => (
                  <tr key={item.date} className="hover:bg-[#fafaff]">
                    <td className="whitespace-nowrap px-4 py-4 font-medium text-[#302720]">
                      {formatDate(item.date)}
                    </td>
                    <td className="px-4 py-4">{item.orders}</td>
                    <td className="whitespace-nowrap px-4 py-4 font-medium">
                      {formatCurrency(item.revenue)}
                    </td>
                    <td className="px-4 py-4 text-green-600">
                      {item.completed}
                    </td>
                    <td className="px-4 py-4 text-blue-600">
                      {item.processing + item.pending}
                    </td>
                    <td className="px-4 py-4 text-red-500">
                      {item.cancelled}
                    </td>
                    <td className="whitespace-nowrap px-4 py-4 font-semibold text-[#5145e5]">
                      {formatNumber(item.consumedMl)} ml
                    </td>
                  </tr>
                ))}
              </tbody>

              <tfoot className="bg-[#fafaff] font-bold text-[#302720]">
                <tr>
                  <td className="px-4 py-4">Tổng cộng</td>
                  <td className="px-4 py-4">
                    {dailySummary.orders}
                  </td>
                  <td className="whitespace-nowrap px-4 py-4">
                    {formatCurrency(dailySummary.revenue)}
                  </td>
                  <td className="px-4 py-4">
                    {dailySummary.completed}
                  </td>
                  <td className="px-4 py-4">
                    {dailySummary.processing + dailySummary.pending}
                  </td>
                  <td className="px-4 py-4">
                    {dailySummary.cancelled}
                  </td>
                  <td className="whitespace-nowrap px-4 py-4 text-[#5145e5]">
                    {formatNumber(dailySummary.consumedMl)} ml
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        ) : (
          <div className="rounded-xl border border-dashed border-[#d9d8ee] py-12 text-center">
            <CalendarDays
              size={32}
              className="mx-auto mb-3 text-[#a5a2c5]"
            />
            <p className="font-medium text-[#64748b]">
              Không có dữ liệu trong khoảng thời gian này
            </p>
            <p className="mt-1 text-xs text-[#95877c]">
              Vui lòng chọn khoảng ngày khác
            </p>
          </div>
        )}
      </SectionCard>

      {/* ORDER STATUS + TOP PRODUCTS */}
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <SectionCard
          title="Thống kê đơn hàng"
          description="Phân loại theo trạng thái đơn hàng"
        >
          <div className="space-y-6">
            {orderStatus.map((item) => (
              <div key={item.label}>
                <div className="mb-2 flex justify-between text-sm">
                  <span className="text-[#64748b]">{item.label}</span>
                  <span className="font-semibold text-[#302720]">
                    {item.value} đơn
                  </span>
                </div>

                <div className="h-2.5 overflow-hidden rounded-full bg-[#f0effa]">
                  <div
                    className={`h-full rounded-full ${item.color}`}
                    style={{
                      width: `${(item.value / 136) * 100}%`,
                    }}
                  />
                </div>
              </div>
            ))}
          </div>
        </SectionCard>

        <SectionCard
          title="Sản phẩm tiêu thụ nhiều nhất"
          description="Top sản phẩm theo lượng nước hoa đã sử dụng"
        >
          <div className="space-y-5">
            {products.map((item, index) => (
              <div key={item.name}>
                <div className="mb-2 flex items-center gap-3">
                  <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[#eef0ff] text-sm font-bold text-[#5865f2]">
                    {index + 1}
                  </span>

                  <span className="min-w-0 flex-1 truncate text-sm font-medium text-[#302720]">
                    {item.name}
                  </span>

                  <div className="text-right">
                    <p className="text-sm font-semibold text-[#302720]">
                      {formatNumber(item.consumedMl)} ml
                    </p>
                    <p className="text-xs text-[#95877c]">
                      {formatCurrency(item.revenue)}
                    </p>
                  </div>
                </div>

                <div className="h-2 overflow-hidden rounded-full bg-[#f0effa]">
                  <div
                    className="h-full rounded-full bg-[#747cf5]"
                    style={{
                      width: `${(item.consumedMl / products[0].consumedMl) * 100}%`,
                    }}
                  />
                </div>
              </div>
            ))}
          </div>

          <div className="mt-5 border-t border-[#eeeef7] pt-4 text-sm text-[#64748b]">
            Tổng lượng tiêu thụ của 5 sản phẩm:
            <span className="ml-2 font-bold text-[#5145e5]">
              {formatNumber(totalProductMl)} ml
            </span>
          </div>
        </SectionCard>
      </div>

      {/* MACHINE PERFORMANCE */}
      <SectionCard
        title="Hiệu suất máy nước hoa"
        description="Theo dõi số đơn hàng của từng máy"
      >
        <div className="mb-5 flex items-center justify-end gap-2 text-sm text-[#5865f2]">
          <Cpu size={18} />
          8 máy thuộc thương hiệu
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[650px] text-left text-sm">
            <thead className="bg-[#f4f3ff] text-[#64748b]">
              <tr>
                <th className="px-4 py-4 font-semibold">Mã máy</th>
                <th className="px-4 py-4 font-semibold">Địa điểm</th>
                <th className="px-4 py-4 font-semibold">Số đơn hàng</th>
                <th className="px-4 py-4 font-semibold">Trạng thái</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-[#eeeef7]">
              {machines.map((machine) => (
                <tr key={machine.code} className="hover:bg-[#fafaff]">
                  <td className="px-4 py-4 font-medium text-[#5865f2]">
                    {machine.code}
                  </td>
                  <td className="px-4 py-4 text-[#64748b]">
                    {machine.location}
                  </td>
                  <td className="px-4 py-4 font-semibold text-[#302720]">
                    {machine.orders} đơn
                  </td>
                  <td className="px-4 py-4">
                    {machine.status === 'ONLINE' ? (
                      <span className="inline-flex items-center gap-1 rounded-full bg-green-50 px-3 py-1 text-xs font-semibold text-green-700">
                        <CheckCircle2 size={13} />
                        Hoạt động tốt
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 rounded-full bg-red-50 px-3 py-1 text-xs font-semibold text-red-600">
                        <XCircle size={13} />
                        Mất kết nối
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </SectionCard>
    </div>
  )
}
