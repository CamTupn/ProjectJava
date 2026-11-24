import { 
  Bell, 
  Search,
  ShieldCheck,
  Sparkles,
 } from 'lucide-react'

export default function Header() {
  return (
    <header className="sticky top-0 z-30 flex min-h-20 items-center justify-between gap-4 border-b border-slate-100 bg-white/95 px-4 py-3 shadow-sm backdrop-blur-md sm:px-6 lg:px-8">

      {/* Heading */}
      <div className="min-w-0">
        <div className="mb-1 flex items-center gap-2">
          <Sparkles size={15} className="text-indigo-500" />
          <span className="text-[11px] font-bold uppercase tracking-[0.18em] text-indigo-500">
            ScentStation Workspace
          </span>
        </div>

        <h2 className="text-lg font-bold tracking-tight text-slate-800 sm:text-xl">
          Quản trị hệ thống
        </h2>
      </div>

      {/* Right actions */}
      <div className="flex shrink-0 items-center gap-3 sm:gap-4">

        <div className="hidden h-11 w-56 items-center gap-3 rounded-2xl border border-slate-100 bg-slate-50 px-4 xl:flex">
          <Search size={18} className="text-slate-400" />
          <input
            type="text"
            placeholder="Tìm kiếm..."
            aria-label="Tìm kiếm"
            className="min-w-0 flex-1 bg-transparent text-sm text-slate-700 outline-none placeholder:text-slate-400"
          />
        </div>

        <button
          type="button"
          aria-label="Thông báo"
          className="relative flex h-11 w-11 items-center justify-center rounded-2xl border border-slate-100 bg-white text-slate-500 transition hover:border-indigo-200 hover:bg-indigo-50 hover:text-indigo-600"
        >
          <Bell size={20} />
          <span className="absolute right-2.5 top-2.5 h-2 w-2 rounded-full bg-rose-500 ring-2 ring-white" />
        </button>

        <div className="flex items-center gap-3 border-l border-slate-100 pl-3 sm:pl-4">
          <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-violet-600 font-bold text-white shadow-sm">
            SA
          </div>

          <div className="hidden xl:block">
            <p className="text-sm font-bold text-slate-800">
              Super Admin
            </p>

            <p className="flex items-center gap-1 text-xs text-slate-400">
              <ShieldCheck size={12} />
              Quản trị nền tảng
            </p>
          </div>
        </div>

      </div>
    </header>
  )
}
