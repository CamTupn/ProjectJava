# Data Dictionary — ScentStation Platform

**Task:** TASK-004 — ERD & MySQL Data Model  
**Phiên bản:** v1.0 (bản dự thảo review)
**Hệ quản trị CSDL:** MySQL 8.0+  
** 1. Quy ước:**  
- `PK`: khóa chính; `FK`: khóa ngoại; `UQ`: duy nhất; `NN`: `NOT NULL`; `AI`: `AUTO_INCREMENT`.
- ID và FK dùng `BIGINT UNSIGNED`; tiền dùng `DECIMAL(12,2)`; thể tích dùng `DECIMAL(8,2)` (ml).
- `DATETIME` nên lưu theo một quy ước múi giờ thống nhất, khuyến nghị UTC.
- Các trạng thái được liệt kê là giá trị đề xuất; cần thống nhất trước khi triển khai.
- FK đơn lẻ không bảo đảm máy, khe, sản phẩm và tenant thuộc cùng phạm vi. Backend cần xác minh hoặc thiết kế composite FK.

## 2. Danh mục bảng

| # | Bảng | Mục đích |
|---:|---|---|
| 1 | `tenants` | Đơn vị thương hiệu/tenant |
| 2 | `users` | Tài khoản quản trị và vận hành |
| 3 | `roles` | Danh mục vai trò |
| 4 | `user_roles` | Gán vai trò cho người dùng |
| 5 | `locations` | Địa điểm đặt máy |
| 6 | `products` | Danh mục nước hoa |
| 7 | `machines` | Máy/kiosk IoT |
| 8 | `machine_slots` | Khe chứa nước hoa |
| 9 | `orders` | Đơn trải nghiệm |
| 10 | `payments` | Các lần thử thanh toán |
| 11 | `dispense_commands` | Lệnh điều khiển phun |
| 12 | `dispense_executions` | Kết quả thực thi lệnh |
| 13 | `inventory_transactions` | Lịch sử biến động tồn kho |
| 14 | `refills` | Lịch sử nạp nước hoa |
| 15 | `sensor_readings` | Số đo cảm biến |
| 16 | `device_events` | Sự kiện thiết bị |
| 17 | `alerts` | Cảnh báo cần xử lý |
| 18 | `maintenance_tickets` | Phiếu bảo trì |
| 19 | `maintenance_logs` | Nhật ký xử lý bảo trì |
| 20 | `audit_logs` | Nhật ký kiểm toán |
| 21 | `notifications` | Thông báo người dùng |

## 3. Chi tiết bảng

### 3.1 Bảng `tenants`   
Lưu đơn vị thương hiệu, làm ranh giới dữ liệu đa tenant.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |  
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID tenant |
| `name` | VARCHAR(150) | NN | Tên tenant |
| `code` | VARCHAR(50) | NN, UQ | Mã tenant duy nhất |
| `status` | VARCHAR(20) | NN, DEFAULT 'ACTIVE' | ACTIVE/SUSPENDED/INACTIVE |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo |
| `updated_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời điểm cập nhật |

### 3.2 Bảng `users`
Lưu tài khoản quản trị và nhân viên vận hành.

| Cột | Kiểu dữ liệu| Ràng buộc | Mô tả |  
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID người dùng |
| `tenant_id` | BIGINT UNSIGNED | FK → tenants.id, NULL | Tenant của người dùng; NULL có thể dành cho quản trị nền tảng |
| `username` | VARCHAR(100) | NN, UQ | Tên đăng nhập, đề xuất duy nhất toàn hệ thống |
| `email` | VARCHAR(255) | NN, UQ | Email, đề xuất duy nhất toàn hệ thống |
| `password_hash` | VARCHAR(255) | NN | Chuỗi băm mật khẩu, không lưu mật khẩu thuần |
| `full_name` | VARCHAR(150) | NN | Họ tên |
| `status` | VARCHAR(20) | NN, DEFAULT 'ACTIVE' | ACTIVE/LOCKED/INACTIVE |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo |
| `updated_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời điểm cập nhật |

### 3.3 Bảng `roles` (Quản lý danh mục sản phẩm nước hoa)

Danh mục vai trò RBAC.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID vai trò |
| `code` | VARCHAR(50) | NN, UQ | Mã vai trò |
| `name` | VARCHAR(100) | NN | Tên hiển thị |
| `description` | VARCHAR(255) | NULL | Mô tả |

Mã vai trò gợi ý: `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`, `OPERATIONS_MANAGER`, `TECHNICIAN`, `INVENTORY_STAFF`.

### 3.4 Bảng `user_role` 
Bảng nối quan hệ N–N giữa người dùng và vai trò.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `user_id` | BIGINT UNSIGNED | PK ghép, FK → users.id, NN | Người dùng |
| `role_id` | BIGINT UNSIGNED | PK ghép, FK → roles.id, NN | Vai trò |
| `assigned_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm gán |

PK ghép: (`user_id`, `role_id`).

### 3.5 Bảng `location` 
Địa điểm đặt máy.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID địa điểm |
| `tenant_id` | BIGINT UNSIGNED | FK → tenants.id, NN | Tenant sở hữu |
| `name` | VARCHAR(150) | NN | Tên địa điểm |
| `address` | VARCHAR(255) | NN | Địa chỉ |
| `description` | TEXT | NULL | Mô tả thêm |
| `status` | VARCHAR(20) | NN, DEFAULT 'ACTIVE' | Trạng thái địa điểm |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo |
| `updated_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời điểm cập nhật |

### 3.6 Bảng `products` 
Danh mục sản phẩm nước hoa theo tenant.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID sản phẩm |
| `tenant_id` | BIGINT UNSIGNED | FK → tenants.id, NN | Tenant sở hữu |
| `sku` | VARCHAR(100) | NN, UQ(tenant_id, sku) | Mã SKU duy nhất trong tenant |
| `name` | VARCHAR(150) | NN | Tên sản phẩm |
| `description` | TEXT | NULL | Mô tả |
| `fragrance_notes` | TEXT | NULL | Các nốt hương |
| `image_url` | VARCHAR(500) | NULL | URL ảnh |
| `price` | DECIMAL(12,2) | NN, CHECK >= 0 | Giá trải nghiệm |
| `volume_per_spray_ml` | DECIMAL(8,2) | NN, CHECK > 0 | Thể tích mỗi lần phun chuẩn |
| `status` | VARCHAR(20) | NN, DEFAULT 'ACTIVE' | Trạng thái sản phẩm |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo |
| `updated_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời điểm cập nhật |

### 3.7 Bảng `machines`
Thông tin kiosk và trạng thái kết nối/vận hành.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID máy |
| `tenant_id` | BIGINT UNSIGNED | FK → tenants.id, NN | Tenant sở hữu |
| `location_id` | BIGINT UNSIGNED | FK → locations.id, NULL | Địa điểm lắp đặt |
| `machine_code` | VARCHAR(100) | NN, UQ | Mã máy duy nhất |
| `serial_number` | VARCHAR(100) | NN, UQ | Số serial duy nhất |
| `device_identity` | VARCHAR(150) | NN, UQ | Định danh thiết bị |
| `connection_status` | VARCHAR(20) | NN | ONLINE/OFFLINE |
| `operational_status` | VARCHAR(20) | NN | ACTIVE/MAINTENANCE/DISABLED/ERROR |
| `firmware_version` | VARCHAR(50) | NULL | Phiên bản firmware |
| `last_ping_at` | DATETIME | NULL | Heartbeat gần nhất |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo |
| `updated_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời điểm cập nhật |

### 3.8 Bảng `machine_slots`
Quản lý từng khe nước hoa độc lập trên máy.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID khe |
| `machine_id` | BIGINT UNSIGNED | FK → machines.id, NN | Máy chứa khe |
| `slot_number` | INT UNSIGNED | NN, UQ(machine_id, slot_number) | Số khe duy nhất trong máy |
| `product_id` | BIGINT UNSIGNED | FK → products.id, NULL | Sản phẩm gán vào khe |
| `capacity_ml` | DECIMAL(8,2) | NN, CHECK > 0 | Dung tích tối đa |
| `current_volume_ml` | DECIMAL(8,2) | NN, CHECK 0..capacity_ml | Thể tích ước tính hiện tại |
| `low_stock_threshold_ml` | DECIMAL(8,2) | NN, CHECK >= 0 | Ngưỡng tồn thấp |
| `status` | VARCHAR(20) | NN | AVAILABLE/LOW_STOCK/EMPTY/DISABLED/ERROR |
| `updated_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Thời điểm cập nhật |

### 3.9 Bảng `orders`
Đơn trải nghiệm của khách hàng; lưu snapshot để giữ nguyên tên và giá lúc đặt.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID đơn |
| `order_code` | VARCHAR(100) | NN, UQ | Mã đơn hiển thị duy nhất |
| `tenant_id` | BIGINT UNSIGNED | FK → tenants.id, NN | Tenant cung cấp dịch vụ |
| `machine_id` | BIGINT UNSIGNED | FK → machines.id, NN | Máy tạo đơn |
| `slot_id` | BIGINT UNSIGNED | FK → machine_slots.id, NN | Khe được chọn |
| `product_id` | BIGINT UNSIGNED | FK → products.id, NN | Sản phẩm được chọn |
| `product_name_snapshot` | VARCHAR(150) | NN | Tên sản phẩm tại thời điểm đặt |
| `unit_price` | DECIMAL(12,2) | NN | Giá tại thời điểm đặt |
| `currency` | CHAR(3) | NN, DEFAULT 'VND' | Mã tiền tệ |
| `status` | VARCHAR(30) | NN | PENDING_PAYMENT/PAID/DISPENSING/DISPENSED/FAILED/EXPIRED/REFUNDED/REVIEW |
| `expires_at` | DATETIME | NN | Hạn thanh toán |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo |
| `completed_at` | DATETIME | NULL | Thời điểm hoàn tất |

### 3.10 Bảng `payments`
Lưu nhiều lần thử thanh toán nếu cần.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID thanh toán |
| `order_id` | BIGINT UNSIGNED | FK → orders.id, NN | Đơn liên quan |
| `payment_ref` | VARCHAR(150) | NN, UQ | Mã tham chiếu nội bộ |
| `provider` | VARCHAR(50) | NN | Cổng thanh toán |
| `amount` | DECIMAL(12,2) | NN | Số tiền |
| `currency` | CHAR(3) | NN, DEFAULT 'VND' | Mã tiền tệ |
| `status` | VARCHAR(30) | NN | PENDING/SUCCESS/FAILED/CANCELLED/EXPIRED/REFUNDED |
| `gateway_transaction_id` | VARCHAR(150) | NULL, UQ | Mã giao dịch từ cổng thanh toán |
| `gateway_payload` | JSON | NULL | Payload đã loại bỏ dữ liệu nhạy cảm |
| `paid_at` | DATETIME | NULL | Thời điểm xác nhận thanh toán |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo |

Backend phải xác minh callback từ cổng thanh toán và xử lý callback lặp an toàn; không tin trạng thái từ frontend.

### 3.11 Bảng `dispense_commands`
Lệnh điều khiển phun gửi tới máy.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID lệnh |
| `command_token` | VARCHAR(150) | NN, UQ | Token chống xử lý lệnh trùng |
| `order_id` | BIGINT UNSIGNED | FK → orders.id, NULL | Đơn khách hàng; NULL nếu lệnh chẩn đoán |
| `machine_id` | BIGINT UNSIGNED | FK → machines.id, NN | Máy nhận lệnh |
| `slot_id` | BIGINT UNSIGNED | FK → machine_slots.id, NN | Khe cần phun |
| `volume_ml` | DECIMAL(8,2) | NN | Thể tích yêu cầu phun |
| `status` | VARCHAR(30) | NN | CREATED/SENT/ACKED/EXECUTED/FAILED/EXPIRED/REJECTED/REVIEW |
| `sent_at` | DATETIME | NULL | Thời điểm gửi |
| `acknowledged_at` | DATETIME | NULL | Thời điểm máy xác nhận nhận lệnh |
| `finished_at` | DATETIME | NULL | Thời điểm kết thúc |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo |

**Cần nhóm duyệt:** bổ sung `command_type` (`CUSTOMER_ORDER`/`DIAGNOSTIC`) để phân biệt lệnh bán hàng và lệnh chẩn đoán. Cần ràng buộc tối đa một lệnh khách hàng cho mỗi đơn.

### 3.12 Bảng `dispense_executions`
Kết quả tổng kết thực thi lệnh phun.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID kết quả |
| `command_id` | BIGINT UNSIGNED | FK → dispense_commands.id, NN, UQ | Lệnh tương ứng; tối đa một kết quả tổng kết/lệnh |
| `execution_status` | VARCHAR(30) | NN | SUCCESS/FAILED/UNCERTAIN |
| `error_code` | VARCHAR(100) | NULL | Mã lỗi |
| `hardware_telemetry` | JSON | NULL | Telemetry phục vụ đối soát |
| `recorded_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm ghi nhận |

### 3.13 Bảng `inventory_transactions`
Lịch sử biến động tồn kho.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID giao dịch tồn kho |
| `slot_id` | BIGINT UNSIGNED | FK → machine_slots.id, NN | Khe bị tác động |
| `transaction_type` | VARCHAR(30) | NN | DISPENSE_SALE/DIAGNOSTIC_SPRAY/REFILL/ADJUSTMENT/REMOVAL/DISPOSAL |
| `quantity_ml` | DECIMAL(8,2) | NN | Lượng biến động; cần thống nhất quy ước dấu |
| `quantity_before_ml` | DECIMAL(8,2) | NN | Lượng trước biến động |
| `quantity_after_ml` | DECIMAL(8,2) | NN | Lượng sau biến động |
| `order_id` | BIGINT UNSIGNED | FK → orders.id, NULL | Đơn liên quan nếu có |
| `dispense_execution_id` | BIGINT UNSIGNED | FK → dispense_executions.id, NULL | Kết quả phun liên quan |
| `refill_id` | BIGINT UNSIGNED | FK → refills.id, NULL | Lần nạp liên quan |
| `performed_by_user_id` | BIGINT UNSIGNED | FK → users.id, NULL | Người thao tác nếu có |
| `note` | TEXT | NULL | Ghi chú |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm ghi nhận |

Chỉ lần phun bán hàng được xác nhận thành công mới giảm tồn kho bán được; phun chẩn đoán cần ghi riêng.

### 3.14 Bảng `refills`
Lịch sử nạp nước hoa vào khe.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID lần nạp |
| `slot_id` | BIGINT UNSIGNED | FK → machine_slots.id, NN | Khe được nạp |
| `product_id` | BIGINT UNSIGNED | FK → products.id, NN | Sản phẩm được nạp |
| `quantity_added_ml` | DECIMAL(8,2) | NN | Lượng nạp thêm |
| `quantity_before_ml` | DECIMAL(8,2) | NULL | Lượng trước nạp |
| `quantity_after_ml` | DECIMAL(8,2) | NULL | Lượng sau nạp |
| `performed_by_user_id` | BIGINT UNSIGNED | FK → users.id, NN | Nhân viên thực hiện |
| `status` | VARCHAR(20) | NN | PENDING/COMPLETED/CANCELLED/REVIEW |
| `note` | TEXT | NULL | Ghi chú |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo |
| `completed_at` | DATETIME | NULL | Thời điểm hoàn tất |

### 3.15 Bảng `sensor_readings`
Dữ liệu đo cảm biến từ thiết bị.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID bản ghi đo |
| `machine_id` | BIGINT UNSIGNED | FK → machines.id, NN | Máy gửi dữ liệu |
| `slot_id` | BIGINT UNSIGNED | FK → machine_slots.id, NULL | Khe liên quan nếu có |
| `sensor_type` | VARCHAR(50) | NN | Loại cảm biến |
| `sensor_value` | DECIMAL(12,4) | NN | Giá trị đo |
| `unit` | VARCHAR(20) | NN | Đơn vị đo |
| `status` | VARCHAR(20) | NULL | Trạng thái/chất lượng phép đo |
| `recorded_at` | DATETIME | NN | Thời điểm thiết bị đo |
| `received_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm backend nhận |

### 3.16 Bảng `device_events`
Sự kiện từ thiết bị IoT.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID sự kiện nội bộ |
| `machine_id` | BIGINT UNSIGNED | FK → machines.id, NN | Máy phát sinh sự kiện |
| `event_code` | VARCHAR(100) | NN | Mã sự kiện |
| `severity` | VARCHAR(20) | NN | Mức độ nghiêm trọng |
| `description` | TEXT | NULL | Mô tả |
| `payload` | JSON | NULL | Dữ liệu bổ sung đã kiểm soát |
| `device_event_id` | VARCHAR(150) | NULL | ID sự kiện phía thiết bị nếu có |
| `occurred_at` | DATETIME | NN | Thời điểm sự kiện xảy ra |
| `received_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm backend nhận |

Có thể dùng `UNIQUE(machine_id, device_event_id)` nếu ID sự kiện từ thiết bị ổn định để chống ghi trùng.

### 3.17 Bảng `alerts`
Cảnh báo cần theo dõi/xử lý.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID cảnh báo |
| `tenant_id` | BIGINT UNSIGNED | FK → tenants.id, NN | Tenant sở hữu |
| `machine_id` | BIGINT UNSIGNED | FK → machines.id, NULL | Máy liên quan |
| `slot_id` | BIGINT UNSIGNED | FK → machine_slots.id, NULL | Khe liên quan |
| `device_event_id` | BIGINT UNSIGNED | FK → device_events.id, NULL | Sự kiện nguồn |
| `alert_type` | VARCHAR(50) | NN | Loại cảnh báo |
| `title` | VARCHAR(150) | NN | Tiêu đề |
| `severity` | VARCHAR(20) | NN | Mức độ nghiêm trọng |
| `description` | TEXT | NULL | Chi tiết |
| `status` | VARCHAR(20) | NN | OPEN/ACKNOWLEDGED/IN_PROGRESS/RESOLVED/DISMISSED |
| `assigned_to_user_id` | BIGINT UNSIGNED | FK → users.id, NULL | Người được giao |
| `detected_at` | DATETIME | NN | Thời điểm phát hiện |
| `acknowledged_at` | DATETIME | NULL | Thời điểm xác nhận |
| `resolved_at` | DATETIME | NULL | Thời điểm giải quyết |

### 3.18  Bảng `maintenance_tickets`
Phiếu bảo trì/sửa chữa máy.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID phiếu |
| `tenant_id` | BIGINT UNSIGNED | FK → tenants.id, NN | Tenant sở hữu |
| `machine_id` | BIGINT UNSIGNED | FK → machines.id, NN | Máy cần bảo trì |
| `alert_id` | BIGINT UNSIGNED | FK → alerts.id, NULL | Cảnh báo liên quan |
| `ticket_code` | VARCHAR(100) | NN, UQ | Mã phiếu duy nhất |
| `title` | VARCHAR(150) | NN | Tiêu đề |
| `description` | TEXT | NULL | Mô tả công việc/sự cố |
| `priority` | VARCHAR(20) | NN | LOW/MEDIUM/HIGH/CRITICAL |
| `status` | VARCHAR(20) | NN | OPEN/ASSIGNED/IN_PROGRESS/ON_HOLD/COMPLETED/CANCELLED |
| `reported_by_user_id` | BIGINT UNSIGNED | FK → users.id, NULL | Người báo sự cố |
| `assigned_to_user_id` | BIGINT UNSIGNED | FK → users.id, NULL | Kỹ thuật viên được giao |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo |
| `started_at` | DATETIME | NULL | Thời điểm bắt đầu |
| `completed_at` | DATETIME | NULL | Thời điểm hoàn tất |

### 3.19 Bảng `maintenance_logs`
Nhật ký từng thao tác xử lý bảo trì.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID nhật ký |
| `ticket_id` | BIGINT UNSIGNED | FK → maintenance_tickets.id, NN | Phiếu liên quan |
| `performed_by_user_id` | BIGINT UNSIGNED | FK → users.id, NN | Người thực hiện |
| `action_type` | VARCHAR(50) | NN | Loại thao tác |
| `description` | TEXT | NULL | Công việc đã làm |
| `result` | TEXT | NULL | Kết quả xử lý |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm ghi log |

### 3.20 Bảng `audit_logs`
Nhật ký kiểm toán và truy vết thao tác.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID log |
| `tenant_id` | BIGINT UNSIGNED | FK → tenants.id, NULL | Tenant liên quan |
| `user_id` | BIGINT UNSIGNED | FK → users.id, NULL | Người thao tác; NULL nếu hệ thống thực hiện |
| `action` | VARCHAR(100) | NN | Hành động |
| `entity_type` | VARCHAR(100) | NN | Loại đối tượng bị tác động |
| `entity_id` | BIGINT UNSIGNED | NULL | ID đối tượng; tham chiếu đa hình, không có FK đơn |
| `ip_address` | VARCHAR(45) | NULL | IPv4/IPv6 nếu có |
| `detail` | JSON | NULL | Chi tiết đã loại bỏ dữ liệu bí mật |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm ghi log |

### 3.21 Bảng `notifications`
Thông báo gửi đến người dùng.

| Cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | PK, AI, NN | ID thông báo |
| `tenant_id` | BIGINT UNSIGNED | FK → tenants.id, NN | Tenant của thông báo |
| `user_id` | BIGINT UNSIGNED | FK → users.id, NN | Người nhận |
| `notification_type` | VARCHAR(50) | NN | Loại thông báo |
| `title` | VARCHAR(150) | NN | Tiêu đề |
| `message` | TEXT | NN | Nội dung |
| `reference_type` | VARCHAR(100) | NULL | Loại đối tượng được tham chiếu |
| `reference_id` | BIGINT UNSIGNED | NULL | ID đối tượng tham chiếu đa hình |
| `is_read` | BOOLEAN | NN, DEFAULT FALSE | Đã đọc hay chưa |
| `created_at` | DATETIME | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm tạo |
| `read_at` | DATETIME | NULL | Thời điểm đọc |

## 4. Quan hệ chính

| Bảng cha | Bảng con | Quan hệ |
|---|---|---|
| `tenants` | `users` | 1–N; `users.tenant_id` có thể NULL cho quản trị nền tảng |
| `users`, `roles` | `user_roles` | N–N qua khóa ghép |
| `tenants` | `locations`, `products`, `machines` | 1–N |
| `locations` | `machines` | 1–N |
| `machines` | `machine_slots` | 1–N |
| `products` | `machine_slots` | 1–N |
| `orders` | `payments` | 1–N lần thử thanh toán |
| `orders` | `dispense_commands` | 1–0..1 lệnh phun khách hàng theo quy tắc nghiệp vụ |
| `dispense_commands` | `dispense_executions` | 1–0..1 kết quả tổng kết trong mô hình hiện tại |
| `machine_slots` | `inventory_transactions`, `refills` | 1–N |
| `machines` | `sensor_readings`, `device_events` | 1–N |
| `maintenance_tickets` | `maintenance_logs` | 1–N |

## 5. Quy tắc nghiệp vụ quan trọng

1. Backend kiểm tra quyền và tenant cho mọi thao tác; không tin `tenant_id` do client tự gửi.
2. Không phun nếu máy offline, đang bảo trì, cửa mở, khe rỗng hoặc có lỗi nghiêm trọng.
3. Chỉ backend xác minh thanh toán thành công mới cho phép tạo lệnh phun bán hàng.
4. Mỗi đơn tối đa một lệnh phun khách hàng; token lệnh duy nhất và xử lý thông điệp idempotent.
5. Chỉ giảm tồn kho bán được sau khi xác nhận phun thành công; phun chẩn đoán phải ghi riêng.
6. Nếu kết quả phun `UNCERTAIN`, chuyển sang kiểm tra và không tự động phun lại.
7. Cần xác minh tenant, máy, khe và sản phẩm thuộc đúng ngữ cảnh; FK đơn lẻ chưa đủ bảo đảm điều này.
8. Tránh xóa cứng lịch sử thanh toán, tồn kho và audit nếu làm mất khả năng đối soát.


**Trạng thái:** Bản dự thảo review v1.0; cần đối chiếu với ERD cuối cùng của nhóm trước khi coi là bản chốt.