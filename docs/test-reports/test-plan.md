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
