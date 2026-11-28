import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import {
  Bell,
  Search,
  Menu,
  X,
} from 'lucide-react'

import { useAuth } from '../auth/AuthContext'
import AccountMenu from '../components/common/AccountMenu'
import TenantAdminSidebar from '../components/tenant-admin/TenantAdminSidebar'

export default function TenantAdminLayout() {
  const { user } = useAuth()

  const [sidebarOpen, setSidebarOpen] = useState(false)
  const location = useLocation()

  const tenantName =
    user?.tenantName ||
    user?.tenantCode ||
    'Thương hiệu'

  useEffect(() => {
    setSidebarOpen(false)
  }, [location.pathname])

  useEffect(() => {
    if (!sidebarOpen) return

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [sidebarOpen])

  return (
    <div className="min-h-screen bg-[#FAF8FD]">
      {/* Mobile overlay */}
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
          sidebarOpen
            ? 'translate-x-0'
            : '-translate-x-full'
        }`}
      >
        <TenantAdminSidebar />

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
      <div className="min-h-screen min-w-0 lg:ml-64">

        {/* Mobile top bar */}
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

          <span className="ml-3 truncate font-bold text-[#202124]">
            {tenantName}
          </span>
        </div>

        {/* Header */}
        <header className="sticky top-0 z-30 flex min-h-20 items-center justify-between gap-4 border-b border-[#DCE8FF] bg-white px-4 py-3 sm:px-6 lg:px-8">

          <div className="min-w-0">
            <h1 className="truncate text-base font-bold text-[#202124] sm:text-lg">
              {tenantName} Dashboard
            </h1>

            <p className="hidden truncate text-sm text-[#95877C] sm:block">
              Quản lý hoạt động thương hiệu {tenantName}
            </p>
          </div>

          <div className="flex shrink-0 items-center gap-3 sm:gap-5">

            {/* Search desktop */}
            <div className="hidden h-12 w-56 items-center gap-3 rounded-xl bg-[#F0F0FF] px-4 xl:flex">
              <Search
                size={19}
                className="shrink-0 text-[#95877C]"
              />

              <input
                type="text"
                placeholder="Tìm kiếm..."
                className="min-w-0 flex-1 bg-transparent text-sm outline-none placeholder:text-[#95877C]"
              />
            </div>

            {/* Notification */}
            <button
              type="button"
              aria-label="Thông báo"
              className="relative text-[#64748B] hover:text-[#4F5FE8]"
            >
              <Bell size={22} />

              <span className="absolute -right-1 -top-1 h-2.5 w-2.5 rounded-full bg-red-500" />
            </button>

            {/* Account menu */}
            <div className="border-l border-[#E2E8F0] pl-3 sm:pl-5">
              <AccountMenu />
            </div>

          </div>
        </header>

        {/* Page content */}
        <main className="min-w-0 p-4 sm:p-6 lg:p-8">
          <Outlet />
        </main>

      </div>
    </div>
  )
}