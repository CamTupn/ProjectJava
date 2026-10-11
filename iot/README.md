# IoT — ScentStation

**Vai trò:** MQTT, gateway, thiết bị và simulator. Nên phát triển simulator trước khi có phần cứng.

## Cấu trúc gợi ý
```text
iot/
├── README.md
├── simulator/README.md
├── protocol/mqtt-message-contract.md
└── firmware/README.md
```

## MQTT topic dự kiến
- `scentstation/{machineId}/status`
- `scentstation/{machineId}/command`
- `scentstation/{machineId}/event`

Topic và payload cần được thống nhất trước khi tích hợp. Mỗi lệnh cần ID duy nhất, chống xử lý trùng và phản hồi tham chiếu đúng lệnh. Timeout không chứng minh rằng lệnh chưa thực thi; kết quả không xác định phải được đối soát, không tự động phun lần hai.
