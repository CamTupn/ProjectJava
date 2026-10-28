-- =========================================================
-- ScentStation Seed Data
-- TASK-007: Database Schema & MySQL Initialization
-- MySQL 8.0+
-- =========================================================

USE scentstation;

START TRANSACTION;

-- ---------------------------------------------------------
-- 1. Demo tenant
-- ---------------------------------------------------------

INSERT INTO tenants (name, code, status)
VALUES ('ScentStation Demo', 'SCENT-DEMO', 'ACTIVE')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    status = VALUES(status);

-- ---------------------------------------------------------
-- 2. System roles
-- ---------------------------------------------------------

INSERT INTO roles (code, name, description)
VALUES
    (
        'PLATFORM_SUPER_ADMIN',
        'Platform Super Admin',
        'Quản trị toàn bộ nền tảng ScentStation'
    ),
    (
        'BRAND_ADMIN',
        'Brand Admin',
        'Quản trị dữ liệu và người dùng của thương hiệu'
    ),
    (
        'OPERATIONS_MANAGER',
        'Operations Manager',
        'Quản lý hoạt động và tình trạng máy'
    ),
    (
        'TECHNICIAN',
        'Technician',
        'Kiểm tra và bảo trì thiết bị IoT'
    ),
    (
        'INVENTORY_STAFF',
        'Inventory Staff',
        'Quản lý tồn kho và tiếp nước hoa'
    )
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description);

COMMIT;

-- ---------------------------------------------------------
-- 3. Verify seed data
-- ---------------------------------------------------------

SELECT id, name, code, status
FROM tenants
WHERE code = 'SCENT-DEMO';

SELECT id, code, name
FROM roles
ORDER BY id;
