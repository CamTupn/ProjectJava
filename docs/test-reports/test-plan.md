# Kế hoạch kiểm thử hệ thống ScentStation

## 1. Phạm vi và phương pháp

JUnit/Mockito cho nghiệp vụ, MockMvc cho hợp đồng HTTP và RBAC; integration test với MySQL test riêng; end-to-end cho frontend, thanh toán sandbox và MQTT simulator. Không sử dụng dữ liệu production. Chạy `cd backend` rồi `.\mvnw.cmd clean test` trên Windows. Lưu Surefire XML làm bằng chứng; không ghi token hoặc mật khẩu vào tài liệu.

## 2. Ma trận kiểm thử theo module

| Module | Kịch bản cần kiểm tra | Tiêu chí |
|---|---|---|
| Auth/JWT | Login đúng/sai, token hết hạn, thiếu token | 401 khi chưa xác thực |
| Tenant/User/RBAC | 5 role, truy cập chéo tenant, khóa user | 403 khi thiếu quyền; không lộ dữ liệu tenant khác |
| Location/Product | CRUD, SKU trùng, liên kết máy/slot, soft delete | Không xóa vật lý; từ chối liên kết không hợp lệ |
| Machine | CRUD, bộ lọc, mã trùng, location khác tenant | 200/201 hợp lệ; 400/403/404/409 tùy lỗi |
| Machine status | OFFLINE hợp lệ, ONLINE thủ công, trạng thái sai, DISABLED | Hai trạng thái độc lập; ONLINE thủ công bị chặn |
| Slot | Gán sản phẩm cùng tenant, dung tích, slot trùng | Không vượt dung tích hoặc truy cập chéo tenant |
| Kiosk/Order | Máy không sẵn sàng, giá, tạo order | Không cho giao dịch không hợp lệ |
| Payment | Callback trùng, chữ ký sai, sai số tiền | Không xác nhận thanh toán sai hoặc lặp |
| Dispensing | Command trùng, timeout, ACK, kết quả muộn | Tối đa một lần xịt thành công/order; timeout cần đối soát |
| MQTT | Heartbeat, mất kết nối, message trùng | Xác thực thiết bị, không xử lý lặp |
| Inventory | Refill, adjustment, dispense thành công/thất bại | Chỉ trừ tồn khi xác nhận xịt thành công |
| Alert/Maintenance | Cảnh báo, xử lý ticket, phân quyền | Không lộ dữ liệu tenant khác |
| Reporting/Audit | Tổng hợp, lọc tenant, nhật ký | Dữ liệu nhất quán, không lộ bí mật |
| Frontend | Login/logout, route guard, lỗi API | Đúng dashboard, xử lý lỗi rõ ràng |

## 3. Điều kiện hoàn thành

Mỗi module cần test nghiệp vụ, phân quyền, dữ liệu biên và lỗi; regression không có failure/error; kiểm thử tích hợp có bằng chứng riêng. Các kịch bản trên là kế hoạch, không phải kết quả đã chạy. Xem [báo cáo chung](test-report.md).

## 4. Kiểm thử chi tiết quản lý khe

| Nhóm | Kịch bản | Kỳ vọng |
|---|---|---|
| Volume | capacity=0, volume âm/vượt capacity, threshold âm/vượt capacity | 400; không ghi dữ liệu |
| Product | khác tenant, inactive, null khi tồn dương | 404/409/400 |
| Configuration | đổi/gỡ product khi còn tồn; đổi khi hết tồn; trùng số khe | 409 hoặc thành công hợp lệ |
| Status | EMPTY, LOW_STOCK, AVAILABLE, ERROR, DISABLED | Tính đúng; giữ ERROR khi PUT; DELETE idempotent |
| Tenant | list/create/detail/update/PATCH/delete khác tenant | 404; không ghi dữ liệu |
| RBAC | 5 role x 6 route; thiếu authentication | 30 trường hợp role; 6 trường hợp 401 |
| Inventory | tồn ban đầu dương, bằng 0, disable khi còn tồn | ADJUSTMENT đúng user/volume; không ghi thừa; 409 |
| Integration (chưa xác nhận) | MySQL thật, rollback khi insert transaction lỗi, tạo khe trùng đồng thời, JWT thật | Không mất tồn, không ghi dở dang, không lộ tenant |
| E2E (giai đoạn sau) | Kiosk/order/dispense với slot DISABLED/ERROR/EMPTY | Không cho phun hoặc tạo giao dịch không hợp lệ |

Chạy regression chung một lần sau khi hoàn thiện source. Không cần lặp lại toàn bộ Postman chỉ để chứng minh các mock test đã PASS; bằng chứng tích hợp vẫn được theo dõi riêng.


## TASK-021 — Kiểm thử tích hợp và nghiệm thu Sprint 4

| ID | Kịch bản | Kỳ vọng |
|---|---|---|
| IT-DB-001 | Khởi động Spring context | JdbcTemplate được inject; Hibernate validate thành công |
| IT-DB-002 | SELECT DATABASE() | scentstation_test |
| IT-DB-003 | Đếm base table | 21 |
| IT-DB-004 | SELECT 1 | 1 |

Chạy opt-in theo README. Regression cuối gồm 341 test, trong đó 39 integration. Đối chiếu Postman cũ; thay luồng Register bằng cấp tài khoản có kiểm soát. Không đánh đồng mock test với E2E. Báo cáo chi tiết nằm trong test-report.md.

## Kịch bản tích hợp cuối

Chuẩn bị schema test 21 bảng và seed 5 role; bật biến môi trường theo README. Authentication/BusinessFlow dùng MockMvc, JWT thật, fixture UUID, transaction rollback. Kiểm tra DB đúng trước ghi. Kỳ vọng toàn bộ assertion thành công; tên method dưới đây là định danh kịch bản, kết quả thực tế xem test-report.

### DatabaseIntegrationTest

| ID | Method | Kỳ vọng |
|---|---|---|
| DAT-01 | `applicationContextShouldLoad` | Assertion thành công |
| DAT-02 | `shouldConnectToTestDatabase` | Assertion thành công |
| DAT-03 | `shouldContainAllRequiredTables` | Assertion thành công |
| DAT-04 | `shouldConnectToMySqlServer` | Assertion thành công |

### AuthenticationIntegrationTest

| ID | Method | Kỳ vọng |
|---|---|---|
| AUT-01 | `shouldLoginWithUsername` | Assertion thành công |
| AUT-02 | `shouldLoginWithEmail` | Assertion thành công |
| AUT-03 | `shouldRejectIncorrectPassword` | Assertion thành công |
| AUT-04 | `shouldRejectUnknownUser` | Assertion thành công |
| AUT-05 | `shouldRejectInvalidLoginRequest` | Assertion thành công |
| AUT-06 | `validJwtShouldAccessProtectedApi` | Assertion thành công |
| AUT-07 | `missingJwtShouldReturn401` | Assertion thành công |
| AUT-08 | `invalidJwtShouldReturn401` | Assertion thành công |
| AUT-09 | `tamperedJwtShouldReturn401` | Assertion thành công |
| AUT-10 | `shouldCreateTenantAndBrandAdminThenLogin` | Assertion thành công |
| AUT-11 | `createdUserPasswordShouldBeHashedWithBCrypt` | Assertion thành công |
| AUT-12 | `brandAdminShouldCreateEmployeeInOwnTenant` | Assertion thành công |
| AUT-13 | `shouldRejectDuplicateEmail` | Assertion thành công |
| AUT-14 | `shouldRejectDuplicateUsername` | Assertion thành công |
| AUT-15 | `technicianShouldNotAccessUserManagement` | Assertion thành công |
| AUT-16 | `brandAdminShouldNotCreatePlatformSuperAdmin` | Assertion thành công |
| AUT-17 | `brandAdminShouldNotReadAnotherTenantUser` | Assertion thành công |
| AUT-18 | `lockedUserShouldNotLogin` | Assertion thành công |
| AUT-19 | `lockedUserExistingJwtShouldBeRejected` | Assertion thành công |
| AUT-20 | `brandAdminShouldNotCreateTenant` | Assertion thành công |
| AUT-21 | `publicRegistrationShouldNotBeAllowed` | Assertion thành công |

### BusinessFlowIntegrationTest

| ID | Method | Kỳ vọng |
|---|---|---|
| BUS-01 | `shouldCompleteBusinessConfigurationFlow` | Assertion thành công |
| BUS-02 | `shouldRecordInitialInventoryTransaction` | Assertion thành công |
| BUS-03 | `shouldRejectCrossTenantLocationAccess` | Assertion thành công |
| BUS-04 | `shouldRejectCrossTenantProductAccess` | Assertion thành công |
| BUS-05 | `shouldRejectCrossTenantMachineAccess` | Assertion thành công |
| BUS-06 | `shouldRejectCrossTenantSlotAccess` | Assertion thành công |
| BUS-07 | `shouldRejectProductAssignmentFromAnotherTenant` | Assertion thành công |
| BUS-08 | `shouldRejectDuplicateSlotNumber` | Assertion thành công |
| BUS-09 | `technicianShouldReadButNotCreateMachineSlot` | Assertion thành công |
| BUS-10 | `inventoryStaffShouldCreateSlotButNotDisableIt` | Assertion thành công |
| BUS-11 | `shouldRejectDisablingSlotWithRemainingInventory` | Assertion thành công |
| BUS-12 | `shouldSoftDisableEmptySlot` | Assertion thành công |
| BUS-13 | `platformAdminShouldReadResourcesAcrossTenants` | Assertion thành công |
| BUS-14 | `protectedBusinessEndpointsShouldRequireJwt` | Assertion thành công |

## Account Management & Tenant Identity — Test Plan

### Testing objectives

- Verify authentication and tenant identity.
- Verify current user profile retrieval.
- Verify password change and administrative password reset.
- Verify mandatory password change enforcement.
- Verify JWT revocation.
- Verify role-based access control and tenant isolation.

### Test scenarios

| ID | Scenario | Expected result |
|---|---|---|
| AM-01 | Login with valid credentials | HTTP 200 and tenant identity |
| AM-02 | Retrieve current user profile | HTTP 200 |
| AM-03 | Change password successfully | HTTP 200 |
| AM-04 | Access API using revoked JWT | HTTP 401 |
| AM-05 | Login with new password | HTTP 200 |
| AM-06 | Change password with incorrect current password | HTTP 400 |
| AM-07 | Administrator resets password | HTTP 200 |
| AM-08 | Login with temporary password | HTTP 200 |
| AM-09 | Access business API before mandatory password change | HTTP 403 |
| AM-10 | Complete mandatory password change | HTTP 200 and restored access |
| AM-11 | Cross-tenant password reset | HTTP 404 |
| AM-12 | Unauthorized employee password reset | HTTP 403 |

### Testing tools

- JUnit
- Mockito
- Spring Boot Test
- MockMvc
- Maven Surefire
- Postman
- MySQL Test Database

### Acceptance criteria

- All automated tests pass.
- All planned manual API scenarios pass.
- Revoked JWTs cannot access protected resources.
- Unauthorized users cannot reset passwords.
- Tenant isolation is maintained.
- Mandatory password changes are enforced by the backend.