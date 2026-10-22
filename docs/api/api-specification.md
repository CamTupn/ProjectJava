[api-specification.md](https://github.com/user-attachments/files/33478238/api-specification.md)
# ScentStation — REST API Specification

**Document:** API Specification  
**Version:** 0.1  
**Status:** Draft for Review; not yet approved
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

The API uses opaque resource identifiers such as `Id`, `productId`, `machineId`, `slotId`, `orderId`, `paymentId`, `commandId` and `maintenanceId`.

IDs are represented as opaque strings in JSON to avoid JavaScript precision issues. Database PK/FK columns use BIGINT UNSIGNED internally; the backend maps those keys to API identifiers. Prefixes such as `ord_001` are illustrative public IDs, not literal database BIGINT values. The implementation must choose and document one public-ID format consistently.

---

# 2. Roles and Access Control

| Role | Code | Scope |
|---|---|---|
| Platform Super Admin | `PLATFORM_SUPER_ADMIN` | Entire platform |
| Brand Admin | `BRAND_ADMINB` | Assigned tenant(s) |
| Operations Manager | `OPERATIONS_MANAGER` | Assigned operational scope |
| Technician | `TECHNICIAN` | Assigned operational scope |
| Inventory Staff | `INVENTORY_STAFF` | Assigned inventory/brand scope |

Integration/session principals used by this API are `PUBLIC_KIOSK` (public kiosk session), `KIOSK_DEVICE` (authenticated device/gateway), and `PAYMENT_PROVIDER` (verified payment callback). These are not database `roles` rows in the current TASK-004 model. Device authentication and public kiosk session binding must be defined separately from human-user RBAC.

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
| Tenants | GET | `/tenants` | PLATFORM_SUPER_ADMIN |
| Tenants | POST | `/tenants` | PLATFORM_SUPER_ADMIN |
| Tenants | GET | `/tenants/{tenantId}` | PLATFORM_SUPER_ADMIN / scoped user |
| Tenants | PATCH | `/tenants/{tenantId}` | PLATFORM_SUPER_ADMIN |
| Products | GET | `/products` | Public / scoped |
| Products | POST | `/products` | PLATFORM_SUPER_ADMIN / BRAND_ADMIN |
| Products | GET | `/products/{productId}` | Public / scoped |
| Products | PATCH | `/products/{productId}` (update fields or set `status=INACTIVE`) | PLATFORM_SUPER_ADMIN / BRAND_ADMIN |
| Machines | GET | `/machines` | PLATFORM_SUPER_ADMIN / BRAND_ADMIN / OPERATIONS_MANAGER / TECHNICIAN |
| Machines | POST | `/machines` | PLATFORM_SUPER_ADMIN / BRAND_ADMIN / OPERATIONS_MANAGER |
| Machines | GET | `/machines/{machineId}` | Scoped |
| Machines | PATCH | `/machines/{machineId}` | PLATFORM_SUPER_ADMIN / BRAND_ADMIN / OPERATIONS_MANAGER |
| Machines | GET | `/machines/{machineId}/slots` | Scoped |
| Slots | PATCH | `/machines/{machineId}/slots/{slotId}` | BRAND_ADMIN / OPERATIONS_MANAGER / INVENTORY_STAFF / TECHNICIAN |
| Customer Catalog | GET | `/kiosks/{machineId}/products` | Public |
| Orders | POST | `/orders` | PUBLIC_KIOSK |
| Orders | GET | `/orders/{orderId}` | PUBLIC_KIOSK / scoped staff |
| Payments | POST | `/payments/callback` | PAYMENT_PROVIDER / simulator |
| Payments | POST | `/orders/{orderId}/payment-sessions` | PUBLIC_KIOSK bound to order session |
| Payments | GET | `/orders/{orderId}/payments` | PUBLIC_KIOSK bound to order session / scoped staff |
| Dispensing | GET | `/orders/{orderId}/dispensing` | PUBLIC_KIOSK / scoped staff |
| Dispensing | Internal workflow | Create command only after verified payment and safety re-check | Backend service only; not a public endpoint |
| Device | POST | `/devices/heartbeat` | KIOSK_DEVICE |
| Device | POST | `/devices/events` | KIOSK_DEVICE |
| Inventory | GET | `/inventory` | BRAND_ADMIN / INVENTORY_STAFF |
| Inventory | POST | `/inventory/refills` | INVENTORY_STAFF |
| Inventory | POST | `/inventory/adjustments` | INVENTORY_STAFF / authorized BRAND_ADMIN |
| Alerts | GET | `/alerts` | OPERATIONS_MANAGER / TECHNICIAN / INVENTORY_STAFF |
| Alerts | PATCH | `/alerts/{alertId}` | OPERATIONS_MANAGER / TECHNICIAN / INVENTORY_STAFF |
| Maintenance | GET | `/maintenance` | OPERATIONS_MANAGER / TECHNICIAN |
| Maintenance | POST | `/maintenance` | OPERATIONS_MANAGER |
| Maintenance | GET | `/maintenance/{maintenanceId}` | OPERATIONS_MANAGER / TECHNICIAN |
| Maintenance | PATCH | `/maintenance/{maintenanceId}` | OPERATIONS_MANAGER / TECHNICIAN |
| Reports | GET | `/dashboard/summary` | PLATFORM_SUPER_ADMIN / BRAND_ADMIN / OPERATIONS_MANAGER |
| Users | GET | `/users` | PLATFORM_SUPER_ADMIN / BRAND_ADMIN with tenant scope |
| Users | POST | `/users` | PLATFORM_SUPER_ADMIN / BRAND_ADMIN with tenant scope |
| Users | PATCH | `/users/{userId}` | PLATFORM_SUPER_ADMIN / BRAND_ADMIN with tenant scope |
| Roles | GET | `/roles` | Authenticated administrative user |
| Locations | GET/POST | `/locations` | PLATFORM_SUPER_ADMIN / BRAND_ADMIN |
| Locations | PATCH | `/locations/{locationId}` | PLATFORM_SUPER_ADMIN / BRAND_ADMIN |
| Orders | GET | `/orders` | PLATFORM_SUPER_ADMIN / BRAND_ADMIN / OPERATIONS_MANAGER |
| Orders | POST | `/orders/{orderId}/cancel` | Order session / authorized staff |
| Inventory | GET | `/inventory/transactions` | BRAND_ADMIN / INVENTORY_STAFF |
| Inventory | POST | `/machines/{machineId}/diagnostic-sprays` | TECHNICIAN / authorized OPERATIONS_MANAGER |
| Telemetry | GET | `/machines/{machineId}/sensor-readings` | Scoped authorized staff |
| Notifications | GET | `/notifications` | Authenticated user |
| Notifications | PATCH | `/notifications/{notificationId}` | Notification owner |
| Audit | GET | `/audit-logs` | PLATFORM_SUPER_ADMIN / authorized staff |

> Dispensing command creation is an internal backend workflow, not a public customer endpoint. The backend may create a command only after verified payment and a fresh machine/slot safety check. The customer cannot directly request a successful spray.

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
      "roles": ["PLATFORM_SUPER_ADMIN"],
      "tenantIds": []
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
    "roles": ["PLATFORM_SUPER_ADMIN"],
    "tenantIds": []
  }
}
```

---

# 5. Tenant Management

## 5.1 List Tenants

### `GET /api/v1/tenants`

**Access:** `PLATFORM_SUPER_ADMIN`

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
        "id": "tenant_001",
        "name": "Scent Tenant",
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

## 5.2 Create Tenant

### `POST /api/v1/tenants`

**Access:** `PLATFORM_SUPER_ADMIN`

### Request

```json
{
  "name": "Scent Tenant",
  "description": "Example tenant",
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
    "id": "tenant_001",
    "name": "Scent Tenant",
    "description": "Example tenant",
    "status": "ACTIVE"
  },
  "message": "Brand created successfully"
}
```

### Errors

- `400 VALIDATION_ERROR`
- `409 TENANT_ALREADY_EXISTS`
- `403 FORBIDDEN`

---

## 5.3 Get Tenant

### `GET /api/v1/tenants/{tenantId}`

**Access:** `PLATFORM_SUPER_ADMIN`; scoped users may access only assigned tenants.

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "id": "tenant_001",
    "name": "Scent Tenant",
    "description": "Example tenant",
    "status": "ACTIVE"
  }
}
```

### Errors

- `401 UNAUTHORIZED`
- `403 TENANT_ACCESS_DENIED`
- `404 TENANT_NOT_FOUND`

---

## 5.4 Update Tenant

### `PATCH /api/v1/tenants/{tenantId}`

**Access:** `PLATFORM_SUPER_ADMIN`

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
- `404 TENANT_NOT_FOUND`
- `409 TENANT_ALREADY_EXISTS`

---

# 6. Product Module

## 6.1 List Products

### `GET /api/v1/products`

**Access:** Public for kiosk catalog; authenticated for management views.

### Query parameters

```text
?tenantId=tenant_001&status=ACTIVE&page=0&size=20
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
        "tenantId": "tenant_001",
        "name": "Ocean Mist",
        "description": "Fresh aquatic fragrance",
        "price": "15000.00",
        "volumePerSprayMl": "0.10",
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

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`

### Request

```json
{
  "tenantId": "tenant_001",
  "name": "Ocean Mist",
  "description": "Fresh aquatic fragrance",
  "imageUrl": "https://example.local/ocean-mist.jpg",
  "scentNotes": ["Citrus", "Marine", "Woody"],
  "price": "15000.00",
  "currency": "VND",
  "status": "ACTIVE"
}
```

### Validation

- `tenantId`: required and inside caller scope.
- `name`: required, non-blank.
- `price`: positive.
- `currency`: supported currency.
- `status`: `ACTIVE` or `INACTIVE`.

### Response `201 Created`

Returns the created product.

### Errors

- `400 VALIDATION_ERROR`
- `403 TENANT_ACCESS_DENIED`
- `404 TENANT_NOT_FOUND`
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

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`

### Validation

Same validation rules as create. A product outside the caller's tenant scope must be rejected.

### Response

`200 OK` with updated product.

---

## 6.5 Deactivate Product

### `PATCH /api/v1/products/{productId}`

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`

**Request**

```json
{
  "status": "INACTIVE"
}
```

**Behavior:** This endpoint deactivates the product logically. It does not physically delete records referenced by orders or history.

### Response `200 OK` with the updated product.

### Errors

- `403 TENANT_ACCESS_DENIED`
- `404 PRODUCT_NOT_FOUND`
- `409 PRODUCT_IN_USE` only if an explicit business rule prevents deactivation; otherwise this error is not applicable.

---

# 7. Machine and Slot Module

## 7.1 List Machines

### `GET /api/v1/machines`

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`, `OPERATIONS_MANAGER`, `TECHNICIAN`

### Query

```text
?tenantId=tenant_001&status=ONLINE&page=0&size=20
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
        "brandId": "tenant_001",
        "location": "Store A",
        "status": "ONLINE",
        "operationalStatus": "ACTIVE",
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

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`, `OPERATIONS_MANAGER`

### Request

```json
{
  "code": "KIOSK-001",
  "brandId": "tenant_001",
  "location": "Store A",
  "status": "ACTIVE"
}
```

### Validation

- `code`: required and unique.
- `tenantId`: required and authorized.
- `location`: required according to deployment policy.
- Machine must not be registered to an unauthorized tenant.

### Response `201 Created`

Returns machine data.

### Errors

- `400 VALIDATION_ERROR`
- `403 TENANT_ACCESS_DENIED`
- `404 TENANT_NOT_FOUND`
- `409 MACHINE_CODE_ALREADY_EXISTS`

---

## 7.3 Get Machine

### `GET /api/v1/machines/{machineId}`

**Access:** Scoped administrative users.

Returns machine status, `lastSeenAt`, and high-level operational state.

---

## 7.4 Update Machine

### `PATCH /api/v1/machines/{machineId}`

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`, `OPERATIONS_MANAGER`

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

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`, `OPERATIONS_MANAGER`, `TECHNICIAN`, `INVENTORY_STAFF`

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
        "capacityMl": "100.00",
        "currentVolumeMl": "85.00",
        "lowStockThresholdMl": "20.00",
        "status": "AVAILABLE"
      }
    ]
  }
}
```

---

## 7.6 Update Slot

### `PATCH /api/v1/machines/{machineId}/slots/{slotId}`

**Access:** `BRAND_ADMIN`, `OPERATIONS_MANAGER`, `INVENTORY_STAFF`, `TECHNICIAN`

### Request

```json
{
  "productId": "prod_001",
  "capacityMl": "100.00",
  "currentVolumeMl": "85.00",
  "lowStockThresholdMl": "20.00",
  "status": "AVAILABLE"
}
```

### Validation

- `slotId` must belong to `machineId`.
- Product must exist and belong to the same tenant.
- The selected product must belong to the same tenant as the machine.
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
- Slot has sufficient `currentVolumeMl` for the configured spray volume.
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
        "price": "15000.00",
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

**Access:** `PUBLIC_KIOSK`

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
-  A new order is initially `PENDING_PAYMENT`. Payment QR/session data is created only by `POST /api/v1/orders/{orderId}/payment-sessions`; this endpoint does not create a payment session.
- The order is bound to exactly one machine and slot.

### Response `201 Created`

```json
{
  "success": true,
  "data": {
    "orderId": "ord_001",
    "machineId": "mach_001",
    "slotId": "slot_001",
    "productId": "prod_001",
    "productNameSnapshot": "Ocean Mist",
    "unitPrice": "15000.00",
    "currency": "VND",
    "status": "PENDING_PAYMENT",
    "expiresAt": "2026-10-15T13:10:00Z",
    "createdAt": "2026-10-15T13:00:00Z"
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
    "amount": "15000.00",
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

**Access:** `PAYMENT_PROVIDER` / payment simulator

> This endpoint is not a customer endpoint.

### Request

```json
{
  "providerReference": "PAY-20261015-0001",
  "orderId": "order_001",
  "amount": "15000.00",
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
    "amount": "15000.00",
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
- `409 SLOT_NOT_AVAILABLE`
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
    "status": "DISPENSED",
    "executionStatus": "SUCCESS",
    "result": "SPRAY_COMPLETED",
    "completedAt": "2026-10-15T13:05:45Z"
  }
}
```

Possible dispensing statuses:

```text
PENDING
SENT
SUCCESS
FAILED
UNCERTAIN
EXPIRED
```

### Important rule

A timeout or missing response does **not** prove that spraying did not occur. The order must move to `REVIEW` and the execution status to `UNCERTAIN` when the result is uncertain and must not automatically issue a second spray command.

---

# 12. IoT Device Module

## 12.1 Heartbeat

### `POST /api/v1/devices/heartbeat`

**Access:** `KIOSK_DEVICE`

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

**Access:** `KIOSK_DEVICE`

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

**Access:** `BRAND_ADMIN`, `INVENTORY_STAFF`

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
        "productId": "prod_001",
        "capacityMl": "100.00",
        "currentVolumeMl": "18.00",
        "lowStockThresholdMl": "20.00",
        "status": "LOW_STOCK"
      }
    ]
  }
}
```

---

## 13.2 Record Refill

### `POST /api/v1/inventory/refills`

**Access:** `INVENTORY_STAFF`

### Request

```json
{
  "machineId": "machine_001",
  "slotId": "slot_001",
  "quantityAddedMl": "100.00",
  "note": "Refilled slot after product replenishment"
}
```

### Validation

- Machine and slot must exist, and the slot must belong to the specified machine.
- `quantityAddedMl` must be positive.
- `currentVolumeMl + quantityAddedMl` must not exceed `capacityMl`; otherwise reject the refill or require an explicitly authorized adjustment workflow.
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
    "quantityAddedMl": "100.00",
    "currentVolumeMl": "118.00",
    "status": "AVAILABLE"
  },
  "message": "Refill recorded successfully"
}
```

### Errors

- `400 VALIDATION_ERROR`
- `403 FORBIDDEN`
- `404 SLOT_NOT_FOUND`
- `409 INVALID_SLOT_CONFIGURATION`

---

## 13.3 Inventory Adjustment

### `POST /api/v1/inventory/adjustments`

**Access:** `INVENTORY_STAFF`, authorized `BRAND_ADMIN`

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

**Access:** `OPERATIONS_MANAGER`, `TECHNICIAN`, `INVENTORY_STAFF`

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

**Access:**  `OPERATIONS_MANAGER`, `TECHNICIAN`, `INVENTORY_STAFF`

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

**Access:** `OPERATIONS_MANAGER`, `TECHNICIAN`

### Query

```text
?machineId=machine_001&status=OPEN&page=0&size=20
```

---

## 15.2 Create Maintenance Ticket

### `POST /api/v1/maintenance`

**Access:** `OPERATIONS_MANAGER`

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

**Access:** `OPERATIONS_MANAGER`, `TECHNICIAN`

Returns ticket details and maintenance history.

---

## 15.4 Update Maintenance Ticket

### `PATCH /api/v1/maintenance/{maintenanceId}`

**Access:** `OPERATIONS_MANAGER`, `TECHNICIAN`

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

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`, `OPERATIONS_MANAGER`

### Query

```text
?tenantId=tenant_001&from=2026-10-01&to=2026-10-15
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

**Access:** `PLATFORM_SUPER_ADMIN`; additional access only if explicitly granted by the RBAC policy.

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
DISPENSED
FAILED
EXPIRED
REFUNDED
REVIEW
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
REVIEW
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
AVAILABLE
LOW_STOCK
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
    "amount": "15000.00",
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
  "amount": "15000.00",
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
| `/tenants/*` | SRS-BRAND-001 |
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
- [ ] `tenant_id` ownership and tenant isolation.
- [ ] Product-to-slot relationship.
- [ ] `machines` to `machine_slots` and `products` to `machine_slots` relationships; no cartridge entity is assumed.
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
8. Timeout or uncertain result becomes `REVIEW`.
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
- [ ] Endpoints grouped by functional module.
- [ ] HTTP method defined.
- [ ] Access control/role defined.
- [ ] Request examples provided.
- [ ] Response examples provided.
- [ ] Validation rules provided.
- [ ] Error codes and HTTP status codes provided.
- [ ] Payment-to-dispensing flow documented.
- [ ] Idempotency rules documented.
- [ ] Tenant isolation documented.
- [ ] Inventory update rule documented.
- [ ] TASK-004 synchronization points documented.
- [ ] TASK-006 synchronization points documented.
- [ ] Final ERD field names reviewed and synchronized.
- [ ] Team review completed.
- [ ] API contract approved before backend implementation.


---

# 28. Reconciliation Notes and Additional MVP Endpoints (v0.2)

This section resolves remaining contract gaps against the current TASK-004 Data Dictionary proposal. If any earlier example conflicts with this section, use this section as the proposed canonical contract until the team approves it. This document is still a draft; do not treat it as an implementation guarantee.

## 28.1 Canonical entity and field mapping

| API concept | TASK-004 entity/field | Canonical API field | Decision |
|---|---|---|---|
| Brand/account owning data | `tenants` | `tenantId` | Use tenant terminology in API and database. A tenant may represent a perfume brand/company. |
| Product price | `products.price` | `price` | JSON decimal string, e.g. `"15000.00"`. |
| Spray volume | `products.volume_per_spray_ml` | `volumePerSprayMl` | JSON decimal string, unit ml. |
| Fragrance notes | `products.fragrance_notes` | `fragranceNotes` | String or array needs a team decision; DB column is TEXT, so canonical write format is a string unless a JSON conversion is explicitly agreed. |
| Machine connection | `machines.connection_status` | `connectionStatus` | `ONLINE` or `OFFLINE`. |
| Machine operation | `machines.operational_status` | `operationalStatus` | `ACTIVE`, `MAINTENANCE`, `DISABLED`, `ERROR`. |
| Slot volume | `machine_slots.current_volume_ml` | `currentVolumeMl` | Decimal string in ml. |
| Slot threshold | `machine_slots.low_stock_threshold_ml` | `lowStockThresholdMl` | Decimal string in ml. |
| Slot state | `machine_slots.status` | `status` | `AVAILABLE`, `LOW_STOCK`, `EMPTY`, `DISABLED`, `ERROR`. |
| Order completed successfully | `orders.status` | `DISPENSED` | Do not use `SUCCESS` as an order state. `SUCCESS` belongs to payment/execution result. |
| Uncertain physical result | `orders.status` + `dispense_executions.execution_status` | `REVIEW` + `UNCERTAIN` | Do not automatically retry a spray. |
| Payment attempts | `payments` (1:N order) | `payments[]` | Keep attempts/history; do not assume exactly one payment row per order. |
| Diagnostic spray | `dispense_commands.command_type` | `DIAGNOSTIC` | No customer order required; technician authorization and audit required. |

## 28.2 Idempotency and decimal conventions

For `POST /orders`, `POST /orders/{orderId}/payment-sessions`, `POST /inventory/refills`, `POST /inventory/adjustments`, and internal dispense-command creation, clients/services must supply or derive an `Idempotency-Key` unique to one logical operation. Reusing the same key with the same request returns the original result; reusing it with a different payload returns `409 IDEMPOTENCY_KEY_REUSED`. This mechanism supplements but does not replace the unique command token and device-side duplicate-command protection.

Money and measured volumes are decimal strings in JSON. Examples: `"price": "15000.00"`, `"amount": "15000.00"`, `"volumePerSprayMl": "0.10"`. The backend stores values using MySQL DECIMAL fields and must not use binary floating point for monetary calculations.

## 28.3 List orders

### `GET /api/v1/orders`

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`, `OPERATIONS_MANAGER`; tenant scope is enforced server-side.

**Query:** `?tenantId=tenant_001&machineId=machine_001&status=PAID&page=0&size=20&sort=createdAt,desc`

**Response `200 OK`:** paginated `items`, each containing `orderId`, `orderCode`, `tenantId`, `machineId`, `slotId`, `productId`, `productNameSnapshot`, `unitPrice`, `currency`, `status`, `expiresAt`, `createdAt`, and nullable `completedAt`.

**Errors:** `401 UNAUTHORIZED`, `403 TENANT_ACCESS_DENIED`, `400 VALIDATION_ERROR`.

## 28.4 Create a payment session

### `POST /api/v1/orders/{orderId}/payment-sessions`

**Access:** authenticated kiosk/order session or `PUBLIC_KIOSK` bound to the order's machine/session. Require `Idempotency-Key`.

**Purpose:** creates a payment attempt and returns the QR/payment payload. The backend must calculate amount/currency from the order, not trust the client.

**Response `201 Created` example:**

```json
{
  "success": true,
  "data": {
    "paymentId": "pay_001",
    "orderId": "ord_001",
    "provider": "SIMULATOR",
    "paymentRef": "PAY-20261022-0001",
    "amount": "15000.00",
    "currency": "VND",
    "status": "PENDING",
    "qrPayload": "<provider-generated-payload>",
    "expiresAt": "2026-10-22T10:10:00Z"
  }
}
```

**Errors:** `401 UNAUTHORIZED`, `403 ORDER_ACCESS_DENIED`, `404 ORDER_NOT_FOUND`, `409 ORDER_NOT_PAYABLE`, `409 IDEMPOTENCY_KEY_REUSED`, `422 PAYMENT_SESSION_CREATION_FAILED`.

## 28.5 List payment attempts

### `GET /api/v1/orders/{orderId}/payments`

**Access:** order owner/kiosk session or authorized staff in tenant scope.

Returns a paginated or bounded list of payment attempts with `paymentId`, `paymentRef`, `provider`, `amount`, `currency`, `status`, `gatewayTransactionId` when safe to expose, `paidAt`, and `createdAt`. Do not expose secrets or raw signed callback credentials.

**Errors:** `401 UNAUTHORIZED`, `403 ORDER_ACCESS_DENIED`, `404 ORDER_NOT_FOUND`.

## 28.6 Cancel an unpaid order

### `POST /api/v1/orders/{orderId}/cancel`

**Access:** order owner/kiosk session or authorized staff.

Allowed only before confirmed payment and before dispensing command dispatch. A paid order must follow the refund/review policy; it must not be silently cancelled.

**Response:** `200 OK` with updated order status `EXPIRED` or the team's separately approved cancellation state. Because the current `orders` enum does not include `CANCELLED`, this draft proposes `EXPIRED` for an unpaid order that is intentionally closed. This is a provisional mapping and requires team approval; do not implement until approved.

**Errors:** `403 ORDER_ACCESS_DENIED`, `404 ORDER_NOT_FOUND`, `409 ORDER_ALREADY_PAID`, `409 DISPENSING_ALREADY_STARTED`.

## 28.7 Diagnostic spray

### `POST /api/v1/machines/{machineId}/diagnostic-sprays`

**Access:** `TECHNICIAN` or `OPERATIONS_MANAGER` with explicit diagnostic permission. Require `Idempotency-Key` and record an audit event.

**Request:**

```json
{
  "slotId": "slot_001",
  "volumeMl": "0.10",
  "reason": "Post-refill nozzle test"
}
```

**Response `202 Accepted`:** returns `commandId`, `commandType: "DIAGNOSTIC"`, `machineId`, `slotId`, `status: "PENDING"`, and `expiresAt`.

A diagnostic command has `orderId: null`, must be explicitly typed `DIAGNOSTIC`, and its inventory transaction must be recorded as `DIAGNOSTIC_SPRAY`, not `DISPENSE_SALE`. Apply the same machine/slot safety and duplicate-command controls as customer sprays.

**Errors:** `401 UNAUTHORIZED`, `403 FORBIDDEN`, `404 MACHINE_NOT_FOUND`, `404 SLOT_NOT_FOUND`, `409 MACHINE_UNAVAILABLE`, `409 SLOT_NOT_AVAILABLE`, `409 IDEMPOTENCY_KEY_REUSED`.

## 28.8 Get inventory transaction history

### `GET /api/v1/inventory/transactions`

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`, `OPERATIONS_MANAGER`, `INVENTORY_STAFF`; tenant scope enforced.

**Query:** `?slotId=slot_001&type=REFILL&from=2026-10-01T00:00:00Z&to=2026-10-22T23:59:59Z&page=0&size=20`.

**Response:** paginated transactions with `transactionId`, `slotId`, `transactionType`, `quantityMl`, `orderId` (nullable), `executionId` (nullable), `refillId` (nullable), `performedByUserId` (nullable), `createdAt`, and `note` if supported by the final dictionary.

Inventory is reduced for customer sales only after confirmed successful dispensing. Diagnostic spray, refill, adjustment, removal, and disposal must remain separately identifiable in the ledger.

## 28.9 Sensor readings

### `GET /api/v1/machines/{machineId}/sensor-readings`

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`, `OPERATIONS_MANAGER`, `TECHNICIAN` in scope.

**Query:** `?slotId=slot_001&from=<ISO-8601>&to=<ISO-8601>&page=0&size=50`.

Returns readings from `sensor_readings` with `readingId`, `machineId`, nullable `slotId`, `sensorType`, `value`, `unit`, and `recordedAt`. Exact measurement fields must match TASK-004 before implementation.

**Errors:** `401 UNAUTHORIZED`, `403 TENANT_ACCESS_DENIED`, `404 MACHINE_NOT_FOUND`, `400 VALIDATION_ERROR`.

## 28.10 Notifications

### `GET /api/v1/notifications`

**Access:** authenticated user; only that user's notifications and permitted tenant scope.

**Query:** `?status=UNREAD&page=0&size=20`.

Returns `notificationId`, `type`, `title`, `message`, `referenceType`, `referenceId`, `status`, and `createdAt`. Exact notification status enum must be approved with TASK-004.

### `PATCH /api/v1/notifications/{notificationId}`

**Access:** notification owner only.

**Request:** `{"status":"READ"}`.

**Errors:** `401 UNAUTHORIZED`, `403 NOTIFICATION_ACCESS_DENIED`, `404 NOTIFICATION_NOT_FOUND`, `400 VALIDATION_ERROR`.

## 28.11 Tenant and location APIs

Tenant management endpoints use `/api/v1/tenants` and fields matching `tenants` (`name`, `code`, `status`). Tenant creation/update is restricted to `PLATFORM_SUPER_ADMIN`.

### `GET /api/v1/locations`

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`, `OPERATIONS_MANAGER`; scope enforced.

**Query:** `?tenantId=tenant_001&status=ACTIVE&page=0&size=20`.

Returns `locationId`, `tenantId`, `name`, `address`, `description`, `status`, `createdAt`, and `updatedAt`.

### `POST /api/v1/locations`

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN` within tenant scope.

**Request:** `{"tenantId":"tenant_001","name":"Store A","address":"Example address","description":"Optional"}`.

**Response:** `201 Created` with the new location. Validate that caller can manage `tenantId`, `name` and `address` are non-blank, and status defaults to `ACTIVE`.

### `PATCH /api/v1/locations/{locationId}`

**Access:** `PLATFORM_SUPER_ADMIN`, `BRAND_ADMIN`, `OPERATIONS_MANAGER` within scope. Partial update of `name`, `address`, `description`, and/or `status`.

**Errors:** `401 UNAUTHORIZED`, `403 TENANT_ACCESS_DENIED`, `404 LOCATION_NOT_FOUND`, `400 VALIDATION_ERROR`.

## 28.12 User and role management

### `GET /api/v1/users`

**Access:** `PLATFORM_SUPER_ADMIN`; tenant administrators may list users only if the team authorizes this capability.

**Query:** `?tenantId=tenant_001&status=ACTIVE&page=0&size=20`.

Returns user profile fields only: `userId`, `tenantId` (nullable for platform-level users), `username`, `email`, `fullName`, `status`, `roles`, `createdAt`, `updatedAt`. Never return `passwordHash`.

### `POST /api/v1/users`

**Access:** `PLATFORM_SUPER_ADMIN` (tenant-scoped user provisioning may be delegated by explicit policy).

**Request:** `{"tenantId":"tenant_001","username":"ops01","email":"ops@example.local","fullName":"Operations User","temporaryPassword":"<one-time-secret>","roleCodes":["OPERATIONS_MANAGER"]}`.

Validate email/username uniqueness, role codes against `roles.code`, tenant scope, and password policy. Store only a secure password hash; never log the temporary password.

### `PATCH /api/v1/users/{userId}`

**Access:** `PLATFORM_SUPER_ADMIN`; authorized tenant admin only if policy allows.

May update `fullName`, `email`, `status`, and role assignments. Role changes must be audited. Never accept a client-supplied password hash.

### `GET /api/v1/roles`

**Access:** `PLATFORM_SUPER_ADMIN` or authenticated users when needed to populate permitted role choices. Returns role `code`, `name`, and `description` from `roles`.

**Note:** `KIOSK_DEVICE`, `PAYMENT_PROVIDER`, and `PUBLIC_KIOSK` describe integration/session principals in this API draft, not rows in `roles` unless TASK-004 is changed to include them. The current `roles` table contains the five human/platform role codes listed in Section 2.

## 28.13 Device command acknowledgement and event processing

MQTT is the primary device channel per TASK-002. REST device event endpoints are testing/fallback interfaces only unless the team explicitly decides otherwise. The backend consumes device events and updates `dispense_executions` idempotently.

A dispensing result event must include `eventId`, `eventType`, `commandId`, `machineId`, `slotId`, `result`, `recordedAt`, and optional `errorCode`/telemetry. The backend verifies the command belongs to that machine and slot, rejects duplicate or misdirected events, and does not accept a device-originated event as proof of payment. `SUCCESS` is accepted only from the authenticated device channel after actual hardware confirmation.

## 28.14 Order/payment/dispensing state mapping

| Event | `orders.status` | `payments.status` | `dispense_commands.status` / execution |
|---|---|---|---|
| Order created | `PENDING_PAYMENT` | `PENDING` after payment attempt created | No command |
| Verified payment success | `PAID` | `SUCCESS` | Command created after safety re-check |
| Command dispatched | `DISPENSING` | `SUCCESS` | `SENT` |
| Spray confirmed | `DISPENSED` | `SUCCESS` | Execution `SUCCESS` |
| Device confirms failure | `FAILED` or approved recovery state | `SUCCESS` pending refund/review policy | Execution `FAILED` |
| Result uncertain/timeout | `REVIEW` | `SUCCESS` | Execution `UNCERTAIN`; never auto-retry |
| Unpaid order expires | `EXPIRED` | `EXPIRED` if attempt exists | No command |
| Refund confirmed | `REFUNDED` | `REFUNDED` | Existing execution retained for audit |

Payment success does not automatically imply spray success. If dispensing fails or is uncertain, refund/compensation policy must be defined by the team and reflected in TASK-006.

## 28.15 Corrected endpoint inventory

The following endpoint groups are included in this contract proposal. A module does not need CRUD for every database table; endpoints should exist only where required by MVP use cases.

| Area | Required routes |
|---|---|
| Authentication | `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, `GET /auth/me` |
| Tenants | `GET/POST /tenants`, `GET/PATCH /tenants/{tenantId}` |
| Users/roles | `GET/POST /users`, `PATCH /users/{userId}`, `GET /roles` |
| Locations | `GET/POST /locations`, `PATCH /locations/{locationId}` |
| Products | `GET/POST /products`, `GET/PATCH /products/{productId}`, deactivate via `PATCH status=INACTIVE` preferred |
| Machines/slots | `GET/POST /machines`, `GET/PATCH /machines/{machineId}`, `GET/PATCH /machines/{machineId}/slots/{slotId}` |
| Kiosk/order | `GET /kiosks/{machineId}/products`, `POST /orders`, `GET /orders`, `GET /orders/{orderId}`, `POST /orders/{orderId}/cancel` |
| Payments | `POST /orders/{orderId}/payment-sessions`, provider callback, `GET /orders/{orderId}/payments` |
| Dispensing | Internal customer command flow, `GET /orders/{orderId}/dispensing`, diagnostic spray, authenticated MQTT result events |
| IoT | MQTT heartbeat/status/event primary; REST heartbeat/event only fallback/testing |
| Inventory | `GET /inventory`, `POST /inventory/refills`, `POST /inventory/adjustments`, `GET /inventory/transactions` |
| Alerts/maintenance | Existing alert and maintenance routes; maintenance logs should be recorded as part of ticket workflow |
| Telemetry | `GET /machines/{machineId}/sensor-readings` |
| Reporting/audit | `GET /dashboard/summary`, `GET /audit-logs` |
| Notifications | `GET /notifications`, `PATCH /notifications/{notificationId}` |

## 28.16 Team decisions still required

- [ ] Approve whether “brand” is exactly represented by `tenants` in the current model.
- [ ] Confirm `fragranceNotes` API type: plain string to match DB TEXT, or an explicit structured JSON model.
- [ ] Confirm whether `orders.status` includes `CANCELLED`; current dictionary proposal does not, so this draft maps unpaid user cancellation to `EXPIRED` pending approval.
- [ ] Confirm refund and failed-dispense compensation policy.
- [ ] Confirm payment provider/simulator, callback signature and payment-session protocol.
- [ ] Confirm exact `sensor_readings` fields and notification status enum against TASK-004.
- [ ] Confirm tenant-scoped user administration permissions.
- [ ] Confirm whether IDs are numeric strings in API responses and whether examples should use `ord_001`-style labels only as illustrative aliases.
- [ ] Review all endpoint access rules with TASK-003 Use Case and TASK-006 Sequence diagrams.
- [ ] Approve final contract before backend implementation.

## 28.17 Revised acceptance checklist

- [x] API prefix `/api/v1` and JSON conventions documented.
- [x] Canonical role codes listed for human roles.
- [x] Tenant terminology normalized to `tenants`; team confirmation still required.
- [x] Product price/volume/fragrance field mapping documented.
- [x] Machine connection and operational states separated.
- [x] Inventory examples use `machine_slots` fields; verify against the approved TASK-004 schema.
- [x] Order, payment and execution states separated.
- [x] Idempotency, payment verification and no-auto-retry safety rules documented.
- [x] Missing MVP endpoint proposals added.
- [ ] All endpoint examples and status enums verified end-to-end against TASK-004 and TASK-006.
- [ ] TASK-004 ERD/Data Dictionary approved and frozen.
- [ ] TASK-003/TASK-006 consistency review completed.
- [ ] Team approval recorded on GitHub Issue/PR.
