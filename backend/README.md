# ScentStation Backend – Development Guide

Tài liệu hướng dẫn thành viên thiết lập môi trường, tải source code, khởi tạo database, cấu hình và chạy Backend của dự án **ScentStation – Nền tảng IoT đa thương hiệu cho máy trải nghiệm và quản lý nước hoa**.

Mục tiêu của tài liệu là giúp thành viên mới có thể tự thiết lập môi trường trên máy cá nhân và chạy được Backend mà không cần tạo lại database hoặc nhập dữ liệu thủ công.

---

## 1. Giới thiệu Backend

ScentStation Backend cung cấp REST API phục vụ hệ thống quản lý máy trải nghiệm nước hoa IoT đa thương hiệu.

Các chức năng nền tảng hiện có:

- Xác thực người dùng bằng JWT.
- Phân quyền theo vai trò (Role-Based Access Control – RBAC).
- Quản lý thông tin Tenant.
- Quản lý thông tin người dùng.
- Kiểm tra trạng thái hoạt động của Backend.
- Kết nối và kiểm tra dữ liệu thông qua MySQL.

Các module nghiệp vụ khác sẽ tiếp tục được phát triển và tích hợp theo tiến độ của nhóm.

## 2. Công nghệ sử dụng

| Công nghệ | Phiên bản / Mô tả |
|---|---|
| Java | JDK 21 |
| Spring Boot | 4.1.2 |
| Maven | Maven Wrapper |
| MySQL Server | 8.0 trở lên |
| MySQL Workbench | Công cụ quản lý database |
| Spring Data JPA | Truy cập và quản lý dữ liệu |
| Hibernate | ORM và kiểm tra cấu trúc database |
| Spring Security | Xác thực và phân quyền |
| JWT | Xác thực bằng Access Token |
| Lombok | Giảm code boilerplate |
| Spring Boot Validation | Kiểm tra dữ liệu đầu vào |
| Postman | Kiểm thử REST API |
| Git / GitHub | Quản lý mã nguồn |

**Lưu ý:** Project đã tích hợp Maven Wrapper nên thành viên không cần cài Maven riêng để chạy Backend.

## 3. Yêu cầu môi trường

Trước khi bắt đầu, cần cài đặt các công cụ sau:

### 3.1. Java JDK 21

Cài đặt JDK 21.

Sau khi cài đặt, mở PowerShell và chạy:

```powershell
java -version
```

Kết quả cần hiển thị phiên bản Java 21.

Nếu máy chưa nhận lệnh `java`, kiểm tra lại biến môi trường `JAVA_HOME` và `Path`.

### 3.2. Git

Cài đặt Git để tải source code từ GitHub.

Kiểm tra:

```powershell
git --version
```

### 3.3. MySQL Server 8.0+

Cài đặt MySQL Server 8.0 trở lên.

Trong quá trình cài đặt:
- Ghi nhớ mật khẩu tài khoản MySQL.
- Đảm bảo MySQL Server đã được khởi động.
- Cổng mặc định sử dụng là `3306`.

Có thể kiểm tra trạng thái dịch vụ MySQL trên Windows bằng:

```powershell
Get-Service MySQL80
```

Nếu dịch vụ chưa chạy:

```powershell
Start-Service MySQL80
```

Tên dịch vụ có thể khác tùy theo cấu hình cài đặt MySQL trên máy.

### 3.4. MySQL Workbench

Cài đặt MySQL Workbench để:
- Kết nối MySQL Server.
- Chạy các file SQL.
- Xem cấu trúc bảng.
- Kiểm tra dữ liệu mẫu.

### 3.5. IDE

Có thể sử dụng một trong các công cụ:

- Visual Studio Code.
- IntelliJ IDEA.

Đối với Visual Studio Code, nên cài thêm Extension Pack for Java và các extension hỗ trợ Spring Boot.

### 3.6. Postman

Sử dụng Postman để gửi HTTP Request và kiểm tra API.

---

## 4. Clone source code từ GitHub

### 4.1. Clone repository

Mở PowerShell tại thư mục muốn lưu dự án.

Chạy:

```powershell
git clone https://github.com/CamTupn/ProjectJava.git
```

Sau khi clone thành công, di chuyển vào thư mục dự án:

```powershell
cd ProjectJava
```

### 4.2. Kiểm tra cấu trúc thư mục

Repository có cấu trúc chính như sau:

```text
ProjectJava/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/scentstation/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── README.md
│
├── database/
│   ├── schema.sql
│   ├── seed.sql
│   └── README.md
│
├── frontend/
├── iot/
├── docs/
├── .env.example
└── README.md
```

### 4.3. Di chuyển vào Backend

```powershell
cd backend
```

Kiểm tra:

```powershell
dir
```

Cần thấy các thành phần:
- `pom.xml`
- `mvnw`
- `mvnw.cmd`
- `src`
- `README.md`

Thư mục `target` sẽ được tạo sau lần build đầu tiên.

---

## 5. Khởi tạo Database

ScentStation sử dụng MySQL để lưu trữ dữ liệu.

Các file SQL được đặt trong thư mục `database/` ở thư mục gốc repository.

| File | Chức năng |
|---|---|
| `schema.sql` | Tạo database và cấu trúc 21 bảng |
| `seed.sql` | Nạp dữ liệu mẫu phục vụ phát triển và kiểm thử |

### 5.1. Kiểm tra MySQL Server

Mở MySQL Workbench.

Tại màn hình Home, chọn kết nối MySQL Server đang sử dụng, ví dụ:

```text
Local instance MySQL80
```

Nhập mật khẩu MySQL nếu được yêu cầu.

Đảm bảo kết nối thành công trước khi thực hiện các bước tiếp theo.

### 5.2. Tạo database và các bảng

Mở file:

```text
ProjectJava/database/schema.sql
```

Có hai cách thực hiện.

**Cách 1: Sử dụng MySQL Workbench**

1. Mở MySQL Workbench.
2. Kết nối đến MySQL Server.
3. Chọn File → Open SQL Script.
4. Chọn file `database/schema.sql`.
5. Kiểm tra nội dung file.
6. Nhấn biểu tượng tia sét (Execute) để chạy toàn bộ script.
7. Chờ MySQL thực thi xong.

File `schema.sql` đã có câu lệnh tạo database:

```sql
CREATE DATABASE IF NOT EXISTS scentstation
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE scentstation;
```

Các bảng được tạo bằng `CREATE TABLE IF NOT EXISTS`.

**Cách 2: Sử dụng PowerShell**

Mở PowerShell tại thư mục gốc `ProjectJava`.

Chạy:

```powershell
Get-Content ".\database\schema.sql" -Raw |
mysql -u root -p
```

Nhập mật khẩu MySQL khi được yêu cầu.

Nếu PowerShell không nhận lệnh `mysql`, có thể sử dụng đường dẫn đầy đủ đến `mysql.exe`, tùy theo vị trí cài đặt MySQL trên máy.

### 5.3. Kiểm tra cấu trúc database

Sau khi chạy `schema.sql`, mở MySQL Workbench và thực thi:

```sql
USE scentstation;

SHOW TABLES;
```

Kiểm tra số lượng bảng:

```sql
SELECT COUNT(*) AS total_tables
FROM information_schema.tables
WHERE table_schema = 'scentstation'
  AND table_type = 'BASE TABLE';
```

Kết quả mong đợi:

```text
total_tables = 21
```

Nếu kết quả là 21, database đã có đủ cấu trúc bảng theo schema hiện tại.

### 5.4. Nạp dữ liệu mẫu

Sau khi tạo cấu trúc bảng thành công, tiếp tục mở:

```text
ProjectJava/database/seed.sql
```

Thực hiện tương tự như bước 5.2:

1. Mở file `seed.sql` bằng MySQL Workbench.
2. Kiểm tra câu lệnh `USE scentstation;` ở đầu file.
3. Nhấn Execute để chạy toàn bộ script.
4. Chờ các câu lệnh INSERT hoàn tất.

Có thể chạy bằng PowerShell:

```powershell
Get-Content ".\database\seed.sql" -Raw |
mysql -u root -p
```

**Lưu ý quan trọng về dữ liệu mẫu:**

File `seed.sql` hiện tại được xuất từ database demo bằng MySQL Workbench/MySQL Dump.

File sử dụng ID cố định và có các câu lệnh khóa bảng, tạm tắt kiểm tra khóa ngoại trong quá trình import. Vì vậy:

- Chỉ chạy seed sau khi đã tạo schema thành công.
- Chỉ chạy một lần trên database mới.
- Không chạy lại seed trên database đã có dữ liệu.
- Không dùng seed này để cập nhật database đang có dữ liệu thực tế.
- Không tự ý xóa database hoặc các bảng nếu chưa sao lưu.

### 5.5. Dữ liệu mẫu hiện có

Bộ dữ liệu mẫu được chuẩn bị để các thành viên có thể kiểm thử các chức năng CRUD và quan hệ giữa các bảng.

Một số dữ liệu chính:

| Bảng | Số bản ghi mẫu |
|---|---:|
| tenants | 2 |
| users | 6 |
| roles | 5 |
| user_roles | 6 |
| locations | 7 |
| products | 6 |
| machines | 7 |
| machine_slots | 19 |
| orders | 2 |
| payments | 2 |
| refills | 1 |
| inventory_transactions | 1 |
| sensor_readings | 9 |
| device_events | 2 |
| alerts | 3 |
| maintenance_tickets | 2 |
| maintenance_logs | 1 |
| notifications | 2 |
| audit_logs | 1 |
| dispense_commands | 0 |
| dispense_executions | 0 |

Hai bảng `dispense_commands` và `dispense_executions` hiện chưa có bản ghi mẫu. Đây không phải lỗi khởi tạo database; các bảng vẫn được tạo đầy đủ để phát triển chức năng sau này.

### 5.6. Kiểm tra dữ liệu sau khi nạp

Chạy:

```sql
USE scentstation;

SELECT * FROM tenants;

SELECT * FROM users;

SELECT * FROM roles;

SELECT * FROM user_roles;

SELECT * FROM locations;

SELECT * FROM products;

SELECT * FROM machines;

SELECT * FROM machine_slots;
```

Kiểm tra nhanh số lượng bản ghi:

```sql
SELECT 'Tenants' AS table_name, COUNT(*) AS total FROM tenants
UNION ALL
SELECT 'Users', COUNT(*) FROM users
UNION ALL
SELECT 'Roles', COUNT(*) FROM roles
UNION ALL
SELECT 'Locations', COUNT(*) FROM locations
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Machines', COUNT(*) FROM machines
UNION ALL
SELECT 'Machine Slots', COUNT(*) FROM machine_slots
UNION ALL
SELECT 'Orders', COUNT(*) FROM orders
UNION ALL
SELECT 'Payments', COUNT(*) FROM payments;
```

Nếu số lượng bản ghi khớp với dữ liệu mẫu, có thể chuyển sang bước cấu hình Backend.

---

## 6. Cấu hình kết nối Backend với MySQL

### 6.1. Mở application.properties

Mở file:

```text
backend/src/main/resources/application.properties
```

Cấu hình hiện tại:

```properties
spring.application.name=scentstation-backend

server.port=8080
server.servlet.context-path=/

# API
app.api.base-path=/api/v1

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/scentstation
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}

# JPA / Hibernate Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

# JWT Configuration
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=86400000
```

**Giải thích các cấu hình:**

| Thuộc tính | Ý nghĩa |
|---|---|
| `server.port` | Cổng chạy Backend |
| `spring.datasource.url` | Địa chỉ MySQL và tên database |
| `DB_USERNAME` | Tài khoản MySQL |
| `DB_PASSWORD` | Mật khẩu MySQL |
| `ddl-auto=validate` | Kiểm tra cấu trúc database, không tự tạo bảng |
| `show-sql=true` | Hiển thị SQL Hibernate thực thi |
| `app.jwt.secret` | Khóa bí mật dùng để ký và xác thực JWT |
| `app.jwt.expiration` | Thời hạn Access Token, tính bằng milliseconds |

**Quan trọng:** Không đổi `ddl-auto=validate` thành `create` hoặc `create-drop`, vì có thể gây mất dữ liệu hoặc thay đổi cấu trúc database ngoài ý muốn.

### 6.2. Khai báo biến môi trường

Backend sử dụng biến môi trường để lưu thông tin kết nối và JWT Secret.

Mở PowerShell tại thư mục `backend`.

Khai báo tài khoản MySQL:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="mat_khau_mysql_cua_ban"
```

Thay `mat_khau_mysql_cua_ban` bằng mật khẩu MySQL trên máy của bạn.

### 6.3. Tạo JWT Secret

Backend yêu cầu `JWT_SECRET` được mã hóa Base64.

Mở PowerShell và chạy:

```powershell
$bytes = New-Object byte[] 64

[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)

$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
```

Lệnh trên tạo một khóa ngẫu nhiên 64 byte và chuyển thành chuỗi Base64 để sử dụng cho JWT.

Không sử dụng một JWT Secret ngắn hoặc chuỗi tùy ý không được mã hóa Base64, vì `JwtService` của dự án giải mã giá trị này trước khi tạo khóa ký.

### 6.4. Kiểm tra biến môi trường

Chạy:

```powershell
Write-Output $env:DB_USERNAME
Write-Output $env:DB_PASSWORD
Write-Output $env:JWT_SECRET
```

Không chia sẻ các giá trị mật khẩu và JWT Secret lên GitHub hoặc gửi cho người khác.

**Lưu ý:**

Các biến môi trường được khai báo bằng `$env:` chỉ tồn tại trong phiên PowerShell hiện tại.

Nếu mở Terminal mới, cần khai báo lại trước khi chạy Backend.

---

## 7. Build và chạy Backend

Đảm bảo:

- Đã cài JDK 21.
- MySQL Server đang chạy.
- Database `scentstation` đã được tạo.
- Đã nạp dữ liệu mẫu.
- Đã khai báo `DB_USERNAME`, `DB_PASSWORD` và `JWT_SECRET`.

### 7.1. Mở đúng thư mục Backend

Trong PowerShell:

```powershell
cd ProjectJava/backend
```

Nếu đang đứng ở thư mục gốc repository thì chỉ cần chạy:

```powershell
cd backend
```

### 7.2. Build project

Chạy:

```powershell
.\mvnw.cmd clean package
```

Maven Wrapper sẽ tự tải Maven cần thiết nếu máy chưa có sẵn.

Quá trình build sẽ:
- Tải các dependency được khai báo trong `pom.xml`.
- Biên dịch source code Java.
- Biên dịch và chạy các bài kiểm thử.
- Đóng gói ứng dụng thành file JAR.

Kết quả mong đợi:

```text
BUILD SUCCESS
```

Nếu xuất hiện `BUILD FAILURE`, xem thông báo lỗi trong Terminal để xác định nguyên nhân.

### 7.3. Chạy Backend bằng Maven

Sau khi build thành công, chạy:

```powershell
.\mvnw.cmd spring-boot:run
```

Nếu ứng dụng khởi động thành công, console sẽ hiển thị thông tin Spring Boot và Tomcat.

Backend sử dụng cổng:

```text
8080
```

Địa chỉ Backend:

```text
http://localhost:8080
```

API Base URL:

```text
http://localhost:8080/api/v1
```

Để dừng ứng dụng, nhấn:

```text
Ctrl + C
```

### 7.4. Chạy Backend bằng file JAR

Sau khi build thành công, có thể chạy trực tiếp:

```powershell
java -jar target/scentstation-backend-0.0.1-SNAPSHOT.jar
```

Cần khai báo các biến môi trường trước khi chạy lệnh này.

---

## 8. Kiểm tra API bằng Postman

Sau khi Backend chạy thành công, mở Postman để kiểm tra các endpoint hiện có.

### 8.1. Health Check API

Mục đích: kiểm tra Backend có đang hoạt động hay không.

Thiết lập:

| Thuộc tính | Giá trị |
|---|---|
| Method | GET |
| URL | `http://localhost:8080/api/v1/health` |

Nhấn Send.

Kết quả mong đợi:

```json
{
  "status": "UP",
  "service": "ScentStation Backend"
}
```

### 8.2. Đăng nhập

Endpoint:

```http
POST http://localhost:8080/api/v1/auth/login
```

Request Body cần gửi theo cấu trúc DTO `LoginRequest` của Backend:

```json
{
  "usernameOrEmail": "brandadmin",
  "password": "Scent123456"
}
```

Đây là tài khoản demo được tạo trong database mẫu.

Nếu đăng nhập thành công, API trả về thông tin người dùng và Access Token JWT.

**Lưu ý:** Chỉ tài khoản có trạng thái `ACTIVE` mới được đăng nhập. Không sử dụng tài khoản demo cho môi trường production.

### 8.3. Lấy danh sách Tenant

Endpoint:

```http
GET http://localhost:8080/api/v1/tenants
```

Đây là API có yêu cầu xác thực.

Trong Postman:
1. Chọn tab Authorization.
2. Chọn loại Bearer Token.
3. Dán Access Token nhận được từ API đăng nhập.
4. Nhấn Send.

### 8.4. Lấy Tenant theo ID

Endpoint:

```http
GET http://localhost:8080/api/v1/tenants/1
```

Thay `1` bằng ID Tenant cần truy vấn.

### 8.5. Cập nhật Tenant

Endpoint:

```http
PUT http://localhost:8080/api/v1/tenants/1
```

Cần gửi Bearer Token và Request Body theo DTO cập nhật Tenant hiện được khai báo trong source code.

### 8.6. Các API hiện có

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/v1/health` | Kiểm tra trạng thái Backend |
| POST | `/api/v1/auth/login` | Đăng nhập |
| GET | `/api/v1/tenants` | Lấy danh sách Tenant |
| GET | `/api/v1/tenants/{id}` | Lấy Tenant theo ID |
| PUT | `/api/v1/tenants/{id}` | Cập nhật Tenant |
| GET | `/api/v1/users` | Lấy danh sách User |
| GET | `/api/v1/users/{id}` | Lấy User theo ID |
| PUT | `/api/v1/users/{id}` | Cập nhật User |
| GET | `/api/v1/machines/{machineId}/slots` | Lấy danh sách Slot của máy và tổng số Slot |
| POST | `/api/v1/machines/{machineId}/slots` | Tạo Slot |
| GET | `/api/v1/machine-slots/{id}` | Lấy chi tiết Slot |
| PUT | `/api/v1/machine-slots/{id}` | Cập nhật cấu hình Slot, không cập nhật lượng tồn |
| PATCH | `/api/v1/machine-slots/{id}/status` | Cập nhật trạng thái lỗi/khôi phục Slot |
| DELETE | `/api/v1/machine-slots/{id}` | Vô hiệu hóa Slot (soft delete) |

Ngoài ra, Backend có các endpoint kiểm thử phân quyền dưới `/api/v1/test/authorization`.

Machine Slot APIs enforce role and tenant access through the parent Machine and optional Product. Slot status is calculated from inventory volume and the warning threshold, with `ERROR` allowed as an operational override. Non-zero stock supplied on slot creation is recorded atomically as an `ADJUSTMENT` inventory transaction. A slot with remaining stock cannot be disabled until inventory is handled through the inventory-removal workflow.

Các endpoint nghiệp vụ khác sẽ được bổ sung khi nhóm hoàn thành từng task. Không giả định endpoint đã tồn tại nếu chưa được triển khai trong source code.

---

## 9. Tài khoản demo

Database mẫu có hai Tenant:

| Tenant | Code | Mục đích |
|---|---|---|
| ScentStation Demo | `SCENT-DEMO` | Thương hiệu demo thứ nhất |
| Aroma House Demo | `AROMA-DEMO` | Thương hiệu demo thứ hai |

Một số tài khoản mẫu:

| Username | Vai trò | Trạng thái |
|---|---|---|
| `brandadmin` | BRAND_ADMIN | ACTIVE |
| `platformadmin` | PLATFORM_SUPER_ADMIN | ACTIVE |
| `ops_scent` | OPERATIONS_MANAGER | INACTIVE |
| `tech_scent` | TECHNICIAN | ACTIVE |
| `stock_scent` | INVENTORY_STAFF | ACTIVE |
| `admin_aroma` | BRAND_ADMIN | ACTIVE |

Mật khẩu demo hiện tại của các tài khoản được seed:

```text
Scent123456
```

Đây là tài khoản phục vụ phát triển và kiểm thử. Không sử dụng các thông tin đăng nhập này trong môi trường thật.

Tài khoản `ops_scent` hiện có trạng thái `INACTIVE`, vì vậy không thể đăng nhập cho đến khi trạng thái được cập nhật.

---

## 10. Cấu trúc source code Backend

Cấu trúc package hiện tại:

```text
backend/
│
├── src/
│   ├── main/
│   │   ├── java/com/scentstation/
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/com/scentstation/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

Các package Java được tổ chức theo chức năng, bao gồm:

| Package | Chức năng |
|---|---|
| `controller` | Tiếp nhận HTTP Request và trả HTTP Response |
| `service` | Xử lý nghiệp vụ |
| `repository` | Truy cập database thông qua JPA |
| `entity` | Ánh xạ các bảng database |
| `dto` | Định nghĩa dữ liệu Request và Response |
| `security` / cấu hình liên quan | Xác thực, JWT và phân quyền |
| `common` | Các thành phần dùng chung |

Khi phát triển chức năng mới, thành viên cần tuân theo cấu trúc và cách tổ chức code đang được nhóm sử dụng.

---

## 11. Chạy Unit Test

Để chạy toàn bộ Unit Test:

```powershell
.\mvnw.cmd test
```

Để chạy kiểm tra và đóng gói đầy đủ:

```powershell
.\mvnw.cmd clean verify
```

Nếu tất cả bước thành công, Maven hiển thị:

```text
BUILD SUCCESS
```

Kết quả kiểm thử được lưu tại:

```text
backend/target/surefire-reports/
```

Khi phát triển chức năng mới, cần bổ sung test phù hợp với nghiệp vụ và các trường hợp lỗi.

---

## 12. Cập nhật source code từ GitHub

Trước khi bắt đầu công việc mới, cần cập nhật source code mới nhất.

Mở PowerShell tại thư mục repository:

```powershell
cd ProjectJava
```

Kiểm tra branch hiện tại:

```powershell
git branch
```

Nếu đang làm việc trên branch riêng, hãy lưu hoặc commit các thay đổi trước khi cập nhật.

Chuyển về `main` và cập nhật:

```powershell
git checkout main
git pull origin main
```

Sau đó tạo branch mới cho task:

```powershell
git checkout -b feature/task-xxx-ten-task
```

Ví dụ:

```powershell
git checkout -b feature/task-017-user-tenant-management
```

Nếu branch đã tồn tại trên GitHub, có thể checkout branch đó thay vì tạo mới.

Sau khi cập nhật source code, quay lại thư mục Backend:

```powershell
cd backend
```

Nếu có thay đổi dependency hoặc source code, chạy lại:

```powershell
.\mvnw.cmd clean package
```

---

## 13. Quy trình làm việc nhóm

Để tránh xung đột mã nguồn và database, các thành viên cần tuân thủ:

1. Đọc README và tài liệu liên quan trước khi bắt đầu task.
2. Pull source code mới nhất từ `main`.
3. Tạo branch riêng cho task được phân công.
4. Không làm việc trực tiếp trên `main`.
5. Không tự ý thay đổi cấu trúc database khi chưa thống nhất với nhóm.
6. Nếu thay đổi schema, cần cập nhật file SQL tương ứng và thông báo các thành viên.
7. Không commit mật khẩu, JWT Secret hoặc thông tin kết nối cá nhân.
8. Kiểm tra build và test trước khi tạo Pull Request.
9. Tạo Pull Request để thành viên khác review.
10. Sau khi merge, cập nhật lại source code từ `main`.

Đặc biệt, khi làm việc với database:

- Database local của mỗi thành viên có thể chứa dữ liệu riêng.
- Không commit file dump chứa thông tin cá nhân hoặc dữ liệu nhạy cảm.
- Không tự ý xóa database để xử lý lỗi.
- Luôn backup trước khi thay đổi dữ liệu hoặc cấu trúc bảng.

---

## 14. Một số lỗi thường gặp và cách xử lý

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| `java is not recognized` | Chưa cài JDK hoặc chưa cấu hình PATH | Kiểm tra JDK 21 và JAVA_HOME |
| `Unknown database 'scentstation'` | Chưa tạo database | Chạy `schema.sql` |
| `Access denied for user` | Sai tài khoản hoặc mật khẩu MySQL | Kiểm tra DB_USERNAME và DB_PASSWORD |
| `Communications link failure` | MySQL chưa chạy hoặc sai cổng | Khởi động MySQL Server và kiểm tra cổng 3306 |
| `Schema-validation: missing table` | Database thiếu bảng | Kiểm tra và chạy schema đúng phiên bản |
| `Schema-validation: wrong column type` | Cấu trúc bảng không khớp Entity | Đối chiếu schema.sql với Entity |
| `Could not resolve placeholder DB_PASSWORD` | Chưa khai báo biến môi trường | Khai báo DB_PASSWORD trong Terminal |
| `Could not resolve placeholder JWT_SECRET` | Chưa khai báo JWT Secret | Tạo và khai báo JWT_SECRET |
| JWT Base64 decode error | JWT Secret không đúng định dạng Base64 | Tạo lại khóa bằng hướng dẫn ở mục 6.3 |
| `BUILD FAILURE` | Lỗi biên dịch, dependency hoặc test | Đọc lỗi trong Maven Console |
| `Port 8080 was already in use` | Có ứng dụng khác đang sử dụng cổng 8080 | Dừng ứng dụng đó hoặc đổi cổng |
| Duplicate entry khi chạy seed | Seed đã được chạy trước đó | Không chạy lại seed trên database đã có dữ liệu |

### 14.1. Lỗi không kết nối được MySQL

Kiểm tra dịch vụ:

```powershell
Get-Service MySQL80
```

Nếu dịch vụ chưa chạy:

```powershell
Start-Service MySQL80
```

Sau đó thử kết nối lại bằng MySQL Workbench.

### 14.2. Lỗi thiếu database hoặc bảng

Kiểm tra:

```sql
SHOW DATABASES;

USE scentstation;

SHOW TABLES;
```

Nếu database chưa có, thực hiện lại bước 5.

Không chạy lại `seed.sql` chỉ để xử lý lỗi thiếu bảng. Cần xác định nguyên nhân từ schema trước.

### 14.3. Lỗi không đăng nhập được

Kiểm tra:
- Username hoặc email.
- Password.
- Trạng thái tài khoản có phải `ACTIVE` không.
- Tài khoản đã được gán Role trong `user_roles` chưa.
- Backend đã kết nối đúng database chưa.

---

## 15. Bảo mật và cấu hình môi trường

Các thông tin sau không được đưa lên GitHub:

- Mật khẩu MySQL thật.
- JWT Secret.
- Access Token.
- Các thông tin xác thực của môi trường thật.
- Dữ liệu người dùng hoặc dữ liệu nghiệp vụ nhạy cảm.

Repository có file `.env.example` để tham khảo tên biến môi trường.

File `.env.example` không chứa mật khẩu thật.

Không commit file `.env` cá nhân.

Mỗi thành viên cần tự cấu hình thông tin kết nối và JWT Secret trên máy của mình.

---

## 16. Checklist dành cho thành viên mới

Trước khi bắt đầu phát triển chức năng, hãy hoàn thành checklist sau:

### A. Chuẩn bị môi trường

- [ ] Cài đặt JDK 21.
- [ ] Cài đặt Git.
- [ ] Cài đặt MySQL Server 8.0+.
- [ ] Cài đặt MySQL Workbench.
- [ ] Cài đặt IDE.
- [ ] Cài đặt Postman.

### B. Chuẩn bị source code

- [ ] Clone repository thành công.
- [ ] Mở đúng thư mục `ProjectJava`.
- [ ] Kiểm tra thư mục `backend`.
- [ ] Kiểm tra file `pom.xml`.

### C. Chuẩn bị database

- [ ] Kết nối MySQL Server thành công.
- [ ] Chạy `schema.sql`.
- [ ] Kiểm tra database `scentstation`.
- [ ] Kiểm tra đủ 21 bảng.
- [ ] Chạy `seed.sql` một lần.
- [ ] Kiểm tra dữ liệu mẫu.

### D. Cấu hình Backend

- [ ] Kiểm tra `application.properties`.
- [ ] Khai báo `DB_USERNAME`.
- [ ] Khai báo `DB_PASSWORD`.
- [ ] Tạo và khai báo `JWT_SECRET`.
- [ ] Chạy `.\mvnw.cmd clean package`.
- [ ] Đạt kết quả `BUILD SUCCESS`.

### E. Kiểm tra ứng dụng

- [ ] Chạy Backend thành công.
- [ ] Kiểm tra Health API.
- [ ] Đăng nhập bằng tài khoản demo.
- [ ] Nhận được Access Token.
- [ ] Kiểm tra API có yêu cầu xác thực bằng Bearer Token.

Khi hoàn thành các bước trên, môi trường phát triển Backend đã sẵn sàng để thực hiện các task được phân công.

---

## 17. Thông tin dự án

**Tên dự án:** ScentStation

**Mô tả:** Nền tảng IoT đa thương hiệu cho máy trải nghiệm và quản lý nước hoa.

**Repository:** https://github.com/CamTupn/ProjectJava

**Backend:** Java 21, Spring Boot 4.1.2, Maven, Spring Security, JWT, Spring Data JPA.

**Database:** MySQL 8.0+.

**Database initialization:**
- `database/schema.sql`
- `database/seed.sql`

**Backend Development Guide**

Tài liệu sẽ được cập nhật khi nhóm bổ sung các module, API và quy trình triển khai mới.
### Machine Slot — quy tắc triển khai

POST requires positive slotNumber, capacity >= 0.01, non-negative initial volume and threshold within capacity. Product must be ACTIVE and match the machine tenant; positive stock requires a product. PUT replaces configuration including slotNumber; productId null unassigns only when stock is zero. PUT cannot change currentVolumeMl. PATCH ERROR sets error; AVAILABLE/LOW_STOCK/EMPTY recalculates actual status. Disabled machines reject create/update/PATCH; disabled slots reject update/PATCH. DELETE returns the disabled DTO and is idempotent; remaining stock returns 409. See shared API specification and test report in docs. Latest reported regression: 229/229 PASS (27/11/2026); integration evidence tracked separately.


## TASK-021 — MySQL Integration Test (opt-in)

Prerequisites: MySQL 8, schema `scentstation_test` initialized from the project schema (21 tables) and seeded with the five role codes, separate local account `scentstation_tester` with SELECT/INSERT/UPDATE/DELETE only on that schema. Do not import scripts containing `USE scentstation` into the test environment without adapting the database name. Do not store passwords or JWT secrets in Git.

The test profile is `src/test/resources/application-integration.properties`, using `TEST_DB_PASSWORD` and `TEST_JWT_SECRET`, `ddl-auto=validate`, `spring.sql.init.mode=never`. The local JDBC connection uses `useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`; this is for local tests only, not production.

From backend in PowerShell:

```powershell
$env:TEST_DB_PASSWORD = Read-Host "Test database password"
$bytes = New-Object byte[] 64
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:TEST_JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
$env:RUN_INTEGRATION_TESTS = "true"
.\mvnw.cmd clean test
```

Latest verified run: 341 tests, 0 failures/errors/skipped (27/11/2026). To run only database tests: `.\mvnw.cmd test "-Dtest=DatabaseIntegrationTest"`. To opt out: `Remove-Item Env:RUN_INTEGRATION_TESTS -ErrorAction SilentlyContinue`. 39 integration tests are then skipped (expected 302 remaining). Do not commit raw Surefire XML or credentials.

## TASK-021 — Chính sách cấp tài khoản (27/11/2026)

Thay đổi phạm vi TASK-014/015: không tự đăng ký công khai; `RegisterRequest` đã xóa, `/api/v1/auth/register` không có controller và không còn public. Platform Super Admin được khởi tạo ngoài API cấp tài khoản. Platform Super Admin tạo tenant và cấp Brand Admin hoặc nhân viên cho tenant ACTIVE; Brand Admin chỉ cấp Operations Manager, Technician, Inventory Staff trong tenant của mình. Không role nào được tạo Platform Super Admin qua API. Login vẫn public, JWT bắt buộc cho API quản lý. Đây là thay đổi yêu cầu cần được nhóm xác nhận khi nghiệm thu, không phải hoàn thành Register theo issue cũ.

Run all integration classes: `./mvnw.cmd test "-Dtest=DatabaseIntegrationTest,AuthenticationIntegrationTest,BusinessFlowIntegrationTest"` (PowerShell). Expected 39 tests. Authentication/BusinessFlow use test-only fixtures and rollback. Never use real credentials as fixture constants.

## Account Management & Tenant Identity

The backend supports:

- Authentication with tenant identity.
- Current user profile.
- Password change.
- Administrative password reset.
- Mandatory password change.
- JWT revocation using token version.
- Role-based access control and tenant isolation.

### API endpoints

| Method | Endpoint |
|---|---|
| POST | /api/v1/auth/login |
| GET | /api/v1/auth/me |
| POST | /api/v1/auth/change-password |
| POST | /api/v1/users/{id}/reset-password |

### Database requirements

Existing databases must apply the migration:

`database/migrations/001_account_security.sql`

Apply the migration before starting the updated backend. New databases should use the updated schema.sql.

See [Database README](../database/README.md) for migration instructions.

### Authentication behavior

- Login accepts usernameOrEmail and password.
- Authentication responses include tenant identity and mustChangePassword.
- Users must change temporary passwords before accessing protected business APIs.
- Password changes and resets invalidate previously issued JWTs.
- Users must authenticate again after changing their passwords.

### Testing results

- Automated tests: 420/420 passed.
- Manual API testing: 12/12 planned scenarios passed.

### Documentation

- [Account Management](../docs/features/account-management.md)
- [API Specification](../docs/api/api-specification.md)
- [Test Report](../docs/test-reports/test-report.md)