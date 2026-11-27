# TASK-027: IoT Monitoring & Alerts

## 1. Mục tiêu
- Lắng nghe các sự kiện IoT (`Heartbeat`, `Telemetry`, `DeviceFault`, `DeviceAck`, `DeviceResult`) thông qua Spring Events.
- Cập nhật trạng thái kết nối (`ONLINE` / `OFFLINE`) và thời điểm ping gần nhất (`lastPingAt`) của máy mà không thay đổi `operationalStatus`.
- Lưu trữ dữ liệu cảm biến (`sensor_readings`) và nhật ký sự kiện thiết bị (`device_events`), không ghi đè tồn kho `currentVolumeMl`.
- Tự động tạo và quản lý vòng đời 5 loại cảnh báo (`LOW_STOCK`, `EMPTY_SLOT`, `MACHINE_OFFLINE`, `DEVICE_ERROR`, `INVENTORY_MISMATCH`), chống tạo trùng cảnh báo đang mở.
- Cung cấp REST API cho Dashboard Monitoring và quản lý Alert có phân quyền RBAC và cô lập dữ liệu theo `tenantId`.

## 2. Danh sách file đã tạo
- **Entity:** `Alert.java`, `AlertStatus.java`, `AlertType.java`, `SensorReading.java`, `DeviceEvent.java`
- **Repository:** `AlertRepository.java`, `SensorReadingRepository.java`, `DeviceEventRepository.java`, `MonitoringLockRepository.java`
- **DTO & Mapper:** `AlertResponse.java`, `MonitoringOverviewResponse.java`, `MachineMonitoringResponse.java`, `SensorReadingResponse.java`, `DeviceEventResponse.java`, `MonitoringMapper.java`
- **Service:** `AlertService.java`, `MonitoringService.java`
- **Event Handler & Scheduler:** `MonitoringEventHandler.java`, `HeartbeatMonitor.java`, `StockAlertJob.java`
- **Controller:** `AlertController.java`, `MonitoringController.java`
- **Test:** `AlertServiceTest.java`, `MonitoringServiceTest.java`

## 3. Luồng xử lý & Vòng đời Alert
- **Heartbeat & Offline:** `MonitoringEventHandler` nhận `Heartbeat` → khóa dòng `Machine` (`PESSIMISTIC_WRITE`), kiểm tra `deviceIdentity` và thứ tự thời gian → cập nhật `ONLINE` và tự động `RESOLVE` cảnh báo `MACHINE_OFFLINE`. `HeartbeatMonitor` định kỳ quét máy quá hạn ping để chuyển `OFFLINE` và tạo `MACHINE_OFFLINE`.
- **Telemetry & Tồn kho:** Lưu bản ghi vào `sensor_readings`; nếu chênh lệch giữa cảm biến và `currentVolumeMl` vượt ngưỡng cho phép thì tạo `INVENTORY_MISMATCH`. `StockAlertJob` định kỳ kiểm tra slot để tạo `LOW_STOCK` hoặc `EMPTY_SLOT`.
- **Vòng đời Alert:**
  - `OPEN` → `ACKNOWLEDGED`, `RESOLVED`, `DISMISSED`
  - `ACKNOWLEDGED` → `RESOLVED`, `DISMISSED`
  - `RESOLVED` / `DISMISSED` → trạng thái kết thúc (từ chối chuyển tiếp)

## 4. API Endpoints
- `GET /api/v1/monitoring/overview`
- `GET /api/v1/monitoring/machines/{machineId}`
- `GET /api/v1/monitoring/machines/{machineId}/sensor-readings`
- `GET /api/v1/monitoring/machines/{machineId}/device-events`
- `GET /api/v1/alerts`
- `POST /api/v1/alerts/{id}/acknowledge`
- `POST /api/v1/alerts/{id}/resolve`
- `POST /api/v1/alerts/{id}/dismiss`

## 5. Giới hạn trách nhiệm & Giả định
- Không can thiệp MQTT client, Simulator, logic trừ kho `currentVolumeMl`, luồng Order/Payment/DispenseCommand hay Frontend.
- Các sự kiện đầu vào từ module MQTT tuân thủ contract của TASK-022.