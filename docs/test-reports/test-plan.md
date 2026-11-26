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

Chạy opt-in theo README. Regression bao gồm 229 test hiện có và 4 test database. Kiểm tra lại bằng chứng Postman cũ trước khi bổ sung test; chỉ bổ sung luồng Register/Login/JWT hoặc Tenant/Location/Machine/Product/Slot nếu chưa có bằng chứng. Không đánh đồng mock test với E2E. Báo cáo chi tiết nằm trong test-report.md.
