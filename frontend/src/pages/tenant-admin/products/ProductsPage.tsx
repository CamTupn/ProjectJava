import { useMemo, useState } from 'react'
import {
  Search,
  Plus,
  Eye,
  Pencil,
  Trash2,
  Package,
  TrendingUp,
  CircleCheck,
  CircleX,
  X,
  Save,
  Droplets,
  Filter,
  Upload,
} from 'lucide-react'

type ProductStatus = 'ACTIVE' | 'INACTIVE'

interface Product {
  id: number
  sku: string
  name: string
  brand: string
  category: string
  concentration: string
  volumeMl: number
  price: number
  stock: number
  description: string
  image: string
  status: ProductStatus
  createdAt: string
}

const initialProducts: Product[] = [
  {
    id: 1,
    sku: 'PRD-001',
    name: 'Rose Elegance',
    brand: 'ScentStation Demo',
    category: 'Floral',
    concentration: 'EDP',
    volumeMl: 50,
    price: 15000,
    stock: 245,
    description: 'Hương hoa hồng thanh lịch, nhẹ nhàng và quyến rũ.',
    image: '',
    status: 'ACTIVE',
    createdAt: '01/11/2026',
  },
  {
    id: 2,
    sku: 'PRD-002',
    name: 'Royal Oud',
    brand: 'ScentStation Demo',
    category: 'Woody',
    concentration: 'EDP',
    volumeMl: 50,
    price: 20000,
    stock: 198,
    description: 'Hương gỗ trầm ấm, sang trọng và mạnh mẽ.',
    image: '',
    status: 'ACTIVE',
    createdAt: '03/11/2026',
  },
  {
    id: 3,
    sku: 'PRD-003',
    name: 'Ocean Breeze',
    brand: 'ScentStation Demo',
    category: 'Fresh',
    concentration: 'EDT',
    volumeMl: 30,
    price: 18000,
    stock: 156,
    description: 'Hương biển tươi mát, mang lại cảm giác dễ chịu.',
    image: '',
    status: 'ACTIVE',
    createdAt: '05/11/2026',
  },
  {
    id: 4,
    sku: 'PRD-004',
    name: 'Vanilla Bloom',
    brand: 'ScentStation Demo',
    category: 'Gourmand',
    concentration: 'EDP',
    volumeMl: 50,
    price: 30000,
    stock: 132,
    description: 'Hương vani ngọt ngào và ấm áp.',
    image: '',
    status: 'ACTIVE',
    createdAt: '08/11/2026',
  },
  {
    id: 5,
    sku: 'PRD-005',
    name: 'White Musk',
    brand: 'ScentStation Demo',
    category: 'Musk',
    concentration: 'EDT',
    volumeMl: 30,
    price: 12000,
    stock: 98,
    description: 'Hương xạ hương tinh tế, phù hợp sử dụng hằng ngày.',
    image: '',
    status: 'INACTIVE',
    createdAt: '10/11/2026',
  },
]

const emptyProduct: Omit<Product, 'id' | 'createdAt'> = {
  sku: '',
  name: '',
  brand: 'ScentStation Demo',
  category: 'Floral',
  concentration: 'EDP',
  volumeMl: 50,
  price: 15000,
  stock: 0,
  description: '',
  image: '',
  status: 'ACTIVE',
}

const formatCurrency = (value: number) =>
  `${value.toLocaleString('vi-VN')} đ`

function StatusBadge({ status }: { status: ProductStatus }) {
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full px-3 py-1 text-xs font-medium ${
        status === 'ACTIVE'
          ? 'bg-green-50 text-green-700'
          : 'bg-gray-100 text-gray-600'
      }`}
    >
      {status === 'ACTIVE' ? (
        <CircleCheck size={13} />
      ) : (
        <CircleX size={13} />
      )}
      {status === 'ACTIVE' ? 'Đang kinh doanh' : 'Ngừng kinh doanh'}
    </span>
  )
}

function ProductModal({
  product,
  onClose,
  onSave,
}: {
  product: Product | null
  onClose: () => void
  onSave: (data: Omit<Product, 'id' | 'createdAt'>) => void
}) {
  const [form, setForm] = useState(
    product
      ? {
          sku: product.sku,
          name: product.name,
          brand: product.brand,
          category: product.category,
          concentration: product.concentration,
          volumeMl: product.volumeMl,
          price: product.price,
          stock: product.stock,
          description: product.description,
          image: product.image,
          status: product.status,
        }
      : emptyProduct,
  )

  const update = (key: keyof typeof form, value: string | number) => {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    const allowedTypes = [
      'image/png',
      'image/jpeg',
      'image/webp',
    ]

    if (!allowedTypes.includes(file.type)) {
      alert('Vui lòng chọn ảnh PNG, JPG hoặc WEBP.')
      e.target.value = ''
      return
    }

    if (file.size > 2 * 1024 * 1024) {
      alert('Ảnh không được vượt quá 2MB.')
      e.target.value = ''
      return
    }

    const reader = new FileReader()

    reader.onload = () => {
      update('image', reader.result as string)
    }

    reader.onerror = () => {
      alert('Không thể đọc file ảnh. Vui lòng thử lại.')
    }

    reader.readAsDataURL(file)
    e.target.value = ''
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    if (!form.sku.trim() || !form.name.trim()) {
      alert('Vui lòng nhập mã SKU và tên sản phẩm.')
      return
    }

    if (form.volumeMl <= 0 || form.price < 0 || form.stock < 0) {
      alert('Dung tích phải lớn hơn 0. Giá và tồn kho không được âm.')
      return
    }

    onSave(form)
  }

  const inputClass =
    'mt-2 w-full rounded-xl border border-[#e5e4f5] bg-white px-4 py-3 text-sm text-[#302720] outline-none transition focus:border-[#5865f2] focus:ring-2 focus:ring-[#5865f2]/10'

  const labelClass = 'text-sm font-medium text-[#302720]'

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-[#eeeef5] px-6 py-5">
          <div>
            <h2 className="text-xl font-bold text-[#302720]">
              {product ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới'}
            </h2>
            <p className="mt-1 text-sm text-[#95877c]">
              Nhập thông tin sản phẩm nước hoa
            </p>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-2 hover:bg-gray-100"
          >
            <X size={21} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 p-6">
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
            <label className={labelClass}>
              Mã SKU *
              <input
                className={inputClass}
                value={form.sku}
                onChange={(e) => update('sku', e.target.value)}
                placeholder="PRD-006"
              />
            </label>

            <label className={labelClass}>
              Tên sản phẩm *
              <input
                className={inputClass}
                value={form.name}
                onChange={(e) => update('name', e.target.value)}
                placeholder="Tên nước hoa"
              />
            </label>

            <label className={labelClass}>
              Thương hiệu
              <input
                className={inputClass}
                value={form.brand}
                onChange={(e) => update('brand', e.target.value)}
                disabled
              />
            </label>

            <label className={labelClass}>
              Nhóm hương
              <select
                className={inputClass}
                value={form.category}
                onChange={(e) => update('category', e.target.value)}
              >
                <option value="Floral">Floral - Hương hoa</option>
                <option value="Woody">Woody - Hương gỗ</option>
                <option value="Fresh">Fresh - Hương tươi mát</option>
                <option value="Gourmand">Gourmand - Hương ngọt</option>
                <option value="Musk">Musk - Hương xạ hương</option>
                <option value="Oriental">Oriental - Hương phương Đông</option>
              </select>
            </label>

            <label className={labelClass}>
              Nồng độ
              <select
                className={inputClass}
                value={form.concentration}
                onChange={(e) => update('concentration', e.target.value)}
              >
                <option value="EDP">EDP</option>
                <option value="EDT">EDT</option>
                <option value="EDC">EDC</option>
                <option value="Parfum">Parfum</option>
              </select>
            </label>

            <label className={labelClass}>
              Dung tích chai (ml)
              <input
                type="number"
                min="1"
                className={inputClass}
                value={form.volumeMl}
                onChange={(e) => update('volumeMl', Number(e.target.value))}
              />
            </label>

            <label className={labelClass}>
              Giá bán mỗi ml (đ)
              <input
                type="number"
                min="0"
                className={inputClass}
                value={form.price}
                onChange={(e) => update('price', Number(e.target.value))}
              />
            </label>

            <label className={labelClass}>
              Số lượng tồn kho (chai)
              <input
                type="number"
                min="0"
                className={inputClass}
                value={form.stock}
                onChange={(e) => update('stock', Number(e.target.value))}
              />
            </label>
          </div>

          <label className={labelClass + ' block'}>
            Mô tả sản phẩm
            <textarea
              rows={3}
              className={inputClass}
              value={form.description}
              onChange={(e) => update('description', e.target.value)}
              placeholder="Mô tả đặc điểm và mùi hương..."
            />
          </label>

          {/* UPLOAD ẢNH SẢN PHẨM */}
          <div>
            <label className={labelClass + ' block'}>
              Hình ảnh sản phẩm
            </label>

            <div className="mt-2 rounded-xl border border-dashed border-[#c9c8e8] bg-[#f8f7ff] p-5">
              {form.image ? (
                <div className="flex flex-col items-center gap-3">
                  <img
                    src={form.image}
                    alt="Ảnh xem trước sản phẩm"
                    className="h-40 w-40 rounded-xl border border-[#e5e4f5] bg-white object-cover"
                  />

                  <div className="flex flex-wrap justify-center gap-2">
                    <label className="cursor-pointer rounded-lg border border-[#e5e4f5] bg-white px-4 py-2 text-sm font-medium text-[#5865f2] hover:bg-[#eef0ff]">
                      <span className="flex items-center gap-2">
                        <Upload size={15} />
                        Thay ảnh
                      </span>

                      <input
                        type="file"
                        accept="image/png,image/jpeg,image/webp"
                        className="hidden"
                        onChange={handleImageChange}
                      />
                    </label>

                    <button
                      type="button"
                      onClick={() => update('image', '')}
                      className="rounded-lg border border-red-100 bg-white px-4 py-2 text-sm font-medium text-red-500 hover:bg-red-50"
                    >
                      Xóa ảnh
                    </button>
                  </div>
                </div>
              ) : (
                <label className="flex cursor-pointer flex-col items-center gap-3 py-5">
                  <div className="flex h-14 w-14 items-center justify-center rounded-full bg-[#eef0ff] text-[#5865f2]">
                    <Upload size={26} />
                  </div>

                  <div className="text-center">
                    <p className="font-semibold text-[#302720]">
                      Nhấn để chọn ảnh sản phẩm
                    </p>
                    <p className="mt-1 text-xs text-[#95877c]">
                      PNG, JPG, JPEG hoặc WEBP · Tối đa 2MB
                    </p>
                  </div>

                  <input
                    type="file"
                    accept="image/png,image/jpeg,image/webp"
                    className="hidden"
                    onChange={handleImageChange}
                  />
                </label>
              )}
            </div>
          </div>

          <label className={labelClass + ' block'}>
            Trạng thái
            <select
              className={inputClass}
              value={form.status}
              onChange={(e) => update('status', e.target.value)}
            >
              <option value="ACTIVE">Đang kinh doanh</option>
              <option value="INACTIVE">Ngừng kinh doanh</option>
            </select>
          </label>

          <div className="flex justify-end gap-3 border-t border-[#eeeef5] pt-5">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl border border-[#e5e4f5] px-5 py-3 text-sm font-medium hover:bg-gray-50"
            >
              Hủy
            </button>

            <button
              type="submit"
              className="flex items-center gap-2 rounded-xl bg-[#4f46e5] px-5 py-3 text-sm font-semibold text-white hover:bg-[#4338ca]"
            >
              <Save size={17} />
              Lưu sản phẩm
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
function ProductDetail({
  product,
  onClose,
}: {
  product: Product
  onClose: () => void
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl">
        <div className="mb-5 flex items-center justify-between">
          <h2 className="text-xl font-bold text-[#302720]">
            Chi tiết sản phẩm
          </h2>

          <button
            onClick={onClose}
            className="rounded-lg p-2 hover:bg-gray-100"
          >
            <X size={21} />
          </button>
        </div>

        <div className="mb-5 flex items-center gap-4 rounded-xl bg-[#f5f4ff] p-4">
          <div className="flex h-16 w-16 items-center justify-center overflow-hidden rounded-xl bg-white text-[#5865f2]">
            {product.image ? (
              <img
                src={product.image}
                alt={product.name}
                className="h-full w-full rounded-xl object-cover"
              />
            ) : (
              <Droplets size={30} />
            )}
          </div>

          <div>
            <h3 className="font-bold text-[#302720]">{product.name}</h3>
            <p className="mt-1 text-sm text-[#95877c]">{product.sku}</p>
            <div className="mt-2">
              <StatusBadge status={product.status} />
            </div>
          </div>
        </div>

        <div className="space-y-3 text-sm">
          {[
            ['Thương hiệu', product.brand],
            ['Nhóm hương', product.category],
            ['Nồng độ', product.concentration],
            ['Dung tích chai', `${product.volumeMl} ml`],
            ['Giá bán mỗi ml', formatCurrency(product.price)],
            ['Tồn kho', `${product.stock} chai`],
            ['Ngày tạo', product.createdAt],
          ].map(([label, value]) => (
            <div
              key={label}
              className="flex justify-between gap-4 border-b border-[#eeeef5] pb-3"
            >
              <span className="text-[#95877c]">{label}</span>
              <span className="text-right font-medium text-[#302720]">
                {value}
              </span>
            </div>
          ))}
        </div>

        <div className="mt-4">
          <p className="mb-2 text-sm text-[#95877c]">Mô tả</p>
          <p className="rounded-xl bg-[#f8f7ff] p-3 text-sm text-[#302720]">
            {product.description || 'Chưa có mô tả.'}
          </p>
        </div>

        <button
          onClick={onClose}
          className="mt-6 w-full rounded-xl bg-[#4f46e5] py-3 text-sm font-semibold text-white hover:bg-[#4338ca]"
        >
          Đóng
        </button>
      </div>
    </div>
  )
}

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>(initialProducts)
  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [categoryFilter, setCategoryFilter] = useState('ALL')
  const [showForm, setShowForm] = useState(false)
  const [editingProduct, setEditingProduct] = useState<Product | null>(null)
  const [viewingProduct, setViewingProduct] = useState<Product | null>(null)

  const filteredProducts = useMemo(() => {
    return products.filter((product) => {
      const search = keyword.toLowerCase()

      const matchesKeyword =
        product.name.toLowerCase().includes(search) ||
        product.sku.toLowerCase().includes(search)

      const matchesStatus =
        statusFilter === 'ALL' || product.status === statusFilter

      const matchesCategory =
        categoryFilter === 'ALL' || product.category === categoryFilter

      return matchesKeyword && matchesStatus && matchesCategory
    })
  }, [products, keyword, statusFilter, categoryFilter])

  const activeCount = products.filter(
    (p) => p.status === 'ACTIVE',
  ).length

  const inactiveCount = products.filter(
    (p) => p.status === 'INACTIVE',
  ).length

  const totalStock = products.reduce(
    (sum, p) => sum + p.stock,
    0,
  )

  const saveProduct = (
    data: Omit<Product, 'id' | 'createdAt'>,
  ) => {
    const duplicateSku = products.some(
      (p) =>
        p.sku.toLowerCase() === data.sku.trim().toLowerCase() &&
        p.id !== editingProduct?.id,
    )

    if (duplicateSku) {
      alert('Mã SKU đã tồn tại. Vui lòng nhập mã khác.')
      return
    }

    if (editingProduct) {
      setProducts((prev) =>
        prev.map((p) =>
          p.id === editingProduct.id
            ? {
                ...p,
                ...data,
                sku: data.sku.trim(),
                name: data.name.trim(),
              }
            : p,
        ),
      )
    } else {
      setProducts((prev) => [
        {
          ...data,
          sku: data.sku.trim(),
          name: data.name.trim(),
          id: Math.max(0, ...prev.map((p) => p.id)) + 1,
          createdAt: new Date().toLocaleDateString('vi-VN'),
        },
        ...prev,
      ])
    }

    setShowForm(false)
    setEditingProduct(null)
  }

  const deleteProduct = (product: Product) => {
    const confirmed = window.confirm(
      `Bạn có chắc muốn xóa sản phẩm "${product.name}" không?`,
    )

    if (!confirmed) return

    setProducts((prev) =>
      prev.filter((p) => p.id !== product.id),
    )
  }

  const openEdit = (product: Product) => {
    setEditingProduct(product)
    setShowForm(true)
  }

  const statCards = [
    {
      label: 'Tổng sản phẩm',
      value: products.length,
      icon: Package,
      color: 'text-[#5865f2]',
      bg: 'bg-[#eef0ff]',
    },
    {
      label: 'Đang kinh doanh',
      value: activeCount,
      icon: CircleCheck,
      color: 'text-green-600',
      bg: 'bg-green-50',
    },
    {
      label: 'Ngừng kinh doanh',
      value: inactiveCount,
      icon: CircleX,
      color: 'text-red-500',
      bg: 'bg-red-50',
    },
    {
      label: 'Tổng tồn kho',
      value: `${totalStock.toLocaleString('vi-VN')} chai`,
      icon: TrendingUp,
      color: 'text-purple-600',
      bg: 'bg-purple-50',
    },
  ]

  return (
    <div className="min-h-full bg-[#f0efff] p-6 md:p-8">
      {/* PAGE HEADER */}
      <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Quản lý sản phẩm
          </h1>

          <p className="mt-1 text-sm text-[#95877c]">
            Quản lý danh mục nước hoa và thông tin sản phẩm của thương hiệu
          </p>
        </div>

        <button
          onClick={() => {
            setEditingProduct(null)
            setShowForm(true)
          }}
          className="flex items-center gap-2 rounded-xl bg-[#4f46e5] px-5 py-3 text-sm font-semibold text-white shadow-sm hover:bg-[#4338ca]"
        >
          <Plus size={18} />
          Thêm sản phẩm
        </button>
      </div>

      {/* STATISTICS */}
      <div className="mb-7 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {statCards.map((card) => {
          const Icon = card.icon

          return (
            <div
              key={card.label}
              className="rounded-2xl border border-[#e5e4f5] bg-white p-6 shadow-sm"
            >
              <div className="flex items-center justify-between">
                <span className="text-sm text-[#95877c]">
                  {card.label}
                </span>

                <div className={`rounded-xl p-3 ${card.bg} ${card.color}`}>
                  <Icon size={22} />
                </div>
              </div>

              <p className={`mt-5 text-3xl font-bold ${card.color}`}>
                {card.value}
              </p>
            </div>
          )
        })}
      </div>
            {/* PRODUCT LIST */}
      <section className="overflow-hidden rounded-2xl border border-[#e5e4f5] bg-white shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-4 p-5">
          <div>
            <h2 className="font-bold text-[#302720]">
              Danh sách sản phẩm
            </h2>

            <p className="mt-1 text-sm text-[#95877c]">
              Hiển thị {filteredProducts.length} / {products.length} sản phẩm
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            <div className="relative">
              <Search
                size={18}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-[#95877c]"
              />

              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Tìm tên, mã SKU..."
                className="w-56 rounded-xl border border-[#e5e4f5] bg-[#f8f7ff] py-3 pl-10 pr-4 text-sm outline-none focus:border-[#5865f2]"
              />
            </div>

            <select
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              className="rounded-xl border border-[#e5e4f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
            >
              <option value="ALL">Tất cả nhóm hương</option>
              <option value="Floral">Floral</option>
              <option value="Woody">Woody</option>
              <option value="Fresh">Fresh</option>
              <option value="Gourmand">Gourmand</option>
              <option value="Musk">Musk</option>
              <option value="Oriental">Oriental</option>
            </select>

            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="rounded-xl border border-[#e5e4f5] bg-white px-4 py-3 text-sm outline-none focus:border-[#5865f2]"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="ACTIVE">Đang kinh doanh</option>
              <option value="INACTIVE">Ngừng kinh doanh</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[1050px] text-left">
            <thead className="bg-[#f4f3ff] text-sm text-[#5c5a91]">
              <tr>
                <th className="px-5 py-4 font-semibold">Sản phẩm</th>
                <th className="px-5 py-4 font-semibold">Mã SKU</th>
                <th className="px-5 py-4 font-semibold">Nhóm hương</th>
                <th className="px-5 py-4 font-semibold">Dung tích</th>
                <th className="px-5 py-4 font-semibold">Giá / ml</th>
                <th className="px-5 py-4 font-semibold">Tồn kho</th>
                <th className="px-5 py-4 font-semibold">Trạng thái</th>
                <th className="px-5 py-4 text-center font-semibold">
                  Thao tác
                </th>
              </tr>
            </thead>

            <tbody className="divide-y divide-[#eeeef5]">
              {filteredProducts.map((product) => (
                <tr
                  key={product.id}
                  className="transition hover:bg-[#faf9ff]"
                >
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-12 w-12 shrink-0 items-center justify-center overflow-hidden rounded-xl bg-[#eef0ff] text-[#5865f2]">
                        {product.image ? (
                          <img
                            src={product.image}
                            alt={product.name}
                            className="h-full w-full object-cover"
                          />
                        ) : (
                          <Droplets size={23} />
                        )}
                      </div>

                      <div>
                        <p className="font-semibold text-[#302720]">
                          {product.name}
                        </p>

                        <p className="mt-1 text-xs text-[#95877c]">
                          {product.concentration}
                        </p>
                      </div>
                    </div>
                  </td>

                  <td className="px-5 py-4 text-sm font-medium text-[#5865f2]">
                    {product.sku}
                  </td>

                  <td className="px-5 py-4 text-sm text-[#302720]">
                    {product.category}
                  </td>

                  <td className="px-5 py-4 text-sm text-[#302720]">
                    {product.volumeMl} ml
                  </td>

                  <td className="px-5 py-4 text-sm text-[#302720]">
                    {formatCurrency(product.price)}
                  </td>

                  <td className="px-5 py-4 text-sm text-[#302720]">
                    {product.stock} chai
                  </td>

                  <td className="px-5 py-4">
                    <StatusBadge status={product.status} />
                  </td>

                  <td className="px-5 py-4">
                    <div className="flex justify-center gap-2">
                      <button
                        title="Xem chi tiết"
                        onClick={() => setViewingProduct(product)}
                        className="rounded-lg border border-[#e5e4f5] p-2 text-[#5865f2] hover:bg-[#eef0ff]"
                      >
                        <Eye size={17} />
                      </button>

                      <button
                        title="Chỉnh sửa"
                        onClick={() => openEdit(product)}
                        className="rounded-lg border border-[#e5e4f5] p-2 text-[#5865f2] hover:bg-[#eef0ff]"
                      >
                        <Pencil size={17} />
                      </button>

                      <button
                        title="Xóa sản phẩm"
                        onClick={() => deleteProduct(product)}
                        className="rounded-lg border border-red-100 p-2 text-red-500 hover:bg-red-50"
                      >
                        <Trash2 size={17} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}

              {filteredProducts.length === 0 && (
                <tr>
                  <td colSpan={8} className="px-5 py-14 text-center">
                    <div className="flex flex-col items-center text-[#95877c]">
                      <Filter size={32} className="mb-3 opacity-50" />

                      <p className="font-medium">
                        Không tìm thấy sản phẩm
                      </p>

                      <p className="mt-1 text-sm">
                        Thử thay đổi từ khóa hoặc bộ lọc.
                      </p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      {/* ADD / EDIT MODAL */}
      {showForm && (
        <ProductModal
          product={editingProduct}
          onClose={() => {
            setShowForm(false)
            setEditingProduct(null)
          }}
          onSave={saveProduct}
        />
      )}

      {/* PRODUCT DETAIL */}
      {viewingProduct && (
        <ProductDetail
          product={viewingProduct}
          onClose={() => setViewingProduct(null)}
        />
      )}
    </div>
  )
}