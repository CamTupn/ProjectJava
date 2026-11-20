import type { Order } from '../../../types/orderTypes'
import OrderStatusBadge from './OrderStatusBadge'

interface OrderDetailProps {
  order: Order | null
  onClose: () => void
}

const formatCurrency = (amount: number) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)

export default function OrderDetail({
  order,
  onClose,
}: OrderDetailProps) {
  if (!order) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-2xl overflow-hidden rounded-2xl bg-white shadow-xl">
        {/* Header */}
        <div className="flex items-start justify-between border-b border-gray-100 px-6 py-5">
          <div>
            <h2 className="text-xl font-bold text-gray-900">
              Chi tiết đơn hàng
            </h2>
            <p className="mt-1 text-sm text-gray-500">
              Thông tin chi tiết đơn {order.orderCode}
            </p>
          </div>

          <button
            onClick={onClose}
            className="rounded-lg px-3 py-1 text-2xl text-gray-400 hover:bg-gray-100"
          >
            ×
          </button>
        </div>

        {/* Content */}
        <div className="max-h-[70vh] space-y-6 overflow-y-auto p-6">
          {/* Order code */}
          <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl bg-[#eef0ff] p-4">
            <div>
              <p className="text-sm text-gray-500">Mã đơn hàng</p>
              <p className="mt-1 text-lg font-bold text-[#4935df]">
                {order.orderCode}
              </p>
            </div>

            <OrderStatusBadge status={order.status} />
          </div>

          {/* Customer information */}
          <section>
            <h3 className="mb-3 font-semibold text-gray-800">
              Thông tin khách hàng
            </h3>

            <div className="grid grid-cols-1 gap-4 rounded-xl border border-gray-100 p-4 sm:grid-cols-2">
              <div>
                <p className="text-sm text-gray-500">Tên khách hàng</p>
                <p className="mt-1 font-medium text-gray-800">
                  {order.customerName}
                </p>
              </div>

              <div>
                <p className="text-sm text-gray-500">Số điện thoại</p>
                <p className="mt-1 font-medium text-gray-800">
                  {order.customerPhone}
                </p>
              </div>

              <div>
                <p className="text-sm text-gray-500">Máy phục vụ</p>
                <p className="mt-1 font-medium text-[#5865f2]">
                  {order.machineCode}
                </p>
              </div>

              <div>
                <p className="text-sm text-gray-500">Ngày tạo</p>
                <p className="mt-1 font-medium text-gray-800">
                  {order.createdAt}
                </p>
              </div>
            </div>
          </section>

          {/* Products */}
          <section>
            <h3 className="mb-3 font-semibold text-gray-800">
              Sản phẩm trong đơn
            </h3>

            <div className="overflow-hidden rounded-xl border border-gray-100">
              <table className="w-full text-sm">
                <thead className="bg-[#f7f6ff] text-left text-gray-600">
                  <tr>
                    <th className="px-4 py-3">Sản phẩm</th>
                    <th className="px-3 py-3 text-center">SL</th>
                    <th className="px-4 py-3 text-right">Đơn giá</th>
                  </tr>
                </thead>

                <tbody>
                  {order.items.map((item) => (
                    <tr
                      key={item.productId}
                      className="border-t border-gray-100"
                    >
                      <td className="px-4 py-3 font-medium text-gray-800">
                        {item.productName}
                      </td>

                      <td className="px-3 py-3 text-center">
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

          {/* Payment */}
          <section className="space-y-3 rounded-xl border border-gray-100 p-4">
            <div className="flex justify-between gap-3 text-sm">
              <span className="text-gray-500">
                Phương thức thanh toán
              </span>

              <span className="font-medium text-gray-800">
                {order.paymentMethod}
              </span>
            </div>

            <div className="flex justify-between gap-3 border-t border-gray-100 pt-3">
              <span className="font-semibold text-gray-800">
                Tổng thanh toán
              </span>

              <span className="text-lg font-bold text-[#4935df]">
                {formatCurrency(order.totalAmount)}
              </span>
            </div>
          </section>

          {/* Note */}
          {order.note && (
            <section>
              <h3 className="mb-2 font-semibold text-gray-800">
                Ghi chú
              </h3>

              <p className="rounded-xl bg-gray-50 p-4 text-sm text-gray-600">
                {order.note}
              </p>
            </section>
          )}
        </div>

        {/* Footer */}
        <div className="flex justify-end border-t border-gray-100 px-6 py-4">
          <button
            onClick={onClose}
            className="rounded-xl bg-[#4935df] px-6 py-3 text-sm font-semibold text-white hover:bg-[#3f2bc7]"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  )
}