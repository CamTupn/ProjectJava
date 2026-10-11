# Backend — ScentStation

**Vai trò:** REST API, xác thực/phân quyền, nghiệp vụ, MySQL và tích hợp MQTT/thanh toán.

**Công nghệ dự kiến:** Java 21, Spring Boot 3.5.x, Maven 3.9.x, Spring Web, Validation, Spring Security, Spring Data JPA, MySQL 8.4, JUnit 5, Mockito.

## Cấu trúc mục tiêu sau khi khởi tạo Spring Boot
```text
backend/
├── pom.xml
├── mvnw / mvnw.cmd
└── src/
    ├── main/java/com/scentstation/
    │   ├── ScentStationApplication.java
    │   ├── common/
    │   ├── config/
    │   ├── auth/
    │   ├── tenant/
    │   ├── brand/
    │   ├── product/
    │   ├── machine/
    │   ├── order/
    │   ├── payment/
    │   ├── dispensing/
    │   ├── inventory/
    │   ├── maintenance/
    │   └── reporting/
    └── test/java/com/scentstation/
```

Đây là cấu trúc đề xuất, chưa phải source Spring Boot chạy được. Nhóm cần chốt cách tổ chức package trước khi tạo module. Package chữ thường; class PascalCase; method/variable camelCase. API prefix dự kiến `/api/v1`.
