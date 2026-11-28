# ScentStation IoT Device Simulator (JavaFX)

Ứng dụng Desktop độc lập mô phỏng phần cứng thiết bị IoT (ESP32, máy bơm, bình chứa nước hoa, cảm biến mức dung dịch) cho hệ thống **ScentStation** (TASK-037).

## 1. Yêu cầu môi trường
- **Java 21** (JDK 21)
- **Eclipse Mosquitto Broker** (xem hướng dẫn chạy tại `compose.mqtt.yml` / `infra/mosquitto/`)
- Maven Wrapper (`mvnw` / `mvnw.cmd`) đã đi kèm sẵn trong thư mục `iot/simulator/` (tự động tải JavaFX 21 và các thư viện cần thiết).

## 2. Build & Chạy Unit Test
Trên Windows (Command Prompt / PowerShell):
```cmd
cd iot\simulator
.\mvnw.cmd clean compile
.\mvnw.cmd test
```

## 3. Khởi chạy Simulator
Chạy instance mặc định:
```cmd
.\mvnw.cmd javafx:run
```

Chạy nhiều instance đồng thời bằng tham số CLI:
```cmd
.\mvnw.cmd javafx:run -Djavafx.args="--machine-id=simulator-001 --device-identity=demo-1"
.\mvnw.cmd javafx:run -Djavafx.args="--machine-id=simulator-002 --device-identity=demo-2"
```

## 4. Cấu hình trên giao diện & Kiểm thử
1. Khởi động Mosquitto Broker (`docker compose -f compose.mqtt.yml up -d`).
2. Nhập **Broker URL** (`tcp://localhost:1883`), **Machine ID**, **Device Identity**, **MQTT Username/Password** (khớp tài khoản trong Mosquitto `passwordfile` & `aclfile`).
3. Kiểm tra/cấu hình danh sách khe chứa nước hoa (`SLOT-1`..`SLOT-4`) — dữ liệu tự động lưu vào SQLite tại `iot/simulator/data/simulator-<machineId>.db`.
4. Chọn kịch bản mô phỏng (`NORMAL`, `DEVICE_ERROR`, `EMPTY_SLOT`, `TIMEOUT`, `OFFLINE`) và bấm **Connect Device**.
5. Quan sát Heartbeat/Telemetry định kỳ trên topic `scentstation/{machineId}/status` và gửi lệnh xịt JSON tới `scentstation/{machineId}/command`.
