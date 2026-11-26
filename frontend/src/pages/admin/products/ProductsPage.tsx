import { useMemo, useState } from 'react'
import {
  Plus,
  Search,
  Eye,
  Pencil,
  Ban,
  Package,
  CheckCircle2,
  CircleSlash,
  X,
} from 'lucide-react'
import type { Product, ProductRequest, ProductStatus } from '../../../types/product'
import ProductForm from './ProductForm'
import ProductDetail from './ProductDetail'

const initialProducts: Product[] = [
  {
    id: 1,
    tenantId: 1,
    tenantName: 'ScentStation Demo',
    sku: 'SCENT-ROSE-001',
    name: 'Rose Elegance',
    description: 'Hương hoa hồng thanh lịch, nhẹ nhàng.',
    fragranceNotes: 'Hoa hồng, hoa nhài, xạ hương',
    imageUrl: '',
    price: 15000,
    volumePerSprayMl: 0.15,
    status: 'ACTIVE',
    createdAt: '10/11/2026',
    updatedAt: '10/11/2026',
  },
  {
    id: 2,
    tenantId: 1,
    tenantName: 'ScentStation Demo',
    sku: 'SCENT-OUD-002',
    name: 'Royal Oud',
    description: 'Mùi hương gỗ trầm ấm và sang trọng.',
    fragranceNotes: 'Gỗ trầm, hổ phách, vanilla',
    imageUrl: '',
    price: 20000,
    volumePerSprayMl: 0.15,
    status: 'ACTIVE',
    createdAt: '11/11/2026',
    updatedAt: '11/11/2026',
  },
  {
    id: 3,
    tenantId: 2,
    tenantName: 'Brand Aroma',
    sku: 'AROMA-FRESH-001',
    name: 'Ocean Breeze',
    description: 'Hương biển tươi mát, năng động.',
    fragranceNotes: 'Cam bergamot, hương biển, tuyết tùng',
    imageUrl: '',
    price: 12000,
    volumePerSprayMl: 0.12,
    status: 'ACTIVE',
    createdAt: '12/11/2026',
    updatedAt: '12/11/2026',
  },
  {
    id: 4,
    tenantId: 2,
    tenantName: 'Brand Aroma',
    sku: 'AROMA-FLORAL-002',
    name: 'Floral Dream',
    description: 'Hương hoa dịu dàng và nữ tính.',
    fragranceNotes: 'Hoa mẫu đơn, hoa cam, xạ hương',
    imageUrl: '',
    price: 18000,
    volumePerSprayMl: 0.15,
    status: 'INACTIVE',
    createdAt: '13/11/2026',
    updatedAt: '13/11/2026',
  },
  {
    id: 5,
    tenantId: 3,
    tenantName: 'Brand Luxury',
    sku: 'LUXURY-MUSK-001',
    name: 'White Musk',
    description: 'Hương xạ hương tinh tế và hiện đại.',
    fragranceNotes: 'Xạ hương trắng, hoa diên vĩ, gỗ đàn hương',
    imageUrl: '',
    price: 25000,
    volumePerSprayMl: 0.18,
    status: 'ACTIVE',
    createdAt: '14/11/2026',
    updatedAt: '14/11/2026',
  },
  {
    id: 6,
    tenantId: 4,
    tenantName: 'Flower Perfume',
    sku: 'FLOWER-VANILLA-001',
    name: 'Vanilla Bloom',
    description: 'Hương vanilla ngọt ngào, ấm áp.',
    fragranceNotes: 'Vanilla, hoa lan, caramel',
    imageUrl: '',
    price: 16000,
    volumePerSprayMl: 0.15,
    status: 'ACTIVE',
    createdAt: '15/11/2026',
    updatedAt: '15/11/2026',
  },
]

const formatPrice = (price: number) =>
  `${price.toLocaleString('vi-VN')} đ`

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>(initialProducts)
  const [keyword, setKeyword] = useState('')
  const [tenantFilter, setTenantFilter] = useState('ALL')
  const [statusFilter, setStatusFilter] = useState('ALL')

  const [showForm, setShowForm] = useState(false)
  const [editingProduct, setEditingProduct] = useState<Product | null>(null)
  const [viewingProduct, setViewingProduct] = useState<Product | null>(null)

  const tenants = useMemo(
    () => [...new Set(products.map((item) => item.tenantName))],
    [products],
  )

  const filteredProducts = useMemo(() => {
    return products.filter((product) => {
      const search = keyword.toLowerCase().trim()

      const matchesKeyword =
        product.name.toLowerCase().includes(search) ||
        product.sku.toLowerCase().includes(search) ||
        product.tenantName.toLowerCase().includes(search)

      const matchesTenant =
        tenantFilter === 'ALL' || product.tenantName === tenantFilter

      const matchesStatus =
        statusFilter === 'ALL' || product.status === statusFilter

      return matchesKeyword && matchesTenant && matchesStatus
    })
  }, [products, keyword, tenantFilter, statusFilter])

  const activeCount = products.filter(
    (item) => item.status === 'ACTIVE',
  ).length

  const inactiveCount = products.filter(
    (item) => item.status === 'INACTIVE',
  ).length

  const handleAdd = () => {
    setEditingProduct(null)
    setShowForm(true)
  }

  const handleEdit = (product: Product) => {
    setEditingProduct(product)
    setShowForm(true)
  }

  const handleSubmit = (data: ProductRequest) => {
    const tenantNames: Record<number, string> = {
      1: 'ScentStation Demo',
      2: 'Brand Aroma',
      3: 'Brand Luxury',
      4: 'Flower Perfume',
      5: 'Oriental Scents',
    }

    if (editingProduct) {
      setProducts((prev) =>
        prev.map((item) =>
          item.id === editingProduct.id
            ? {
                ...item,
                ...data,
                tenantName: tenantNames[data.tenantId] ?? item.tenantName,
                updatedAt: new Date().toLocaleDateString('vi-VN'),
              }
            : item,
        ),
      )
    } else {
      const newProduct: Product = {
        id: Math.max(0, ...products.map((item) => item.id)) + 1,
        ...data,
        tenantName: tenantNames[data.tenantId] ?? 'Thương hiệu mới',
        createdAt: new Date().toLocaleDateString('vi-VN'),
        updatedAt: new Date().toLocaleDateString('vi-VN'),
      }

      setProducts((prev) => [newProduct, ...prev])
    }

    setShowForm(false)
    setEditingProduct(null)
  }

  const handleToggleStatus = (product: Product) => {
    const nextStatus: ProductStatus =
      product.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'

    const confirmed = window.confirm(
      nextStatus === 'INACTIVE'
        ? `Bạn có chắc muốn ngừng kinh doanh "${product.name}"?`
        : `Bạn có muốn kích hoạt lại "${product.name}"?`,
    )

    if (!confirmed) return

    setProducts((prev) =>
      prev.map((item) =>
        item.id === product.id
          ? { ...item, status: nextStatus }
          : item,
      ),
    )
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-[#302720]">
            Quản lý sản phẩm
          </h1>
          <p className="mt-1 text-sm text-[#95877c]">
            Quản lý danh mục nước hoa của các thương hiệu
          </p>
        </div>

        <button
          onClick={handleAdd}
          className="flex items-center justify-center gap-2 rounded-xl bg-[#5865f2] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#4854df]"
        >
          <Plus size={19} />
          Thêm sản phẩm
        </button>
      </div>

      <div className="grid grid-cols-1 gap-5 md:grid-cols-3">
        <StatCard
          title="Tổng sản phẩm"
          value={products.length}
          icon={<Package size={21} />}
        />
        <StatCard
          title="Đang kinh doanh"
          value={activeCount}
          icon={<CheckCircle2 size={21} />}
        />
        <StatCard
          title="Ngừng kinh doanh"
          value={inactiveCount}
          icon={<CircleSlash size={21} />}
        />
      </div>

      <div className="overflow-hidden rounded-2xl border border-[#e8e5f5] bg-white shadow-sm">
        <div className="flex flex-col gap-4 border-b border-[#eeeaf8] p-5 xl:flex-row xl:items-center xl:justify-between">
          <div>
            <h3 className="font-bold text-[#302720]">
              Danh sách sản phẩm
            </h3>
            <p className="mt-1 text-sm text-[#95877c]">
              Hiển thị {filteredProducts.length} / {products.length} sản phẩm
            </p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row">
            <div className="flex items-center gap-2 rounded-xl border border-[#e8e5f5] bg-[#f7f6ff] px-3">
              <Search size={18} className="text-[#95877c]" />
              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Tìm tên, mã sản phẩm..."
                className="w-full bg-transparent py-3 text-sm outline-none sm:w-52"
              />
              {keyword && (
                <button onClick={() => setKeyword('')}>
                  <X size={16} className="text-[#95877c]" />
                </button>
              )}
            </div>

            <select
              value={tenantFilter}
              onChange={(e) => setTenantFilter(e.target.value)}
              className="rounded-xl border border-[#e8e5f5] bg-white px-3 py-3 text-sm text-[#76675c] outline-none focus:border-[#5865f2]"
            >
              <option value="ALL">Tất cả thương hiệu</option>
              {tenants.map((tenant) => (
                <option key={tenant} value={tenant}>
                  {tenant}
                </option>
              ))}
            </select>

            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="rounded-xl border border-[#e8e5f5] bg-white px-3 py-3 text-sm text-[#76675c] outline-none focus:border-[#5865f2]"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="ACTIVE">Đang kinh doanh</option>
              <option value="INACTIVE">Ngừng kinh doanh</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[950px] text-left text-sm">
            <thead className="bg-[#f7f6ff] text-[#766b91]">
              <tr>
                <th className="px-6 py-4 font-medium">Sản phẩm</th>
                <th className="px-6 py-4 font-medium">Mã SKU</th>
                <th className="px-6 py-4 font-medium">Thương hiệu</th>
                <th className="px-6 py-4 font-medium">Giá / lần xịt</th>
                <th className="px-6 py-4 font-medium">Ngày tạo</th>
                <th className="px-6 py-4 font-medium">Trạng thái</th>
                <th className="px-6 py-4 text-center font-medium">Thao tác</th>
              </tr>
            </thead>

            <tbody>
              {filteredProducts.map((product) => (
                <tr
                  key={product.id}
                  className="border-b border-[#f0edf8] transition hover:bg-[#faf9ff] last:border-0"
                >
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-[#eef0ff] text-[#5865f2]">
                        <Package size={21} />
                      </div>
                      <div>
                        <p className="font-semibold text-[#302720]">
                          {product.name}
                        </p>
                        <p className="mt-1 text-xs text-[#95877c]">
                          {product.volumePerSprayMl} ml / lần
                        </p>
                      </div>
                    </div>
                  </td>

                  <td className="px-6 py-4 text-[#5865f2]">
                    {product.sku}
                  </td>

                  <td className="px-6 py-4 text-[#76675c]">
                    {product.tenantName}
                  </td>

                  <td className="px-6 py-4 font-medium text-[#302720]">
                    {formatPrice(product.price)}
                  </td>

                  <td className="px-6 py-4 text-[#95877c]">
                    {product.createdAt}
                  </td>

                  <td className="px-6 py-4">
                    <StatusBadge status={product.status} />
                  </td>

                  <td className="px-6 py-4">
                    <div className="flex justify-center gap-2">
                      <ActionButton
                        title="Xem chi tiết"
                        onClick={() => setViewingProduct(product)}
                      >
                        <Eye size={17} />
                      </ActionButton>

                      <ActionButton
                        title="Chỉnh sửa"
                        onClick={() => handleEdit(product)}
                      >
                        <Pencil size={17} />
                      </ActionButton>

                      <ActionButton
                        title={
                          product.status === 'ACTIVE'
                            ? 'Ngừng kinh doanh'
                            : 'Kinh doanh lại'
                        }
                        onClick={() => handleToggleStatus(product)}
                      >
                        <Ban size={17} />
                      </ActionButton>
                    </div>
                  </td>
                </tr>
              ))}

              {filteredProducts.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-[#95877c]">
                    Không tìm thấy sản phẩm phù hợp.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="border-t border-[#eeeaf8] px-6 py-4 text-xs text-[#95877c]">
          Dữ liệu minh họa phục vụ phát triển giao diện Frontend
        </div>
      </div>

      {showForm && (
        <ProductForm
          initialData={editingProduct}
          onClose={() => {
            setShowForm(false)
            setEditingProduct(null)
          }}
          onSubmit={handleSubmit}
        />
      )}

      {viewingProduct && (
        <ProductDetail
          product={viewingProduct}
          onClose={() => setViewingProduct(null)}
        />
      )}
    </div>
  )
}

function StatCard({
  title,
  value,
  icon,
}: {
  title: string
  value: number
  icon: React.ReactNode
}) {
  return (
    <div className="rounded-2xl border border-[#e8e5f5] bg-white p-6 shadow-sm">
      <div className="flex items-center justify-between">
        <p className="text-sm text-[#95877c]">{title}</p>
        <div className="rounded-xl bg-[#eef0ff] p-3 text-[#5865f2]">
          {icon}
        </div>
      </div>
      <h2 className="mt-4 text-3xl font-bold text-[#302720]">{value}</h2>
    </div>
  )
}

function StatusBadge({ status }: { status: ProductStatus }) {
  return (
    <span
      className={`whitespace-nowrap rounded-full px-3 py-1.5 text-xs font-medium ${
        status === 'ACTIVE'
          ? 'bg-emerald-50 text-emerald-700'
          : 'bg-slate-100 text-slate-600'
      }`}
    >
      {status === 'ACTIVE' ? 'Đang kinh doanh' : 'Ngừng kinh doanh'}
    </span>
  )
}

function ActionButton({
  children,
  title,
  onClick,
}: {
  children: React.ReactNode
  title: string
  onClick: () => void
}) {
  return (
    <button
      type="button"
      title={title}
      onClick={onClick}
      className="rounded-lg border border-[#e8e5f5] p-2 text-[#766b91] transition hover:border-[#5865f2] hover:bg-[#eef0ff] hover:text-[#5865f2]"
    >
      {children}
    </button>
  )
}
