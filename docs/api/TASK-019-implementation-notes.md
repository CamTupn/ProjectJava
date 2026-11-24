# TASK-019 — Machine Management

## Implemented routes

| Method | Route | Description |
|---|---|---|
| GET | `/api/v1/machines` | List machines; optional `connectionStatus`, `operationalStatus`, `locationId`, `tenantId` |
| GET | `/api/v1/machines/{id}` | Machine detail including nested Location info |
| POST | `/api/v1/machines` | Register a new machine (defaults: `connectionStatus=OFFLINE`, `operationalStatus=ACTIVE`) |
| PUT | `/api/v1/machines/{id}` | Update `machineCode`, `serialNumber`, `deviceIdentity`, `locationId`, `firmwareVersion` |
| DELETE | `/api/v1/machines/{id}` | Soft-delete (`operationalStatus=DISABLED`) |
| PATCH | `/api/v1/machines/{id}/status?status=MAINTENANCE` | Update `operationalStatus` independently (`ACTIVE`, `MAINTENANCE`, `DISABLED`, `ERROR`) |
| PATCH | `/api/v1/machines/{id}/connection-status?status=OFFLINE` | Update `connectionStatus` independently (`ONLINE`, `OFFLINE`) |

## Authorization & Tenant Isolation

- `PLATFORM_SUPER_ADMIN`: manages machines across all tenants (`tenantId` required on create).
- `BRAND_ADMIN`: full CRUD + operational status updates within their own tenant; cannot forge `ONLINE` status on creation or modify `connectionStatus` manually.
- `OPERATIONS_MANAGER`: read machines and update `operationalStatus` within their tenant.
- `TECHNICIAN`: read machines and update `operationalStatus` / `connectionStatus` within their tenant.
- `INVENTORY_STAFF`: denied by default (`403 Forbidden`).
