# Backend — ScentStation


Backend REST API của dự án ScentStation, được xây dựng bằng Java Spring Boot.

## 1. Công nghệ sử dụng

| Công nghệ | Phiên bản |
|---|---|
| Java | 21 (Java target) |
| Spring Boot | 4.1.2 |
| Apache Maven | 3.10.0 |
| Maven Wrapper | 3.3.4 |
| Packaging | Jar |
| Web Framework | Spring MVC |
| Validation | Spring Boot Validation |
| Spring Data JPA | Quản lý truy cập dữ liệu |
| Hibernate | ORM, schema validation|
| MySQL Connector/J | JDBC Driver |
| MySQL Server | 8.0.42 |

## 2. Yêu cầu môi trường

Trước khi chạy backend, cần cài đặt:

- JDK 21 .
- Git.
- Visual Studio Code hoặc IntelliJ IDEA (tùy chọn).
- MySQL Server 8.0.x.

Project đã tích hợp Maven Wrapper nên không cần cài Maven riêng.

Kiểm tra Java:

```powershell
java -version
```

## 3. Lấy source code

Clone repository:

```powershell
git clone https://github.com/CamTupn/ProjectJava.git
```

Di chuyển vào thư mục backend:

```powershell
cd ProjectJava/backend
```

Nếu đã clone repository trước đó:

```powershell
cd ProjectJava
git pull origin main
cd backend
```

## 4. Database Setup

Backend sử dụng MySQL 8.0.x để lưu trữ dữ liệu.

### 4.1. Tạo database

Database được đặt tên:

` scentstation `

Nếu chưa có database, thực hiện:

```sql
CREATE DATABASE scentstation;
```

### 4.2. Khởi tạo cấu trúc database

Tại thư mục gốc repository, thực thi hai file SQL theo thứ tự:

1. `database/schema.sql`
2. `database/seed.sql`

Các file này tạo cấu trúc bảng và dữ liệu khởi tạo cho hệ thống.

### 4.3. Cấu hình thông tin kết nối

Backend sử dụng biến môi trường:

- `DB_USERNAME`: tài khoản MySQL.
- `DB_PASSWORD`: mật khẩu MySQL.

Ví dụ trên PowerShell:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
```

Không commit thông tin xác thực thật lên GitHub.

## 5. Cấu hình môi trường - Database and JPA Configuration

Backend sử dụng Spring Data JPA kết hợp Hibernate để ánh xạ Java Entity với các bảng trong MySQL.

Cấu hình trong `application.properties`:

- Database: `scentstation`
- Hibernate DDL mode: `validate`
- SQL logging: Enabled
- Open Session in View: Disabled

Hibernate sử dụng chế độ `validate` để kiểm tra cấu trúc Entity
với database hiện có, không tự động tạo hoặc thay đổi bảng.


## 6. Chạy backend

### Cách 1: Chạy bằng Maven Wrapper (khuyến nghị)

Tại thư mục `backend`, chạy:

```powershell
.\mvnw.cmd spring-boot:run
```

Khi ứng dụng khởi động thành công, backend mặc định chạy tại:

```text
http://localhost:8080
```

Giữ terminal đang chạy để backend tiếp tục hoạt động.

Để dừng ứng dụng, nhấn:

```text
Ctrl + C
```

### Cách 2: Build và chạy file JAR

Build project:

```powershell
.\mvnw.cmd clean package
```

Sau khi build thành công, chạy:

```powershell
java -jar target/scentstation-backend-0.0.1-SNAPSHOT.jar
```

Để dừng ứng dụng, nhấn `Ctrl + C`.

## 7. API Endpoints

Backend hiện cung cấp các REST API cơ bản:

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/health` | Kiểm tra trạng thái backend |
| GET | `/api/v1/tenants` | Lấy danh sách tenant |

### Health Check

Request:

```http
GET http://localhost:8080/api/v1/health
```

Response:

```json
{
  "status": "UP",
  "service": "ScentStation Backend"
}
```

### Get All Tenants

Request:

```http
GET http://localhost:8080/api/v1/tenants
```

Response mẫu:

```json
[
  {
    "id": 1,
    "name": "ScentStation Demo",
    "code": "SCENT-DEMO",
    "status": "ACTIVE",
    "createdAt": "2026-10-28T17:33:51",
    "updatedAt": "2026-10-28T17:33:51"
  }
]
```

## 8. Chạy kiểm thử

Chạy toàn bộ quá trình clean, compile và test:

```powershell
.\mvnw.cmd clean verify
```

Nếu thành công, Maven sẽ hiển thị:

```text
BUILD SUCCESS
```

## 9. Cấu trúc thư mục


```text
backend/
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/scentstation/
│   │   │       ├── controller/
│   │   │       │   ├── HealthController.java
│   │   │       │   └── TenantController.java
│   │   │       ├── entity/
│   │   │       │   └── Tenant.java
│   │   │       ├── repository/
│   │   │       │   └── TenantRepository.java
│   │   │       └── ScentstationBackendApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```


## 10. Lưu ý

- Chạy các lệnh Maven Wrapper tại thư mục `backend`.
- Backend sử dụng cổng mặc định `8080`.
- Nếu cổng `8080` đang được ứng dụng khác sử dụng, cần giải phóng cổng hoặc thay đổi cấu hình.
- Không commit mật khẩu, API key, token hoặc thông tin xác thực lên GitHub.
- Database và JPA đã được tích hợp trong phiên bản backend hiện tại.
- Authentication, authorization và MQTT sẽ được triển khai trong các task tiếp theo.
- Thành viên cần pull source code mới nhất trước khi làm việc.