# TASK-025 — Order & Dispense Workflow

## 1. Mục tiêu & Phạm vi
Module quản lý đơn hàng trải nghiệm nước hoa (`orders`) và điều phối nghiệp vụ từ lúc khách hàng chọn nước hoa tại Kiosk đến khi thanh toán, kích hoạt phun nước hoa và hoàn tất/đối soát đơn hàng.

- `OrderService` chỉ đóng vai trò **điều phối nghiệp vụ**, không triển khai lại Payment, Dispense hoặc Inventory.
- Giao tiếp với các module khác thông qua `contract/` (`OrderPort`, `OrderSnapshot`, `DispensePort`, `InventoryPort`, `PaymentResultEvent`, `DispenseResultEvent`).
- Không chỉnh sửa `SecurityConfig` hay các file thuộc nhiệm vụ của thành viên khác.

---

## 2. Cấu trúc Module
Package gốc: `backend/src/main/java/com/scentstation/`

| File | Trách nhiệm |
|---|---|
| `entity/Order.java` | Mapping bảng `orders` và các hằng số trạng thái |
| `repository/OrderRepository.java` | Truy vấn đơn hàng, khóa bi quan (`PESSIMISTIC_WRITE`), tìm đơn hết hạn |
| `service/OrderService.java` | Nghiệp vụ tạo đơn, quản lý trạng thái, điều phối Payment/Dispense/Inventory, implement `OrderPort` |
| `service/KioskCatalogService.java` | Lấy danh sách sản phẩm khả dụng trên máy Kiosk |
| `service/OrderEventHandler.java` | Consumer nhận `PaymentResultEvent` và `DispenseResultEvent` theo `AFTER_COMMIT` |
| `service/OrderExpiryJob.java` | Job định kỳ chuyển các đơn `PENDING_PAYMENT` quá hạn sang `EXPIRED` |
| `security/KioskTokenService.java` | Sinh và xác thực JWT token phiên giao dịch Kiosk gắn với `orderId` và `machineId` |
| `controller/OrderController.java` | API quản trị đơn hàng (`/api/v1/orders`) |
| `controller/KioskController.java` | API Kiosk công khai (`/api/v1/kiosks/**`) |
| `mapper/OrderMapper.java` | Chuyển đổi `Order` sang `OrderResponse`, `CreateOrderResponse`, `OrderSnapshot` |
| `dto/request/CreateOrderRequest.java` | Request tạo đơn hàng từ Kiosk |
| `dto/response/OrderResponse.java` | DTO đơn hàng cho Management API (có `tenantId`) |
| `dto/response/KioskProductResponse.java` | DTO sản phẩm khả dụng tại Kiosk (không có `tenantId`) |
| `dto/response/CreateOrderResponse.java` | DTO đơn hàng Kiosk kèm `accessToken` (không có `tenantId`) |
| `contract/OrderPort.java` | Interface đọc thông tin đơn hàng cho các module khác |
| `contract/OrderSnapshot.java` | DTO contract bất biến đại diện cho đơn hàng |
| `contract/DispensePort.java` | Contract điều phối lệnh phun sang Dispense module |
| `contract/InventoryPort.java` | Contract trừ tồn kho sang Inventory module |
| `contract/PaymentResultEvent.java` | Event kết quả thanh toán |
| `contract/DispenseResultEvent.java` | Event kết quả phun từ thiết bị |

---

## 3. Trạng thái Đơn hàng & Quy tắc Concurrency
### Luồng trạng thái
- `PENDING_PAYMENT` -> `PAID` -> `DISPENSING` -> `DISPENSED` (khi phun `SUCCESS` và trừ tồn kho thành công)
- `PENDING_PAYMENT` -> `EXPIRED` (khi quá `expiresAt` bởi `OrderExpiryJob` hoặc event `EXPIRED`)
- `DISPENSING` -> `FAILED` (khi thiết bị báo `FAILED` hoặc không đủ điều kiện an toàn trước khi phun)
- `DISPENSING` / `EXPIRED` -> `REVIEW` (khi nhận thanh toán muộn, sai số tiền/tiền tệ, kết quả phun `UNCERTAIN`, lỗi gửi lệnh IoT, hoặc lỗi trừ tồn kho sau khi đã phun)

### Đảm bảo Transaction & Concurrency
1. **Khóa bi quan khi đổi trạng thái:** `OrderRepository.findByIdForUpdate(id)` sử dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)`.
2. **Không giữ transaction khi chờ MQTT:** `processPaymentResult` cập nhật `PENDING_PAYMENT -> PAID -> DISPENSING` trong transaction `REQUIRES_NEW` và commit trước; sau đó `dispatchToIoT` gọi `DispensePort` bên ngoài transaction.
3. **Consumer `AFTER_COMMIT` với transaction mới:** `OrderEventHandler` dùng `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)`, gọi các method `@Transactional(propagation = Propagation.REQUIRES_NEW)` của `OrderService`.
4. **Idempotency & chống phun trùng:** Event thanh toán/phun trùng hoặc đến sai trạng thái bị bỏ qua an toàn; không bao giờ tạo lệnh phun thứ hai cho cùng `orderId` và không tự động xịt lại khi `FAILED`/`UNCERTAIN`.
5. **Lưu vết đối soát:** Các sự cố bất thường (thanh toán muộn, `UNCERTAIN`, lỗi trừ kho) chuyển đơn sang `REVIEW` để vận hành đối soát. Lưu ý: event in-memory của Spring không đảm bảo delivery nếu Backend crash giữa chừng; dữ liệu trạng thái đã lưu trong DB phục vụ đối soát thủ công.

---

## 4. Phân quyền (RBAC) & Bảo mật Kiosk
- **Management API (`/api/v1/orders`):**
  - `PLATFORM_SUPER_ADMIN`: xem toàn hệ thống.
  - `BRAND_ADMIN`, `OPERATIONS_MANAGER`: chỉ xem đơn hàng thuộc `tenantId` của mình.
  - Các role khác (`TECHNICIAN`, `INVENTORY_STAFF`, v.v.): bị từ chối (`403 Forbidden`).
  - Không có endpoint cho client tự cập nhật `status` đơn hàng.
- **Public Kiosk API (`/api/v1/kiosks/**`):**
  - Chỉ cho phép xem catalog khả dụng, tạo đơn hàng mới, và xem chi tiết đơn hàng khi có header `X-Kiosk-Token` hợp lệ khớp `orderId` và `machineId`.
  - Mọi response của Kiosk đều **không** trả về `tenantId`.

---

## 5. API Request / Response Mẫu

### 5.1 Lấy Catalog khả dụng tại Kiosk
`GET /api/v1/kiosks/10/products`

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Kiosk catalog retrieved successfully",
  "data": [
    {
      "slotId": 20,
      "slotNumber": 1,
      "productId": 30,
      "name": "Ocean Mist",
      "description": "Fresh marine fragrance",
      "fragranceNotes": "Sea salt, bergamot, cedar",
      "imageUrl": "https://cdn.example.com/ocean-mist.png",
      "price": 15000.00,
      "currency": "VND",
      "volumePerSprayMl": 2.00,
      "available": true
    }
  ],
  "errors": {},
  "timestamp": "2026-11-27T18:00:00"
}
```

### 5.2 Tạo Order từ Kiosk
`POST /api/v1/kiosks/orders`

**Request:**
```json
{
  "machineId": 10,
  "slotId": 20,
  "productId": 30
}
```

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 100,
    "machineId": 10,
    "slotId": 20,
    "productId": 30,
    "productName": "Ocean Mist",
    "unitPrice": 15000.00,
    "currency": "VND",
    "status": "PENDING_PAYMENT",
    "expiresAt": "2026-11-27T18:10:00",
    "createdAt": "2026-11-27T18:00:00",
    "updatedAt": "2026-11-27T18:00:00",
    "accessToken": "<kiosk-jwt-token>"
  },
  "errors": {},
  "timestamp": "2026-11-27T18:00:00"
}
```

### 5.3 Xem Order tại Kiosk
`GET /api/v1/kiosks/orders/100`
Header: `X-Kiosk-Token: <kiosk-jwt-token>`

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Order retrieved successfully",
  "data": {
    "id": 100,
    "machineId": 10,
    "slotId": 20,
    "productId": 30,
    "productName": "Ocean Mist",
    "unitPrice": 15000.00,
    "currency": "VND",
    "status": "PAID",
    "expiresAt": "2026-11-27T18:10:00",
    "createdAt": "2026-11-27T18:00:00",
    "updatedAt": "2026-11-27T18:01:00",
    "accessToken": null
  },
  "errors": {},
  "timestamp": "2026-11-27T18:01:00"
}
```

### 5.4 Danh sách Order (Management API)
`GET /api/v1/orders?machineId=10&status=PAID`
Header: `Authorization: Bearer <staff-jwt-token>`

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": [
    {
      "id": 100,
      "tenantId": 1,
      "machineId": 10,
      "slotId": 20,
      "productId": 30,
      "productName": "Ocean Mist",
      "unitPrice": 15000.00,
      "currency": "VND",
      "status": "PAID",
      "expiresAt": "2026-11-27T18:10:00",
      "createdAt": "2026-11-27T18:00:00",
      "updatedAt": "2026-11-27T18:01:00"
    }
  ],
  "errors": {},
  "timestamp": "2026-11-27T18:01:00"
}
```
