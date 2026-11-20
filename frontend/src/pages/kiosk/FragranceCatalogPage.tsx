import { LanguageSwitcher, useKioskLanguage } from '../../kiosk/language'
import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, ArrowRight, Search, Sparkles, Droplets } from 'lucide-react'

interface Product {
  id: number; sku: string; name: string; brand: string; category: string
  concentration: string; volumeMl: number; price: number; stock: number
  description: string; image: string; status: 'ACTIVE' | 'INACTIVE'; createdAt: string
}

const initialProducts: Product[] = [
  { id: 1, sku: 'PRD-001', name: 'Rose Elegance', brand: 'ScentStation Demo', category: 'Floral', concentration: 'EDP', volumeMl: 50, price: 15000, stock: 245, description: 'Hương hoa hồng thanh lịch, nhẹ nhàng và quyến rũ.', image: '', status: 'ACTIVE', createdAt: '01/11/2026' },
  { id: 2, sku: 'PRD-002', name: 'Royal Oud', brand: 'ScentStation Demo', category: 'Woody', concentration: 'EDP', volumeMl: 50, price: 20000, stock: 198, description: 'Hương gỗ trầm ấm, sang trọng và mạnh mẽ.', image: '', status: 'ACTIVE', createdAt: '03/11/2026' },
  { id: 3, sku: 'PRD-003', name: 'Ocean Breeze', brand: 'ScentStation Demo', category: 'Fresh', concentration: 'EDT', volumeMl: 30, price: 18000, stock: 156, description: 'Hương biển tươi mát, mang lại cảm giác dễ chịu.', image: '', status: 'ACTIVE', createdAt: '05/11/2026' },
  { id: 4, sku: 'PRD-004', name: 'Vanilla Bloom', brand: 'ScentStation Demo', category: 'Gourmand', concentration: 'EDP', volumeMl: 50, price: 30000, stock: 132, description: 'Hương vani ngọt ngào và ấm áp.', image: '', status: 'ACTIVE', createdAt: '08/11/2026' },
  { id: 5, sku: 'PRD-005', name: 'White Musk', brand: 'ScentStation Demo', category: 'Musk', concentration: 'EDT', volumeMl: 30, price: 12000, stock: 98, description: 'Hương xạ hương tinh tế, phù hợp sử dụng hằng ngày.', image: '', status: 'INACTIVE', createdAt: '10/11/2026' },
]

const formatPrice = (price: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
const categoryColors: Record<string, string> = {
  Floral: 'from-pink-100 to-rose-200', Woody: 'from-amber-100 to-orange-200',
  Fresh: 'from-cyan-100 to-blue-200', Gourmand: 'from-yellow-100 to-amber-200', Musk: 'from-purple-100 to-violet-200',
}
const descriptionsEn: Record<number, string> = {
  1: 'An elegant, delicate and captivating rose fragrance.',
  2: 'A warm, luxurious and bold woody fragrance.',
  3: 'A refreshing ocean-inspired fragrance.',
  4: 'A sweet and comforting vanilla fragrance.',
  5: 'A subtle musk scent for everyday wear.',
}
const categoryNames: Record<string, string> = {
  Floral: 'Hương hoa', Woody: 'Hương gỗ', Fresh: 'Tươi mát', Gourmand: 'Hương ngọt', Musk: 'Xạ hương',
}

export default function FragranceCatalogPage() {
  const navigate = useNavigate()
  const { language, changeLanguage, isEnglish } = useKioskLanguage()
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('All')
  const categories = useMemo(() => ['All', ...new Set(initialProducts.filter(p => p.status === 'ACTIVE').map(p => p.category))], [])
  const filteredProducts = useMemo(() => {
    const keyword = search.trim().toLowerCase()
    return initialProducts.filter(p => p.status === 'ACTIVE' && (category === 'All' || p.category === category) &&
      [p.name, p.brand, p.sku].some(value => value.toLowerCase().includes(keyword)))
  }, [search, category])

  return (
    <div className="min-h-screen bg-[#faf7f9] text-[#30253b]">
      <header className="border-b border-purple-100 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">
          <button onClick={() => navigate('/kiosk')} className="flex items-center gap-2 text-sm font-medium text-purple-700 hover:text-purple-900"><ArrowLeft size={18} />{isEnglish ? 'Back' : 'Quay lại'}</button>
          <div className="flex items-center gap-2"><Sparkles className="text-purple-600" size={25} /><span className="text-2xl font-bold">ScentStation</span></div>
          <LanguageSwitcher language={language} onChange={changeLanguage} />
        </div>
      </header>
      <section className="px-6 pb-10 pt-14 text-center">
        <p className="mb-3 text-sm font-semibold uppercase tracking-[0.3em] text-purple-500">{isEnglish ? 'THE FRAGRANCE COLLECTION' : 'BỘ SƯU TẬP NƯỚC HOA'}</p>
        <h1 className="mb-4 text-4xl font-bold md:text-5xl">{isEnglish ? 'Find Your Perfect Scent' : 'Tìm kiếm mùi hương yêu thích'}</h1>
        <p className="mx-auto max-w-2xl text-gray-500">{isEnglish ? 'Explore our fragrance collection and choose a scent that suits your style.' : 'Khám phá bộ sưu tập nước hoa và lựa chọn mùi hương phù hợp với phong cách của bạn.'}</p>
      </section>
      <section className="mx-auto max-w-7xl px-6">
        <div className="mb-7 flex flex-col gap-5">
          <div className="relative">
            <Search size={20} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
            <input type="text" value={search} onChange={e => setSearch(e.target.value)} placeholder={isEnglish ? 'Search fragrances...' : 'Tìm kiếm nước hoa...'} className="w-full rounded-2xl border border-purple-100 bg-white py-4 pl-12 pr-4 outline-none focus:border-purple-400" />
          </div>
          <div className="flex flex-wrap gap-3">
            {categories.map(item => <button key={item} onClick={() => setCategory(item)} className={`rounded-full px-6 py-2.5 text-sm font-medium transition ${category === item ? 'bg-[#332346] text-white' : 'border border-purple-100 bg-white text-gray-600 hover:bg-purple-50'}`}>{item === 'All' ? (isEnglish ? 'All' : 'Tất cả') : (isEnglish ? item : (categoryNames[item] || item))}</button>)}
          </div>
        </div>
        <p className="mb-5 text-sm text-gray-500">{isEnglish ? `Showing ${filteredProducts.length} fragrances` : `Hiển thị ${filteredProducts.length} sản phẩm`}</p>
        {filteredProducts.length === 0 ? (
          <div className="rounded-2xl bg-white p-16 text-center">
            <Droplets size={48} className="mx-auto mb-4 text-purple-300" />
            <h3 className="mb-2 text-xl font-semibold">{isEnglish ? 'No fragrances found' : 'Không tìm thấy sản phẩm'}</h3>
            <p className="mb-5 text-sm text-gray-500">{isEnglish ? 'Try another keyword or fragrance family.' : 'Hãy thử từ khóa hoặc nhóm hương khác.'}</p>
            <button onClick={() => { setSearch(''); setCategory('All') }} className="rounded-xl bg-[#332346] px-6 py-3 text-white hover:bg-[#51376a]">{isEnglish ? 'Clear Filters' : 'Xóa bộ lọc'}</button>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-7 pb-16 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {filteredProducts.map(product => {
              const available = product.stock > 0
              return <div key={product.id} className="overflow-hidden rounded-3xl border border-purple-100 bg-white shadow-sm transition duration-300 hover:-translate-y-1 hover:shadow-xl">
                <div className={`relative flex h-56 items-center justify-center bg-gradient-to-br ${categoryColors[product.category] || 'from-gray-100 to-gray-200'}`}>
                  {product.image ? <img src={product.image} alt={product.name} className="h-full w-full object-cover" /> : <Droplets size={75} strokeWidth={1} className="text-purple-700/40" />}
                  <span className={`absolute right-4 top-4 rounded-full bg-white px-3 py-1 text-xs font-semibold ${available ? 'text-green-700' : 'text-red-600'}`}>{available ? (isEnglish ? 'Available' : 'Còn hàng') : (isEnglish ? 'Out of Stock' : 'Hết hàng')}</span>
                </div>
                <div className="p-6">
                  <p className="mb-2 text-xs font-semibold uppercase tracking-widest text-purple-500">{product.brand}</p>
                  <h3 className="mb-2 text-xl font-bold">{product.name}</h3>
                  <p className="mb-4 text-sm text-gray-500">{isEnglish ? product.category : (categoryNames[product.category] || product.category)} · {product.concentration}</p>
                  <p className="mb-5 line-clamp-2 min-h-10 text-sm text-gray-500">{isEnglish ? descriptionsEn[product.id] || product.description : product.description}</p>
                  <div className="mb-5 border-t border-gray-100 pt-4"><p className="mb-1 text-xs text-gray-400">{isEnglish ? 'Price per spray (demo data)' : 'Giá mỗi lần xịt (dữ liệu mẫu)'}</p><p className="text-2xl font-bold text-purple-800">{formatPrice(product.price)}</p></div>
                  <button disabled={!available} onClick={() => navigate(`/kiosk/fragrances/${product.id}`)} className="flex w-full items-center justify-center gap-2 rounded-xl bg-[#332346] px-4 py-3 font-semibold text-white transition hover:bg-[#51376a] disabled:cursor-not-allowed disabled:bg-gray-300">{available ? (isEnglish ? 'View Details' : 'Xem chi tiết') : (isEnglish ? 'Unavailable' : 'Không khả dụng')} {available && <ArrowRight size={18} />}</button>
                </div>
              </div>
            })}
          </div>
        )}
      </section>
      <footer className="border-t border-purple-100 bg-white py-6 text-center"><p className="text-sm text-gray-400">ScentStation · Trải nghiệm hương thơm đẳng cấp</p></footer>
    </div>
  )
}
