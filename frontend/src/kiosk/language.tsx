import { useState } from 'react'

export type KioskLanguage = 'vi' | 'en'
const KEY = 'scentstation-kiosk-language'

export function useKioskLanguage() {
  const [language, setLanguage] = useState<KioskLanguage>(() =>
    typeof window !== 'undefined' && localStorage.getItem(KEY) === 'en' ? 'en' : 'vi'
  )
  const changeLanguage = (next: KioskLanguage) => {
    localStorage.setItem(KEY, next)
    setLanguage(next)
  }
  return { language, changeLanguage, isEnglish: language === 'en' }
}

export function LanguageSwitcher({
  language, onChange,
}: {
  language: KioskLanguage
  onChange: (next: KioskLanguage) => void
}) {
  return (
    <div className="inline-flex shrink-0 rounded-full border border-purple-200 bg-white/90 p-1 text-sm shadow-sm" aria-label="Chọn ngôn ngữ / Choose language">
      {(['vi', 'en'] as const).map(code => (
        <button key={code} type="button" onClick={() => onChange(code)}
          aria-pressed={language === code}
          className={`min-h-10 min-w-12 rounded-full px-3 font-semibold transition ${
            language === code ? 'bg-[#332346] text-white' : 'text-[#332346] hover:bg-purple-100'
          }`}>
          {code === 'vi' ? 'VI' : 'EN'}
        </button>
      ))}
    </div>
  )
}
