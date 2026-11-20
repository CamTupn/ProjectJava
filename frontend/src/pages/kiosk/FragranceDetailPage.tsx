import { LanguageSwitcher, useKioskLanguage } from '../../kiosk/language'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, ArrowRight, Sparkles, Droplets, CheckCircle2, XCircle, ShieldCheck, Heart } from 'lucide-react'

type Product = {
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
  Floral: 'from-pink-100 via-rose-100 to-pink-200', Woody: 'from-amber-100 via-orange-100 to-amber-200',
  Fresh: 'from-cyan-100 via-sky-100 to-blue-200', Gourmand: 'from-yellow-100 via-amber-100 to-yellow-200',
  Musk: 'from-purple-100 via-violet-100 to-purple-200',
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

export default function FragranceDetailPage() {
  const navigate = useNavigate()
  const { language, changeLanguage, isEnglish } = useKioskLanguage()
  const { id } = useParams<{ id: string }>()
  const product = initialProducts.find(item => item.id === Number(id))

  if (!product || product.status !== 'ACTIVE') {
    return <div className="flex min-h-screen flex-col items-center justify-center bg-[#faf7f9] px-6 text-center">
      <div className="absolute right-5 top-5"><LanguageSwitcher language={language} onChange={changeLanguage} /></div>
      <XCircle size={65} className="mb-6 text-purple-300" />
      <h1 className="mb-3 text-3xl font-bold text-[#30253b]">{isEnglish ? 'Fragrance Unavailable' : 'Sản phẩm không khả dụng'}</h1>
      <p className="mb-8 max-w-md text-gray-500">{isEnglish ? 'This fragrance is currently unavailable. Please choose another scent.' : 'Sản phẩm này hiện không thể trải nghiệm. Vui lòng chọn mùi hương khác.'}</p>
      <button onClick={() => navigate('/kiosk/fragrances')} className="flex items-center gap-3 rounded-xl bg-[#332346] px-8 py-4 font-semibold text-white"><ArrowLeft size={20} />{isEnglish ? 'Back to Collection' : 'Quay lại danh sách'}</button>
    </div>
  }

  const available = product.stock > 0
  return <div className="min-h-screen bg-[#faf7f9] text-[#30253b]">
    <header className="border-b border-purple-100 bg-white">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">
        <button onClick={() => navigate('/kiosk/fragrances')} className="flex items-center gap-2 font-medium text-purple-700 transition hover:text-purple-900"><ArrowLeft size={20} />{isEnglish ? 'Back to Collection' : 'Quay lại danh sách'}</button>
        <div className="flex items-center gap-2"><Sparkles size={25} className="text-purple-600" /><span className="text-2xl font-bold">ScentStation</span></div>
        <LanguageSwitcher language={language} onChange={changeLanguage} />
      </div>
    </header>
    <main className="mx-auto max-w-7xl px-6 py-10 md:py-16">
      <div className="grid grid-cols-1 items-start gap-10 lg:grid-cols-2 lg:gap-16">
        <div>
          <div className={`relative flex min-h-[350px] items-center justify-center overflow-hidden rounded-[32px] bg-gradient-to-br md:min-h-[520px] ${categoryColors[product.category] || 'from-gray-100 to-gray-200'}`}>
            {product.image ? <img src={product.image} alt={product.name} className="h-full w-full object-cover" /> :
              <div className="flex flex-col items-center gap-5"><div className="flex h-40 w-40 items-center justify-center rounded-full border border-white/50 bg-white/20"><Droplets size={100} strokeWidth={1} className="text-purple-700/50" /></div><p className="text-center text-sm font-medium uppercase tracking-[0.2em] text-purple-700/60">{isEnglish ? 'ScentStation Collection' : 'Bộ sưu tập ScentStation'}</p></div>}
            <span className={`absolute right-6 top-6 flex items-center gap-2 rounded-full bg-white px-4 py-2 text-sm font-semibold shadow-sm ${available ? 'text-green-700' : 'text-red-600'}`}>
              {available ? <CheckCircle2 size={17} /> : <XCircle size={17} />}{available ? (isEnglish ? 'Available' : 'Còn hàng') : (isEnglish ? 'Out of Stock' : 'Hết hàng')}
            </span>
          </div>
          <div className="mt-6 flex items-start gap-4 rounded-2xl border border-purple-100 bg-white p-5"><ShieldCheck size={25} className="shrink-0 text-purple-600" /><div><h3 className="mb-1 font-semibold">{isEnglish ? 'Personalized Fragrance Experience' : 'Trải nghiệm nước hoa cá nhân hóa'}</h3><p className="text-sm leading-relaxed text-gray-500">{isEnglish ? 'Choose your favorite fragrance and enjoy a convenient experience at the ScentStation kiosk.' : 'Lựa chọn mùi hương yêu thích và trải nghiệm nước hoa tiện lợi ngay tại máy ScentStation.'}</p></div></div>
        </div>
        <div className="flex flex-col">
          <div className="mb-5 flex items-center gap-2"><Sparkles size={17} className="text-purple-500" /><p className="text-sm font-semibold uppercase tracking-[0.25em] text-purple-500">{product.brand}</p></div>
          <h1 className="mb-4 text-4xl font-bold leading-tight md:text-5xl">{product.name}</h1>
          <div className="mb-7 flex flex-wrap gap-3"><span className="rounded-full bg-purple-100 px-5 py-2 text-sm font-semibold text-purple-700">{isEnglish ? product.category : (categoryNames[product.category] || product.category)}</span><span className="rounded-full bg-white px-5 py-2 text-sm font-medium text-gray-600">{product.concentration}</span></div>
          <p className="mb-8 text-lg leading-relaxed text-gray-600">{isEnglish ? descriptionsEn[product.id] || product.description : product.description}</p>
          <div className="mb-8 rounded-2xl border border-purple-100 bg-white p-6">
            <h2 className="mb-5 text-lg font-bold">{isEnglish ? 'Fragrance Information' : 'Thông tin nước hoa'}</h2>
            <div className="space-y-4">
              <div className="flex items-center justify-between border-b border-gray-100 pb-4"><span className="text-gray-500">{isEnglish ? 'Fragrance Family' : 'Nhóm hương'}</span><span className="font-semibold">{isEnglish ? product.category : (categoryNames[product.category] || product.category)}</span></div>
              <div className="flex items-center justify-between border-b border-gray-100 pb-4"><span className="text-gray-500">{isEnglish ? 'Concentration' : 'Nồng độ'}</span><span className="font-semibold">{product.concentration}</span></div>
              <div className="flex items-center justify-between"><span className="text-gray-500">{isEnglish ? 'Product Code' : 'Mã sản phẩm'}</span><span className="font-semibold">{product.sku}</span></div>
            </div>
          </div>
          <div className="mb-7 rounded-3xl border border-purple-100 bg-gradient-to-br from-white to-purple-50 p-7">
            <p className="mb-2 text-sm font-medium text-gray-500">{isEnglish ? 'Price per spray (demo data)' : 'Giá mỗi lần xịt (dữ liệu mẫu)'}</p>
            <div className="flex flex-wrap items-baseline gap-3"><h2 className="text-4xl font-bold text-purple-800">{formatPrice(product.price)}</h2><span className="text-sm text-gray-400">{isEnglish ? '/ spray' : '/ lần xịt'}</span></div>
            <div className="mt-5 flex items-center gap-2 text-sm text-gray-500"><Heart size={17} className="text-pink-500" /> Khám phá mùi hương yêu thích chỉ với một lần xịt.</div>
          </div>
          <button disabled={!available} onClick={() => navigate(`/kiosk/payment/${product.id}`)} className="flex min-h-16 w-full items-center justify-center gap-3 rounded-2xl bg-[#332346] px-8 py-5 text-lg font-semibold text-white shadow-lg shadow-purple-200 transition hover:bg-[#51376a] active:scale-[0.98] disabled:cursor-not-allowed disabled:bg-gray-300">
            {available ? (isEnglish ? 'Proceed to Payment' : 'Tiến hành thanh toán') : (isEnglish ? 'Unavailable' : 'Sản phẩm không khả dụng')} {available && <ArrowRight size={23} />}
          </button>
          <p className="mt-4 text-center text-sm text-gray-400"><ShieldCheck size={15} className="mr-1 inline" /> Vui lòng hoàn tất thanh toán trước khi sử dụng máy.</p>
        </div>
      </div>
    </main>
    <footer className="border-t border-purple-100 bg-white py-6 text-center"><p className="text-sm text-gray-400">ScentStation · Trải nghiệm hương thơm đẳng cấp</p></footer>
  </div>
}
