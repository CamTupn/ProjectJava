import { Outlet } from 'react-router-dom'

export default function KioskLayout() {
  return (
    <main className="min-h-screen">
      <Outlet />
    </main>
  )
}
