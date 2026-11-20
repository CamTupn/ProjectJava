import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  ArrowLeft,
  ArrowRight,
  CheckCircle2,
  Clock3,
  CreditCard,
  QrCode,
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
  price: number
  status: 'ACTIVE' | 'INACTIVE'
  stock: number
}

// ==================== MOCK DATA ====================

const initialProducts: Product[] = [
  {
    id: 1,
    name: 'Rose Elegance',
    brand: 'ScentStation Demo',
    price: 15000,
    status: 'ACTIVE',
    stock: 245,
  },
  {
    id: 2,
    name: 'Royal Oud',
    brand: 'ScentStation Demo',
    price: 20000,
    status: 'ACTIVE',
    stock: 198,
  },
  {
    id: 3,
    name: 'Ocean Breeze',
    brand: 'ScentStation Demo',
    price: 18000,
    status: 'ACTIVE',
    stock: 156,
  },
  {
    id: 4,
    name: 'Vanilla Bloom',
    brand: 'ScentStation Demo',
    price: 30000,
    status: 'ACTIVE',
    stock: 132,
  },
  {
    id: 5,
    name: 'White Musk',
    brand: 'ScentStation Demo',
    price: 12000,
    status: 'INACTIVE',
    stock: 98,
  },
]

// ==================== HELPERS ====================

const formatPrice = (price: number) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)

// ==================== COMPONENT ====================

export default function PaymentPage() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()

  const {
    language,
    changeLanguage,
    isEnglish,
  } = useKioskLanguage()

  const [paymentSuccess, setPaymentSuccess] = useState(false)

  const product = initialProducts.find(
    (item) => item.id === Number(id)
  )

  const t = (vi: string, en: string) =>
    isEnglish ? en : vi

  // ==================== INVALID PRODUCT ====================

  if (
    !product ||
    product.status !== 'ACTIVE' ||
    product.stock <= 0
  ) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center bg-[#faf7f9] px-6 text-center">

        <XCircle
          size={65}
          className="mb-6 text-purple-300"
        />

        <h1 className="mb-3 text-3xl font-bold text-[#30253b]">
          {t(
            'Sản phẩm không khả dụng',
            'Fragrance Unavailable'
          )}
        </h1>

        <p className="mb-8 text-gray-500">
          {t(
            'Vui lòng chọn một sản phẩm khác.',
            'Please choose another fragrance.'
          )}
        </p>

        <button
          onClick={() => navigate('/kiosk/fragrances')}
          className="rounded-xl bg-[#332346] px-8 py-4 font-semibold text-white"
        >
          {t('Quay lại danh sách', 'Back to Collection')}
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

          <button
            onClick={() =>
              navigate(`/kiosk/fragrances/${product.id}`)
            }
            className="flex items-center gap-2 font-medium text-purple-700"
          >
            <ArrowLeft size={20} />
            <span className="hidden sm:inline">
              {t('Quay lại', 'Back')}
            </span>
          </button>

          <div className="flex items-center gap-2">
            <Sparkles
              size={25}
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

      {/* ================= PAGE TITLE ================= */}

      <section className="px-6 pb-10 pt-12 text-center">

        <p className="mb-3 text-sm font-semibold uppercase tracking-[0.25em] text-purple-500">
          {t('BƯỚC THANH TOÁN', 'PAYMENT STEP')}
        </p>

        <h1 className="mb-4 text-4xl font-bold">
          {t('Thanh toán trải nghiệm', 'Experience Payment')}
        </h1>

        <p className="mx-auto max-w-2xl text-gray-500">
          {t(
            'Kiểm tra thông tin và hoàn tất thanh toán để bắt đầu trải nghiệm nước hoa.',
            'Review your order and complete payment to begin your fragrance experience.'
          )}
        </p>

      </section>

      {/* ================= CONTENT ================= */}

      <main className="mx-auto max-w-6xl px-6 pb-16">

        <div className="grid grid-cols-1 items-start gap-8 lg:grid-cols-2">

          {/* ================= ORDER SUMMARY ================= */}

          <div className="rounded-3xl border border-purple-100 bg-white p-7 shadow-sm">

            <div className="mb-7 flex items-center gap-3">
              <CreditCard
                size={25}
                className="text-purple-600"
              />

              <h2 className="text-2xl font-bold">
                {t('Thông tin đơn hàng', 'Order Summary')}
              </h2>
            </div>

            {/* Product */}

            <div className="mb-7 rounded-2xl bg-purple-50 p-6">

              <p className="mb-2 text-sm uppercase tracking-widest text-purple-500">
                {product.brand}
              </p>

              <h3 className="text-2xl font-bold">
                {product.name}
              </h3>

              <p className="mt-2 text-sm text-gray-500">
                {t(
                  'Trải nghiệm nước hoa tại máy Kiosk',
                  'Fragrance experience at the kiosk'
                )}
              </p>

            </div>

            {/* Price Details */}

            <div className="space-y-5">

              <div className="flex justify-between gap-4">
                <span className="text-gray-500">
                  {t('Đơn giá mỗi lần xịt', 'Price per Spray')}
                </span>

                <span className="font-semibold">
                  {formatPrice(product.price)}
                </span>
              </div>

              <div className="flex justify-between gap-4">
                <span className="text-gray-500">
                  {t('Số lần xịt', 'Number of Sprays')}
                </span>

                <span className="font-semibold">
                  1
                </span>
              </div>

              <div className="border-t border-gray-100 pt-5">

                <div className="flex items-center justify-between gap-4">

                  <span className="text-lg font-semibold">
                    {t('Tổng thanh toán', 'Total Payment')}
                  </span>

                  <span className="text-3xl font-bold text-purple-800">
                    {formatPrice(product.price)}
                  </span>

                </div>

              </div>

            </div>

            {/* Note */}

            <div className="mt-8 flex items-start gap-3 rounded-2xl bg-purple-50 p-5">

              <ShieldCheck
                size={23}
                className="shrink-0 text-purple-600"
              />

              <p className="text-sm leading-relaxed text-gray-600">
                {t(
                  'Đây là giao diện thanh toán thử nghiệm. Chưa có giao dịch ngân hàng thật hoặc điều khiển máy xịt.',
                  'This is a demo payment interface. No real bank transaction or dispensing action will occur.'
                )}
              </p>

            </div>

          </div>

          {/* ================= PAYMENT AREA ================= */}

          <div className="rounded-3xl border border-purple-100 bg-white p-7 text-center shadow-sm">

            {!paymentSuccess ? (

              <>
                <div className="mb-5 flex items-center justify-center gap-3">

                  <QrCode
                    size={27}
                    className="text-purple-600"
                  />

                  <h2 className="text-2xl font-bold">
                    {t('Thanh toán QR', 'QR Payment')}
                  </h2>

                </div>

                <p className="mb-7 text-sm leading-relaxed text-gray-500">
                  {t(
                    'Khu vực hiển thị mã QR khi tích hợp cổng thanh toán.',
                    'The payment QR code will appear here after payment integration.'
                  )}
                </p>

                {/* QR Placeholder */}

                <div className="mx-auto mb-7 flex h-64 w-64 flex-col items-center justify-center rounded-3xl border-2 border-dashed border-purple-200 bg-purple-50">

                  <QrCode
                    size={100}
                    strokeWidth={1.3}
                    className="mb-4 text-purple-300"
                  />

                  <span className="text-sm font-semibold text-purple-500">
                    {t(
                      'QR DEMO - CHƯA THANH TOÁN',
                      'QR DEMO - NO PAYMENT'
                    )}
                  </span>

                </div>

                {/* Waiting Status */}

                <div className="mb-7 flex items-center justify-center gap-2 rounded-xl bg-amber-50 px-4 py-4 text-amber-700">

                  <Clock3 size={20} />

                  <span className="font-medium">
                    {t(
                      'Đang chờ thanh toán (mô phỏng)',
                      'Awaiting Payment (Demo)'
                    )}
                  </span>

                </div>

                {/* Demo Button */}

                <button
                  onClick={() => setPaymentSuccess(true)}
                  className="flex min-h-16 w-full items-center justify-center gap-3 rounded-2xl bg-[#332346] px-6 py-4 text-base font-semibold text-white transition hover:bg-[#51376a]"
                >
                  {t(
                    'Mô phỏng thanh toán thành công',
                    'Simulate Successful Payment'
                  )}

                  <ArrowRight size={21} />
                </button>

                <p className="mt-4 text-xs text-gray-400">
                  {t(
                    'Chỉ dùng để kiểm tra giao diện Frontend.',
                    'For frontend testing only.'
                  )}
                </p>
              </>

            ) : (

              <>
                {/* ================= SUCCESS ================= */}

                <div className="mx-auto mb-6 flex h-24 w-24 items-center justify-center rounded-full bg-green-100">
                  <CheckCircle2
                    size={55}
                    className="text-green-600"
                  />
                </div>

                <h2 className="mb-4 text-3xl font-bold">
                  {t(
                    'Thanh toán mô phỏng thành công!',
                    'Demo Payment Successful!'
                  )}
                </h2>

                <p className="mb-8 text-gray-500">
                  {t(
                    'Trạng thái này chỉ được xác nhận trong bản demo, chưa có giao dịch thật.',
                    'This confirmation is simulated. No real transaction has been made.'
                  )}
                </p>

                <div className="mb-8 rounded-2xl bg-green-50 p-6">

                  <p className="mb-2 text-sm text-green-700">
                    {t(
                      'Số tiền mô phỏng',
                      'Simulated Payment Amount'
                    )}
                  </p>

                  <p className="text-3xl font-bold text-green-700">
                    {formatPrice(product.price)}
                  </p>

                </div>

                <button
                  onClick={() =>
                    navigate(`/kiosk/dispensing/${product.id}`, {
                      state: {
                        demoPaymentConfirmed: true,
                      },
                    })
                  }
                  className="flex min-h-16 w-full items-center justify-center gap-3 rounded-2xl bg-[#332346] px-6 py-4 text-lg font-semibold text-white transition hover:bg-[#51376a]"
                >
                  {t(
                    'Tiếp tục trải nghiệm mô phỏng',
                    'Continue Demo Experience'
                  )}

                  <ArrowRight size={22} />
                </button>

              </>

            )}

          </div>

        </div>

        {/* ================= BACK TO CATALOG ================= */}

        <div className="mt-9 text-center">

          <button
            onClick={() => navigate('/kiosk/fragrances')}
            className="font-medium text-purple-700 hover:underline"
          >
            {t(
              'Chọn nước hoa khác',
              'Choose Another Fragrance'
            )}
          </button>

        </div>

      </main>

      {/* ================= FOOTER ================= */}

      <footer className="border-t border-purple-100 bg-white py-6 text-center">

        <p className="text-sm text-gray-400">
          ScentStation · {t(
            'Trải nghiệm hương thơm đẳng cấp',
            'A Premium Fragrance Experience'
          )}
        </p>

      </footer>

    </div>
  )
}
