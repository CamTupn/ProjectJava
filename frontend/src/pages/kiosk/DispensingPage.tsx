import { useEffect, useState } from 'react'
import {
  useLocation,
  useNavigate,
  useParams,
} from 'react-router-dom'
import {
  ArrowLeft,
  ArrowRight,
  CheckCircle2,
  Droplets,
  LoaderCircle,
  ShieldCheck,
  Sparkles,
  XCircle,
} from 'lucide-react'

import {
  LanguageSwitcher,
  useKioskLanguage,
} from '../../kiosk/language'

// ==================== TYPES ====================

interface Product {
  id: number
  name: string
  brand: string
  status: 'ACTIVE' | 'INACTIVE'
  stock: number
}

type DispensingStatus =
  | 'PROCESSING'
  | 'SUCCESS'
  | 'FAILED'

// ==================== MOCK DATA ====================

const initialProducts: Product[] = [
  {
    id: 1,
    name: 'Rose Elegance',
    brand: 'ScentStation Demo',
    status: 'ACTIVE',
    stock: 245,
  },
  {
    id: 2,
    name: 'Royal Oud',
    brand: 'ScentStation Demo',
    status: 'ACTIVE',
    stock: 198,
  },
  {
    id: 3,
    name: 'Ocean Breeze',
    brand: 'ScentStation Demo',
    status: 'ACTIVE',
    stock: 156,
  },
  {
    id: 4,
    name: 'Vanilla Bloom',
    brand: 'ScentStation Demo',
    status: 'ACTIVE',
    stock: 132,
  },
  {
    id: 5,
    name: 'White Musk',
    brand: 'ScentStation Demo',
    status: 'INACTIVE',
    stock: 98,
  },
]

// ==================== COMPONENT ====================

export default function DispensingPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { id } = useParams<{ id: string }>()

  const {
    language,
    changeLanguage,
    isEnglish,
  } = useKioskLanguage()

  const t = (vi: string, en: string) =>
    isEnglish ? en : vi

  const product = initialProducts.find(
    (item) => item.id === Number(id)
  )

  // Chỉ cho phép đi tiếp từ bước thanh toán demo
  const demoPaymentConfirmed =
    location.state?.demoPaymentConfirmed === true

  const validProduct =
    !!product &&
    product.status === 'ACTIVE' &&
    product.stock > 0

  const canStart = demoPaymentConfirmed && validProduct

  // ==================== STATES ====================

  const [progress, setProgress] = useState(0)

  const [status, setStatus] =
    useState<DispensingStatus>('PROCESSING')

  // ==================== DEMO PROCESS ====================

  useEffect(() => {
    if (!canStart || status !== 'PROCESSING') {
      return
    }

    const timer = window.setInterval(() => {
      setProgress((current) =>
        Math.min(current + 2, 100)
      )
    }, 120)

    return () => window.clearInterval(timer)
  }, [canStart, status])

  useEffect(() => {
    if (progress >= 100 && status === 'PROCESSING') {
      setStatus('SUCCESS')
    }
  }, [progress, status])

  // ==================== HELPERS ====================

  const getProgressMessage = () => {
    if (progress < 30) {
      return t(
        'Đang chuẩn bị hệ thống...',
        'Preparing the system...'
      )
    }

    if (progress < 65) {
      return t(
        'Đang xử lý yêu cầu...',
        'Processing your request...'
      )
    }

    if (progress < 100) {
      return t(
        'Đang mô phỏng quá trình xịt...',
        'Simulating fragrance dispensing...'
      )
    }

    return t(
      'Mô phỏng hoàn tất!',
      'Simulation completed!'
    )
  }

  // ==================== INVALID ACCESS ====================

  if (!canStart) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center bg-[#faf7f9] px-6 text-center">

        <XCircle
          size={70}
          className="mb-6 text-purple-300"
        />

        <h1 className="mb-4 text-3xl font-bold text-[#30253b]">
          {t(
            'Không thể bắt đầu trải nghiệm',
            'Unable to Start Experience'
          )}
        </h1>

        <p className="mb-8 max-w-lg text-gray-500">
          {t(
            'Vui lòng chọn sản phẩm và hoàn thành bước thanh toán mô phỏng trước khi tiếp tục.',
            'Please select a fragrance and complete the demo payment step before continuing.'
          )}
        </p>

        <button
          onClick={() => navigate('/kiosk/fragrances')}
          className="flex items-center gap-3 rounded-xl bg-[#332346] px-8 py-4 font-semibold text-white"
        >
          <ArrowLeft size={20} />

          {t(
            'Quay lại danh sách',
            'Back to Collection'
          )}
        </button>

      </div>
    )
  }

  // ==================== MAIN ====================

  return (
    <div className="min-h-screen bg-[#faf7f9] text-[#30253b]">

      {/* ================= HEADER ================= */}

      <header className="border-b border-purple-100 bg-white">

        <div className="mx-auto flex max-w-7xl items-center justify-between gap-3 px-6 py-5">

          <div className="flex items-center gap-2">
            <Sparkles
              size={26}
              className="text-purple-600"
            />

            <span className="text-xl font-bold sm:text-2xl">
              ScentStation
            </span>
          </div>

          <LanguageSwitcher
            language={language}
            onChange={changeLanguage}
          />

        </div>

      </header>

      {/* ================= CONTENT ================= */}

      <main className="mx-auto max-w-4xl px-6 py-12">

        <div className="mb-10 text-center">

          <p className="mb-3 text-sm font-semibold uppercase tracking-[0.3em] text-purple-500">
            {t(
              'TRẢI NGHIỆM NƯỚC HOA',
              'FRAGRANCE EXPERIENCE'
            )}
          </p>

          <h1 className="mb-4 text-4xl font-bold md:text-5xl">
            {status === 'PROCESSING'
              ? t(
                  'Đang chuẩn bị mùi hương',
                  'Preparing Your Fragrance'
                )
              : status === 'SUCCESS'
                ? t(
                    'Trải nghiệm mô phỏng hoàn tất!',
                    'Demo Experience Completed!'
                  )
                : t(
                    'Mô phỏng gặp sự cố',
                    'Simulation Failed'
                  )}
          </h1>

          <p className="text-gray-500">
            {t(
              'Vui lòng theo dõi trạng thái trên màn hình.',
              'Please follow the status on the screen.'
            )}
          </p>

        </div>

        {/* ================= STATUS CARD ================= */}

        <div className="rounded-[32px] border border-purple-100 bg-white p-8 shadow-sm md:p-12">

          {/* ================= ANIMATION ================= */}

          <div className="mb-10 flex justify-center">

            <div className="relative flex h-56 w-56 items-center justify-center">

              {status === 'PROCESSING' && (
                <>
                  <div className="absolute inset-0 animate-ping rounded-full bg-purple-100 opacity-30" />

                  <div className="absolute inset-5 animate-pulse rounded-full bg-purple-100" />
                </>
              )}

              <div
                className={`relative flex h-40 w-40 items-center justify-center rounded-full ${
                  status === 'SUCCESS'
                    ? 'bg-green-100'
                    : status === 'FAILED'
                      ? 'bg-red-100'
                      : 'bg-gradient-to-br from-purple-100 to-pink-100'
                }`}
              >

                {status === 'PROCESSING' && (
                  <Droplets
                    size={85}
                    strokeWidth={1.3}
                    className="animate-pulse text-purple-600"
                  />
                )}

                {status === 'SUCCESS' && (
                  <CheckCircle2
                    size={85}
                    strokeWidth={1.5}
                    className="text-green-600"
                  />
                )}

                {status === 'FAILED' && (
                  <XCircle
                    size={85}
                    strokeWidth={1.5}
                    className="text-red-600"
                  />
                )}

              </div>

            </div>

          </div>

          {/* ================= PRODUCT INFO ================= */}

          <div className="mb-8 text-center">

            <p className="mb-2 text-sm uppercase tracking-widest text-purple-500">
              {product.brand}
            </p>

            <h2 className="mb-3 text-3xl font-bold">
              {product.name}
            </h2>

            <p className="text-gray-500">
              {t(
                'Số lần xịt mô phỏng: 1',
                'Simulated sprays: 1'
              )}
            </p>

          </div>

          {/* ================= PROGRESS ================= */}

          {status === 'PROCESSING' && (
            <div className="mb-8">

              <div className="mb-4 flex items-center justify-between">

                <span className="flex items-center gap-2 font-medium text-purple-700">
                  <LoaderCircle
                    size={19}
                    className="animate-spin"
                  />

                  {t(
                    'Đang xử lý mô phỏng',
                    'Simulation in Progress'
                  )}
                </span>

                <span className="font-bold text-purple-700">
                  {progress}%
                </span>

              </div>

              <div className="h-4 overflow-hidden rounded-full bg-purple-100">

                <div
                  className="h-full rounded-full bg-gradient-to-r from-purple-600 to-pink-500 transition-all duration-200"
                  style={{ width: `${progress}%` }}
                />

              </div>

              <p
                className="mt-5 text-center text-sm text-gray-500"
                aria-live="polite"
              >
                {getProgressMessage()}
              </p>

            </div>
          )}

          {/* ================= SUCCESS ================= */}

          {status === 'SUCCESS' && (
            <div className="mb-8 rounded-2xl bg-green-50 p-6 text-center">

              <div className="mb-3 flex items-center justify-center gap-2 text-green-700">
                <CheckCircle2 size={23} />

                <span className="text-lg font-bold">
                  {t(
                    'Mô phỏng thành công',
                    'Simulation Successful'
                  )}
                </span>
              </div>

              <p className="text-sm leading-relaxed text-green-800">
                {t(
                  'Quá trình xịt nước hoa đã được mô phỏng hoàn tất. Máy thực tế chưa thực hiện thao tác xịt.',
                  'The fragrance dispensing simulation is complete. No physical dispensing has occurred.'
                )}
              </p>

            </div>
          )}

          {/* ================= FAILED ================= */}

          {status === 'FAILED' && (
            <div className="mb-8 rounded-2xl bg-red-50 p-6 text-center">

              <p className="font-semibold text-red-700">
                {t(
                  'Đã xảy ra lỗi trong quá trình mô phỏng.',
                  'An error occurred during the simulation.'
                )}
              </p>

            </div>
          )}

          {/* ================= SAFETY NOTE ================= */}

          <div className="mb-8 flex items-start gap-3 rounded-2xl bg-purple-50 p-5">

            <ShieldCheck
              size={24}
              className="shrink-0 text-purple-600"
            />

            <p className="text-sm leading-relaxed text-gray-600">
              {t(
                'Đây là bản demo Frontend. Hệ thống chưa kết nối với máy xịt nước hoa và không thực hiện giao dịch thật.',
                'This is a frontend demo. The system is not connected to a physical dispensing machine and does not process real payments.'
              )}
            </p>

          </div>

          {/* ================= ACTION BUTTONS ================= */}

          {status === 'SUCCESS' && (
            <button
              onClick={() =>
                navigate(`/kiosk/result/${product.id}`, {
                  state: {
                    demoCompleted: true,
                  },
                })
              }
              className="flex min-h-16 w-full items-center justify-center gap-3 rounded-2xl bg-[#332346] px-6 py-4 text-lg font-semibold text-white transition hover:bg-[#51376a]"
            >
              {t(
                'Xem kết quả trải nghiệm',
                'View Experience Result'
              )}

              <ArrowRight size={22} />
            </button>
          )}

          {status === 'FAILED' && (
            <button
              onClick={() => {
                setProgress(0)
                setStatus('PROCESSING')
              }}
              className="min-h-16 w-full rounded-2xl bg-[#332346] px-6 py-4 font-semibold text-white"
            >
              {t('Thử lại mô phỏng', 'Retry Simulation')}
            </button>
          )}

          {status === 'PROCESSING' && (
            <div className="text-center text-sm text-gray-400">
              {t(
                'Vui lòng chờ quá trình mô phỏng hoàn tất.',
                'Please wait for the simulation to complete.'
              )}
            </div>
          )}

        </div>

        {/* ================= FOOTER NOTE ================= */}

        <p className="mt-8 text-center text-sm text-gray-400">
          ScentStation · {t(
            'Trải nghiệm hương thơm đẳng cấp',
            'A Premium Fragrance Experience'
          )}
        </p>

      </main>

    </div>
  )
}
