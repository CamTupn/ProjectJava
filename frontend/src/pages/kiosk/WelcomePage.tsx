import { LanguageSwitcher, useKioskLanguage } from '../../kiosk/language'
import { useNavigate } from 'react-router-dom'
import { ArrowRight, Sparkles } from 'lucide-react'

export default function WelcomePage() {
  const navigate = useNavigate()
  const { language, changeLanguage, isEnglish } = useKioskLanguage()
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gradient-to-br from-[#17132d] via-[#332346] to-[#65435c] px-6 text-center text-white">
      <div className="absolute right-5 top-5"><LanguageSwitcher language={language} onChange={changeLanguage} /></div>
      <div className="mb-8 flex h-20 w-20 items-center justify-center rounded-full border border-white/30 bg-white/10"><Sparkles size={36} /></div>
      <p className="mb-4 text-sm uppercase tracking-[0.3em] text-pink-200">{isEnglish ? 'THE ART OF FRAGRANCE' : 'NGHỆ THUẬT MÙI HƯƠNG'}</p>
      <h1 className="mb-5 text-5xl font-semibold tracking-wide md:text-7xl">ScentStation</h1>
      <p className="mb-10 max-w-xl text-lg leading-relaxed text-white/75">{isEnglish ? 'Discover your signature scent. Explore our unique fragrance collection with ScentStation.' : 'Khám phá mùi hương dành riêng cho bạn. Trải nghiệm bộ sưu tập nước hoa độc đáo cùng ScentStation.'}</p>
      <button onClick={() => navigate('/kiosk/fragrances')} className="flex items-center gap-3 rounded-full bg-white px-9 py-4 font-semibold text-[#332346] transition hover:scale-105 hover:bg-pink-100">{isEnglish ? 'Start Exploring' : 'Bắt đầu trải nghiệm'} <ArrowRight size={20} /></button>
      <p className="mt-16 text-sm text-white/50 md:absolute md:bottom-8 md:mt-0">{isEnglish ? 'A premium fragrance experience' : 'Trải nghiệm hương thơm đẳng cấp'}</p>
    </div>
  )
}
