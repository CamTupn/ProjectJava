type SlotStatus =
  | 'AVAILABLE'
  | 'LOW_STOCK'
  | 'EMPTY'
  | 'DISABLED'
  | 'ERROR'

interface SlotStatusBadgeProps {
  status: SlotStatus
}

const statusConfig: Record<
  SlotStatus,
  { label: string; className: string }
> = {
  AVAILABLE: {
    label: 'Sẵn sàng',
    className: 'bg-green-50 text-green-700',
  },
  LOW_STOCK: {
    label: 'Sắp hết nước hoa',
    className: 'bg-yellow-50 text-yellow-700',
  },
  EMPTY: {
    label: 'Đã hết nước hoa',
    className: 'bg-gray-100 text-gray-600',
  },
  DISABLED: {
    label: 'Đã vô hiệu hóa',
    className: 'bg-slate-100 text-slate-600',
  },
  ERROR: {
    label: 'Đang gặp lỗi',
    className: 'bg-red-50 text-red-700',
  },
}

export default function SlotStatusBadge({
  status,
}: SlotStatusBadgeProps) {
  const config = statusConfig[status] ?? statusConfig.ERROR

  return (
    <span
      className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium whitespace-nowrap ${config.className}`}
    >
      {config.label}
    </span>
  )
}
