[api-specification.md](https://github.com/user-attachments/files/33478238/api-specification.md)
# ScentStation — REST API Specification

**Document:** API Specification  
**Version:** 0.1  
**Status:** Draft for Review  
**API Prefix:** `/api/v1`  
**Format:** HTTPS + REST/JSON  
**Backend:** Java 21 + Spring Boot 3.5.x  
**Database:** MySQL 8.4  
**Related tasks:** TASK-002, TASK-003, TASK-004, TASK-006

> This document is the API contract draft for TASK-005. Endpoint names and payloads are designed from the current SRS and system architecture. The final field names, identifiers and enum values must be reconciled with TASK-004 (ERD/Data Dictionary) before implementation.

---

## 1. API Conventions

### 1.1 Base URL

```text
/api/v1
```

Example:

```http
GET /api/v1/products
```

### 1.2 Content Type

Requests containing a body:

```http
Content-Type: application/json
```

Successful responses use:

```http
Content-Type: application/json
```

### 1.3 Authentication

Protected endpoints require:

```http
Authorization: Bearer <access-token>
```

Device endpoints use device authentication appropriate to the IoT deployment. Device credentials/secrets must never be exposed to the kiosk frontend.

### 1.4 Common response envelope

Success:

```json
{
  "success": true,
  "data": {},
  "message": "Request completed successfully",
  "timestamp": "2026-10-15T13:00:00Z"
}
```

Error:

```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "The requested resource was not found",
    "details": []
  },
  "timestamp": "2026-10-15T13:00:00Z",
  "path": "/api/v1/products/123"
}
```

### 1.5 Pagination

List endpoints that may return large datasets should support:

```text
?page=0&size=20&sort=createdAt,desc
```

Recommended response:

```json
{
  "success": true,
  "data": {
    "items": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

### 1.6 Identifier convention

The API uses opaque resource identifiers such as `brandId`, `productId`, `machineId`, `slotId`, `orderId`, `paymentId`, `commandId` and `maintenanceId`.

The exact database type/format is subject to TASK-004. API clients must not depend on the physical database key type.

---

# 2. Roles and Access Control

| Role | Code | Scope |
|---|---|---|
| Platform Super Admin | `PSA` | Entire platform |
| Brand Admin | `BA` | Assigned brand(s) |
| Operations Manager | `OM` | Assigned operational scope |
| Technician | `TECH` | Assigned operational scope |
| Inventory Staff | `INV` | Assigned inventory/brand scope |
| Customer / Kiosk User | `CUST` | Public kiosk experience |
| Device / Gateway | `DEV` | Authenticated device channel |
| Payment Provider | `PAY` | Payment callback only |

All protected endpoints perform authorization on the backend. Hiding a UI button is not considered authorization.

Tenant isolation is mandatory: a user belonging to one brand must not read or modify another brand's data unless their role explicitly grants platform-wide access.

---

# 3. Endpoint Summary

| Module | Method | Path | Access |
|---|---|---|---|
| Authentication | POST | `/auth/login` | Public |
| Authentication | POST | `/auth/refresh` | Public with refresh token |
| Authentication | POST | `/auth/logout` | Authenticated |
| Authentication | GET | `/auth/me` | Authenticated |
| Brands | GET | `/brands` | PSA |
| Brands | POST | `/brands` | PSA |
| Brands | GET | `/brands/{brandId}` | PSA / scoped user |
| Brands | PATCH | `/brands/{brandId}` | PSA |
| Products | GET | `/products` | Public / scoped |
| Products | POST | `/products` | PSA / BA |
| Products | GET | `/products/{productId}` | Public / scoped |
| Products | PATCH | `/products/{productId}` | PSA / BA |
| Products | DELETE | `/products/{productId}` | PSA / BA |
| Machines | GET | `/machines` | PSA / BA / OM / TECH |
| Machines | POST | `/machines` | PSA / BA / OM |
| Machines | GET | `/machines/{machineId}` | Scoped |
| Machines | PATCH | `/machines/{machineId}` | PSA / BA / OM |
| Machines | GET | `/machines/{machineId}/slots` | Scoped |
| Slots | PATCH | `/machines/{machineId}/slots/{slotId}` | BA / OM / INV / TECH |
| Customer Catalog | GET | `/kiosks/{machineId}/products` | Public |
| Orders | POST | `/orders` | CUST |
| Orders | GET | `/orders/{orderId}` | CUST / scoped staff |
| Payments | POST | `/payments/callback` | PAY / simulator |
| Payments | GET | `/orders/{orderId}/payment` | CUST / scoped staff |
| Dispensing | GET | `/orders/{orderId}/dispensing` | CUST / scoped staff |
| Dispensing | POST | `/orders/{orderId}/dispensing` | Backend/internal workflow |
| Device | POST | `/devices/heartbeat` | DEV |
| Device | POST | `/devices/events` | DEV |
| Inventory | GET | `/inventory` | BA / INV |
| Inventory | POST | `/inventory/refills` | INV |
| Inventory | POST | `/inventory/adjustments` | INV / authorized BA |
| Alerts | GET | `/alerts` | OM / TECH / INV |
| Alerts | PATCH | `/alerts/{alertId}` | OM / TECH / INV |
| Maintenance | GET | `/maintenance` | OM / TECH |
| Maintenance | POST | `/maintenance` | OM |
| Maintenance | GET | `/maintenance/{maintenanceId}` | OM / TECH |
| Maintenance | PATCH | `/maintenance/{maintenanceId}` | OM / TECH |
| Reports | GET | `/dashboard/summary` | PSA / BA / OM |
| Audit | GET | `/audit-logs` | PSA / authorized staff |

> `POST /orders/{orderId}/dispensing` is an internal/backend workflow endpoint and should not be exposed as a normal customer action. The customer cannot directly request a successful spray.

---

# 4. Authentication Module

## 4.1 Login

### `POST /api/v1/auth/login`

**Access:** Public

**Purpose:** Authenticate an administrative user.

### Request

```json
{
  "email": "admin@scentstation.local",
  "password": "********"
}
```

### Validation

- `email`: required, valid email format.
- `password`: required.
- Account must be active.
- Invalid credentials must not reveal whether an account exists.

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "accessToken": "<jwt>",
    "refreshToken": "<refresh-token>",
    "expiresIn": 3600,
    "user": {
      "id": "usr_001",
      "email": "admin@scentstation.local",
      "roles": ["PSA"],
      "brandIds": []
    }
  },
  "message": "Login successful"
}
```

### Errors

- `400 VALIDATION_ERROR`
- `401 INVALID_CREDENTIALS`
- `403 ACCOUNT_DISABLED`
- `423 ACCOUNT_LOCKED`
- `500 INTERNAL_ERROR`

---

## 4.2 Refresh Token

### `POST /api/v1/auth/refresh`

**Access:** Refresh token

### Request

```json
{
  "refreshToken": "<refresh-token>"
}
```

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "accessToken": "<new-jwt>",
    "expiresIn": 3600
  }
}
```

### Errors

- `400 VALIDATION_ERROR`
- `401 INVALID_REFRESH_TOKEN`
- `401 REFRESH_TOKEN_EXPIRED`

---

## 4.3 Logout

### `POST /api/v1/auth/logout`

**Access:** Authenticated

### Request

```json
{
  "refreshToken": "<refresh-token>"
}
```

### Response `204 No Content`

### Errors

- `401 UNAUTHORIZED`
- `404 SESSION_NOT_FOUND`

---

## 4.4 Current User

### `GET /api/v1/auth/me`

**Access:** Authenticated

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "id": "usr_001",
    "email": "admin@scentstation.local",
    "roles": ["PSA"],
    "brandIds": []
  }
}
```

---

# 5. Brand Management

## 5.1 List Brands

### `GET /api/v1/brands`

**Access:** `PSA`

### Query parameters

```text
?page=0&size=20&status=ACTIVE
```

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "brand_001",
        "name": "Scent Brand",
        "description": "Example brand",
        "status": "ACTIVE"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### Errors

- `401 UNAUTHORIZED`
- `403 FORBIDDEN`

---

## 5.2 Create Brand

### `POST /api/v1/brands`

**Access:** `PSA`

### Request

```json
{
  "name": "Scent Brand",
  "description": "Example perfume brand",
  "status": "ACTIVE"
}
```

### Validation

- `name`: required, non-blank.
- Name/identifier must be unique according to the finalized ERD policy.
- `status`: `ACTIVE` or `INACTIVE`.

### Response `201 Created`

```json
{
  "success": true,
  "data": {
    "id": "brand_001",
    "name": "Scent Brand",
    "description": "Example perfume brand",
    "status": "ACTIVE"
  },
  "message": "Brand created successfully"
}
```

### Errors

- `400 VALIDATION_ERROR`
- `409 BRAND_ALREADY_EXISTS`
- `403 FORBIDDEN`

---

## 5.3 Get Brand

### `GET /api/v1/brands/{brandId}`

**Access:** `PSA`; scoped users may access only assigned brands.

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "id": "brand_001",
    "name": "Scent Brand",
    "description": "Example perfume brand",
    "status": "ACTIVE"
  }
}
```

### Errors

- `401 UNAUTHORIZED`
- `403 TENANT_ACCESS_DENIED`
- `404 BRAND_NOT_FOUND`

---

## 5.4 Update Brand

### `PATCH /api/v1/brands/{brandId}`

**Access:** `PSA`

### Request

```json
{
  "name": "Updated Brand",
  "description": "Updated description",
  "status": "ACTIVE"
}
```

### Response `200 OK`

Returns the updated brand.

### Errors

- `400 VALIDATION_ERROR`
- `403 FORBIDDEN`
- `404 BRAND_NOT_FOUND`
- `409 BRAND_ALREADY_EXISTS`

---

# 6. Product Module

## 6.1 List Products

### `GET /api/v1/products`

**Access:** Public for kiosk catalog; authenticated for management views.

### Query parameters

```text
?brandId=brand_001&status=ACTIVE&page=0&size=20
```

For the customer catalog, only products that are active and currently available through a valid machine/slot should be returned.

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "prod_001",
        "brandId": "brand_001",
        "name": "Ocean Mist",
        "description": "Fresh aquatic fragrance",
        "pricePerSpray": 15000,
        "currency": "VND",
        "status": "ACTIVE"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## 6.2 Create Product

### `POST /api/v1/products`

**Access:** `PSA`, `BA`

### Request

```json
{
  "brandId": "brand_001",
  "name": "Ocean Mist",
  "description": "Fresh aquatic fragrance",
  "imageUrl": "https://example.local/ocean-mist.jpg",
  "scentNotes": ["Citrus", "Marine", "Woody"],
  "pricePerSpray": 15000,
  "currency": "VND",
  "status": "ACTIVE"
}
```

### Validation

- `brandId`: required and inside caller scope.
- `name`: required, non-blank.
- `pricePerSpray`: positive.
- `currency`: supported currency.
- `status`: `ACTIVE` or `INACTIVE`.

### Response `201 Created`

Returns the created product.

### Errors

- `400 VALIDATION_ERROR`
- `403 TENANT_ACCESS_DENIED`
- `404 BRAND_NOT_FOUND`
- `409 PRODUCT_ALREADY_EXISTS`

---

## 6.3 Get Product

### `GET /api/v1/products/{productId}`

**Access:** Public for public catalog data; scoped users for management data.

### Errors

- `404 PRODUCT_NOT_FOUND`
- `403 TENANT_ACCESS_DENIED`

---

## 6.4 Update Product

### `PATCH /api/v1/products/{productId}`

**Access:** `PSA`, `BA`

### Validation

Same validation rules as create. A product outside the caller's tenant scope must be rejected.

### Response

`200 OK` with updated product.

---

## 6.5 Deactivate Product

### `DELETE /api/v1/products/{productId}`

**Access:** `PSA`, `BA`

**Behavior:** Logical deactivation is preferred over physical deletion when the product is referenced by orders/history.

### Response `204 No Content`

### Errors

- `403 TENANT_ACCESS_DENIED`
- `404 PRODUCT_NOT_FOUND`
- `409 PRODUCT_IN_USE`

---

# 7. Machine and Slot Module

## 7.1 List Machines

### `GET /api/v1/machines`

**Access:** `PSA`, `BA`, `OM`, `TECH`

### Query

```text
?brandId=brand_001&status=ONLINE&page=0&size=20
```

### Response

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "machine_001",
        "code": "KIOSK-001",
        "brandId": "brand_001",
        "location": "Store A",
        "status": "ONLINE",
        "lastSeenAt": "2026-10-15T12:59:30Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## 7.2 Register Machine

### `POST /api/v1/machines`

**Access:** `PSA`, `BA`, `OM`

### Request

```json
{
  "code": "KIOSK-001",
  "brandId": "brand_001",
  "location": "Store A",
  "status": "ACTIVE"
}
```

### Validation

- `code`: required and unique.
- `brandId`: required and authorized.
- `location`: required according to deployment policy.
- Machine must not be registered to an unauthorized tenant.

### Response `201 Created`

Returns machine data.

### Errors

- `400 VALIDATION_ERROR`
- `403 TENANT_ACCESS_DENIED`
- `404 BRAND_NOT_FOUND`
- `409 MACHINE_CODE_ALREADY_EXISTS`

---

## 7.3 Get Machine

### `GET /api/v1/machines/{machineId}`

**Access:** Scoped administrative users.

Returns machine status, `lastSeenAt`, and high-level operational state.

---

## 7.4 Update Machine

### `PATCH /api/v1/machines/{machineId}`

**Access:** `PSA`, `BA`, `OM`

### Request

```json
{
  "location": "Store B",
  "status": "ACTIVE"
}
```

### Errors

- `400 VALIDATION_ERROR`
- `403 TENANT_ACCESS_DENIED`
- `404 MACHINE_NOT_FOUND`

---

## 7.5 List Machine Slots

### `GET /api/v1/machines/{machineId}/slots`

**Access:** `PSA`, `BA`, `OM`, `TECH`, `INV`

### Response

```json
{
  "success": true,
  "data": {
    "machineId": "machine_001",
    "slots": [
      {
        "id": "slot_001",
        "slotCode": "SLOT-01",
        "productId": "prod_001",
        "cartridgeId": "cart_001",
        "status": "READY",
        "estimatedRemaining": 85
      }
    ]
  }
}
```

---

## 7.6 Update Slot

### `PATCH /api/v1/machines/{machineId}/slots/{slotId}`

**Access:** `BA`, `OM`, `INV`, `TECH`

### Request

```json
{
  "productId": "prod_001",
  "cartridgeId": "cart_001",
  "status": "READY",
  "lowStockThreshold": 20
}
```

### Validation

- `slotId` must belong to `machineId`.
- Product must exist and belong to the same tenant.
- Cartridge must be valid for the selected product.
- A slot can have only one active cartridge at a time.
- A slot must not be marked ready if the machine is under maintenance or has a critical fault.

### Errors

- `400 VALIDATION_ERROR`
- `403 TENANT_ACCESS_DENIED`
- `404 MACHINE_NOT_FOUND`
- `404 SLOT_NOT_FOUND`
- `409 SLOT_NOT_AVAILABLE`

---

# 8. Kiosk Catalog Module

## 8.1 Get Available Products for a Machine

### `GET /api/v1/kiosks/{machineId}/products`

**Access:** Public kiosk request

**Purpose:** Return only products that can currently be ordered from the specified kiosk.

### Validation

The backend must verify:

- Machine exists.
- Machine is online.
- Machine is operational.
- Slot is active/ready.
- Cartridge exists and has sufficient estimated inventory.
- Product is active.

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "machineId": "machine_001",
    "products": [
      {
        "productId": "prod_001",
        "slotId": "slot_001",
        "name": "Ocean Mist",
        "pricePerSpray": 15000,
        "currency": "VND",
        "available": true
      }
    ]
  }
}
```

### Errors

- `404 MACHINE_NOT_FOUND`
- `409 MACHINE_OFFLINE`
- `409 MACHINE_UNAVAILABLE`

---

# 9. Order Module

## 9.1 Create Experience Order

### `POST /api/v1/orders`

**Access:** `CUST`

### Request

```json
{
  "machineId": "machine_001",
  "slotId": "slot_001",
  "productId": "prod_001"
}
```

### Validation

- `machineId`, `slotId`, `productId`: required.
- Slot must belong to machine.
- Product must match the slot.
- Machine must be online and operational.
- Slot must be ready.
- Product must be active.
- Inventory must be sufficient.
- Server determines the payable amount; client-provided price is not trusted.
- Server stores product/price snapshot.
- A new order is initially `PENDING_PAYMENT`.
- The order is bound to exactly one machine and slot.

### Response `201 Created`

```json
{
  "success": true,
  "data": {
    "orderId": "order_001",
    "machineId": "machine_001",
    "slotId": "slot_001",
    "productId": "prod_001",
    "productName": "Ocean Mist",
    "amount": 15000,
    "currency": "VND",
    "status": "PENDING_PAYMENT",
    "expiresAt": "2026-10-15T13:10:00Z",
    "payment": {
      "method": "QR",
      "qrPayload": "<payment-payload>"
    }
  },
  "message": "Order created successfully"
}
```

### Errors

- `400 VALIDATION_ERROR`
- `404 MACHINE_NOT_FOUND`
- `404 SLOT_NOT_FOUND`
- `404 PRODUCT_NOT_FOUND`
- `409 MACHINE_OFFLINE`
- `409 MACHINE_UNAVAILABLE`
- `409 SLOT_EMPTY`
- `409 PRODUCT_UNAVAILABLE`
- `422 ORDER_CREATION_FAILED`

---

## 9.2 Get Order

### `GET /api/v1/orders/{orderId}`

**Access:** Customer who owns the kiosk session/order; authorized staff within tenant scope.

### Response

```json
{
  "success": true,
  "data": {
    "orderId": "order_001",
    "status": "PAID",
    "amount": 15000,
    "currency": "VND",
    "machineId": "machine_001",
    "slotId": "slot_001",
    "productId": "prod_001",
    "expiresAt": "2026-10-15T13:10:00Z"
  }
}
```

### Errors

- `401 UNAUTHORIZED`
- `403 ORDER_ACCESS_DENIED`
- `404 ORDER_NOT_FOUND`

---

# 10. Payment Module

## 10.1 Payment Callback / Webhook

### `POST /api/v1/payments/callback`

**Access:** `PAY` / payment simulator

> This endpoint is not a customer endpoint.

### Request

```json
{
  "providerReference": "PAY-20261015-0001",
  "orderId": "order_001",
  "amount": 15000,
  "currency": "VND",
  "status": "SUCCESS",
  "paidAt": "2026-10-15T13:05:20Z",
  "signature": "<provider-signature>"
}
```

### Validation

- Provider/device source must be authenticated.
- Signature must be verified according to provider integration.
- `orderId` must exist.
- Callback amount must equal the order amount.
- Callback currency must match the order currency.
- Transaction/provider reference must be valid.
- Duplicate callbacks must be idempotent.
- Expired/terminal orders must not trigger unintended dispensing.

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "paymentId": "pay_001",
    "orderId": "order_001",
    "status": "SUCCESS",
    "idempotent": false
  },
  "message": "Payment processed"
}
```

### Errors

- `400 INVALID_CALLBACK`
- `401 INVALID_PAYMENT_SIGNATURE`
- `404 ORDER_NOT_FOUND`
- `409 PAYMENT_AMOUNT_MISMATCH`
- `409 ORDER_ALREADY_PROCESSED`

---

## 10.2 Get Order Payment

### `GET /api/v1/orders/{orderId}/payment`

**Access:** Customer/order owner or authorized staff.

### Response

```json
{
  "success": true,
  "data": {
    "paymentId": "pay_001",
    "orderId": "order_001",
    "providerReference": "PAY-20261015-0001",
    "amount": 15000,
    "currency": "VND",
    "status": "SUCCESS"
  }
}
```

---

# 11. Dispensing Module

## 11.1 Create Dispensing Command

### `POST /api/v1/orders/{orderId}/dispensing`

**Access:** Backend/internal workflow only

**Purpose:** Create and dispatch a unique spray command after verified payment.

### Request

```json
{
  "machineId": "machine_001",
  "slotId": "slot_001"
}
```

### Validation

- Order must exist.
- Order must be successfully paid.
- Order must not already have a successful dispensing.
- Machine must match the order.
- Slot must match the order.
- Machine must be online.
- Machine must not be under maintenance.
- Slot must be ready.
- Command ID must be generated uniquely by backend.
- Command must have an expiry time.
- No client may directly mark dispensing as successful.

### Response `202 Accepted`

```json
{
  "success": true,
  "data": {
    "commandId": "cmd_001",
    "orderId": "order_001",
    "machineId": "machine_001",
    "slotId": "slot_001",
    "status": "SENT",
    "expiresAt": "2026-10-15T13:06:30Z"
  },
  "message": "Dispensing command dispatched"
}
```

### Errors

- `404 ORDER_NOT_FOUND`
- `409 PAYMENT_NOT_CONFIRMED`
- `409 DISPENSING_ALREADY_SUCCESSFUL`
- `409 MACHINE_OFFLINE`
- `409 MACHINE_UNAVAILABLE`
- `409 SLOT_NOT_READY`
- `409 DUPLICATE_COMMAND`

---

## 11.2 Get Dispensing Status

### `GET /api/v1/orders/{orderId}/dispensing`

**Access:** Customer/order owner or authorized operational staff.

### Response

```json
{
  "success": true,
  "data": {
    "orderId": "order_001",
    "commandId": "cmd_001",
    "status": "SUCCESS",
    "result": "SPRAY_COMPLETED",
    "completedAt": "2026-10-15T13:05:45Z"
  }
}
```

Possible dispensing statuses:

```text
PENDING
SENT
ACKNOWLEDGED
SUCCESS
FAILED
REVIEW_REQUIRED
EXPIRED
```

### Important rule

A timeout or missing response does **not** prove that spraying did not occur. The system must move to `REVIEW_REQUIRED` when the result is uncertain and must not automatically issue a second spray command.

---

# 12. IoT Device Module

## 12.1 Heartbeat

### `POST /api/v1/devices/heartbeat`

**Access:** `DEV`

### Request

```json
{
  "machineId": "machine_001",
  "sentAt": "2026-10-15T13:05:00Z",
  "status": "ONLINE"
}
```

### Validation

- Device must be authenticated.
- `machineId` must belong to the authenticated device.
- Timestamp must be valid.
- Payload schema must be valid.

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "machineId": "machine_001",
    "status": "ONLINE",
    "lastSeenAt": "2026-10-15T13:05:00Z"
  }
}
```

### Errors

- `400 INVALID_DEVICE_PAYLOAD`
- `401 DEVICE_UNAUTHORIZED`
- `404 DEVICE_NOT_FOUND`

---

## 12.2 Device Event / Telemetry

### `POST /api/v1/devices/events`

**Access:** `DEV`

### Request

```json
{
  "machineId": "machine_001",
  "eventId": "evt_001",
  "eventType": "DISPENSING_RESULT",
  "commandId": "cmd_001",
  "slotId": "slot_001",
  "result": "SUCCESS",
  "errorCode": null,
  "occurredAt": "2026-10-15T13:05:45Z"
}
```

### Validation

- Device must be authenticated.
- `machineId` must match authenticated device.
- `eventId` must be unique/idempotent.
- `commandId` is required for dispensing-related events.
- Event type and payload must match the schema.
- Event must not modify another machine.

### Response `202 Accepted`

```json
{
  "success": true,
  "data": {
    "eventId": "evt_001",
    "accepted": true
  }
}
```

### Errors

- `400 INVALID_DEVICE_PAYLOAD`
- `401 DEVICE_UNAUTHORIZED`
- `404 MACHINE_NOT_FOUND`
- `409 DUPLICATE_EVENT`

---

# 13. Inventory and Refill Module

## 13.1 Get Inventory

### `GET /api/v1/inventory`

**Access:** `BA`, `INV`

### Query

```text
?machineId=machine_001&slotId=slot_001&lowStock=true&page=0&size=20
```

### Response

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "machineId": "machine_001",
        "slotId": "slot_001",
        "cartridgeId": "cart_001",
        "estimatedRemaining": 18,
        "lowStockThreshold": 20,
        "status": "LOW"
      }
    ]
  }
}
```

---

## 13.2 Record Refill

### `POST /api/v1/inventory/refills`

**Access:** `INV`

### Request

```json
{
  "machineId": "machine_001",
  "slotId": "slot_001",
  "cartridgeId": "cart_001",
  "operation": "REFILL",
  "quantityAdded": 100,
  "note": "Refilled cartridge"
}
```

### Validation

- Machine and slot must exist.
- Slot must belong to machine.
- Cartridge must be valid.
- `quantityAdded` must be positive.
- Required fields cannot be null.
- User must have inventory permission.
- The operation must be recorded with actor and timestamp.

### Response `201 Created`

```json
{
  "success": true,
  "data": {
    "refillId": "refill_001",
    "machineId": "machine_001",
    "slotId": "slot_001",
    "quantityAdded": 100,
    "estimatedRemaining": 118
  },
  "message": "Refill recorded successfully"
}
```

### Errors

- `400 VALIDATION_ERROR`
- `403 FORBIDDEN`
- `404 SLOT_NOT_FOUND`
- `409 INVALID_CARTRIDGE`

---

## 13.3 Inventory Adjustment

### `POST /api/v1/inventory/adjustments`

**Access:** `INV`, authorized `BA`

### Request

```json
{
  "machineId": "machine_001",
  "slotId": "slot_001",
  "quantity": -5,
  "reason": "Manual stock correction"
}
```

### Validation

- Quantity must be non-zero.
- Reason is required.
- Machine/slot must exist and be in scope.
- Adjustment actor must be authorized.

### Response

`201 Created` with updated inventory and adjustment record.

### Errors

- `400 VALIDATION_ERROR`
- `403 FORBIDDEN`
- `404 SLOT_NOT_FOUND`

---

# 14. Alerts Module

## 14.1 List Alerts

### `GET /api/v1/alerts`

**Access:** `OM`, `TECH`, `INV`

### Query

```text
?machineId=machine_001&severity=HIGH&status=OPEN&page=0&size=20
```

### Response

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "alert_001",
        "machineId": "machine_001",
        "slotId": "slot_001",
        "type": "LOW_STOCK",
        "severity": "MEDIUM",
        "status": "OPEN",
        "description": "Estimated inventory is below threshold",
        "createdAt": "2026-10-15T12:30:00Z"
      }
    ]
  }
}
```

---

## 14.2 Update Alert

### `PATCH /api/v1/alerts/{alertId}`

**Access:** `OM`, `TECH`, `INV`

### Request

```json
{
  "status": "ACKNOWLEDGED",
  "note": "Technician has been notified"
}
```

### Validation

- Alert must exist.
- Alert must be inside the user's operational scope.
- Status transition must be valid.
- Note is required when required by the workflow.

### Errors

- `400 VALIDATION_ERROR`
- `403 TENANT_ACCESS_DENIED`
- `404 ALERT_NOT_FOUND`
- `409 INVALID_ALERT_STATE`

---

# 15. Maintenance Module

## 15.1 List Maintenance Tickets

### `GET /api/v1/maintenance`

**Access:** `OM`, `TECH`

### Query

```text
?machineId=machine_001&status=OPEN&page=0&size=20
```

---

## 15.2 Create Maintenance Ticket

### `POST /api/v1/maintenance`

**Access:** `OM`

### Request

```json
{
  "machineId": "machine_001",
  "description": "Spray mechanism is not responding",
  "priority": "HIGH",
  "assigneeId": "usr_tech_001"
}
```

### Validation

- `machineId`: required and in scope.
- `description`: required and non-blank.
- `priority`: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
- Assignee must have technician role if an assignee is supplied.

### Response `201 Created`

```json
{
  "success": true,
  "data": {
    "maintenanceId": "mnt_001",
    "machineId": "machine_001",
    "status": "OPEN",
    "priority": "HIGH",
    "assigneeId": "usr_tech_001"
  }
}
```

---

## 15.3 Get Maintenance Ticket

### `GET /api/v1/maintenance/{maintenanceId}`

**Access:** `OM`, `TECH`

Returns ticket details and maintenance history.

---

## 15.4 Update Maintenance Ticket

### `PATCH /api/v1/maintenance/{maintenanceId}`

**Access:** `OM`, `TECH`

### Request

```json
{
  "status": "RESOLVED",
  "result": "Replaced spray actuator",
  "note": "Diagnostic test passed"
}
```

### Validation

- Ticket must exist and be in scope.
- Status transition must follow the maintenance workflow.
- A technician can only update tickets assigned to them or within their authorized scope.
- Machine must pass required checks before returning to active service.
- Test spray is recorded separately from customer dispensing.

### Errors

- `400 VALIDATION_ERROR`
- `403 FORBIDDEN`
- `404 MAINTENANCE_NOT_FOUND`
- `409 INVALID_MAINTENANCE_STATE`

---

# 16. Dashboard and Reporting

## 16.1 Dashboard Summary

### `GET /api/v1/dashboard/summary`

**Access:** `PSA`, `BA`, `OM`

### Query

```text
?brandId=brand_001&from=2026-10-01&to=2026-10-15
```

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "machines": {
      "total": 10,
      "online": 8,
      "offline": 2
    },
    "orders": {
      "pendingPayment": 3,
      "paid": 20,
      "completed": 18,
      "failed": 2
    },
    "dispensing": {
      "success": 18,
      "failed": 2,
      "reviewRequired": 0
    },
    "inventory": {
      "lowStockSlots": 4,
      "emptySlots": 1
    },
    "maintenance": {
      "open": 3
    }
  }
}
```

Tenant scope must be applied before calculating statistics.

---

# 17. Audit Log

## 17.1 List Audit Logs

### `GET /api/v1/audit-logs`

**Access:** `PSA`; additional access only if explicitly granted by the RBAC policy.

### Query

```text
?actorId=usr_001&action=DISPENSING&from=2026-10-01&to=2026-10-15&page=0&size=50
```

### Response

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "audit_001",
        "occurredAt": "2026-10-15T13:05:45Z",
        "actor": "SYSTEM",
        "action": "DISPENSING_SUCCESS",
        "objectType": "ORDER",
        "objectId": "order_001",
        "result": "SUCCESS",
        "correlationId": "corr_001"
      }
    ]
  }
}
```

### Rules

- Audit logs are read-only through the API.
- Normal users cannot update/delete audit logs.
- Passwords, secrets and unnecessary sensitive data must never be logged.

---

# 18. Common HTTP Status Codes

| HTTP | Error Code | Meaning |
|---:|---|---|
| 200 | — | Request completed successfully |
| 201 | — | Resource created |
| 202 | — | Request accepted for asynchronous processing |
| 204 | — | Successful request with no response body |
| 400 | `VALIDATION_ERROR` | Invalid request data |
| 401 | `UNAUTHORIZED` | Missing/invalid authentication |
| 401 | `INVALID_CREDENTIALS` | Login credentials invalid |
| 403 | `FORBIDDEN` | Authenticated but not authorized |
| 403 | `TENANT_ACCESS_DENIED` | Resource is outside tenant scope |
| 404 | `RESOURCE_NOT_FOUND` | Requested resource does not exist |
| 409 | `RESOURCE_ALREADY_EXISTS` | Unique resource conflict |
| 409 | `MACHINE_OFFLINE` | Machine cannot accept a new order |
| 409 | `MACHINE_UNAVAILABLE` | Machine cannot currently operate |
| 409 | `SLOT_EMPTY` | Slot/cartridge has insufficient availability |
| 409 | `PRODUCT_UNAVAILABLE` | Product cannot be ordered |
| 409 | `PAYMENT_AMOUNT_MISMATCH` | Callback amount differs from order |
| 409 | `PAYMENT_NOT_CONFIRMED` | Payment has not been verified |
| 409 | `DISPENSING_ALREADY_SUCCESSFUL` | Order already has a successful spray |
| 409 | `DUPLICATE_COMMAND` | Command already processed |
| 409 | `INVALID_ALERT_STATE` | Invalid alert state transition |
| 409 | `INVALID_MAINTENANCE_STATE` | Invalid maintenance state transition |
| 409 | `DUPLICATE_EVENT` | Device event already received |
| 422 | `ORDER_CREATION_FAILED` | Business rule prevents order creation |
| 500 | `INTERNAL_ERROR` | Unexpected server error |
| 503 | `SERVICE_UNAVAILABLE` | Required service temporarily unavailable |

---

# 19. Validation and Business Rules

## 19.1 Tenant isolation

Every resource containing a brand/tenant relationship must be checked against the authenticated user's scope.

The backend must reject cross-tenant access even if the caller knows the resource ID.

## 19.2 Payment

The kiosk must never be allowed to mark an order as paid.

Only a verified payment callback/provider or approved simulator can transition payment to success.

Duplicate payment callbacks must be idempotent.

## 19.3 Dispensing

The dispensing flow must satisfy:

```text
Order created
    ↓
Payment verified
    ↓
Machine/slot rechecked
    ↓
Unique command created
    ↓
Command sent to correct device
    ↓
Device validates safety conditions
    ↓
Spray executed at most once
    ↓
Result received
    ↓
Order/inventory/audit updated
```

A sent command is not proof of a successful spray.

If the result is uncertain:

```text
REVIEW_REQUIRED
```

must be used and no automatic second spray may be generated.

## 19.4 Inventory

Customer inventory is reduced only after a successful dispensing result is confirmed.

Diagnostic/test spray must be recorded separately.

Inventory adjustments require a reason and actor.

## 19.5 Machine availability

A machine that is offline, under maintenance or in a critical-error state must not accept new customer orders.

---

# 20. State Definitions

## 20.1 Order

Recommended states:

```text
PENDING_PAYMENT
PAID
DISPENSING
SUCCESS
FAILED
EXPIRED
REVIEW_REQUIRED
```

## 20.2 Payment

```text
PENDING
SUCCESS
FAILED
EXPIRED
```

## 20.3 Dispensing Command

```text
PENDING
SENT
ACKNOWLEDGED
SUCCESS
FAILED
REVIEW_REQUIRED
EXPIRED
```

## 20.4 Machine

Recommended minimum states:

```text
ACTIVE
OFFLINE
MAINTENANCE
ERROR
```

## 20.5 Slot

Recommended minimum states:

```text
READY
EMPTY
DISABLED
ERROR
```

---

# 21. Core Payment-to-Dispensing Example

### Step 1 — Kiosk creates order

```http
POST /api/v1/orders
```

```json
{
  "machineId": "machine_001",
  "slotId": "slot_001",
  "productId": "prod_001"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "orderId": "order_001",
    "amount": 15000,
    "status": "PENDING_PAYMENT",
    "expiresAt": "2026-10-15T13:10:00Z"
  }
}
```

### Step 2 — Payment provider sends callback

```http
POST /api/v1/payments/callback
```

```json
{
  "providerReference": "PAY-001",
  "orderId": "order_001",
  "amount": 15000,
  "currency": "VND",
  "status": "SUCCESS",
  "signature": "<signature>"
}
```

### Step 3 — Backend verifies payment

Backend changes the payment/order state and creates the controlled dispensing workflow.

### Step 4 — Backend sends unique command

The internal dispensing service creates:

```json
{
  "commandId": "cmd_001",
  "orderId": "order_001",
  "machineId": "machine_001",
  "slotId": "slot_001",
  "expiresAt": "2026-10-15T13:06:30Z"
}
```

The command is delivered through MQTT to the correct device.

### Step 5 — Device responds

```json
{
  "eventId": "evt_001",
  "eventType": "DISPENSING_RESULT",
  "commandId": "cmd_001",
  "machineId": "machine_001",
  "slotId": "slot_001",
  "result": "SUCCESS",
  "errorCode": null
}
```

### Step 6 — Backend finalizes transaction

Backend updates:

- dispensing result;
- order status;
- inventory only after confirmed success;
- audit log.

---

# 22. MQTT / REST Boundary

REST is the external application API between Kiosk/Admin Portal and Backend.

MQTT is the device communication channel and is not exposed directly to the customer frontend.

Recommended MQTT topics from the architecture:

```text
scentstation/{machineId}/status
scentstation/{machineId}/command
scentstation/{machineId}/event
```

Every dispensing command must contain at minimum:

```text
commandId
orderId
machineId
slotId
expiresAt
```

The device/gateway must reject expired commands, wrong-device commands, unsafe commands and duplicate commands.

---

# 23. API-to-SRS Traceability

| API Area | SRS Requirement |
|---|---|
| `/auth/*` | SRS-AUTH-001, SRS-AUTH-002 |
| `/brands/*` | SRS-BRAND-001 |
| `/products/*` | SRS-PROD-001 |
| `/machines/*` | SRS-MACH-001, SRS-MACH-002 |
| `/kiosks/{machineId}/products` | SRS-ORDER-001 |
| `/orders/*` | SRS-ORDER-001 |
| `/payments/*` | SRS-PAY-001 |
| `/orders/{orderId}/dispensing` | SRS-DISP-001, SRS-DISP-002 |
| `/devices/heartbeat` | SRS-IOT-001 |
| `/devices/events` | SRS-IOT-002 |
| `/inventory/*` | SRS-INV-001, SRS-INV-002, SRS-INV-003 |
| `/alerts/*` | SRS-OPS-001 |
| `/maintenance/*` | SRS-MAINT-001 |
| `/dashboard/*` | SRS-REPORT-001 |
| `/audit-logs` | SRS-AUDIT-001 |

---

# 24. ERD Synchronization Checklist

Before the API contract is marked final, TASK-005 must be reviewed against TASK-004 for:

- [ ] Exact table/entity names.
- [ ] Exact PK/FK names.
- [ ] Identifier type and format.
- [ ] `brand_id` / tenant ownership.
- [ ] Product-to-slot relationship.
- [ ] Slot-to-cartridge relationship.
- [ ] Order-to-machine/slot/product relationship.
- [ ] Order price snapshot fields.
- [ ] Payment-to-order relationship.
- [ ] Dispense command-to-order/machine/slot relationship.
- [ ] Dispense result-to-command relationship.
- [ ] Inventory/refill/adjustment history.
- [ ] Alert and maintenance relationships.
- [ ] Audit log fields.
- [ ] Enum/status values.
- [ ] Unique constraints needed for idempotency.

---

# 25. TASK-006 Synchronization Checklist

The API contract must use the same terminology and state transitions as the payment/dispensing sequence diagrams.

In particular:

1. `POST /orders` creates `PENDING_PAYMENT`.
2. Payment callback changes payment/order state only after verification.
3. Successful payment starts the controlled dispensing workflow.
4. A unique `commandId` is generated.
5. MQTT command targets exactly one machine and slot.
6. ACK and result are correlated using `commandId`.
7. Duplicate callback/command/event handling is idempotent.
8. Timeout or uncertain result becomes `REVIEW_REQUIRED`.
9. No automatic second spray is triggered for an uncertain result.
10. Inventory is updated only after confirmed successful customer dispensing.

---

# 26. Open Decisions for Team Review

The following points should be confirmed with the ERD/API/backend members before implementation:

1. Final identifier type: UUID, numeric ID, or another opaque ID.
2. Final authentication mechanism: JWT/session and refresh-token policy.
3. Exact RBAC permission names.
4. Exact order/payment/dispensing enum values.
5. Exact database field names.
6. Payment provider/simulator payload and signature scheme.
7. Device authentication mechanism.
8. Whether public kiosk requests require a kiosk/device identifier or session token.
9. Pagination and filtering standard.
10. Exact money representation and currency policy.
11. Whether product deletion is always soft-delete/deactivation.
12. Exact MQTT payload schema and REST-to-MQTT responsibility boundary.

These decisions should be recorded in the relevant GitHub Issue before the API is considered final.

---

# 27. Acceptance Checklist — TASK-005

- [x] API prefix standardized to `/api/v1`.
- [x] Endpoints grouped by functional module.
- [x] HTTP method defined.
- [x] Access control/role defined.
- [x] Request examples provided.
- [x] Response examples provided.
- [x] Validation rules provided.
- [x] Error codes and HTTP status codes provided.
- [x] Payment-to-dispensing flow documented.
- [x] Idempotency rules documented.
- [x] Tenant isolation documented.
- [x] Inventory update rule documented.
- [x] TASK-004 synchronization points documented.
- [x] TASK-006 synchronization points documented.
- [ ] Final ERD field names reviewed and synchronized.
- [ ] Team review completed.
- [ ] API contract approved before backend implementation.
