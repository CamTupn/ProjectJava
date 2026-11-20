import { useNavigate, useParams, useLocation } from 'react-router-dom'
import {
  ArrowRight,
  CheckCircle2,
  XCircle,
  Sparkles,
  Droplets,
  RotateCcw,
  Home,
  ShieldCheck,
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
  category: string
  status: 'ACTIVE' | 'INACTIVE'
}

// ==================== MOCK DATA ====================

const initialProducts: Product[] = [
  {
    id: 1,
    name: 'Rose Elegance',
    brand: 'ScentStation Demo',
    category: 'Floral',
    status: 'ACTIVE',
  },
  {
    id: 2,
    name: 'Royal Oud',
    brand: 'ScentStation Demo',
    category: 'Woody',
    status: 'ACTIVE',
  },
  {
    id: 3,
    name: 'Ocean Breeze',
    brand: 'ScentStation Demo',
    category: 'Fresh',
    status: 'ACTIVE',
  },
  {
    id: 4,
    name: 'Vanilla Bloom',
    brand: 'ScentStation Demo',
    category: 'Gourmand',
    status: 'ACTIVE',
  },
  {
    id: 5,
    name: 'White Musk',
    brand: 'ScentStation Demo',
    category: 'Musk',
    status: 'INACTIVE',
  },
]

// ==================== CATEGORY COLORS ====================

const categoryColors: Record<string, string> = {
  Floral: 'from-pink-100 to-rose-200',
  Woody: 'from-amber-100 to-orange-200',
  Fresh: 'from-cyan-100 to-blue-200',
  Gourmand: 'from-yellow-100 to-amber-200',
  Musk: 'from-purple-100 to-violet-200',
}

// ==================== COMPONENT ====================

export default function ResultPage() {
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

  // ==================== DEMO STATUS ====================

  // Chỉ nhận trạng thái từ luồng mô phỏng trước đó.
  // Không sử dụng để xác minh giao dịch thật.

  const demoCompleted =
    location.state?.demoCompleted === true

  const demoFailed =
    location.state?.demoFailed === true

  const validProduct =
    !!product && product.status === 'ACTIVE'

  const isSuccess = validProduct && demoCompleted
  const isFailed = validProduct && demoFailed && !demoCompleted

  // ==================== INVALID ACCESS ====================

  if (!validProduct || (!isSuccess && !isFailed)) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center bg-[#faf7f9] px-6 text-center">

        <XCircle
          size={70}
          className="mb-6 text-purple-300"
        />

        <h1 className="mb-4 text-3xl font-bold text-[#30253b]">
          {t(
            'Không tìm thấy kết quả trải nghiệm',
            'Experience Result Not Found'
          )}
        </h1>

        <p className="mb-8 max-w-lg text-gray-500">
          {t(
            'Không có thông tin trải nghiệm hợp lệ. Vui lòng bắt đầu lại từ danh sách nước hoa.',
            'No valid experience information was found. Please start again from the fragrance collection.'
          )}
        </p>

        <button
          onClick={() =>
            navigate('/kiosk/fragrances', { replace: true })
          }
          className="flex min-h-14 items-center gap-3 rounded-2xl bg-[#332346] px-8 py-4 font-semibold text-white transition hover:bg-[#51376a]"
        >
          <RotateCcw size={20} />

          {t(
            'Bắt đầu trải nghiệm mới',
            'Start New Experience'
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

      {/* ================= MAIN CONTENT ================= */}

      <main className="mx-auto max-w-4xl px-6 py-12 md:py-16">

        <div className="overflow-hidden rounded-[32px] border border-purple-100 bg-white shadow-lg shadow-purple-100/50">

          {/* ================= STATUS SECTION ================= */}

          <div className="px-6 pb-8 pt-12 text-center md:px-12">

            <p className="mb-7 text-sm font-semibold uppercase tracking-[0.3em] text-purple-500">
              {t(
                'KẾT QUẢ TRẢI NGHIỆM',
                'EXPERIENCE RESULT'
              )}
            </p>

            {/* Status Icon */}

            <div className="mb-8 flex justify-center">

              <div
                className={`flex h-36 w-36 items-center justify-center rounded-full ${
                  isSuccess
                    ? 'bg-green-100'
                    : 'bg-red-100'
                }`}
              >

                {isSuccess ? (
                  <CheckCircle2
                    size={85}
                    strokeWidth={1.4}
                    className="text-green-600"
                  />
                ) : (
                  <XCircle
                    size={85}
                    strokeWidth={1.4}
                    className="text-red-600"
                  />
                )}

              </div>

            </div>

            {/* Title */}

            <h1 className="mb-5 text-3xl font-bold leading-tight md:text-5xl">

              {isSuccess
                ? t(
                    'Trải nghiệm mô phỏng hoàn tất!',
                    'Demo Experience Completed!'
                  )
                : t(
                    'Trải nghiệm mô phỏng thất bại',
                    'Demo Experience Failed'
                  )}

            </h1>

            {/* Description */}

            <p className="mx-auto max-w-xl text-base leading-relaxed text-gray-500 md:text-lg">

              {isSuccess
                ? t(
                    'Cảm ơn bạn đã trải nghiệm ScentStation! Quá trình mô phỏng xịt nước hoa đã hoàn tất thành công.',
                    'Thank you for choosing ScentStation! Your fragrance dispensing simulation has been completed successfully.'
                  )
                : t(
                    'Đã xảy ra sự cố trong quá trình mô phỏng. Vui lòng bắt đầu lại trải nghiệm.',
                    'An issue occurred during the simulation. Please start a new experience.'
                  )}

            </p>

          </div>

          {/* ================= PRODUCT CARD ================= */}

          <div className="mx-6 mb-8 rounded-3xl bg-[#faf7f9] p-6 md:mx-12 md:p-8">

            <div className="flex flex-col items-center gap-6 sm:flex-row">

              {/* Product Illustration */}

              <div
                className={`flex h-36 w-36 shrink-0 items-center justify-center rounded-3xl bg-gradient-to-br ${
                  categoryColors[product.category] ||
                  'from-purple-100 to-pink-100'
                }`}
              >

                <Droplets
                  size={75}
                  strokeWidth={1.2}
                  className="text-purple-700/50"
                />

              </div>

              {/* Product Details */}

              <div className="flex-1 text-center sm:text-left">

                <p className="mb-2 text-xs font-semibold uppercase tracking-[0.2em] text-purple-500">
                  {product.brand}
                </p>

                <h2 className="mb-3 text-2xl font-bold md:text-3xl">
                  {product.name}
                </h2>

                <p className="mb-4 text-gray-500">
                  {t(
                    'Nước hoa đã lựa chọn',
                    'Selected Fragrance'
                  )}
                </p>

                <div className="flex flex-wrap justify-center gap-3 sm:justify-start">

                  <span className="rounded-full bg-white px-4 py-2 text-sm font-medium text-purple-700">
                    {t(
                      'Số lần xịt mô phỏng: 1',
                      'Simulated Sprays: 1'
                    )}
                  </span>

                  <span
                    className={`rounded-full px-4 py-2 text-sm font-semibold ${
                      isSuccess
                        ? 'bg-green-100 text-green-700'
                        : 'bg-red-100 text-red-700'
                    }`}
                  >

                    {isSuccess
                      ? t(
                          'Mô phỏng thành công',
                          'Simulation Successful'
                        )
                      : t(
                          'Mô phỏng thất bại',
                          'Simulation Failed'
                        )}

                  </span>

                </div>

              </div>

            </div>

          </div>

          {/* ================= STATUS DETAILS ================= */}

          <div className="mx-6 mb-8 md:mx-12">

            <div
              className={`rounded-2xl p-6 ${
                isSuccess
                  ? 'bg-green-50'
                  : 'bg-red-50'
              }`}
            >

              <div className="flex items-start gap-3">

                {isSuccess ? (
                  <CheckCircle2
                    size={25}
                    className="shrink-0 text-green-600"
                  />
                ) : (
                  <XCircle
                    size={25}
                    className="shrink-0 text-red-600"
                  />
                )}

                <div>

                  <h3
                    className={`mb-2 font-bold ${
                      isSuccess
                        ? 'text-green-800'
                        : 'text-red-800'
                    }`}
                  >

                    {isSuccess
                      ? t(
                          'Hoàn tất trải nghiệm demo',
                          'Demo Experience Completed'
                        )
                      : t(
                          'Không thể hoàn tất mô phỏng',
                          'Unable to Complete Simulation'
                        )}

                  </h3>

                  <p
                    className={`text-sm leading-relaxed ${
                      isSuccess
                        ? 'text-green-700'
                        : 'text-red-700'
                    }`}
                  >

                    {isSuccess
                      ? t(
                          'Hệ thống đã hoàn thành quá trình mô phỏng. Bạn có thể lựa chọn mùi hương khác để tiếp tục trải nghiệm.',
                          'The simulation has finished. You can select another fragrance to continue exploring.'
                        )
                      : t(
                          'Yêu cầu mô phỏng chưa hoàn tất. Bạn có thể quay lại danh sách để bắt đầu một lượt mới.',
                          'The simulation was not completed. You can return to the collection and start again.'
                        )}

                  </p>

                </div>

              </div>

            </div>

          </div>

          {/* ================= DEMO NOTICE ================= */}

          <div className="mx-6 mb-9 flex items-start gap-3 rounded-2xl border border-purple-100 bg-purple-50 p-5 md:mx-12">

            <ShieldCheck
              size={23}
              className="shrink-0 text-purple-600"
            />

            <p className="text-sm leading-relaxed text-gray-600">
              {t(
                'Đây là kết quả mô phỏng Frontend. Hệ thống chưa kết nối thiết bị thực tế, chưa thực hiện xịt nước hoa và chưa xử lý thanh toán thật.',
                'This is a frontend simulation result. No physical fragrance dispensing or real payment has occurred.'
              )}
            </p>

          </div>

          {/* ================= ACTION BUTTONS ================= */}

          <div className="space-y-4 px-6 pb-12 md:px-12">

            <button
              onClick={() =>
                navigate('/kiosk/fragrances', {
                  replace: true,
                })
              }
              className="flex min-h-16 w-full items-center justify-center gap-3 rounded-2xl bg-[#332346] px-6 py-4 text-lg font-semibold text-white transition hover:bg-[#51376a] active:scale-[0.98]"
            >

              <RotateCcw size={22} />

              {t(
                'Trải nghiệm mùi hương khác',
                'Explore Another Fragrance'
              )}

              <ArrowRight size={20} />

            </button>

            <button
              onClick={() =>
                navigate('/kiosk', {
                  replace: true,
                })
              }
              className="flex min-h-16 w-full items-center justify-center gap-3 rounded-2xl border border-purple-200 bg-white px-6 py-4 text-lg font-semibold text-purple-700 transition hover:bg-purple-50"
            >

              <Home size={21} />

              {t(
                'Về màn hình chính',
                'Return to Home'
              )}

            </button>

          </div>

        </div>

        {/* ================= FOOTER ================= */}

        <p className="mt-9 text-center text-sm text-gray-400">
          ScentStation · {t(
            'Trải nghiệm hương thơm đẳng cấp',
            'A Premium Fragrance Experience'
          )}
        </p>

      </main>

    </div>
  )
}
