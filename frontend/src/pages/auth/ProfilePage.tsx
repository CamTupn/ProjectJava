import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import {
  ArrowLeft,
  UserRound,
  Mail,
  AtSign,
  Building2,
  ShieldCheck,
  LockKeyhole,
  RefreshCw,
  AlertCircle,
  LogOut,
  Wind,
} from 'lucide-react'

import {
  getRoleHome,
  useAuth,
} from '../../auth/AuthContext'

import apiClient, {
  getApiErrorMessage,
  type ApiResponse,
} from '../../services/apiClient'

interface ProfileData {
  userId: number
  username: string
  email: string
  fullName: string
  tenantId: number | null
  tenantCode: string | null
  tenantName: string | null
  roles: string[]
  status: string
  mustChangePassword: boolean
}

const ROLE_LABELS: Record<string, string> = {
  PLATFORM_SUPER_ADMIN: 'Platform Super Admin',
  BRAND_ADMIN: 'Brand Admin',
  OPERATIONS_MANAGER: 'Operations Manager',
  TECHNICIAN: 'Technician',
  INVENTORY_STAFF: 'Inventory Staff',
}

export default function ProfilePage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const [profile, setProfile] =
    useState<ProfileData | null>(null)

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reload, setReload] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    const loadProfile = async () => {
      setLoading(true)
      setError('')

      try {
        const response = await apiClient.get<
          ApiResponse<ProfileData>
        >('/auth/me', {
          signal: controller.signal,
        })

        if (
          !response.data.success ||
          !response.data.data
        ) {
          throw new Error(
            response.data.message ||
            'Không thể tải thông tin tài khoản.'
          )
        }

        if (!controller.signal.aborted) {
          setProfile(response.data.data)
        }
      } catch (err) {
        if (!controller.signal.aborted) {
          setError(getApiErrorMessage(err))
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    void loadProfile()

    return () => controller.abort()
  }, [reload])

  const handleBack = () => {
    navigate(
      user ? getRoleHome(user.role) : '/login',
      { replace: true }
    )
  }

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const displayName =
    profile?.fullName ||
    user?.name ||
    'User'

  const initials =
    displayName
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((word) => word.charAt(0))
      .join('')
      .toUpperCase() || 'U'

  return (
    <div className="min-h-screen bg-[#F8F7FC] px-4 py-8 sm:px-6">

      <div className="mx-auto max-w-4xl">

        {/* Top navigation */}
        <div className="mb-8 flex items-center justify-between gap-4">

          <button
            type="button"
            onClick={handleBack}
            className="flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-violet-600"
          >
            <ArrowLeft size={18} />
            Quay lại Dashboard
          </button>

          <div className="flex items-center gap-2 font-bold text-slate-800">
            <Wind size={22} className="text-violet-600" />
            ScentStation
          </div>
        </div>

        {/* Page title */}
        <div className="mb-6">
          <p className="text-xs font-bold uppercase tracking-widest text-violet-600">
            Account Management
          </p>

          <h1 className="mt-2 text-3xl font-bold text-slate-900">
            Thông tin cá nhân
          </h1>

          <p className="mt-2 text-sm text-slate-500">
            Xem thông tin tài khoản và quản lý bảo mật.
          </p>
        </div>

        {loading ? (
          <div className="flex items-center justify-center gap-3 rounded-3xl border border-violet-100 bg-white p-16 text-slate-500">
            <RefreshCw
              size={22}
              className="animate-spin text-violet-600"
            />

            Đang tải thông tin...
          </div>
        ) : error ? (
          <div className="rounded-2xl border border-red-100 bg-red-50 p-6">

            <div className="flex items-center gap-2 text-red-600">
              <AlertCircle size={20} />
              {error}
            </div>

            <button
              type="button"
              onClick={() => setReload((value) => value + 1)}
              className="mt-4 rounded-xl bg-white px-4 py-2 text-sm font-medium text-red-600"
            >
              Thử lại
            </button>
          </div>
        ) : profile && (
          <>
            {/* Profile overview */}
            <section className="overflow-hidden rounded-3xl border border-violet-100 bg-white shadow-sm">

              <div className="h-28 bg-gradient-to-r from-[#302344] via-violet-700 to-indigo-500" />

              <div className="px-6 pb-6 sm:px-8">

                <div className="-mt-10 flex h-20 w-20 items-center justify-center rounded-3xl border-4 border-white bg-violet-100 text-2xl font-bold text-violet-700 shadow-sm">
                  {initials}
                </div>

                <div className="mt-4 flex flex-wrap items-center justify-between gap-4">

                  <div>
                    <h2 className="text-2xl font-bold text-slate-900">
                      {profile.fullName}
                    </h2>

                    <p className="mt-1 text-sm text-slate-500">
                      @{profile.username}
                    </p>

                    <div className="mt-3 flex flex-wrap gap-2">
                      {profile.roles.map((role) => (
                        <span
                          key={role}
                          className="rounded-lg bg-violet-50 px-3 py-1.5 text-xs font-semibold text-violet-700"
                        >
                          {ROLE_LABELS[role] || role}
                        </span>
                      ))}
                    </div>
                  </div>

                  <span
                    className={`rounded-full px-4 py-2 text-xs font-semibold ${
                      profile.status === 'ACTIVE'
                        ? 'bg-emerald-50 text-emerald-700'
                        : 'bg-slate-100 text-slate-600'
                    }`}
                  >
                    {profile.status === 'ACTIVE'
                      ? 'Đang hoạt động / Active'
                      : profile.status}
                  </span>
                </div>
              </div>
            </section>

            {/* Account details */}
            <section className="mt-6 rounded-3xl border border-violet-100 bg-white p-6 shadow-sm sm:p-8">

              <h2 className="text-lg font-bold text-slate-900">
                Thông tin tài khoản
              </h2>

              <p className="mt-1 text-sm text-slate-400">
                Account Information
              </p>

              <div className="mt-6 grid gap-4 sm:grid-cols-2">

                <InfoItem
                  icon={<UserRound size={20} />}
                  label="Họ và tên / Full name"
                  value={profile.fullName}
                />

                <InfoItem
                  icon={<AtSign size={20} />}
                  label="Tên đăng nhập / Username"
                  value={profile.username}
                />

                <InfoItem
                  icon={<Mail size={20} />}
                  label="Email"
                  value={profile.email}
                />

                <InfoItem
                  icon={<Building2 size={20} />}
                  label="Thương hiệu / Brand"
                  value={
                    profile.tenantName ||
                    profile.tenantCode ||
                    'ScentStation Platform'
                  }
                />

                <InfoItem
                  icon={<Building2 size={20} />}
                  label="Mã thương hiệu / Tenant code"
                  value={profile.tenantCode || 'Không áp dụng'}
                />

                <InfoItem
                  icon={<ShieldCheck size={20} />}
                  label="Vai trò / Roles"
                  value={profile.roles
                    .map((role) => ROLE_LABELS[role] || role)
                    .join(', ')}
                />
              </div>

              <p className="mt-6 text-xs leading-relaxed text-slate-400">
                Thông tin tài khoản được quản lý bởi quản trị viên.
                Vui lòng liên hệ quản trị viên nếu cần cập nhật.
              </p>
            </section>

            {/* Account security */}
            <section className="mt-6 rounded-3xl border border-violet-100 bg-white p-6 shadow-sm sm:p-8">

              <h2 className="text-lg font-bold text-slate-900">
                Bảo mật tài khoản
              </h2>

              <p className="mt-1 text-sm text-slate-400">
                Account Security
              </p>

              <div className="mt-6 flex flex-wrap items-center justify-between gap-4 rounded-2xl bg-[#F8F7FC] p-4">

                <div className="flex items-center gap-4">

                  <div className="rounded-xl bg-violet-100 p-3 text-violet-600">
                    <LockKeyhole size={22} />
                  </div>

                  <div>
                    <p className="font-semibold text-slate-800">
                      Mật khẩu
                    </p>

                    <p className="mt-1 text-xs text-slate-500">
                      Thay đổi mật khẩu để bảo vệ tài khoản.
                    </p>
                  </div>
                </div>

                <button
                  type="button"
                  onClick={() => navigate('/change-password')}
                  className="rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-violet-700"
                >
                  Đổi mật khẩu
                </button>
              </div>

              <button
                type="button"
                onClick={handleLogout}
                className="mt-5 flex items-center gap-2 rounded-xl px-4 py-3 text-sm font-medium text-red-600 hover:bg-red-50"
              >
                <LogOut size={18} />
                Đăng xuất khỏi tài khoản
              </button>
            </section>
          </>
        )}

        <p className="mt-8 text-center text-xs text-slate-400">
          ScentStation © 2026 · Account Management
        </p>
      </div>
    </div>
  )
}

function InfoItem({
  icon,
  label,
  value,
}: {
  icon: React.ReactNode
  label: string
  value: string
}) {
  return (
    <div className="flex min-w-0 items-center gap-3 rounded-2xl border border-slate-100 bg-slate-50 p-4">

      <div className="shrink-0 rounded-xl bg-white p-2.5 text-violet-600">
        {icon}
      </div>

      <div className="min-w-0">
        <p className="text-xs text-slate-500">
          {label}
        </p>

        <p className="mt-1 break-words text-sm font-semibold text-slate-800">
          {value}
        </p>
      </div>
    </div>
  )
}