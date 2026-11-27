# TASK-024 — Payment Integration

## Phạm vi

Module payment mô phỏng cho Kiosk, lưu giao dịch trong bảng `payments`, quản lý
trạng thái callback và phát `PaymentNotice`. Module không cập nhật `Order`, không
tạo `DispenseCommand`, không trừ tồn kho và không gọi gateway thật.

## Thành phần

- `Payment` / `PaymentStatus` và `PaymentRepository` ánh xạ bảng payment.
- `PaymentService` tạo payment từ giá/currency của Order và xử lý callback idempotent.
- `PaymentProvider` / `MockPaymentProvider` là abstraction cho provider MVP.
- `PaymentController` cung cấp Kiosk payment và management read API.
- `MockPaymentController` chỉ được tạo trong profile `demo`.
- `PaymentPort`, `PaymentView`, `PaymentNotice` là contract cho TASK-025.

## API

Kiosk request phải có header `X-Order-Token`; token được xác thực bởi
`KioskTokenService`. Token sai hoặc order không tồn tại trả 404.

```http
POST /api/v1/orders/{orderId}/payment-sessions
POST /api/v1/orders/{orderId}/payments
GET  /api/v1/orders/{orderId}/payment
GET  /api/v1/orders/{orderId}/payments
```

POST không cần request body. Amount và currency luôn lấy từ Order ở backend.

Management API:

```http
GET /api/v1/payments?orderId={id}&status={status}
GET /api/v1/payments/{id}
```

Management roles là `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN` và
`OPERATIONS_MANAGER`; dữ liệu non-platform được giới hạn theo tenant.

Mock callback chỉ hoạt động khi bật profile `demo` và chỉ cho
`PLATFORM_SUPER_ADMIN` hoặc `BRAND_ADMIN`:

```http
POST /api/v1/payments/callback
Content-Type: application/json

{
  "providerReference": "MOCK-...",
  "orderId": 10,
  "amount": 15000.00,
  "currency": "VND",
  "status": "SUCCESS",
  "gatewayTransactionId": "GW-001",
  "paidAt": "2026-11-27T10:00:00",
  "signature": "demo-signature"
}
```

## Idempotency và validation

- Mỗi payment reference và gateway transaction ID là duy nhất.
- Payment đang `PENDING` được trả lại khi Kiosk gọi tạo lại.
- Callback kiểm tra order, amount, currency và gateway transaction ID.
- Callback lặp trên payment terminal không ghi đè `SUCCESS` và không phát event mới.
- Callback đến sau khi Order hết hạn không làm payment chuyển trạng thái.
- Payment status chỉ được chuyển bởi callback đã được provider adapter xác nhận.

## Postman

1. Chạy backend với profile `demo` và cấu hình `JWT_SECRET` cùng
   `app.kiosk.token-secret`.
2. Tạo Order đang ở `PENDING_PAYMENT` và tạo token kiosk bằng
   `KioskTokenService.issue(orderId)` trong flow TASK-025.
3. Gửi POST payment-session với `X-Order-Token`, không có body.
4. Dùng `paymentRef` trong response để gửi callback SUCCESS hoặc FAILED.
5. Gửi lại callback để kiểm tra idempotency; đổi amount/currency hoặc gateway ID
   để kiểm tra conflict.
6. Tắt profile `demo` và xác nhận route `/api/v1/payments/callback` không tồn tại.
