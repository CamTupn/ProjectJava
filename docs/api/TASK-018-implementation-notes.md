# TASK-018 — Location & Product Management

## Implemented routes

| Method | Route | Description |
|---|---|---|
| GET | `/api/v1/locations` | List locations; optional `status`, `name`, `tenantId` |
| GET | `/api/v1/locations/{id}` | Location detail |
| POST | `/api/v1/locations` | Create location |
| PUT | `/api/v1/locations/{id}` | Update name, address, description |
| DELETE | `/api/v1/locations/{id}` | Soft-delete (`status=INACTIVE`) |
| GET | `/api/v1/products` | List products; optional `status`, `name`, `tenantId` |
| GET | `/api/v1/products/{id}` | Product detail |
| POST | `/api/v1/products` | Create product |
| PUT | `/api/v1/products/{id}` | Update product details (SKU and tenant are immutable) |
| DELETE | `/api/v1/products/{id}` | Soft-delete (`status=INACTIVE`) |

## Authorization and tenant scope

- `PLATFORM_SUPER_ADMIN`: can read/manage all tenants. When creating a Location or Product, `tenantId` is required in the body.
- `BRAND_ADMIN`: can read/manage only the tenant ID embedded in the authenticated JWT principal. A body `tenantId` is ignored; a query `tenantId` that differs from the caller tenant is rejected as not found.
- `OPERATIONS_MANAGER`, `TECHNICIAN`, and `INVENTORY_STAFF`: denied by default. The repository currently has no per-user permission/permission-grant model, so this task does not invent grants for those roles. Extend the service checks only after the team defines the permission source.
- Tenant scope checks are repeated in the service layer; controller annotations are an additional RBAC boundary.

## Delete protection

- A Location cannot be deactivated while any Machine references it through `machines.location_id`.
- A Product cannot be deactivated while any Machine Slot references it through `machine_slots.product_id`.
- Product rows are never physically deleted. Order history keeps its product foreign-key reference and snapshots; setting the product to `INACTIVE` preserves that history.

## Request examples

Brand Admin creates a Location:

```json
{
  "name": "District 1 Store",
  "address": "123 Nguyen Hue, District 1, Ho Chi Minh City",
  "description": "Retail location"
}
```

Platform Super Admin creates a Product (tenantId is required for this role):

```json
{
  "tenantId": 1,
  "sku": "ROSE-001",
  "name": "Rose Eau de Parfum",
  "description": "Floral fragrance",
  "fragranceNotes": "Rose, musk, vanilla",
  "imageUrl": "https://example.com/rose.jpg",
  "price": 150000.00,
  "volumePerSprayMl": 0.10
}
```

## Postman

Import `TASK-018.postman_collection.json` from this folder. Set `baseUrl` and `jwtToken`. For Brand Admin, use the sample bodies as-is. For Platform Super Admin, add the target `tenantId` to each create body. Set `locationInUseId` and `productInUseId` to existing linked records to run the conflict checks.

## Important verification notes

- The implementation maps to the existing `locations` and `products` tables in `database/schema.sql`; no schema or seed changes were made.
- The uploaded ZIP contains no Git metadata, so whether TASK-017 has been merged into the remote `main` branch cannot be verified from this archive.
- Maven tests could not be run in this environment: the Maven wrapper attempted to download Maven 3.10.0 but network access to Maven Central failed. Run `cd backend && .\\mvnw.cmd test` on Windows (or `./mvnw test` on Linux/macOS) in an environment with Maven Central access before merging.
