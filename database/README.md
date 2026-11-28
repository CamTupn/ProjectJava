# ScentStation Database

## 1. Overview

ScentStation sử dụng MySQL 8.0+ để lưu trữ dữ liệu cho nền tảng quản lý máy trải nghiệm nước hoa IoT.

Database chính: `scentstation`.

## 2. Requirements

- MySQL Server 8.0 trở lên.
- Tài khoản MySQL có quyền tạo database và bảng.
- MySQL client để chạy các file SQL.

## 3. Database Structure

Schema được định nghĩa trong `schema.sql`.

Các nhóm dữ liệu chính:

- Tenant và tài khoản: `tenants`, `users`, `roles`, `user_roles`.
- Sản phẩm và máy IoT: `locations`, `products`, `machines`, `machine_slots`.
- Đơn hàng và thanh toán: `orders`, `payments`.
- Điều khiển xịt: `dispense_commands`, `dispense_executions`.
- Tồn kho: `refills`, `inventory_transactions`.
- Giám sát IoT: `sensor_readings`, `device_events`, `alerts`.
- Bảo trì: `maintenance_tickets`, `maintenance_logs`.
- Quản trị hệ thống: `audit_logs`, `notifications`.

## 4. Initialize Database

Mở PowerShell tại thư mục gốc của repository.

Chạy lệnh sau để tạo database và các bảng:

```powershell
Get-Content ".\database\schema.sql" -Raw | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

Nhập mật khẩu MySQL khi được yêu cầu.
Lưu ý: Nếu chưa cài MySQL Server hoặc chưa tạo tài khoản root, cần thiết lập MySQL trước khi chạy các lệnh này. Nếu máy cài MySQL ở đường dẫn khác, phải chỉnh lại đường dẫn trong lệnh.

## 5. Load Seed Data

Sau khi schema được tạo thành công, chạy:

```powershell
Get-Content ".\database\seed.sql" -Raw | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

Lệnh này nạp tenant demo và các vai trò hệ thống.

Không lưu mật khẩu MySQL thật vào repository.

## 6. Verify Installation

Đăng nhập MySQL rồi chạy:

```sql
USE scentstation;
SHOW TABLES;

SELECT COUNT(*) AS total_tables
FROM information_schema.tables
WHERE table_schema = 'scentstation'
  AND table_type = 'BASE TABLE';

SELECT * FROM tenants;
SELECT * FROM roles;
```

Số lượng bảng dự kiến: 21.

## 7. Important Notes

- Không đưa mật khẩu hoặc thông tin kết nối riêng tư vào Git.
- Không xóa database có dữ liệu cần giữ chỉ để chạy lại schema.
- Các thao tác tạo đơn hàng, thanh toán, xịt nước hoa và cập nhật tồn kho cần được kiểm soát ở backend.
- Chỉ ghi nhận giảm tồn kho bán hàng sau khi kết quả xịt thành công đã được xác nhận.
- Cần đối chiếu schema với ERD và Data Dictionary trước khi nghiệm thu.

## 8. Database Migration — Account Security

Migration file:

`database/migrations/001_account_security.sql`

### Existing database

Đối với thành viên đã có database từ Sprint 4:

1. Dừng backend.
2. Sao lưu database hiện tại.
3. Mở MySQL Workbench.
4. Chọn database cần cập nhật.
5. Kiểm tra cấu trúc bảng users.

```sql
USE scentstation;

DESCRIBE users;
```

Nếu cả hai cột bảo mật chưa tồn tại, thực thi migration:

```sql
ALTER TABLE users
ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE AFTER password_hash,
ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0 AFTER must_change_password;
```

Kiểm tra kết quả:

```sql
SELECT
    id,
    username,
    must_change_password,
    token_version
FROM users;
```

Thực hiện tương tự cho database `scentstation_test` nếu sử dụng integration tests.

Nếu cả hai cột đã tồn tại thì bỏ qua migration. Nếu chỉ tồn tại một cột, cần kiểm tra và bổ sung riêng cột còn thiếu.

### New database

Đối với database mới, sử dụng `database/schema.sql` đã cập nhật. Không cần chạy lại migration này.

### Important notes

- Không xóa database hiện có để cập nhật schema.
- Migration không xóa dữ liệu tài khoản.
- Không chạy migration nhiều lần.
- JWT được cấp từ phiên bản backend cũ cần được thay thế bằng cách đăng nhập lại.
- Không commit mật khẩu, JWT, file backup hoặc thông tin kết nối database lên Git.