# TASK-037 — IoT Device Simulator (JavaFX)

## 1. Tổng quan
Xây dựng ứng dụng Desktop độc lập bằng **Java 21 + JavaFX 21** tại `iot/simulator/` nhằm mô phỏng toàn bộ hành vi phần cứng của trạm nước hoa ScentStation (ESP32 controller, pump actuator, fragrance slots, fluid level sensor) và giao tiếp MQTT hai chiều với hệ thống theo contract của **TASK-022**.

## 2. Kiến trúc & Các thành phần đã triển khai
- **Cấu hình (`config/SimulatorConfig.java`, `simulator.properties`)**: Hỗ trợ cấu hình mặc định và tham số CLI (`--machine-id=...`, `--device-identity=...`) để chạy song song nhiều thiết bị. Không lưu MQTT password ra file cấu hình.
- **SQLite Local Persistence (`persistence/`)**:
  - `SqliteDatabaseManager`: Tự động khởi tạo schema (`slot_state`, `processed_commands`, `simulator_meta`) với chế độ WAL.
  - `SlotStateRepository`: Lưu trữ lượng dung dịch dạng `TEXT` để bảo toàn độ chính xác `BigDecimal`.
  - `ProcessedCommandRepository`: Đảm bảo **Idempotency** bằng transaction SQLite (`IN_PROGRESS` -> `DONE`), từ chối command trùng ID nhưng khác payload nghiệp vụ, và đánh dấu `UNCERTAIN` (`volumeDispensedMl = null`) nếu simulator khởi động lại khi lệnh đang `IN_PROGRESS`.
- **Giao tiếp MQTT (`mqtt/`)**:
  - `MqttClientService`: Kết nối Mosquitto với username/password, chỉ subscribe `scentstation/{machineId}/command` và publish lên `status` & `event`.
  - `MqttMessageHandler`: Parse JSON bằng Jackson, kiểm tra `machineId`, `deviceIdentity`, `slotId`, `volumeMl > 0` trước khi đưa vào `ExecutorService` tuần tự.
- **Nghiệp vụ mô phỏng (`service/`)**:
  - `HeartbeatService` & `TelemetryService`: Gửi heartbeat và dữ liệu mức dung dịch định kỳ trên `ScheduledExecutorService`.
  - `DispenseCommandService`: Xử lý 5 kịch bản `NORMAL`, `DEVICE_ERROR`, `EMPTY_SLOT`, `TIMEOUT`, `OFFLINE`, gửi `ACK` và `RESULT` đúng quy chuẩn.
- **Giao diện JavaFX (`ui/SimulatorController.java`, `fxml/simulator-view.fxml`, `css/simulator.css`)**:
  - Cập nhật UI bất đồng bộ qua `Platform.runLater()`, hiển thị thanh mức dung dịch `ProgressBar`, animation phun sương và MQTT Activity Log trực tiếp.

## 3. Kiểm thử (`src/test/java/...`)
- `DispenseCommandServiceTest`: Kiểm tra 5 kịch bản mô phỏng, chống trừ tồn trùng lặp, từ chối payload xung đột.
- `MqttMessageHandlerTest`: Kiểm tra parse JSON hợp lệ và từ chối JSON lỗi, sai `machineId`, sai `deviceIdentity`, `volumeMl <= 0`.
- `ProcessedCommandRepositoryTest`: Kiểm tra bền vững dữ liệu SQLite qua restart và chuyển trạng thái `IN_PROGRESS` -> `UNCERTAIN`.
