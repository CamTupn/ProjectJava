import { useMemo, useState } from 'react'
import {
  Search,
  ShoppingCart,
  Clock,
  CheckCircle2,
  Eye,
  X,
  XCircle,
  Package,
  User,
  Phone,
  CreditCard,
  CalendarDays,
  AlertTriangle,
} from 'lucide-react'

type OrderStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'CANCELLED'

interface OrderItem {
  id: number
  productName: string
  quantity: number
  unitPrice: number
}

interface Order {
  id: number
  orderCode: string
  customerName: string
  phone: string
  machineCode: string
  locationName: string
  createdAt: string
  paymentMethod: string
  paymentStatus: string
  status: OrderStatus
  items: OrderItem[]
  cancellationReason?: string
}

const initialOrders: Order[] = [
  {
    id: 1,
    orderCode: 'ORD-202611-001',
    customerName: 'Nguyễn Minh Anh',
    phone: '0901234567',
    machineCode: 'SCENT-M01',
    locationName: 'Vincom Đồng Khởi',
    createdAt: '20/11/2026 09:15',
    paymentMethod: 'Ví điện tử',
    paymentStatus: 'Đã thanh toán',
    status: 'PENDING',
    items: [
      { id: 1, productName: 'Rose Elegance', quantity: 1, unitPrice: 15000 },
      { id: 2, productName: 'White Musk', quantity: 1, unitPrice: 12000 },
    ],
  },
  {
    id: 2,
    orderCode: 'ORD-202611-002',
    customerName: 'Trần Quốc Bảo',
    phone: '0912345678',
    machineCode: 'SCENT-M02',
    locationName: 'Landmark 81',
    createdAt: '20/11/2026 09:30',
    paymentMethod: 'Tiền mặt',
    paymentStatus: 'Đã thanh toán',
    status: 'PROCESSING',
    items: [
      { id: 3, productName: 'Royal Oud', quantity: 2, unitPrice: 20000 },
    ],
  },
  {
    id: 3,
    orderCode: 'ORD-202611-003',
    customerName: 'Lê Hoàng Yến',
    phone: '0934567890',
    machineCode: 'SCENT-M03',
    locationName: 'Aeon Mall Tân Phú',
    createdAt: '19/11/2026 15:20',
    paymentMethod: 'Chuyển khoản',
    paymentStatus: 'Đã thanh toán',
    status: 'COMPLETED',
    items: [
      { id: 4, productName: 'Ocean Breeze', quantity: 1, unitPrice: 18000 },
    ],
  },
  {
    id: 4,
    orderCode: 'ORD-202611-004',
    customerName: 'Phạm Gia Huy',
    phone: '0945678901',
    machineCode: 'SCENT-M01',
    locationName: 'Vincom Đồng Khởi',
    createdAt: '19/11/2026 14:05',
    paymentMethod: 'Ví điện tử',
    paymentStatus: 'Đã thanh toán',
    status: 'COMPLETED',
    items: [
      { id: 5, productName: 'Vanilla Bloom', quantity: 2, unitPrice: 15000 },
    ],
  },
  {
    id: 5,
    orderCode: 'ORD-202611-005',
    customerName: 'Đỗ Thu Hà',
    phone: '0967890123',
    machineCode: 'SCENT-M02',
    locationName: 'Landmark 81',
    createdAt: '19/11/2026 10:45',
    paymentMethod: 'Tiền mặt',
    paymentStatus: 'Chưa thanh toán',
    status: 'CANCELLED',
    cancellationReason: 'Khách hàng yêu cầu hủy đơn.',
    items: [
      { id: 6, productName: 'Floral Dream', quantity: 1, unitPrice: 22000 },
    ],
  },
  {
    id: 6,
    orderCode: 'ORD-202611-006',
    customerName: 'Đặng Thanh Tùng',
    phone: '0978901234',
    machineCode: 'SCENT-M02',
    locationName: 'Landmark 81',
    createdAt: '18/11/2026 08:50',
    paymentMethod: 'Ví điện tử',
    paymentStatus: 'Đã thanh toán',
    status: 'PENDING',
    items: [
      { id: 7, productName: 'Rose Elegance', quantity: 2, unitPrice: 15000 },
    ],
  },
]

const statusConfig: Record<
  OrderStatus,
  { label: string; className: string }
> = {
  PENDING: {
    label: 'Chờ xử lý',
    className: 'bg-amber-50 text-amber-700',
  },
  PROCESSING: {
    label: 'Đang xử lý',
    className: 'bg-blue-50 text-blue-700',
  },
  COMPLETED: {
    label: 'Hoàn thành',
    className: 'bg-green-50 text-green-700',
  },
  CANCELLED: {
    label: 'Đã hủy',
    className: 'bg-red-50 text-red-600',
  },
}

const formatCurrency = (amount: number) =>
  `${amount.toLocaleString('vi-VN')} đ`

const getOrderTotal = (order: Order) =>
  order.items.reduce(
    (total, item) => total + item.quantity * item.unitPrice,
    0,
  )

const canCancelOrder = (order: Order) =>
  order.status === 'PENDING' || order.status === 'PROCESSING'

function StatusBadge({ status }: { status: OrderStatus }) {
  const config = statusConfig[status]

  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${config.className}`}
    >
      {config.label}
    </span>
  )
}

// Modal xác nhận hủy đơn hàng
function CancelOrderModal({
  order,
  reason,
  onReasonChange,
  onClose,
  onConfirm,
}: {
  order: Order
  reason: string
  onReasonChange: (value: string) => void
  onClose: () => void
  onConfirm: () => void
}) {
  const trimmedReason = reason.trim()

  return (
    <div className="fixed inset-0 z-[70] flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-[#E8E8F5] px-6 py-5">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-red-50 p-3 text-red-600">
              <AlertTriangle size={21} />
            </div>
            <div>
              <h2 className="text-lg font-bold text-[#302720]">
                Xác nhận hủy đơn hàng
              </h2>
              <p className="mt-1 text-sm text-[#95877C]">
                {order.orderCode}
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="rounded-lg p-2 text-gray-400 hover:bg-gray-100"
            aria-label="Đóng"
          >
            <X size={21} />
          </button>
        </div>

        <div className="space-y-4 p-6">
          <div className="rounded-xl bg-red-50 p-4 text-sm text-red-700">
            Bạn có chắc chắn muốn hủy đơn hàng này không? Thao tác này sẽ
            chuyển trạng thái đơn sang Đã hủy.
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-[#302720]">
              Lý do hủy đơn <span className="text-red-500">*</span>
            </label>

            <textarea
              value={reason}
              onChange={(e) => onReasonChange(e.target.value)}
              rows={4}
              maxLength={500}
              placeholder="Nhập lý do hủy đơn hàng..."
              className="w-full resize-none rounded-xl border border-[#E7E5F4] px-4 py-3 text-sm outline-none focus:border-red-400"
            />

            <p className="mt-1 text-right text-xs text-[#95877C]">
              {reason.length}/500 ký tự
            </p>

            {!trimmedReason && reason.length > 0 && (
              <p className="mt-1 text-xs text-red-500">
                Vui lòng nhập lý do hợp lệ.
              </p>
            )}
          </div>
        </div>

        <div className="flex justify-end gap-3 border-t border-[#E8E8F5] px-6 py-4">
          <button
            onClick={onClose}
            className="rounded-xl border border-[#E7E5F4] px-5 py-2.5 text-sm font-semibold text-[#625A8A] hover:bg-[#F8F7FF]"
          >
            Quay lại
          </button>

          <button
            onClick={onConfirm}
            disabled={!trimmedReason}
            className="rounded-xl bg-red-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-40"
          >
            Xác nhận hủy đơn
          </button>
        </div>
      </div>
    </div>
  )
}

function OrderDetail({
  order,
  onClose,
  onRequestCancel,
}: {
  order: Order
  onClose: () => void
  onRequestCancel: (order: Order) => void
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="flex max-h-[90vh] w-full max-w-3xl flex-col overflow-hidden rounded-2xl bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-[#E8E8F5] px-6 py-5">
          <div>
            <h2 className="text-xl font-bold text-[#302720]">
              Chi tiết đơn hàng
            </h2>
            <p className="mt-1 text-sm text-[#95877C]">
              Thông tin đơn {order.orderCode}
            </p>
          </div>

          <button
            onClick={onClose}
            className="rounded-lg p-2 text-gray-400 hover:bg-gray-100"
          >
            <X size={22} />
          </button>
        </div>

        <div className="space-y-6 overflow-y-auto p-6">
          <div className="flex items-center justify-between rounded-xl bg-[#EEF0FF] p-5">
            <div>
              <p className="text-sm text-[#95877C]">Mã đơn hàng</p>
              <p className="mt-1 text-lg font-bold text-[#4F5FE8]">
                {order.orderCode}
              </p>
            </div>
            <StatusBadge status={order.status} />
          </div>

          <section>
            <h3 className="mb-3 font-semibold text-[#302720]">
              Thông tin khách hàng
            </h3>

            <div className="grid grid-cols-1 gap-4 rounded-xl border border-[#E8E8F5] p-5 sm:grid-cols-2">
              <div className="flex items-center gap-3">
                <User className="text-[#5865F2]" size={19} />
                <div>
                  <p className="text-xs text-[#95877C]">Tên khách hàng</p>
                  <p className="font-medium">{order.customerName}</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Phone className="text-[#5865F2]" size={19} />
                <div>
                  <p className="text-xs text-[#95877C]">Số điện thoại</p>
                  <p className="font-medium">{order.phone}</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Package className="text-[#5865F2]" size={19} />
                <div>
                  <p className="text-xs text-[#95877C]">Máy phục vụ</p>
                  <p className="font-medium">{order.machineCode}</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <CalendarDays className="text-[#5865F2]" size={19} />
                <div>
                  <p className="text-xs text-[#95877C]">Ngày tạo</p>
                  <p className="font-medium">{order.createdAt}</p>
                </div>
              </div>

              <div className="sm:col-span-2">
                <p className="text-xs text-[#95877C]">Địa điểm</p>
                <p className="font-medium">{order.locationName}</p>
              </div>
            </div>
          </section>

          <section>
            <h3 className="mb-3 font-semibold text-[#302720]">
              Sản phẩm trong đơn
            </h3>

            <div className="overflow-hidden rounded-xl border border-[#E8E8F5]">
              <table className="w-full text-sm">
                <thead className="bg-[#F5F4FF] text-[#625A8A]">
                  <tr>
                    <th className="px-4 py-3 text-left">Sản phẩm</th>
                    <th className="px-4 py-3 text-center">SL</th>
                    <th className="px-4 py-3 text-right">Đơn giá</th>
                  </tr>
                </thead>

                <tbody>
                  {order.items.map((item) => (
                    <tr key={item.id} className="border-t border-[#EEEEF5]">
                      <td className="px-4 py-3 font-medium">
                        {item.productName}
                      </td>
                      <td className="px-4 py-3 text-center">
                        {item.quantity}
                      </td>
                      <td className="px-4 py-3 text-right">
                        {formatCurrency(item.unitPrice)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>

          <section className="rounded-xl bg-[#F8F7FF] p-5">
            <div className="flex items-center gap-2">
              <CreditCard size={18} className="text-[#5865F2]" />
              <h3 className="font-semibold">Thông tin thanh toán</h3>
            </div>

            <div className="mt-3 flex justify-between text-sm">
              <span className="text-[#95877C]">Phương thức</span>
              <span className="font-medium">{order.paymentMethod}</span>
            </div>

            <div className="mt-2 flex justify-between text-sm">
              <span className="text-[#95877C]">Trạng thái thanh toán</span>
              <span className="font-medium">{order.paymentStatus}</span>
            </div>

            <div className="mt-4 flex justify-between border-t border-[#E6E3F5] pt-4">
              <span className="font-semibold">Tổng tiền</span>
              <span className="text-lg font-bold text-[#4F5FE8]">
                {formatCurrency(getOrderTotal(order))}
              </span>
            </div>
          </section>

          {order.status === 'CANCELLED' && order.cancellationReason && (
            <section className="rounded-xl border border-red-100 bg-red-50 p-5">
              <h3 className="font-semibold text-red-700">Lý do hủy đơn</h3>
              <p className="mt-2 text-sm text-red-600">
                {order.cancellationReason}
              </p>
            </section>
          )}
        </div>

        <div className="flex flex-wrap justify-between gap-3 border-t border-[#E8E8F5] px-6 py-4">
          <div>
            {canCancelOrder(order) && (
              <button
                onClick={() => onRequestCancel(order)}
                className="flex items-center gap-2 rounded-xl border border-red-200 px-5 py-3 text-sm font-semibold text-red-600 hover:bg-red-50"
              >
                <XCircle size={17} />
                Hủy đơn hàng
              </button>
            )}
          </div>

          <button
            onClick={onClose}
            className="rounded-xl bg-[#4F46E5] px-6 py-3 text-sm font-semibold text-white hover:bg-[#4338CA]"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  )
}

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>(initialOrders)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null)

  const [cancelTarget, setCancelTarget] = useState<Order | null>(null)
  const [cancelReason, setCancelReason] = useState('')

  const [notification, setNotification] = useState('')

  const stats = useMemo(
    () => ({
      total: orders.length,
      pending: orders.filter((o) => o.status === 'PENDING').length,
      processing: orders.filter((o) => o.status === 'PROCESSING').length,
      completed: orders.filter((o) => o.status === 'COMPLETED').length,
    }),
    [orders],
  )

  const filteredOrders = useMemo(() => {
    const keyword = search.trim().toLowerCase()

    return orders.filter((order) => {
      const matchesSearch =
        order.orderCode.toLowerCase().includes(keyword) ||
        order.customerName.toLowerCase().includes(keyword) ||
        order.phone.includes(keyword) ||
        order.machineCode.toLowerCase().includes(keyword)

      const matchesStatus =
        statusFilter === 'ALL' || order.status === statusFilter

      return matchesSearch && matchesStatus
    })
  }, [orders, search, statusFilter])

  const updateStatus = (orderId: number, newStatus: OrderStatus) => {
    setOrders((current) =>
      current.map((order) =>
        order.id === orderId
          ? { ...order, status: newStatus }
          : order,
      ),
    )

    setSelectedOrder((current) =>
      current?.id === orderId
        ? { ...current, status: newStatus }
        : current,
    )
  }

  const openCancelModal = (order: Order) => {
    if (!canCancelOrder(order)) return

    setCancelTarget(order)
    setCancelReason('')
  }

  const closeCancelModal = () => {
    setCancelTarget(null)
    setCancelReason('')
  }

  const confirmCancelOrder = () => {
    if (!cancelTarget || !cancelReason.trim()) return
    if (!canCancelOrder(cancelTarget)) return

    const orderId = cancelTarget.id
    const reason = cancelReason.trim()

    setOrders((current) =>
      current.map((order) =>
        order.id === orderId
          ? {
              ...order,
              status: 'CANCELLED',
              cancellationReason: reason,
            }
          : order,
      ),
    )

    setSelectedOrder((current) =>
      current?.id === orderId
        ? {
            ...current,
            status: 'CANCELLED',
            cancellationReason: reason,
          }
        : current,
    )

    closeCancelModal()
    setNotification(`Đơn hàng ${cancelTarget.orderCode} đã được hủy.`)
  }

  const statCards = [
    {
      label: 'Tổng đơn hàng',
      value: stats.total,
      icon: ShoppingCart,
      color: 'text-[#5865F2]',
      bg: 'bg-[#EEF0FF]',
    },
    {
      label: 'Chờ xử lý',
      value: stats.pending,
      icon: Clock,
      color: 'text-amber-600',
      bg: 'bg-amber-50',
    },
    {
      label: 'Đang xử lý',
      value: stats.processing,
      icon: Package,
      color: 'text-blue-600',
      bg: 'bg-blue-50',
    },
    {
      label: 'Hoàn thành',
      value: stats.completed,
      icon: CheckCircle2,
      color: 'text-green-600',
      bg: 'bg-green-50',
    },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-[#302720]">
          Quản lý đơn hàng
        </h1>
        <p className="mt-1 text-sm text-[#95877C]">
          Theo dõi và xử lý đơn hàng thuộc thương hiệu ScentStation Demo
        </p>
      </div>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {statCards.map((card) => {
          const Icon = card.icon

          return (
            <div
              key={card.label}
              className="rounded-2xl border border-[#E7E5F4] bg-white p-6 shadow-sm"
            >
              <div className="flex items-center justify-between">
                <p className="text-sm text-[#95877C]">{card.label}</p>
                <div className={`rounded-xl p-3 ${card.bg} ${card.color}`}>
                  <Icon size={21} />
                </div>
              </div>

              <p className={`mt-5 text-3xl font-bold ${card.color}`}>
                {card.value}
              </p>
            </div>
          )
        })}
      </div>

      <div className="overflow-hidden rounded-2xl border border-[#E7E5F4] bg-white shadow-sm">
        <div className="flex flex-col justify-between gap-4 border-b border-[#EEEEF5] p-5 lg:flex-row lg:items-center">
          <div>
            <h2 className="font-bold text-[#302720]">Danh sách đơn hàng</h2>
            <p className="mt-1 text-sm text-[#95877C]">
              Hiển thị {filteredOrders.length} / {orders.length} đơn hàng
            </p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row">
            <div className="relative">
              <Search
                size={18}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-[#95877C]"
              />
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Tìm mã đơn, khách hàng, máy..."
                className="w-full rounded-xl border border-[#E7E5F4] bg-[#F8F7FF] py-3 pl-10 pr-4 text-sm outline-none focus:border-[#5865F2] sm:w-72"
              />
            </div>

            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="rounded-xl border border-[#E7E5F4] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865F2]"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="PENDING">Chờ xử lý</option>
              <option value="PROCESSING">Đang xử lý</option>
              <option value="COMPLETED">Hoàn thành</option>
              <option value="CANCELLED">Đã hủy</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[1100px] text-sm">
            <thead className="bg-[#F5F4FF] text-[#625A8A]">
              <tr>
                <th className="px-5 py-4 text-left">Mã đơn hàng</th>
                <th className="px-5 py-4 text-left">Khách hàng</th>
                <th className="px-5 py-4 text-left">Máy phục vụ</th>
                <th className="px-5 py-4 text-left">Sản phẩm</th>
                <th className="px-5 py-4 text-right">Tổng tiền</th>
                <th className="px-5 py-4 text-left">Ngày tạo</th>
                <th className="px-5 py-4 text-left">Trạng thái</th>
                <th className="px-5 py-4 text-center">Thao tác</th>
              </tr>
            </thead>

            <tbody>
              {filteredOrders.map((order) => (
                <tr
                  key={order.id}
                  className="border-t border-[#EEEEF5] hover:bg-[#FAFAFF]"
                >
                  <td className="px-5 py-4">
                    <p className="font-semibold text-[#4F46E5]">
                      {order.orderCode}
                    </p>
                    <p className="mt-1 text-xs text-[#95877C]">
                      ID: {order.id}
                    </p>
                  </td>

                  <td className="px-5 py-4">
                    <p className="font-medium text-[#302720]">
                      {order.customerName}
                    </p>
                    <p className="mt-1 text-xs text-[#95877C]">
                      {order.phone}
                    </p>
                  </td>

                  <td className="px-5 py-4">
                    <p className="font-medium text-[#5865F2]">
                      {order.machineCode}
                    </p>
                    <p className="mt-1 text-xs text-[#95877C]">
                      {order.locationName}
                    </p>
                  </td>

                  <td className="px-5 py-4">
                    <p className="font-medium">
                      {order.items[0]?.productName}
                    </p>
                    {order.items.length > 1 && (
                      <p className="mt-1 text-xs text-[#95877C]">
                        + {order.items.length - 1} sản phẩm khác
                      </p>
                    )}
                    <p className="mt-1 text-xs text-[#95877C]">
                      {order.items.reduce(
                        (sum, item) => sum + item.quantity,
                        0,
                      )}{' '}
                      sản phẩm
                    </p>
                  </td>

                  <td className="px-5 py-4 text-right font-semibold">
                    {formatCurrency(getOrderTotal(order))}
                  </td>

                  <td className="px-5 py-4 text-[#95877C]">
                    {order.createdAt}
                  </td>

                  <td className="px-5 py-4">
                    <StatusBadge status={order.status} />
                  </td>

                  <td className="px-5 py-4">
                    <div className="flex items-center justify-center gap-2">
                      <button
                        onClick={() => setSelectedOrder(order)}
                        title="Xem chi tiết"
                        className="rounded-lg border border-[#E7E5F4] p-2 text-[#5865F2] hover:bg-[#EEF0FF]"
                      >
                        <Eye size={17} />
                      </button>

                      {canCancelOrder(order) && (
                        <button
                          onClick={() => openCancelModal(order)}
                          title="Hủy đơn hàng"
                          className="rounded-lg border border-red-200 p-2 text-red-600 hover:bg-red-50"
                        >
                          <XCircle size={17} />
                        </button>
                      )}

                      {order.status !== 'COMPLETED' &&
                        order.status !== 'CANCELLED' && (
                          <select
                            value={order.status}
                            onChange={(e) =>
                              updateStatus(
                                order.id,
                                e.target.value as OrderStatus,
                              )
                            }
                            className="max-w-[145px] rounded-lg border border-[#E7E5F4] bg-white px-2 py-2 text-xs outline-none focus:border-[#5865F2]"
                          >
                            <option value="PENDING">Chờ xử lý</option>
                            <option value="PROCESSING">Đang xử lý</option>
                            <option value="COMPLETED">Hoàn thành</option>
                          </select>
                        )}
                    </div>
                  </td>
                </tr>
              ))}

              {filteredOrders.length === 0 && (
                <tr>
                  <td
                    colSpan={8}
                    className="px-5 py-12 text-center text-[#95877C]"
                  >
                    Không tìm thấy đơn hàng phù hợp.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {selectedOrder && (
        <OrderDetail
          order={selectedOrder}
          onClose={() => setSelectedOrder(null)}
          onRequestCancel={openCancelModal}
        />
      )}

      {cancelTarget && (
        <CancelOrderModal
          order={cancelTarget}
          reason={cancelReason}
          onReasonChange={setCancelReason}
          onClose={closeCancelModal}
          onConfirm={confirmCancelOrder}
        />
      )}

      {notification && (
        <div className="fixed bottom-6 right-6 z-[80] flex items-center gap-3 rounded-xl bg-green-600 px-5 py-4 text-sm font-medium text-white shadow-lg">
          <CheckCircle2 size={19} />
          {notification}
          <button
            onClick={() => setNotification('')}
            className="ml-2 rounded p-1 hover:bg-green-700"
            aria-label="Đóng thông báo"
          >
            <X size={16} />
          </button>
        </div>
      )}
    </div>
  )
}
