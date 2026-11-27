# TASK-023 — Kiosk Dispense Control

## Phạm vi đã triển khai

- Lưu `DispenseCommand` trước khi publish và tạo token duy nhất.
- Ràng buộc tối đa một command cho mỗi order bằng cả service check và unique constraint hiện hữu.
- Kiểm tra order đã `PAID`/`DISPENSING`, machine `ONLINE` + `ACTIVE`, slot còn đủ dung dịch, product `ACTIVE` và cùng tenant.
- Nhận ACK/result idempotent; result trùng không tạo execution thứ hai.
- Chỉ dispatch command khi order đã chuyển sang `DISPENSING`; command `PENDING` không bị publish sớm.
- Chuyển command đang chờ ACK sang `UNCERTAIN` khi timeout; không tự retry command chưa xác định.
- Cung cấp `DispensePort`, `DispenseRequest`, `CommandView` và `DispenseNotice` để TASK-024/025/022 tích hợp.
- API chỉ đọc command/execution với RBAC cho platform admin, brand admin, operations manager và technician. Không mở POST public.

## Ngoài phạm vi

Task này không triển khai MQTT client, thay đổi order/payment status, trừ tồn kho, simulator hoặc frontend. `DispensePublisher` là abstraction để TASK-022 nối vào.

## Kiểm thử

`DispenseServiceTest` bao phủ tạo command hợp lệ, từ chối order chưa thanh toán và chống command trùng order.

## Xác minh cục bộ

Kiểm tra tĩnh không phát hiện lỗi ở các file mới. Maven test cần JDK phù hợp với `java.version` hiện tại trong `backend/pom.xml`.
