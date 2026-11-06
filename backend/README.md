# ScentStation Backend

Backend của dự án **ScentStation – Nền tảng IoT đa thương hiệu cho máy trải nghiệm và quản lý nước hoa**.

Tài liệu này hướng dẫn thành viên mới cài đặt môi trường, clone source code, khởi tạo database, cấu hình kết nối và chạy backend trên máy cá nhân.

---

## 1. Công nghệ sử dụng

| Công nghệ | Phiên bản / Mô tả |
|---|---|
| Java | JDK 21 |
| Spring Boot | 4.1.2 |
| Maven | Maven Wrapper, sử dụng Maven 3.10.0 |
| MySQL | 8.0 trở lên |
| Spring Data JPA | ORM và truy cập dữ liệu |
| Hibernate | Kiểm tra cấu trúc database |
| Lombok | Giảm code boilerplate |
| Spring Boot Validation | Kiểm tra dữ liệu đầu vào |

## 2. Yêu cầu môi trường

Trước khi chạy project, cần cài đặt:

1. **JDK 21:** Cài đặt Java Development Kit phiên bản 21.
2. **Git:** Dùng để clone và cập nhật source code.
3. **MySQL Server 8.0+:** Dùng để lưu trữ dữ liệu.
4. **MySQL Workbench:** Khuyến nghị sử dụng để quản lý database.
5. **IDE:** Khuyến nghị IntelliJ IDEA hoặc Visual Studio Code.

Kiểm tra Java và Git bằng PowerShell:

```powershell
java -version
git --version
```

Đảm bảo Java hiển thị phiên bản 21.

Project đã tích hợp Maven Wrapper nên không cần cài Maven riêng.

## 3. Clone project

Mở PowerShell tại thư mục muốn lưu project và chạy:

```powershell
git clone https://github.com/CamTupn/ProjectJava.git
```

Di chuyển vào thư mục backend:

```powershell
cd ProjectJava/backend
```

Kiểm tra các file trong thư mục:

```powershell
dir
```

Cần thấy các file/thư mục chính như:

- `pom.xml`
- `mvnw`
- `mvnw.cmd`
- `src`
- `target` (có thể chưa xuất hiện trước lần build đầu tiên)

## 4. Khởi tạo Database

### 4.1. Tạo database và các bảng

ScentStation sử dụng MySQL 8.0 trở lên.

Các file SQL nằm trong thư mục `database/` ở thư mục gốc repository:

```text
ProjectJava/
├── backend/
└── database/
    ├── schema.sql
    ├── seed.sql
    └── README.md
```

Mở PowerShell tại thư mục gốc `ProjectJava` và chạy:

```powershell
Get-Content ".\database\schema.sql" -Raw | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

Nhập mật khẩu MySQL khi được yêu cầu.

**Lưu ý:**
- Đường dẫn trên là đường dẫn cài đặt MySQL mặc định trên Windows.
- Nếu MySQL được cài ở vị trí khác, hãy thay đường dẫn `mysql.exe` tương ứng.
- Tài khoản MySQL cần có quyền tạo database và bảng.
- Nếu đã có database chứa dữ liệu cần giữ, không chạy lại schema một cách tùy tiện.

### 4.2. Nạp dữ liệu mẫu

Sau khi tạo schema thành công, tiếp tục chạy:

```powershell
Get-Content ".\database\seed.sql" -Raw | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

Lệnh này nạp dữ liệu mẫu, bao gồm tenant demo và các vai trò hệ thống.

### 4.3. Kiểm tra Database

Đăng nhập MySQL và chạy:

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

Số lượng bảng dự kiến: **21 bảng**.

## 5. Cấu hình kết nối Database

Mở file:

```text
backend/src/main/resources/application.properties
```

Cấu hình kết nối hiện tại:

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
```

### 5.1. Khai báo biến môi trường

Mở PowerShell tại thư mục `backend` và khai báo:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="mat_khau_mysql_cua_ban"
```

Thay `mat_khau_mysql_cua_ban` bằng mật khẩu MySQL trên máy của bạn.

Nếu sử dụng tài khoản khác `root`, hãy thay giá trị `DB_USERNAME` tương ứng.

**Quan trọng:**
- Không đưa mật khẩu MySQL thật vào Git.
- Không sửa trực tiếp mật khẩu cá nhân vào `application.properties`.
- Các biến môi trường được khai báo trong PowerShell chỉ áp dụng cho phiên terminal hiện tại. Nếu mở terminal mới, cần khai báo lại.

## 6. Build và chạy Backend

Đảm bảo bạn đang đứng trong thư mục `backend` và đã khai báo biến môi trường ở bước 5.

### 6.1. Build project

Chạy:

```powershell
.\mvnw.cmd clean package
```

Nếu build thành công, Maven hiển thị:

```text
BUILD SUCCESS
```

### 6.2. Chạy ứng dụng

Chạy:

```powershell
.\mvnw.cmd spring-boot:run
```

Khi ứng dụng khởi động thành công, console sẽ hiển thị thông tin Spring Boot và Tomcat chạy tại cổng `8080`.

Backend mặc định sử dụng:

```text
http://localhost:8080
```

API base path:

```text
http://localhost:8080/api/v1
```

Để dừng ứng dụng, nhấn `Ctrl + C` trong terminal.

### 6.3. Chạy file JAR (tùy chọn)

Sau khi build thành công, có thể chạy file JAR bằng:

```powershell
java -jar target/scentstation-backend-0.0.1-SNAPSHOT.jar
```

## 7. Kiểm tra API

Sau khi backend khởi động thành công, có thể kiểm tra bằng trình duyệt, Postman hoặc công cụ API tương đương.

| Method | Endpoint | Mục đích |
|---|---|---|
| GET | `/api/v1/health` | Kiểm tra trạng thái backend |
| GET | `/api/v1/tenants` | Lấy danh sách tenant |
| GET | `/api/v1/tenants/{id}` | Lấy thông tin tenant theo ID |

### 7.1. Kiểm tra Health API

Gửi request:

```http
GET http://localhost:8080/api/v1/health
```

Kết quả mong đợi:

```json
{
  "status": "UP",
  "service": "ScentStation Backend"
}
```

### 7.2. Lấy danh sách Tenant

Gửi request:

```http
GET http://localhost:8080/api/v1/tenants
```

API trả về danh sách tenant hiện có trong database.

### 7.3. Lấy Tenant theo ID

Gửi request:

```http
GET http://localhost:8080/api/v1/tenants/1
```

Thay `1` bằng ID tenant đang tồn tại trong database.

## 8. Chạy Unit Test

Để chạy toàn bộ test:

```powershell
.\mvnw.cmd test
```

Hoặc chạy kiểm tra đầy đủ:

```powershell
.\mvnw.cmd clean verify
```

Nếu tất cả test thành công, Maven hiển thị:

```text
BUILD SUCCESS
```

Kết quả test được lưu trong:

```text
backend/target/surefire-reports/
```

## 9. Cấu trúc Backend

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/scentstation/
│   │   │   ├── common/
│   │   │   │   ├── config/
│   │   │   │   ├── exception/
│   │   │   │   └── response/
│   │   │   ├── health/
│   │   │   │   └── controller/
│   │   │   └── tenant/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── mapper/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/scentstation/
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## 10. Cập nhật source code mới nhất

Khi nhóm có thành viên merge code mới vào nhánh `main`, các thành viên cần cập nhật source code trước khi tiếp tục làm việc.

Mở PowerShell tại thư mục repository:

```powershell
cd ProjectJava
git pull origin main
```

Sau đó di chuyển vào backend:

```powershell
cd backend
```

Nếu có thay đổi dependency, nên chạy lại:

```powershell
.\mvnw.cmd clean package
```

## 11. Một số lỗi thường gặp

| Lỗi | Nguyên nhân có thể | Cách xử lý |
|---|---|---|
| `Unknown database 'scentstation'` | Chưa tạo database | Thực hiện lại bước 4 |
| `Access denied for user` | Sai username hoặc password | Kiểm tra `DB_USERNAME`, `DB_PASSWORD` |
| `Communications link failure` | MySQL chưa chạy hoặc sai cổng | Kiểm tra MySQL Server và cổng 3306 |
| `Schema-validation: missing table` | Database thiếu bảng hoặc schema không khớp entity | Kiểm tra và khởi tạo lại schema đúng phiên bản |
| `Port 8080 was already in use` | Có ứng dụng khác sử dụng cổng 8080 | Dừng ứng dụng đang chiếm cổng hoặc cấu hình cổng khác |
| `JAVA_HOME` hoặc Java version error | Chưa cài hoặc cấu hình sai JDK | Kiểm tra JDK 21 và biến môi trường |
| `DB_PASSWORD` unresolved | Chưa khai báo biến môi trường | Khai báo `DB_PASSWORD` trong PowerShell rồi chạy lại |

## 12. Lưu ý khi làm việc nhóm

- Luôn cập nhật code từ `main` trước khi bắt đầu công việc mới.
- Tạo branch riêng cho từng task hoặc feature.
- Không commit mật khẩu, thông tin kết nối cá nhân hoặc dữ liệu nhạy cảm.
- Không tự ý thay đổi schema database khi chưa thống nhất với nhóm.
- Khi thêm dependency, cần kiểm tra và cập nhật `pom.xml`.
- Sau khi hoàn thành chức năng, chạy test trước khi tạo Pull Request.
- Không tự ý xóa hoặc khởi tạo lại database nếu đang có dữ liệu cần giữ.

---

**ScentStation Backend – Development Guide**

Tài liệu này áp dụng cho trạng thái backend hiện tại. Các module như Authentication, Authorization, MQTT và những chức năng nghiệp vụ khác sẽ được cập nhật khi nhóm triển khai các task tiếp theo.