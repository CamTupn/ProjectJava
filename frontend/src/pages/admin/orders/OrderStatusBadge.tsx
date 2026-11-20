import type { OrderStatus } from '../../../types/orderTypes'

interface OrderStatusBadgeProps {
  status: OrderStatus
}

const statusConfig: Record<
  OrderStatus,
  { label: string; className: string }
> = {
  PENDING: {
    label: 'Chờ xử lý',
    className: 'bg-yellow-50 text-yellow-700',
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
    className: 'bg-red-50 text-red-700',
  },
}

export default function OrderStatusBadge({
  status,
}: OrderStatusBadgeProps) {
  const config = statusConfig[status]

  return (
    <span
      className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium whitespace-nowrap ${config.className}`}
    >
      {config.label}
    </span>
  )
}
