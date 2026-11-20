import { useMemo, useState } from 'react'
import type { Order, OrderStatus } from '../../../types/orderTypes'
import OrderStatusBadge from './OrderStatusBadge'
import OrderDetail from './OrderDetail'

interface OrdersPageProps {
  canManage?: boolean
}

const initialOrders: Order[] = [
  {
    id: 1,
    orderCode: 'ORD-202611-001',
    customerName: 'Nguyễn Minh Anh',
    customerPhone: '0901234567',
    machineCode: 'SCENT-M01',
    items: [
      {
        productId: 1,
        productName: 'Rose Elegance',
        quantity: 1,
        unitPrice: 15000,
      },
      {
        productId: 2,
        productName: 'White Musk',
        quantity: 1,
        unitPrice: 12000,
      },
    ],
    totalAmount: 27000,
    status: 'PENDING',
    paymentMethod: 'Ví điện tử',
    createdAt: '20/11/2026 09:15',
    note: 'Khách hàng yêu cầu hỗ trợ tại máy.',
  },
  {
    id: 2,
    orderCode: 'ORD-202611-002',
    customerName: 'Trần Quốc Bảo',
    customerPhone: '0912345678',
    machineCode: 'SCENT-M02',
    items: [
      {
        productId: 3,
        productName: 'Royal Oud',
        quantity: 2,
        unitPrice: 20000,
      },
    ],
    totalAmount: 40000,
    status: 'PROCESSING',
    paymentMethod: 'Thẻ ngân hàng',
    createdAt: '20/11/2026 09:30',
  },
  {
    id: 3,
    orderCode: 'ORD-202611-003',
    customerName: 'Lê Hoàng Yến',
    customerPhone: '0934567890',
    machineCode: 'AROMA-M01',
    items: [
      {
        productId: 4,
        productName: 'Ocean Breeze',
        quantity: 1,
        unitPrice: 18000,
      },
    ],
    totalAmount: 18000,
    status: 'COMPLETED',
    paymentMethod: 'Tiền mặt',
    createdAt: '19/11/2026 15:20',
  },
  {
    id: 4,
    orderCode: 'ORD-202611-004',
    customerName: 'Phạm Gia Huy',
    customerPhone: '0945678901',
    machineCode: 'SCENT-M01',
    items: [
      {
        productId: 5,
        productName: 'Vanilla Bloom',
        quantity: 1,
        unitPrice: 16000,
      },
      {
        productId: 6,
        productName: 'Floral Dream',
        quantity: 1,
        unitPrice: 14000,
      },
    ],
    totalAmount: 30000,
    status: 'COMPLETED',
    paymentMethod: 'Ví điện tử',
    createdAt: '19/11/2026 14:05',
  },
  {
    id: 5,
    orderCode: 'ORD-202611-005',
    customerName: 'Võ Ngọc Hà',
    customerPhone: '0967890123',
    machineCode: 'LUXURY-M01',
    items: [
      {
        productId: 2,
        productName: 'White Musk',
        quantity: 1,
        unitPrice: 12000,
      },
    ],
    totalAmount: 12000,
    status: 'CANCELLED',
    paymentMethod: 'Thẻ ngân hàng',
    createdAt: '18/11/2026 10:40',
    note: 'Khách hàng đã yêu cầu hủy đơn.',
  },
  {
    id: 6,
    orderCode: 'ORD-202611-006',
    customerName: 'Đặng Thanh Tùng',
    customerPhone: '0978901234',
    machineCode: 'SCENT-M02',
    items: [
      {
        productId: 1,
        productName: 'Rose Elegance',
        quantity: 2,
        unitPrice: 15000,
      },
    ],
    totalAmount: 30000,
    status: 'PENDING',
    paymentMethod: 'Ví điện tử',
    createdAt: '18/11/2026 08:50',
  },
]

const formatCurrency = (amount: number) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)

const statusOptions: {
  value: OrderStatus
  label: string
}[] = [
  { value: 'PENDING', label: 'Chờ xử lý' },
  { value: 'PROCESSING', label: 'Đang xử lý' },
  { value: 'COMPLETED', label: 'Hoàn thành' },
  { value: 'CANCELLED', label: 'Đã hủy' },
]

function StatCard({
  label,
  value,
  icon,
  color = 'text-[#5865f2]',
}: {
  label: string
  value: number
  icon: string
  color?: string
}) {
  return (
    <div className="rounded-2xl border border-[#e8e6f5] bg-white p-6 shadow-sm">
      <div className="flex items-center justify-between">
        <p className="text-sm text-[#95877c]">{label}</p>

        <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-[#eef0ff] text-xl">
          {icon}
        </div>
      </div>

      <p className={`mt-4 text-3xl font-bold ${color}`}>
        {value}
      </p>
    </div>
  )
}

export default function OrdersPage({
  canManage = false,
}: OrdersPageProps) {
  const [orders, setOrders] = useState<Order[]>(initialOrders)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null)

  // Statistics
  const stats = useMemo(
    () => ({
      total: orders.length,
      pending: orders.filter(
        (order) => order.status === 'PENDING',
      ).length,
      processing: orders.filter(
        (order) => order.status === 'PROCESSING',
      ).length,
      completed: orders.filter(
        (order) => order.status === 'COMPLETED',
      ).length,
    }),
    [orders],
  )

  // Search and filter
  const filteredOrders = useMemo(() => {
    const keyword = search.trim().toLowerCase()

    return orders.filter((order) => {
      const matchesSearch =
        !keyword ||
        order.orderCode.toLowerCase().includes(keyword) ||
        order.customerName.toLowerCase().includes(keyword) ||
        order.customerPhone.includes(keyword) ||
        order.machineCode.toLowerCase().includes(keyword)

      const matchesStatus =
        statusFilter === 'ALL' ||
        order.status === statusFilter

      return matchesSearch && matchesStatus
    })
  }, [orders, search, statusFilter])

  // Update order status
  const updateStatus = (
    orderId: number,
    newStatus: OrderStatus,
  ) => {
    if (!canManage) return

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

  // Cancel order
  const cancelOrder = (order: Order) => {
    if (!canManage) return

    if (
      order.status === 'COMPLETED' ||
      order.status === 'CANCELLED'
    ) {
      alert('Không thể hủy đơn hàng ở trạng thái này.')
      return
    }

    const confirmed = window.confirm(
      `Bạn có chắc muốn hủy đơn ${order.orderCode}?`,
    )

    if (confirmed) {
      updateStatus(order.id, 'CANCELLED')
    }
  }

  return (
    <div className="min-h-screen bg-[#f0efff] p-6">
      {/* Page header */}
      <div className="mb-7 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            {canManage
              ? 'Quản lý đơn hàng'
              : 'Giám sát đơn hàng'}
          </h1>

          <p className="mt-1 text-sm text-[#95877c]">
            {canManage
              ? 'Theo dõi và xử lý đơn hàng của thương hiệu'
              : 'Theo dõi hoạt động đơn hàng trên toàn hệ thống ScentStation'}
          </p>
        </div>
      </div>

      {/* Statistics */}
      <div className="mb-6 grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          label="Tổng số đơn hàng"
          value={stats.total}
          icon="🛒"
        />

        <StatCard
          label="Chờ xử lý"
          value={stats.pending}
          icon="⏳"
          color="text-yellow-600"
        />

        <StatCard
          label="Đang xử lý"
          value={stats.processing}
          icon="⚙️"
          color="text-blue-600"
        />

        <StatCard
          label="Hoàn thành"
          value={stats.completed}
          icon="✓"
          color="text-green-600"
        />
      </div>

      {/* Order table */}
      <div className="overflow-hidden rounded-2xl border border-[#e8e6f5] bg-white shadow-sm">
        {/* Search and filter */}
        <div className="flex flex-col gap-4 border-b border-gray-100 p-5 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <h2 className="font-bold text-gray-900">
              Danh sách đơn hàng
            </h2>

            <p className="mt-1 text-sm text-gray-500">
              Hiển thị {filteredOrders.length} / {orders.length} đơn hàng
            </p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row">
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Tìm mã đơn, khách hàng, máy..."
              className="w-full rounded-xl border border-[#e8e6f5] bg-[#f7f6ff] px-4 py-3 text-sm outline-none focus:border-[#5865f2] sm:w-72"
            />

            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="rounded-xl border border-[#e8e6f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
            >
              <option value="ALL">
                Tất cả trạng thái
              </option>

              {statusOptions.map((option) => (
                <option
                  key={option.value}
                  value={option.value}
                >
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Table */}
        <div className="overflow-x-auto">
          <table className="w-full min-w-[1050px] text-left text-sm">
            <thead className="bg-[#f7f6ff] text-[#71649b]">
              <tr>
                <th className="px-5 py-4 font-semibold">
                  Mã đơn hàng
                </th>

                <th className="px-5 py-4 font-semibold">
                  Khách hàng
                </th>

                <th className="px-5 py-4 font-semibold">
                  Máy phục vụ
                </th>

                <th className="px-5 py-4 font-semibold">
                  Sản phẩm
                </th>

                <th className="px-5 py-4 font-semibold">
                  Tổng tiền
                </th>

                <th className="px-5 py-4 font-semibold">
                  Ngày tạo
                </th>

                <th className="px-5 py-4 font-semibold">
                  Trạng thái
                </th>

                <th className="px-5 py-4 text-center font-semibold">
                  Thao tác
                </th>
              </tr>
            </thead>

            <tbody>
              {filteredOrders.map((order) => (
                <tr
                  key={order.id}
                  className="border-t border-gray-100 transition hover:bg-[#faf9ff]"
                >
                  {/* Order code */}
                  <td className="px-5 py-4">
                    <p className="font-semibold text-[#4935df]">
                      {order.orderCode}
                    </p>

                    <p className="mt-1 text-xs text-gray-400">
                      ID: {order.id}
                    </p>
                  </td>

                  {/* Customer */}
                  <td className="px-5 py-4">
                    <p className="font-medium text-gray-800">
                      {order.customerName}
                    </p>

                    <p className="mt-1 text-xs text-gray-500">
                      {order.customerPhone}
                    </p>
                  </td>

                  {/* Machine */}
                  <td className="px-5 py-4 font-medium text-[#5865f2]">
                    {order.machineCode}
                  </td>

                  {/* Products */}
                  <td className="px-5 py-4">
                    <p className="font-medium text-gray-800">
                      {order.items[0]?.productName}
                    </p>

                    {order.items.length > 1 && (
                      <p className="mt-1 text-xs text-gray-500">
                        +{order.items.length - 1} sản phẩm khác
                      </p>
                    )}

                    <p className="mt-1 text-xs text-gray-400">
                      {order.items.reduce(
                        (sum, item) => sum + item.quantity,
                        0,
                      )}{' '}
                      sản phẩm
                    </p>
                  </td>

                  {/* Total */}
                  <td className="whitespace-nowrap px-5 py-4 font-semibold text-gray-800">
                    {formatCurrency(order.totalAmount)}
                  </td>

                  {/* Created date */}
                  <td className="whitespace-nowrap px-5 py-4 text-gray-500">
                    {order.createdAt}
                  </td>

                  {/* Status */}
                  <td className="px-5 py-4">
                    <OrderStatusBadge status={order.status} />
                  </td>

                  {/* Actions */}
                  <td className="px-5 py-4">
                    <div className="flex items-center justify-center gap-2">
                      {/* Both roles can view */}
                      <button
                        title="Xem chi tiết"
                        onClick={() =>
                          setSelectedOrder(order)
                        }
                        className="rounded-lg border border-[#e8e6f5] px-3 py-2 text-[#71649b] hover:bg-[#eef0ff] hover:text-[#4935df]"
                      >
                        <span aria-hidden="true">◎</span>
                      </button>

                      {/* Only Brand Admin can manage */}
                      {canManage && (
                        <>
                          <select
                            title="Cập nhật trạng thái"
                            value={order.status}
                            onChange={(e) =>
                              updateStatus(
                                order.id,
                                e.target.value as OrderStatus,
                              )
                            }
                            className="max-w-[130px] rounded-lg border border-[#e8e6f5] bg-white px-2 py-2 text-xs outline-none focus:border-[#5865f2]"
                          >
                            {statusOptions.map((option) => (
                              <option
                                key={option.value}
                                value={option.value}
                              >
                                {option.label}
                              </option>
                            ))}
                          </select>

                          <button
                            title="Hủy đơn"
                            disabled={
                              order.status === 'COMPLETED' ||
                              order.status === 'CANCELLED'
                            }
                            onClick={() => cancelOrder(order)}
                            className="rounded-lg border border-red-100 px-3 py-2 text-red-500 hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-30"
                          >
                            ×
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}

              {/* Empty state */}
              {filteredOrders.length === 0 && (
                <tr>
                  <td
                    colSpan={8}
                    className="px-5 py-14 text-center"
                  >
                    <div className="text-3xl">📦</div>

                    <p className="mt-3 font-medium text-gray-700">
                      Không tìm thấy đơn hàng
                    </p>

                    <p className="mt-1 text-sm text-gray-400">
                      Thử thay đổi từ khóa hoặc bộ lọc trạng thái.
                    </p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Order detail modal */}
      <OrderDetail
        order={selectedOrder}
        onClose={() => setSelectedOrder(null)}
      />
    </div>
  )
}
