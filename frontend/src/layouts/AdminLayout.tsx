import { useEffect, useState } from 'react'
import { Menu, X } from 'lucide-react'
import { Outlet, useLocation } from 'react-router-dom'

import Sidebar from '../components/admin/Sidebar'
import Header from '../components/admin/Header'

export default function AdminLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const location = useLocation()

  // Đóng sidebar khi chuyển trang
  useEffect(() => {
    setSidebarOpen(false)
  }, [location.pathname])

  // Khóa cuộn trang khi mở menu mobile
  useEffect(() => {
    if (!sidebarOpen) return

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [sidebarOpen])

  return (
    <div className="min-h-screen bg-[#F7F8FC]">
      {/* Overlay mobile */}
      {sidebarOpen && (
        <button
          type="button"
          aria-label="Đóng menu"
          onClick={() => setSidebarOpen(false)}
          className="fixed inset-0 z-40 bg-black/40 lg:hidden"
        />
      )}

      {/* Sidebar */}
      <div
        className={`fixed inset-y-0 left-0 z-50 w-64 transform transition-transform duration-300 lg:translate-x-0 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <Sidebar />

        <button
          type="button"
          aria-label="Đóng menu"
          onClick={() => setSidebarOpen(false)}
          className="absolute right-3 top-5 rounded-lg p-2 text-gray-600 hover:bg-gray-100 lg:hidden"
        >
          <X size={22} />
        </button>
      </div>

      {/* Main content */}
      <div className="min-h-screen min-w-0 lg:pl-64">
        {/* Mobile navigation */}
        <div className="flex h-14 items-center border-b border-[#DCE8FF] bg-white px-4 lg:hidden">
          <button
            type="button"
            aria-label="Mở menu"
            aria-expanded={sidebarOpen}
            onClick={() => setSidebarOpen(true)}
            className="rounded-lg p-2 text-[#4F5FE8] hover:bg-[#EEF0FF]"
          >
            <Menu size={24} />
          </button>

          <span className="ml-3 font-bold text-[#202124]">
            ScentStation
          </span>
        </div>

        <Header />

        <main className="min-w-0 p-4 sm:p-6 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
