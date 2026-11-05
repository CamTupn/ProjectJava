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

## 2. Yêu cầu môi trường

Trước khi chạy backend, cần cài đặt:

- JDK 21 .
- Git.
- Visual Studio Code hoặc IntelliJ IDEA (tùy chọn).

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

## 4. Chạy backend

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

## 5. Kiểm tra API

Sau khi backend khởi động, mở trình duyệt hoặc Postman và gửi GET request:

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

Nếu nhận được response trên, backend đã khởi động và API Health Check hoạt động bình thường.

## 6. Chạy kiểm thử

Chạy toàn bộ quá trình clean, compile và test:

```powershell
.\mvnw.cmd clean verify
```

Nếu thành công, Maven sẽ hiển thị:

```text
BUILD SUCCESS
```

## 7. Cấu trúc thư mục

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
│   │   │       │   └── HealthController.java
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

## 8. Lưu ý

- Chạy các lệnh Maven Wrapper tại thư mục `backend`.
- Backend sử dụng cổng mặc định `8080`.
- Nếu cổng `8080` đang được ứng dụng khác sử dụng, cần giải phóng cổng hoặc thay đổi cấu hình.
- Không commit mật khẩu, API key, token hoặc thông tin xác thực lên GitHub.
- Các dependency và cấu hình database, authentication, MQTT sẽ được bổ sung trong các task tiếp theo.
- Thành viên cần pull source code mới nhất trước khi làm việc.