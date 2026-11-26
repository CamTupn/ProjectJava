import { useMemo, useState } from "react";
import {
  Search,
  Plus,
  Eye,
  Pencil,
  Trash2,
  X,
  Cpu,
  MapPin,
  Wifi,
  WifiOff,
  Activity,
  Wrench,
  RefreshCw,
} from "lucide-react";

type MachineStatus = "ACTIVE" | "MAINTENANCE" | "DISABLED" | "ERROR";
type ConnectionStatus = "ONLINE" | "OFFLINE";

type Machine = {
  id: number;
  code: string;
  name: string;
  locationId: number;
  locationName: string;
  address: string;
  status: MachineStatus;
  connection: ConnectionStatus;
  slotCount: number;
  lastOnline: string;
  installedAt: string;
  description: string;
};

const initialLocations = [
  { id: 1, name: "Vincom Center Đồng Khởi", address: "72 Lê Thánh Tôn, Quận 1" },
  { id: 2, name: "Crescent Mall", address: "101 Tôn Dật Tiên, Quận 7" },
  { id: 3, name: "Landmark 81", address: "720A Điện Biên Phủ, Bình Thạnh" },
  { id: 4, name: "Gigamall", address: "240 - 242 Phạm Văn Đồng, Thủ Đức" },
];

const initialMachines: Machine[] = [
  {
    id: 1,
    code: "MCH-001",
    name: "Máy nước hoa tầng 1",
    locationId: 1,
    locationName: "Vincom Center Đồng Khởi",
    address: "72 Lê Thánh Tôn, Quận 1",
    status: "ACTIVE",
    connection: "ONLINE",
    slotCount: 6,
    lastOnline: "20/11/2026 15:45",
    installedAt: "10/08/2026",
    description: "Máy đặt gần khu vực sảnh chính.",
  },
  {
    id: 2,
    code: "MCH-002",
    name: "Máy nước hoa khu mỹ phẩm",
    locationId: 1,
    locationName: "Vincom Center Đồng Khởi",
    address: "72 Lê Thánh Tôn, Quận 1",
    status: "ACTIVE",
    connection: "ONLINE",
    slotCount: 6,
    lastOnline: "20/11/2026 15:42",
    installedAt: "15/08/2026",
    description: "Máy phục vụ khách hàng tại khu mỹ phẩm.",
  },
  {
    id: 3,
    code: "MCH-003",
    name: "Máy nước hoa tầng trệt",
    locationId: 2,
    locationName: "Crescent Mall",
    address: "101 Tôn Dật Tiên, Quận 7",
    status: "MAINTENANCE",
    connection: "OFFLINE",
    slotCount: 8,
    lastOnline: "19/11/2026 09:20",
    installedAt: "02/09/2026",
    description: "Đang kiểm tra hệ thống vòi xịt.",
  },
  {
    id: 4,
    code: "MCH-004",
    name: "Máy nước hoa sảnh B",
    locationId: 3,
    locationName: "Landmark 81",
    address: "720A Điện Biên Phủ, Bình Thạnh",
    status: "ACTIVE",
    connection: "ONLINE",
    slotCount: 6,
    lastOnline: "20/11/2026 15:40",
    installedAt: "20/09/2026",
    description: "Máy đặt tại sảnh B.",
  },
  {
    id: 5,
    code: "MCH-005",
    name: "Máy nước hoa khu giải trí",
    locationId: 4,
    locationName: "Gigamall",
    address: "240 - 242 Phạm Văn Đồng, Thủ Đức",
    status: "ERROR",
    connection: "OFFLINE",
    slotCount: 8,
    lastOnline: "18/11/2026 17:10",
    installedAt: "05/10/2026",
    description: "Phát hiện lỗi kết nối thiết bị.",
  },
];

const statusLabels: Record<MachineStatus, string> = {
  ACTIVE: "Đang hoạt động",
  MAINTENANCE: "Đang bảo trì",
  DISABLED: "Ngừng hoạt động",
  ERROR: "Đang gặp lỗi",
};

const statusStyles: Record<MachineStatus, string> = {
  ACTIVE: "bg-green-50 text-green-700",
  MAINTENANCE: "bg-amber-50 text-amber-700",
  DISABLED: "bg-gray-100 text-gray-600",
  ERROR: "bg-red-50 text-red-600",
};

function MachineStatusBadge({ status }: { status: MachineStatus }) {
  return (
    <span className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ${statusStyles[status]}`}>
      <span className="mr-1.5 h-1.5 w-1.5 rounded-full bg-current" />
      {statusLabels[status]}
    </span>
  );
}

function ConnectionBadge({ status }: { status: ConnectionStatus }) {
  return status === "ONLINE" ? (
    <span className="inline-flex items-center gap-1.5 text-sm font-medium text-green-600">
      <Wifi size={15} /> Online
    </span>
  ) : (
    <span className="inline-flex items-center gap-1.5 text-sm font-medium text-gray-500">
      <WifiOff size={15} /> Offline
    </span>
  );
}

type MachineFormProps = {
  machine: Machine | null;
  onClose: () => void;
  onSave: (machine: Machine) => void;
};

function MachineFormModal({ machine, onClose, onSave }: MachineFormProps) {
  const [name, setName] = useState(machine?.name ?? "");
  const [code, setCode] = useState(machine?.code ?? "");
  const [locationId, setLocationId] = useState(
    String(machine?.locationId ?? initialLocations[0].id)
  );
  const [status, setStatus] = useState<MachineStatus>(
    machine?.status ?? "ACTIVE"
  );
  const [connection, setConnection] = useState<ConnectionStatus>(
    machine?.connection ?? "ONLINE"
  );
  const [slotCount, setSlotCount] = useState(
    String(machine?.slotCount ?? 6)
  );
  const [installedAt, setInstalledAt] = useState(
    machine?.installedAt ?? ""
  );
  const [description, setDescription] = useState(
    machine?.description ?? ""
  );
  const [error, setError] = useState("");

  const selectedLocation = initialLocations.find(
    (location) => location.id === Number(locationId)
  );

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();

    if (!name.trim() || !code.trim() || !locationId || !installedAt) {
      setError("Vui lòng nhập đầy đủ các thông tin bắt buộc.");
      return;
    }

    if (Number(slotCount) < 1 || Number(slotCount) > 20) {
      setError("Số khe máy phải từ 1 đến 20.");
      return;
    }

    onSave({
      id: machine?.id ?? Date.now(),
      code: code.trim().toUpperCase(),
      name: name.trim(),
      locationId: Number(locationId),
      locationName: selectedLocation?.name ?? "",
      address: selectedLocation?.address ?? "",
      status,
      connection,
      slotCount: Number(slotCount),
      lastOnline: machine?.lastOnline ?? "Chưa có dữ liệu",
      installedAt,
      description: description.trim(),
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white shadow-xl">
        <div className="sticky top-0 flex items-center justify-between border-b border-gray-100 bg-white px-6 py-5">
          <div>
            <h2 className="text-xl font-semibold text-gray-900">
              {machine ? "Chỉnh sửa máy nước hoa" : "Thêm máy nước hoa"}
            </h2>
            <p className="mt-1 text-sm text-gray-500">
              Nhập thông tin thiết bị thuộc thương hiệu của bạn
            </p>
          </div>
          <button onClick={onClose} className="rounded-lg p-2 text-gray-400 hover:bg-gray-100">
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 p-6">
          <div className="grid gap-5 sm:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Mã máy <span className="text-red-500">*</span>
              </label>
              <input
                value={code}
                onChange={(e) => setCode(e.target.value)}
                placeholder="Ví dụ: MCH-006"
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Tên máy <span className="text-red-500">*</span>
              </label>
              <input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Nhập tên máy"
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div className="sm:col-span-2">
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Địa điểm lắp đặt <span className="text-red-500">*</span>
              </label>
              <select
                value={locationId}
                onChange={(e) => setLocationId(e.target.value)}
                className="w-full rounded-xl border border-gray-200 bg-white px-4 py-3 outline-none focus:border-indigo-500"
              >
                {initialLocations.map((location) => (
                  <option key={location.id} value={location.id}>
                    {location.name} — {location.address}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Số khe máy
              </label>
              <input
                type="number"
                min="1"
                max="20"
                value={slotCount}
                onChange={(e) => setSlotCount(e.target.value)}
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
              <p className="mt-1 text-xs text-gray-400">Từ 1 đến 20 khe</p>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Ngày lắp đặt <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                value={installedAt.includes("/") ? "" : installedAt}
                onChange={(e) => setInstalledAt(e.target.value)}
                className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Trạng thái hoạt động
              </label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value as MachineStatus)}
                className="w-full rounded-xl border border-gray-200 bg-white px-4 py-3 outline-none focus:border-indigo-500"
              >
                <option value="ACTIVE">Đang hoạt động</option>
                <option value="MAINTENANCE">Đang bảo trì</option>
                <option value="DISABLED">Ngừng hoạt động</option>
                <option value="ERROR">Đang gặp lỗi</option>
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Trạng thái kết nối
              </label>
              <select
                value={connection}
                onChange={(e) => setConnection(e.target.value as ConnectionStatus)}
                className="w-full rounded-xl border border-gray-200 bg-white px-4 py-3 outline-none focus:border-indigo-500"
              >
                <option value="ONLINE">Online</option>
                <option value="OFFLINE">Offline</option>
              </select>
            </div>

            <div className="sm:col-span-2">
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Mô tả
              </label>
              <textarea
                rows={3}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Thông tin bổ sung về máy..."
                className="w-full resize-none rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-indigo-500"
              />
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
              {machine ? "Lưu thay đổi" : "Thêm máy"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function MachineDetailModal({
  machine,
  onClose,
}: {
  machine: Machine;
  onClose: () => void;
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-xl rounded-2xl bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-gray-100 px-6 py-5">
          <div>
            <h2 className="text-xl font-semibold text-gray-900">Chi tiết máy</h2>
            <p className="mt-1 text-sm text-gray-500">{machine.code}</p>
          </div>
          <button onClick={onClose} className="rounded-lg p-2 text-gray-400 hover:bg-gray-100">
            <X size={20} />
          </button>
        </div>

        <div className="space-y-5 p-6">
          <div className="flex items-center gap-4 rounded-xl bg-indigo-50 p-4">
            <div className="rounded-xl bg-white p-3 text-indigo-600">
              <Cpu size={28} />
            </div>
            <div>
              <h3 className="font-semibold text-gray-900">{machine.name}</h3>
              <p className="mt-1 text-sm text-gray-500">{machine.code}</p>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-gray-400">Trạng thái hoạt động</p>
              <div className="mt-2"><MachineStatusBadge status={machine.status} /></div>
            </div>
            <div>
              <p className="text-sm text-gray-400">Kết nối thiết bị</p>
              <div className="mt-2"><ConnectionBadge status={machine.connection} /></div>
            </div>
            <div>
              <p className="text-sm text-gray-400">Số khe máy</p>
              <p className="mt-1 font-medium text-gray-800">{machine.slotCount} khe</p>
            </div>
            <div>
              <p className="text-sm text-gray-400">Ngày lắp đặt</p>
              <p className="mt-1 font-medium text-gray-800">{machine.installedAt}</p>
            </div>
          </div>

          <div className="border-t border-gray-100 pt-4">
            <p className="mb-2 text-sm text-gray-400">Địa điểm lắp đặt</p>
            <p className="flex items-start gap-2 font-medium text-gray-800">
              <MapPin size={17} className="mt-0.5 shrink-0 text-indigo-500" />
              {machine.locationName}
            </p>
            <p className="ml-6 mt-1 text-sm text-gray-500">{machine.address}</p>
          </div>

          <div>
            <p className="text-sm text-gray-400">Lần cuối kết nối</p>
            <p className="mt-1 font-medium text-gray-800">{machine.lastOnline}</p>
          </div>

          <div>
            <p className="text-sm text-gray-400">Mô tả</p>
            <p className="mt-1 text-sm text-gray-700">
              {machine.description || "Chưa có mô tả."}
            </p>
          </div>
        </div>

        <div className="flex justify-end border-t border-gray-100 px-6 py-4">
          <button onClick={onClose} className="rounded-xl bg-indigo-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-indigo-700">
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
}

export default function MachinesPage() {
  const [machines, setMachines] = useState<Machine[]>(initialMachines);
  const [keyword, setKeyword] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [connectionFilter, setConnectionFilter] = useState("ALL");
  const [locationFilter, setLocationFilter] = useState("ALL");
  const [showForm, setShowForm] = useState(false);
  const [editingMachine, setEditingMachine] = useState<Machine | null>(null);
  const [viewingMachine, setViewingMachine] = useState<Machine | null>(null);

  const filteredMachines = useMemo(() => {
    return machines.filter((machine) => {
      const search = keyword.toLowerCase();
      const matchesKeyword =
        machine.name.toLowerCase().includes(search) ||
        machine.code.toLowerCase().includes(search) ||
        machine.locationName.toLowerCase().includes(search);

      const matchesStatus =
        statusFilter === "ALL" || machine.status === statusFilter;
      const matchesConnection =
        connectionFilter === "ALL" || machine.connection === connectionFilter;
      const matchesLocation =
        locationFilter === "ALL" ||
        machine.locationId === Number(locationFilter);

      return matchesKeyword && matchesStatus && matchesConnection && matchesLocation;
    });
  }, [machines, keyword, statusFilter, connectionFilter, locationFilter]);

  const activeCount = machines.filter((m) => m.status === "ACTIVE").length;
  const onlineCount = machines.filter((m) => m.connection === "ONLINE").length;
  const maintenanceCount = machines.filter((m) => m.status === "MAINTENANCE").length;
  const errorCount = machines.filter((m) => m.status === "ERROR").length;

  const handleSave = (machine: Machine) => {
    const duplicateCode = machines.some(
      (item) =>
        item.code.toLowerCase() === machine.code.toLowerCase() &&
        item.id !== machine.id
    );

    if (duplicateCode) {
      alert("Mã máy đã tồn tại. Vui lòng sử dụng mã khác.");
      return;
    }

    setMachines((current) => {
      const exists = current.some((item) => item.id === machine.id);
      return exists
        ? current.map((item) => item.id === machine.id ? machine : item)
        : [machine, ...current];
    });

    setShowForm(false);
    setEditingMachine(null);
  };

  const handleDelete = (machine: Machine) => {
    const confirmed = window.confirm(
      `Bạn có chắc muốn xóa máy "${machine.name}"?`
    );
    if (!confirmed) return;

    setMachines((current) => current.filter((item) => item.id !== machine.id));
  };

  const statCards = [
    {
      label: "Tổng số máy",
      value: machines.length,
      icon: <Cpu size={21} />,
      color: "bg-indigo-50 text-indigo-600",
    },
    {
      label: "Đang hoạt động",
      value: activeCount,
      icon: <Activity size={21} />,
      color: "bg-green-50 text-green-600",
    },
    {
      label: "Đang Online",
      value: onlineCount,
      icon: <Wifi size={21} />,
      color: "bg-blue-50 text-blue-600",
    },
    {
      label: "Bảo trì / Lỗi",
      value: maintenanceCount + errorCount,
      icon: <Wrench size={21} />,
      color: "bg-amber-50 text-amber-600",
    },
  ];

  return (
    <div className="min-h-screen space-y-6 bg-[#f0efff] p-6">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">Quản lý máy nước hoa</h1>
          <p className="mt-1 text-sm text-[#95877c]">
            Theo dõi và quản lý các thiết bị thuộc thương hiệu của bạn
          </p>
        </div>
        <button
          onClick={() => {
            setEditingMachine(null);
            setShowForm(true);
          }}
          className="inline-flex items-center justify-center gap-2 rounded-xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white shadow-sm hover:bg-indigo-700"
        >
          <Plus size={18} /> Thêm máy mới
        </button>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {statCards.map((card) => (
          <div key={card.label} className="rounded-2xl border border-[#e5e4f5] bg-white p-5 shadow-sm">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-500">{card.label}</p>
                <p className="mt-2 text-2xl font-bold text-gray-900">{card.value}</p>
              </div>
              <div className={`rounded-xl p-3 ${card.color}`}>{card.icon}</div>
            </div>
          </div>
        ))}
      </div>

      <div className="overflow-hidden rounded-2xl border border-[#e5e4f5] bg-white shadow-sm">
        <div className="flex flex-col gap-4 border-b border-gray-100 p-5 xl:flex-row xl:items-center xl:justify-between">
          <div>
            <h2 className="font-semibold text-gray-900">Danh sách máy nước hoa</h2>
            <p className="mt-1 text-sm text-gray-400">
              Hiển thị {filteredMachines.length} / {machines.length} máy
            </p>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <div className="relative">
              <Search size={17} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Tìm tên, mã máy..."
                className="w-full rounded-xl border border-gray-200 py-3 pl-10 pr-3 text-sm outline-none focus:border-indigo-400"
              />
            </div>

            <select
              value={locationFilter}
              onChange={(e) => setLocationFilter(e.target.value)}
              className="rounded-xl border border-gray-200 bg-white px-3 py-3 text-sm outline-none focus:border-indigo-400"
            >
              <option value="ALL">Tất cả địa điểm</option>
              {initialLocations.map((location) => (
                <option key={location.id} value={location.id}>{location.name}</option>
              ))}
            </select>

            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="rounded-xl border border-gray-200 bg-white px-3 py-3 text-sm outline-none focus:border-indigo-400"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="ACTIVE">Đang hoạt động</option>
              <option value="MAINTENANCE">Đang bảo trì</option>
              <option value="DISABLED">Ngừng hoạt động</option>
              <option value="ERROR">Đang gặp lỗi</option>
            </select>

            <select
              value={connectionFilter}
              onChange={(e) => setConnectionFilter(e.target.value)}
              className="rounded-xl border border-gray-200 bg-white px-3 py-3 text-sm outline-none focus:border-indigo-400"
            >
              <option value="ALL">Tất cả kết nối</option>
              <option value="ONLINE">Online</option>
              <option value="OFFLINE">Offline</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[1050px] text-left">
            <thead className="bg-[#f5f4ff] text-sm text-indigo-900">
              <tr>
                <th className="px-5 py-4 font-semibold">Máy nước hoa</th>
                <th className="px-5 py-4 font-semibold">Mã máy</th>
                <th className="px-5 py-4 font-semibold">Địa điểm</th>
                <th className="px-5 py-4 font-semibold">Số khe</th>
                <th className="px-5 py-4 font-semibold">Trạng thái</th>
                <th className="px-5 py-4 font-semibold">Kết nối</th>
                <th className="px-5 py-4 text-center font-semibold">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filteredMachines.map((machine) => (
                <tr key={machine.id} className="transition hover:bg-gray-50/70">
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <div className="rounded-xl bg-indigo-50 p-3 text-indigo-600">
                        <Cpu size={21} />
                      </div>
                      <div>
                        <p className="font-semibold text-gray-800">{machine.name}</p>
                        <p className="mt-1 text-xs text-gray-400">Lắp đặt: {machine.installedAt}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-4 text-sm font-medium text-indigo-600">{machine.code}</td>
                  <td className="px-5 py-4">
                    <p className="text-sm font-medium text-gray-700">{machine.locationName}</p>
                    <p className="mt-1 text-xs text-gray-400">{machine.address}</p>
                  </td>
                  <td className="px-5 py-4 text-sm text-gray-700">{machine.slotCount} khe</td>
                  <td className="px-5 py-4"><MachineStatusBadge status={machine.status} /></td>
                  <td className="px-5 py-4"><ConnectionBadge status={machine.connection} /></td>
                  <td className="px-5 py-4">
                    <div className="flex items-center justify-center gap-2">
                      <button
                        title="Xem chi tiết"
                        onClick={() => setViewingMachine(machine)}
                        className="rounded-lg border border-indigo-100 p-2 text-indigo-600 hover:bg-indigo-50"
                      >
                        <Eye size={17} />
                      </button>
                      <button
                        title="Chỉnh sửa"
                        onClick={() => {
                          setEditingMachine(machine);
                          setShowForm(true);
                        }}
                        className="rounded-lg border border-indigo-100 p-2 text-indigo-600 hover:bg-indigo-50"
                      >
                        <Pencil size={17} />
                      </button>
                      <button
                        title="Xóa máy"
                        onClick={() => handleDelete(machine)}
                        className="rounded-lg border border-red-100 p-2 text-red-500 hover:bg-red-50"
                      >
                        <Trash2 size={17} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}

              {filteredMachines.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-5 py-14 text-center">
                    <Cpu size={34} className="mx-auto text-gray-300" />
                    <p className="mt-3 font-medium text-gray-600">Không tìm thấy máy nước hoa</p>
                    <p className="mt-1 text-sm text-gray-400">Thử thay đổi từ khóa hoặc bộ lọc.</p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="flex items-start gap-3 rounded-xl border border-blue-100 bg-blue-50 p-4 text-sm text-blue-800">
        <RefreshCw size={18} className="mt-0.5 shrink-0" />
        <p>
          Trạng thái Online/Offline hiện là dữ liệu mô phỏng. Khi tích hợp IoT Backend,
          trạng thái kết nối và thời gian hoạt động gần nhất sẽ được cập nhật từ thiết bị thực tế.
        </p>
      </div>

      {showForm && (
        <MachineFormModal
          machine={editingMachine}
          onClose={() => {
            setShowForm(false);
            setEditingMachine(null);
          }}
          onSave={handleSave}
        />
      )}

      {viewingMachine && (
        <MachineDetailModal
          machine={viewingMachine}
          onClose={() => setViewingMachine(null)}
        />
      )}
    </div>
  );
}
