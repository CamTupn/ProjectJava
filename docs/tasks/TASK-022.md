# TASK-022 — MQTT Device Communication

## 1. Scope

This module provides the MQTT transport boundary between Spring Boot and ScentStation devices. It publishes device commands, subscribes to device status/events, validates inbound envelopes, and emits Spring application events. It does **not** call DispenseService, MonitoringService, OrderService, PaymentService, InventoryService, or AlertService.

## 2. Components

- `config/MqttConfig.java`: registers MQTT properties.
- `config/MqttClientConfiguration.java`: creates the Eclipse Paho 1.2.5 client when MQTT is enabled; configures authenticated connection and automatic reconnect.
- `mqtt/MqttProperties.java`: external MQTT configuration and per-machine device identity allowlist.
- `mqtt/MqttGateway.java`: outbound `MqttPort` implementation.
- `mqtt/MqttSubscriber.java`: subscribes on initial connection and every reconnect.
- `mqtt/MqttMessageRouter.java`: validates topic, machine identity and message type; malformed messages are logged and discarded.
- `contract/`: outbound command, inbound event DTOs and `MqttPort` interface.

## 3. Topic and payload contract

Topic prefix defaults to `scentstation`.

| Direction | Topic | QoS | Retain |
|---|---|---:|---|
| Backend → device | `scentstation/{machineId}/command` | 1 (configurable) | false |
| Device → backend | `scentstation/{machineId}/status` | 1 (configurable) | Device/simulator choice; normally false |
| Device → backend | `scentstation/{machineId}/event` | 1 (configurable) | Device/simulator choice; normally false |

All inbound payloads must contain `machineId`, `deviceIdentity`, and `type`. The `machineId` must match the topic segment. `deviceIdentity` must match the server-side allowlist entry for that machine; it is not used as the sole authentication factor. Broker username/password and ACL are the transport authorization boundary.

### Command example

```json
{
  "commandId": "cmd-001",
  "machineId": "simulator-001",
  "commandType": "DISPENSE",
  "slotId": "slot-01",
  "orderId": "order-001",
  "expiresAt": "2026-11-27T12:00:00Z",
  "parameters": { "durationMs": 800 }
}
```

### Status messages

`type=HEARTBEAT`: `machineId`, `deviceIdentity`, `status`, optional `firmwareVersion`, `sentAt`.

`type=TELEMETRY`: `machineId`, `deviceIdentity`, `sensorType`, `readings`, `occurredAt`.

### Event messages

`type=ACK`: `machineId`, `deviceIdentity`, optional `eventId`, `commandId`, `status`, `occurredAt`.

`type=RESULT`: `machineId`, `deviceIdentity`, optional `eventId`, `commandId`, `slotId`, `result`, `errorCode`, `occurredAt`.

`type=FAULT`: `machineId`, `deviceIdentity`, `faultCode`, `message`, `severity`, `occurredAt`.

These DTOs are transport contracts only. Consumers may handle them with `@EventListener`; business side effects belong to their owning tasks.

## 4. Spring events

The router publishes `Heartbeat`, `Telemetry`, `DeviceAck`, `DeviceResult`, and `DeviceFault` through `ApplicationEventPublisher`. No service from another business module is injected into the MQTT package.

## 5. Configuration

MQTT is disabled by default so local development and tests do not require a running broker. Set the following environment variables to enable it:

```powershell
$env:MQTT_ENABLED="true"
$env:MQTT_BROKER_URL="tcp://localhost:1883"
$env:MQTT_USERNAME="scentstation-backend"
$env:MQTT_PASSWORD="<backend-password>"
$env:MQTT_DEVICE_IDENTITY="simulator-device-001"
```

The checked-in local default maps `simulator-001` to the example identity `simulator-device-001`. For each additional machine, add a separate `scentstation.mqtt.devices.<machineId>=<deviceIdentity>` entry in a deployment-specific configuration file or environment-specific config source. The identity is an additional payload allowlist check; broker credentials and ACLs remain the actual transport authorization. Do not commit secrets.

## 6. Mosquitto setup (local only)

1. Install Docker Desktop and start it.
2. Copy `infra/mosquitto/config/aclfile.example` to `infra/mosquitto/config/aclfile`.
3. Generate a password file locally (never commit it):

```powershell
docker run --rm -it -v "${PWD}/infra/mosquitto/config:/mosquitto/config" eclipse-mosquitto:2 mosquitto_passwd -c /mosquitto/config/passwordfile scentstation-backend
docker run --rm -it -v "${PWD}/infra/mosquitto/config:/mosquitto/config" eclipse-mosquitto:2 mosquitto_passwd /mosquitto/config/passwordfile simulator-001
```

4. Start broker from repository root:

```powershell
docker compose -f compose.mqtt.yml up -d
```

5. Check logs: `docker logs scentstation-mosquitto`.
6. Set backend environment variables and run the Spring Boot application.

The compose port is bound to `127.0.0.1` only. Do not expose the broker to the public Internet. Keep `passwordfile`, local data/logs and any real credentials out of Git.

## 7. ACL rules

The example ACL gives `scentstation-backend` permission to publish `scentstation/+/command` and subscribe to all device status/event topics. A device logs in with its own MQTT username equal to its `machineId`; `%u` restricts that account to publishing its own status/event and subscribing to its own command topic. Do not reuse a device account across machines.

## 8. Tests

Run from `backend`:

```powershell
.\mvnw.cmd -Dtest=MqttGatewayTest,MqttMessageRouterTest,MqttSubscriberTest test
.\mvnw.cmd test
```

The focused tests verify command topic/JSON/QoS/retain and event routing/rejection. MQTT-disabled tests do not require Mosquitto. End-to-end interoperability must additionally be checked with TASK-037 simulator against a running local broker.

## 9. TASK-037 integration

Simulator should subscribe to `scentstation/{machineId}/command` using its per-device broker account and publish HEARTBEAT/TELEMETRY to `/status`, and ACK/RESULT/FAULT to `/event`. Ensure its MQTT username is exactly the configured `machineId`, and its `deviceIdentity` matches the backend allowlist. Agree on the command/event field names above before merging both tasks.
