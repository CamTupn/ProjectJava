import { Navigate, Route, Routes } from 'react-router-dom'

import AdminLayout from './layouts/AdminLayout'
import TenantAdminLayout from './layouts/TenantAdminLayout'

import DashboardPage from './pages/admin/DashboardPage'
import TenantsPage from './pages/admin/tenants/TenantsPage'
import UsersPage from './pages/admin/users/UsersPage'
import LocationsPage from './pages/admin/locations/LocationsPage'
import ProductsPage from './pages/admin/products/ProductsPage'
import MachinesPage from './pages/admin/machines/MachinesPage'
import SlotsPage from './pages/admin/slots/SlotsPage'
import OrdersPage from './pages/admin/orders/OrdersPage'
import SettingsPage from './pages/admin/settings/SettingsPage'

import TenantDashboardPage from './pages/tenant-admin/DashboardPage'
import TenantOrdersPage from './pages/tenant-admin/orders/OrdersPage'
import ReportsPage from './pages/tenant-admin/reports/ReportsPage'
import TenantSettingsPage from './pages/tenant-admin/settings/SettingsPage'
import TenantUsersPage from './pages/tenant-admin/users/UsersPage'
import TenantLocationsPage from './pages/tenant-admin/locations/LocationsPage'
import TenantProductsPage from './pages/tenant-admin/products/ProductsPage'
import TenantMachinesPage from "./pages/tenant-admin/machines/MachinesPage";
import TenantSlotsPage from "./pages/tenant-admin/slots/SlotsPage";

import KioskLayout from './layouts/KioskLayout'
import WelcomePage from './pages/kiosk/WelcomePage'
import FragranceCatalogPage from './pages/kiosk/FragranceCatalogPage'
import FragranceDetailPage from './pages/kiosk/FragranceDetailPage'
import PaymentPage from './pages/kiosk/PaymentPage'
import DispensingPage from './pages/kiosk/DispensingPage'
import ResultPage from './pages/kiosk/ResultPage'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/admin" replace />} />

      {/* SUPER ADMIN */}
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<DashboardPage />} />
        <Route path="tenants" element={<TenantsPage />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="locations" element={<LocationsPage />} />
        <Route path="products" element={<ProductsPage />} />
        <Route path="machines" element={<MachinesPage />} />
        <Route path="slots" element={<SlotsPage />} />
        <Route path="orders" element={<OrdersPage />} />
        <Route path="settings" element={<SettingsPage />} />
      </Route>

      {/* TENANT ADMIN */}
      <Route path="/tenant-admin" element={<TenantAdminLayout />}>
        <Route index element={<Navigate to="dashboard" replace />} />
        <Route path="dashboard" element={<TenantDashboardPage />} />
        <Route path="orders" element={<TenantOrdersPage />} />
        <Route path="reports" element={<ReportsPage />} />
        <Route path="settings" element={<TenantSettingsPage />} />
        <Route path="users" element={<TenantUsersPage />} />
        <Route path="locations" element={<TenantLocationsPage />} />
        <Route path="products" element={<TenantProductsPage />} />
        <Route path="machines" element={<TenantMachinesPage />} />
        <Route path="slots" element={<TenantSlotsPage />} />
      </Route>

      {/* KIOSK CUSTOMER */}
      <Route path="/kiosk" element={<KioskLayout />}>
        <Route index element={<WelcomePage />} />
        <Route path="fragrances" element={<FragranceCatalogPage />} />
        <Route path="fragrances/:id" element={<FragranceDetailPage />} />
        <Route path="payment/:id" element={<PaymentPage />} />
        <Route path="dispensing/:id" element={<DispensingPage />} />
        <Route path="result/:id" element={<ResultPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/admin" replace />} />
    </Routes>
  )
}

export default App
