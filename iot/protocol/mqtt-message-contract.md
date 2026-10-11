# MQTT Message Contract (Draft)

Trạng thái: bản nháp, cần nhóm thống nhất.

## Topic dự kiến
- `scentstation/{machineId}/status`
- `scentstation/{machineId}/command`
- `scentstation/{machineId}/event`

## Nguyên tắc
- Mỗi lệnh có `commandId` duy nhất, định danh máy và khe đích.
- Thiết bị chống xử lý lặp cùng lệnh.
- Event phản hồi tham chiếu `commandId` và kết quả thực tế.
- Timeout không có nghĩa lệnh chắc chắn chưa chạy.
- Kết quả không xác định cần đối soát; không tự động phun lần hai.

Payload JSON chính thức sẽ được bổ sung sau khi backend và IoT thống nhất.
