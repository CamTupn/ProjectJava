import { useMemo, useState } from "react";
import {
  Search,
  Plus,
  Eye,
  Pencil,
  Trash2,
  X,
  Layers,
  Droplets,
  Package,
  AlertTriangle,
  CheckCircle2,
  Ban,
} from "lucide-react";

type SlotStatus = "AVAILABLE" | "LOW_STOCK" | "EMPTY" | "DISABLED";

type Slot = {
  id: number;
  slotNumber: number;
  code: string;
  machineId: number;
  machineCode: string;
  machineName: string;
  locationName: string;
  productId: number | null;
  productName: string;
  capacityMl: number;
  currentVolumeMl: number;
  lowStockThresholdMl: number;
  status: SlotStatus;
  updatedAt: string;
};

const machines = [
  {
    id: 1,
    code: "MCH-001",
    name: "Máy nước hoa tầng 1",
    location: "Vincom Center Đồng Khởi",
    slotCount: 6,
  },
  {
    id: 2,
    code: "MCH-002",
    name: "Máy nước hoa khu mỹ phẩm",
    location: "Vincom Center Đồng Khởi",
    slotCount: 6,
  },
  {
    id: 3,
    code: "MCH-003",
    name: "Máy nước hoa tầng trệt",
    location: "Crescent Mall",
    slotCount: 8,
  },
  {
    id: 4,
    code: "MCH-004",
    name: "Máy nước hoa sảnh B",
    location: "Landmark 81",
    slotCount: 6,
  },
  {
    id: 5,
    code: "MCH-005",
    name: "Máy nước hoa khu giải trí",
    location: "Gigamall",
    slotCount: 8,
  },
];

const products = [
  { id: 1, code: "PRD-001", name: "Rose Elegance" },
  { id: 2, code: "PRD-002", name: "Royal Oud" },
  { id: 3, code: "PRD-003", name: "Ocean Breeze" },
  { id: 4, code: "PRD-004", name: "Vanilla Bloom" },
  { id: 5, code: "PRD-005", name: "White Musk" },
  { id: 6, code: "PRD-007", name: "Safari" },
];

const initialSlots: Slot[] = [
  {
    id: 1,
    slotNumber: 1,
    code: "MCH-001-S01",
    machineId: 1,
    machineCode: "MCH-001",
    machineName: "Máy nước hoa tầng 1",
    locationName: "Vincom Center Đồng Khởi",
    productId: 1,
    productName: "Rose Elegance",
    capacityMl: 500,
    currentVolumeMl: 420,
    lowStockThresholdMl: 100,
    status: "AVAILABLE",
    updatedAt: "20/11/2026 15:30",
  },
  {
    id: 2,
    slotNumber: 2,
    code: "MCH-001-S02",
    machineId: 1,
    machineCode: "MCH-001",
    machineName: "Máy nước hoa tầng 1",
    locationName: "Vincom Center Đồng Khởi",
    productId: 2,
    productName: "Royal Oud",
    capacityMl: 500,
    currentVolumeMl: 65,
    lowStockThresholdMl: 100,
    status: "LOW_STOCK",
    updatedAt: "20/11/2026 14:20",
  },
  {
    id: 3,
    slotNumber: 3,
    code: "MCH-001-S03",
    machineId: 1,
    machineCode: "MCH-001",
    machineName: "Máy nước hoa tầng 1",
    locationName: "Vincom Center Đồng Khởi",
    productId: 3,
    productName: "Ocean Breeze",
    capacityMl: 500,
    currentVolumeMl: 0,
    lowStockThresholdMl: 100,
    status: "EMPTY",
    updatedAt: "20/11/2026 12:10",
  },
  {
    id: 4,
    slotNumber: 4,
    code: "MCH-001-S04",
    machineId: 1,
    machineCode: "MCH-001",
    machineName: "Máy nước hoa tầng 1",
    locationName: "Vincom Center Đồng Khởi",
    productId: 4,
    productName: "Vanilla Bloom",
    capacityMl: 500,
    currentVolumeMl: 350,
    lowStockThresholdMl: 100,
    status: "AVAILABLE",
    updatedAt: "20/11/2026 13:45",
  },
  {
    id: 5,
    slotNumber: 1,
    code: "MCH-002-S01",
    machineId: 2,
    machineCode: "MCH-002",
    machineName: "Máy nước hoa khu mỹ phẩm",
    locationName: "Vincom Center Đồng Khởi",
    productId: 5,
    productName: "White Musk",
    capacityMl: 500,
    currentVolumeMl: 250,
    lowStockThresholdMl: 100,
    status: "AVAILABLE",
    updatedAt: "20/11/2026 11:30",
  },
  {
    id: 6,
    slotNumber: 2,
    code: "MCH-002-S02",
    machineId: 2,
    machineCode: "MCH-002",
    machineName: "Máy nước hoa khu mỹ phẩm",
    locationName: "Vincom Center Đồng Khởi",
    productId: 6,
    productName: "Safari",
    capacityMl: 500,
    currentVolumeMl: 35,
    lowStockThresholdMl: 100,
    status: "LOW_STOCK",
    updatedAt: "20/11/2026 10:50",
  },
  {
    id: 7,
    slotNumber: 1,
    code: "MCH-003-S01",
    machineId: 3,
    machineCode: "MCH-003",
    machineName: "Máy nước hoa tầng trệt",
    locationName: "Crescent Mall",
    productId: 1,
    productName: "Rose Elegance",
    capacityMl: 500,
    currentVolumeMl: 0,
    lowStockThresholdMl: 100,
    status: "DISABLED",
    updatedAt: "19/11/2026 09:20",
  },
];

const statusLabels: Record<SlotStatus, string> = {
  AVAILABLE: "Còn hàng",
  LOW_STOCK: "Sắp hết",
  EMPTY: "Hết hàng",
  DISABLED: "Ngừng hoạt động",
};

const statusStyles: Record<SlotStatus, string> = {
  AVAILABLE: "bg-green-50 text-green-700",
  LOW_STOCK: "bg-amber-50 text-amber-700",
  EMPTY: "bg-red-50 text-red-600",
  DISABLED: "bg-gray-100 text-gray-600",
};

function getSlotStatus(
  volume: number,
  threshold: number,
  disabled: boolean
): SlotStatus {
  if (disabled) return "DISABLED";
  if (volume <= 0) return "EMPTY";
  if (volume <= threshold) return "LOW_STOCK";
  return "AVAILABLE";
}

function SlotStatusBadge({ status }: { status: SlotStatus }) {
  const icons = {
    AVAILABLE: <CheckCircle2 size={14} />,
    LOW_STOCK: <AlertTriangle size={14} />,
    EMPTY: <Droplets size={14} />,
    DISABLED: <Ban size={14} />,
  };

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-medium ${statusStyles[status]}`}
    >
      {icons[status]}
      {statusLabels[status]}
    </span>
  );
}

function VolumeBar({ current, capacity }: { current: number; capacity: number }) {
  const percentage =
    capacity > 0 ? Math.min(100, Math.max(0, (current / capacity) * 100)) : 0;

  return (
    <div className="min-w-[130px]">
      <div className="mb-1.5 flex items-center justify-between text-xs">
        <span className="font-medium text-gray-700">
          {current} / {capacity} ml
        </span>
        <span className="text-gray-400">{Math.round(percentage)}%</span>
      </div>
      <div className="h-2 overflow-hidden rounded-full bg-gray-100">
        <div
          className={`h-full rounded-full ${
            percentage <= 20
              ? "bg-red-500"
              : percentage <= 50
                ? "bg-amber-400"
                : "bg-green-500"
          }`}
          style={{ width: `${percentage}%` }}
        />
      </div>
    </div>
  );
}

type SlotFormProps = {
  slot: Slot | null;
  onClose: () => void;
  onSave: (slot: Slot) => void;
  slots: Slot[];
};

function SlotFormModal({ slot, onClose, onSave, slots }: SlotFormProps) {
  const [machineId, setMachineId] = useState(
    String(slot?.machineId ?? machines[0].id)
  );
  const [slotNumber, setSlotNumber] = useState(
    String(slot?.slotNumber ?? 1)
  );
  const [productId, setProductId] = useState(
    slot?.productId ? String(slot.productId) : ""
  );
  const [capacity, setCapacity] = useState(
    String(slot?.capacityMl ?? 500)
  );
  const [volume, setVolume] = useState(
    String(slot?.currentVolumeMl ?? 500)
  );
  const [threshold, setThreshold] = useState(
    String(slot?.lowStockThresholdMl ?? 100)
  );
  const [disabled, setDisabled] = useState(
    slot?.status === "DISABLED"
  );
  const [error, setError] = useState("");

  const selectedMachine = machines.find(
    (item) => item.id === Number(machineId)
  );
  const selectedProduct = products.find(
    (item) => item.id === Number(productId)
  );

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();

    const capacityValue = Number(capacity);
    const volumeValue = Number(volume);
    const thresholdValue = Number(threshold);
    const numberValue = Number(slotNumber);

    if (!machineId || !slotNumber || !capacity || !volume || !threshold) {
      setError("Vui lòng nhập đầy đủ các thông tin bắt buộc.");
      return;
    }

    if (
      !Number.isInteger(numberValue) ||
      numberValue < 1 ||
      numberValue > (selectedMachine?.slotCount ?? 20)
    ) {
      setError(
        `Số khe phải từ 1 đến ${selectedMachine?.slotCount ?? 20} theo cấu hình máy.`
      );
      return;
    }

    if (
      capacityValue <= 0 ||
      volumeValue < 0 ||
      volumeValue > capacityValue ||
      thresholdValue < 0 ||
      thresholdValue > capacityValue
    ) {
      setError("Dung tích hoặc ngưỡng cảnh báo không hợp lệ.");
      return;
    }

    const duplicated = slots.some(
      (item) =>
        item.machineId === Number(machineId) &&
        item.slotNumber === numberValue &&
        item.id !== slot?.id
    );

    if (duplicated) {
      setError("Máy này đã có khe với số thứ tự đó.");
      return;
    }

    const status = getSlotStatus(
      volumeValue,
      thresholdValue,
      disabled
    );

    onSave({
      id: slot?.id ?? Date.now(),
      slotNumber: numberValue,
      code: `${selectedMachine?.code}-S${String(numberValue).padStart(2, "0")}`,
      machineId: Number(machineId),
      machineCode: selectedMachine?.code ?? "",
      machineName: selectedMachine?.name ?? "",
      locationName: selectedMachine?.location ?? "",
      productId: productId ? Number(productId) : null,
      productName: selectedProduct?.name ?? "Chưa gắn sản phẩm",
      capacityMl: capacityValue,
      currentVolumeMl: volumeValue,
      lowStockThresholdMl: thresholdValue,
      status,
      updatedAt: new Date().toLocaleString("vi-VN"),
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white shadow-xl">
        <div className="sticky top-0 flex items-center justify-between border-b border-gray-100 bg-white px-6 py-5">
          <div>
            <h2 className="text-xl font-semibold text-gray-900">
              {slot ? "Chỉnh sửa khe máy" : "Thêm khe máy"}
            </h2>
            <p className="mt-1 text-sm text-gray-500">
              Cấu hình sản phẩm và dung tích cho khe máy
            </p>
          </div>
          <button
            onClick={onClose}
            className="rounded-lg p-2 text-gray-400 hover:bg-gray-100"
          >
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 p-6">
          <div className="grid gap-5 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Máy nước hoa <span className="text-red-500">*</span>
              </label>
              <select
                value={machineId}
                onChange={(e) => {
                  setMachineId(e.target.value);
                  setSlotNumber("1");
                }}
                className="w-full rounded-xl border border-gray-200 bg-white px-4 py-3 outline-none focus:border-indigo-500"
              >
                {machines.map((machine) => (
                  <option key={machine.id} value={machine.id}>
                    {machine.code} — {machine.name} ({machine.location})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Số thứ tự khe <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                min="1"
                max={selectedMachine?.slotCount ?? 20}
                value={slotNumber}
                onChange={(e) => setSlotNumber(e.target.value)}
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
              <p className="mt-1 text-xs text-gray-400">
                Máy có {selectedMachine?.slotCount ?? 0} khe theo cấu hình
              </p>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Sản phẩm
              </label>
              <select
                value={productId}
                onChange={(e) => setProductId(e.target.value)}
                className="w-full rounded-xl border border-gray-200 bg-white px-4 py-3 outline-none focus:border-indigo-500"
              >
                <option value="">Chưa gắn sản phẩm</option>
                {products.map((product) => (
                  <option key={product.id} value={product.id}>
                    {product.code} — {product.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Dung tích tối đa (ml) <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                min="1"
                value={capacity}
                onChange={(e) => setCapacity(e.target.value)}
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Lượng nước hoa hiện tại (ml){" "}
                <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                min="0"
                value={volume}
                onChange={(e) => setVolume(e.target.value)}
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Ngưỡng cảnh báo sắp hết (ml)
              </label>
              <input
                type="number"
                min="0"
                value={threshold}
                onChange={(e) => setThreshold(e.target.value)}
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div className="sm:col-span-2">
              <label className="flex cursor-pointer items-center gap-3 rounded-xl border border-gray-200 p-4">
                <input
                  type="checkbox"
                  checked={disabled}
                  onChange={(e) => setDisabled(e.target.checked)}
                  className="h-4 w-4 accent-indigo-600"
                />
                <span>
                  <span className="block text-sm font-medium text-gray-800">
                    Ngừng hoạt động khe này
                  </span>
                  <span className="mt-1 block text-xs text-gray-400">
                    Khe sẽ được đánh dấu ngừng hoạt động, không phục vụ khách hàng.
                  </span>
                </span>
              </label>
            </div>
          </div>

          {error && (
            <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600">
              {error}
            </p>
          )}

          <div className="flex justify-end gap-3 border-t border-gray-100 pt-5">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl border border-gray-200 px-5 py-2.5 text-sm font-medium text-gray-600 hover:bg-gray-50"
            >
              Hủy
            </button>
            <button
              type="submit"
              className="rounded-xl bg-indigo-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-indigo-700"
            >
              {slot ? "Lưu thay đổi" : "Thêm khe máy"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function SlotDetailModal({
  slot,
  onClose,
}: {
  slot: Slot;
  onClose: () => void;
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-xl rounded-2xl bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-gray-100 px-6 py-5">
          <div>
            <h2 className="text-xl font-semibold text-gray-900">
              Chi tiết khe máy
            </h2>
            <p className="mt-1 text-sm text-gray-500">{slot.code}</p>
          </div>
          <button
            onClick={onClose}
            className="rounded-lg p-2 text-gray-400 hover:bg-gray-100"
          >
            <X size={20} />
          </button>
        </div>

        <div className="space-y-5 p-6">
          <div className="flex items-center gap-4 rounded-xl bg-indigo-50 p-4">
            <div className="rounded-xl bg-white p-3 text-indigo-600">
              <Layers size={28} />
            </div>
            <div>
              <h3 className="font-semibold text-gray-900">
                Khe số {slot.slotNumber}
              </h3>
              <p className="mt-1 text-sm text-gray-500">{slot.code}</p>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-gray-400">Trạng thái</p>
              <div className="mt-2">
                <SlotStatusBadge status={slot.status} />
              </div>
            </div>
            <div>
              <p className="text-sm text-gray-400">Sản phẩm</p>
              <p className="mt-2 font-medium text-gray-800">
                {slot.productName}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-400">Dung tích tối đa</p>
              <p className="mt-1 font-medium text-gray-800">
                {slot.capacityMl} ml
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-400">Lượng hiện tại</p>
              <p className="mt-1 font-medium text-gray-800">
                {slot.currentVolumeMl} ml
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-400">Ngưỡng cảnh báo</p>
              <p className="mt-1 font-medium text-gray-800">
                {slot.lowStockThresholdMl} ml
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-400">Cập nhật lần cuối</p>
              <p className="mt-1 font-medium text-gray-800">
                {slot.updatedAt}
              </p>
            </div>
          </div>

          <div className="rounded-xl border border-gray-100 p-4">
            <div className="mb-3 flex items-center gap-2 text-sm font-medium text-gray-700">
              <Droplets size={17} className="text-indigo-500" />
              Mức dung tích hiện tại
            </div>
            <VolumeBar
              current={slot.currentVolumeMl}
              capacity={slot.capacityMl}
            />
          </div>

          <div className="border-t border-gray-100 pt-4">
            <p className="text-sm text-gray-400">Máy nước hoa</p>
            <p className="mt-1 font-medium text-gray-800">
              {slot.machineCode} — {slot.machineName}
            </p>
            <p className="mt-1 text-sm text-gray-500">{slot.locationName}</p>
          </div>
        </div>

        <div className="flex justify-end border-t border-gray-100 px-6 py-4">
          <button
            onClick={onClose}
            className="rounded-xl bg-indigo-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-indigo-700"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
}

export default function SlotsPage() {
  const [slots, setSlots] = useState<Slot[]>(initialSlots);
  const [keyword, setKeyword] = useState("");
  const [machineFilter, setMachineFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [showForm, setShowForm] = useState(false);
  const [editingSlot, setEditingSlot] = useState<Slot | null>(null);
  const [viewingSlot, setViewingSlot] = useState<Slot | null>(null);

  const filteredSlots = useMemo(() => {
    return slots.filter((slot) => {
      const search = keyword.toLowerCase();

      const matchesKeyword =
        slot.code.toLowerCase().includes(search) ||
        slot.machineCode.toLowerCase().includes(search) ||
        slot.machineName.toLowerCase().includes(search) ||
        slot.productName.toLowerCase().includes(search);

      const matchesMachine =
        machineFilter === "ALL" ||
        slot.machineId === Number(machineFilter);

      const matchesStatus =
        statusFilter === "ALL" || slot.status === statusFilter;

      return matchesKeyword && matchesMachine && matchesStatus;
    });
  }, [slots, keyword, machineFilter, statusFilter]);

  const availableCount = slots.filter(
    (slot) => slot.status === "AVAILABLE"
  ).length;
  const lowStockCount = slots.filter(
    (slot) => slot.status === "LOW_STOCK"
  ).length;
  const emptyCount = slots.filter(
    (slot) => slot.status === "EMPTY"
  ).length;
  const disabledCount = slots.filter(
    (slot) => slot.status === "DISABLED"
  ).length;

  const handleSave = (slot: Slot) => {
    setSlots((current) => {
      const exists = current.some((item) => item.id === slot.id);
      return exists
        ? current.map((item) => (item.id === slot.id ? slot : item))
        : [slot, ...current];
    });

    setShowForm(false);
    setEditingSlot(null);
  };

  const handleDelete = (slot: Slot) => {
    const confirmed = window.confirm(
      `Bạn có chắc muốn xóa ${slot.code}?`
    );

    if (!confirmed) return;

    setSlots((current) =>
      current.filter((item) => item.id !== slot.id)
    );
  };

  const statCards = [
    {
      label: "Tổng số khe",
      value: slots.length,
      icon: <Layers size={21} />,
      color: "bg-indigo-50 text-indigo-600",
    },
    {
      label: "Còn hàng",
      value: availableCount,
      icon: <CheckCircle2 size={21} />,
      color: "bg-green-50 text-green-600",
    },
    {
      label: "Sắp hết",
      value: lowStockCount,
      icon: <AlertTriangle size={21} />,
      color: "bg-amber-50 text-amber-600",
    },
    {
      label: "Hết hàng / Ngừng",
      value: emptyCount + disabledCount,
      icon: <Droplets size={21} />,
      color: "bg-red-50 text-red-600",
    },
  ];

  return (
    <div className="min-h-screen space-y-6 bg-[#f0efff] p-6">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Quản lý khe máy
          </h1>
          <p className="mt-1 text-sm text-[#95877c]">
            Quản lý sản phẩm, dung tích và tình trạng từng khe máy
          </p>
        </div>

        <button
          onClick={() => {
            setEditingSlot(null);
            setShowForm(true);
          }}
          className="inline-flex items-center justify-center gap-2 rounded-xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white shadow-sm hover:bg-indigo-700"
        >
          <Plus size={18} /> Thêm khe máy
        </button>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {statCards.map((card) => (
          <div
            key={card.label}
            className="rounded-2xl border border-[#e5e4f5] bg-white p-5 shadow-sm"
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-500">{card.label}</p>
                <p className="mt-2 text-2xl font-bold text-gray-900">
                  {card.value}
                </p>
              </div>
              <div className={`rounded-xl p-3 ${card.color}`}>
                {card.icon}
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="overflow-hidden rounded-2xl border border-[#e5e4f5] bg-white shadow-sm">
        <div className="flex flex-col gap-4 border-b border-gray-100 p-5 xl:flex-row xl:items-center xl:justify-between">
          <div>
            <h2 className="font-semibold text-gray-900">
              Danh sách khe máy
            </h2>
            <p className="mt-1 text-sm text-gray-400">
              Hiển thị {filteredSlots.length} / {slots.length} khe
            </p>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
            <div className="relative">
              <Search
                size={17}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
              />
              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Tìm mã khe, máy, sản phẩm..."
                className="w-full rounded-xl border border-gray-200 py-3 pl-10 pr-3 text-sm outline-none focus:border-indigo-400"
              />
            </div>

            <select
              value={machineFilter}
              onChange={(e) => setMachineFilter(e.target.value)}
              className="rounded-xl border border-gray-200 bg-white px-3 py-3 text-sm outline-none focus:border-indigo-400"
            >
              <option value="ALL">Tất cả máy</option>
              {machines.map((machine) => (
                <option key={machine.id} value={machine.id}>
                  {machine.code} — {machine.name}
                </option>
              ))}
            </select>

            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="rounded-xl border border-gray-200 bg-white px-3 py-3 text-sm outline-none focus:border-indigo-400"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="AVAILABLE">Còn hàng</option>
              <option value="LOW_STOCK">Sắp hết</option>
              <option value="EMPTY">Hết hàng</option>
              <option value="DISABLED">Ngừng hoạt động</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[1100px] text-left">
            <thead className="bg-[#f5f4ff] text-sm text-indigo-900">
              <tr>
                <th className="px-5 py-4 font-semibold">Khe máy</th>
                <th className="px-5 py-4 font-semibold">Máy nước hoa</th>
                <th className="px-5 py-4 font-semibold">Sản phẩm</th>
                <th className="px-5 py-4 font-semibold">Dung tích</th>
                <th className="px-5 py-4 font-semibold">Mức còn lại</th>
                <th className="px-5 py-4 font-semibold">Trạng thái</th>
                <th className="px-5 py-4 text-center font-semibold">
                  Thao tác
                </th>
              </tr>
            </thead>

            <tbody className="divide-y divide-gray-100">
              {filteredSlots.map((slot) => (
                <tr
                  key={slot.id}
                  className="transition hover:bg-gray-50/70"
                >
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <div className="rounded-xl bg-indigo-50 p-3 text-indigo-600">
                        <Layers size={20} />
                      </div>
                      <div>
                        <p className="font-semibold text-gray-800">
                          Khe số {slot.slotNumber}
                        </p>
                        <p className="mt-1 text-xs text-indigo-600">
                          {slot.code}
                        </p>
                      </div>
                    </div>
                  </td>

                  <td className="px-5 py-4">
                    <p className="text-sm font-medium text-gray-800">
                      {slot.machineCode}
                    </p>
                    <p className="mt-1 text-xs text-gray-400">
                      {slot.machineName}
                    </p>
                  </td>

                  <td className="px-5 py-4">
                    <div className="flex items-center gap-2">
                      <Package size={17} className="text-indigo-500" />
                      <span className="text-sm font-medium text-gray-700">
                        {slot.productName}
                      </span>
                    </div>
                  </td>

                  <td className="px-5 py-4 text-sm text-gray-700">
                    {slot.capacityMl} ml
                  </td>

                  <td className="px-5 py-4">
                    <VolumeBar
                      current={slot.currentVolumeMl}
                      capacity={slot.capacityMl}
                    />
                  </td>

                  <td className="px-5 py-4">
                    <SlotStatusBadge status={slot.status} />
                  </td>

                  <td className="px-5 py-4">
                    <div className="flex items-center justify-center gap-2">
                      <button
                        title="Xem chi tiết"
                        onClick={() => setViewingSlot(slot)}
                        className="rounded-lg border border-indigo-100 p-2 text-indigo-600 hover:bg-indigo-50"
                      >
                        <Eye size={17} />
                      </button>

                      <button
                        title="Chỉnh sửa"
                        onClick={() => {
                          setEditingSlot(slot);
                          setShowForm(true);
                        }}
                        className="rounded-lg border border-indigo-100 p-2 text-indigo-600 hover:bg-indigo-50"
                      >
                        <Pencil size={17} />
                      </button>

                      <button
                        title="Xóa khe"
                        onClick={() => handleDelete(slot)}
                        className="rounded-lg border border-red-100 p-2 text-red-500 hover:bg-red-50"
                      >
                        <Trash2 size={17} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}

              {filteredSlots.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-5 py-14 text-center">
                    <Layers size={34} className="mx-auto text-gray-300" />
                    <p className="mt-3 font-medium text-gray-600">
                      Không tìm thấy khe máy
                    </p>
                    <p className="mt-1 text-sm text-gray-400">
                      Thử thay đổi từ khóa hoặc bộ lọc.
                    </p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="flex items-start gap-3 rounded-xl border border-amber-100 bg-amber-50 p-4 text-sm text-amber-800">
        <AlertTriangle size={18} className="mt-0.5 shrink-0" />
        <p>
          Mức dung tích và trạng thái khe hiện là dữ liệu mô phỏng. Khi tích hợp
          IoT Backend, lượng nước hoa còn lại sẽ được cập nhật theo dữ liệu thực
          tế từ thiết bị.
        </p>
      </div>

      {showForm && (
        <SlotFormModal
          slot={editingSlot}
          slots={slots}
          onClose={() => {
            setShowForm(false);
            setEditingSlot(null);
          }}
          onSave={handleSave}
        />
      )}

      {viewingSlot && (
        <SlotDetailModal
          slot={viewingSlot}
          onClose={() => setViewingSlot(null)}
        />
      )}
    </div>
  );
}
