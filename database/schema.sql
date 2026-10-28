-- =========================================================
-- ScentStation Database Schema
-- Database: MySQL 8.0+
-- Task: TASK-007 - Database Schema & MySQL Initialization
-- =========================================================

CREATE DATABASE IF NOT EXISTS scentstation
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE scentstation;

-- =========================================================
-- 1. TENANTS
-- Đại diện cho các thương hiệu/đơn vị sử dụng nền tảng.
-- =========================================================

CREATE TABLE IF NOT EXISTS tenants (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_tenants PRIMARY KEY (id),
    CONSTRAINT uq_tenants_code UNIQUE (code),

    CONSTRAINT chk_tenants_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE'))
) ENGINE=InnoDB;

-- =========================================================
-- 2. USERS
-- Người dùng hệ thống.
-- tenant_id có thể NULL đối với Platform Super Admin.
-- =========================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),

    CONSTRAINT fk_users_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_users_status
        CHECK (status IN ('ACTIVE', 'LOCKED', 'INACTIVE')),

    INDEX idx_users_tenant_id (tenant_id)
) ENGINE=InnoDB;

-- =========================================================
-- 3. ROLES
-- Các vai trò phân quyền của hệ thống.
-- =========================================================

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_code UNIQUE (code),

    CONSTRAINT chk_roles_code CHECK (
        code IN (
            'PLATFORM_SUPER_ADMIN',
            'BRAND_ADMIN',
            'OPERATIONS_MANAGER',
            'TECHNICIAN',
            'INVENTORY_STAFF'
        )
    )
) ENGINE=InnoDB;

-- =========================================================
-- 4. USER_ROLES
-- Quan hệ nhiều-nhiều giữa users và roles.
-- =========================================================

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT UNSIGNED NOT NULL,
    role_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_user_roles_role_id (role_id)
) ENGINE=InnoDB;
-- =========================================================
-- 5. LOCATIONS
-- Địa điểm đặt máy của từng tenant.
-- =========================================================

CREATE TABLE IF NOT EXISTS locations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NULL,
    description TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_locations PRIMARY KEY (id),

    CONSTRAINT fk_locations_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_locations_status
        CHECK (status IN ('ACTIVE', 'INACTIVE')),

    INDEX idx_locations_tenant_id (tenant_id)
) ENGINE=InnoDB;


-- =========================================================
-- 6. PRODUCTS
-- Sản phẩm nước hoa thuộc từng tenant.
-- =========================================================

CREATE TABLE IF NOT EXISTS products (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NOT NULL,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT NULL,
    fragrance_notes TEXT NULL,
    image_url VARCHAR(500) NULL,
    price DECIMAL(12,2) NOT NULL,
    volume_per_spray_ml DECIMAL(8,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uq_products_tenant_sku UNIQUE (tenant_id, sku),

    CONSTRAINT fk_products_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_products_price
        CHECK (price >= 0),

    CONSTRAINT chk_products_spray_volume
        CHECK (volume_per_spray_ml > 0),

    CONSTRAINT chk_products_status
        CHECK (status IN ('ACTIVE', 'INACTIVE')),

    INDEX idx_products_tenant_id (tenant_id),
    INDEX idx_products_status (status)
) ENGINE=InnoDB;
-- =========================================================
-- 7. MACHINES
-- Quản lý máy bán/trải nghiệm nước hoa IoT.
-- =========================================================

CREATE TABLE IF NOT EXISTS machines (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NOT NULL,
    location_id BIGINT UNSIGNED NULL,
    machine_code VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100) NOT NULL,
    device_identity VARCHAR(150) NOT NULL,
    connection_status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    operational_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    firmware_version VARCHAR(100) NULL,
    last_ping_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_machines PRIMARY KEY (id),
    CONSTRAINT uq_machines_code UNIQUE (machine_code),
    CONSTRAINT uq_machines_serial UNIQUE (serial_number),
    CONSTRAINT uq_machines_device_identity UNIQUE (device_identity),

    CONSTRAINT fk_machines_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_machines_location
        FOREIGN KEY (location_id)
        REFERENCES locations(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_machines_connection_status
        CHECK (connection_status IN ('ONLINE', 'OFFLINE')),

    CONSTRAINT chk_machines_operational_status
        CHECK (
            operational_status IN (
                'ACTIVE',
                'MAINTENANCE',
                'DISABLED',
                'ERROR'
            )
        ),

    INDEX idx_machines_tenant_id (tenant_id),
    INDEX idx_machines_location_id (location_id),
    INDEX idx_machines_connection_status (connection_status)
) ENGINE=InnoDB;


-- =========================================================
-- 8. MACHINE_SLOTS
-- Quản lý từng khe chứa nước hoa trên máy.
-- =========================================================

CREATE TABLE IF NOT EXISTS machine_slots (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    machine_id BIGINT UNSIGNED NOT NULL,
    slot_number INT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NULL,
    capacity_ml DECIMAL(8,2) NOT NULL,
    current_volume_ml DECIMAL(8,2) NOT NULL DEFAULT 0,
    low_stock_threshold_ml DECIMAL(8,2) NOT NULL DEFAULT 10,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_machine_slots PRIMARY KEY (id),
    CONSTRAINT uq_machine_slots_number
        UNIQUE (machine_id, slot_number),

    CONSTRAINT fk_machine_slots_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_machine_slots_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_machine_slots_capacity
        CHECK (capacity_ml > 0),

    CONSTRAINT chk_machine_slots_current_volume
        CHECK (
            current_volume_ml >= 0
            AND current_volume_ml <= capacity_ml
        ),

    CONSTRAINT chk_machine_slots_threshold
        CHECK (low_stock_threshold_ml >= 0),

    CONSTRAINT chk_machine_slots_status
        CHECK (
            status IN (
                'AVAILABLE',
                'LOW_STOCK',
                'EMPTY',
                'DISABLED',
                'ERROR'
            )
        ),

    INDEX idx_machine_slots_product_id (product_id),
    INDEX idx_machine_slots_status (status)
) ENGINE=InnoDB;
-- =========================================================
-- 9. ORDERS
-- Đơn hàng trải nghiệm nước hoa.
-- =========================================================

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NOT NULL,
    machine_id BIGINT UNSIGNED NOT NULL,
    slot_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,

    product_name_snapshot VARCHAR(200) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'VND',

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    expires_at DATETIME NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_orders PRIMARY KEY (id),

    CONSTRAINT fk_orders_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_orders_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_orders_slot
        FOREIGN KEY (slot_id)
        REFERENCES machine_slots(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_orders_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_orders_price
        CHECK (unit_price >= 0),

    CONSTRAINT chk_orders_status
        CHECK (
            status IN (
                'PENDING_PAYMENT',
                'PAID',
                'DISPENSING',
                'DISPENSED',
                'FAILED',
                'EXPIRED',
                'REFUNDED',
                'REVIEW'
            )
        ),

    INDEX idx_orders_tenant_created (tenant_id, created_at),
    INDEX idx_orders_machine_id (machine_id),
    INDEX idx_orders_slot_id (slot_id),
    INDEX idx_orders_product_id (product_id),
    INDEX idx_orders_status (status)
) ENGINE=InnoDB;


-- =========================================================
-- 10. PAYMENTS
-- Một đơn hàng có thể có nhiều lần thử thanh toán.
-- =========================================================

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id BIGINT UNSIGNED NOT NULL,
    payment_ref VARCHAR(150) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'VND',

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    gateway_transaction_id VARCHAR(150) NULL,
    payload JSON NULL,
    paid_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT uq_payments_payment_ref UNIQUE (payment_ref),
    CONSTRAINT uq_payments_gateway_transaction
        UNIQUE (gateway_transaction_id),

    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_payments_amount
        CHECK (amount >= 0),

    CONSTRAINT chk_payments_status
        CHECK (
            status IN (
                'PENDING',
                'SUCCESS',
                'FAILED',
                'CANCELLED',
                'EXPIRED',
                'REFUNDED'
            )
        ),

    INDEX idx_payments_order_id (order_id),
    INDEX idx_payments_status (status)
) ENGINE=InnoDB;
-- =========================================================
-- 11. DISPENSE_COMMANDS
-- Lưu lệnh xịt nước hoa gửi đến máy IoT.
-- =========================================================

CREATE TABLE IF NOT EXISTS dispense_commands (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    command_token VARCHAR(150) NOT NULL,
    order_id BIGINT UNSIGNED NULL,
    machine_id BIGINT UNSIGNED NOT NULL,
    slot_id BIGINT UNSIGNED NOT NULL,
    command_type VARCHAR(30) NOT NULL,
    volume_ml DECIMAL(8,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at DATETIME NULL,
    completed_at DATETIME NULL,

    CONSTRAINT pk_dispense_commands PRIMARY KEY (id),
    CONSTRAINT uq_dispense_commands_token UNIQUE (command_token),

    -- Mỗi đơn hàng chỉ có tối đa một lệnh xịt.
    CONSTRAINT uq_dispense_commands_order UNIQUE (order_id),

    CONSTRAINT fk_dispense_commands_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_dispense_commands_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_dispense_commands_slot
        FOREIGN KEY (slot_id)
        REFERENCES machine_slots(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_dispense_commands_type
        CHECK (command_type IN ('CUSTOMER_ORDER', 'DIAGNOSTIC')),

    CONSTRAINT chk_dispense_commands_volume
        CHECK (volume_ml > 0),

    CONSTRAINT chk_dispense_commands_status
        CHECK (
            status IN (
                'PENDING',
                'SENT',
                'ACKNOWLEDGED',
                'SUCCESS',
                'FAILED',
                'UNCERTAIN',
                'CANCELLED'
            )
        ),

    INDEX idx_dispense_commands_machine_status (machine_id, status),
    INDEX idx_dispense_commands_slot_id (slot_id),
    INDEX idx_dispense_commands_created_at (created_at)
) ENGINE=InnoDB;


-- =========================================================
-- 12. DISPENSE_EXECUTIONS
-- Lưu kết quả thực tế của việc thực thi lệnh xịt.
-- =========================================================

CREATE TABLE IF NOT EXISTS dispense_executions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    command_id BIGINT UNSIGNED NOT NULL,
    execution_status VARCHAR(20) NOT NULL,
    error_code VARCHAR(100) NULL,
    hardware_telemetry JSON NULL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_dispense_executions PRIMARY KEY (id),

    -- Mỗi lệnh có tối đa một bản ghi kết quả cuối cùng.
    CONSTRAINT uq_dispense_executions_command UNIQUE (command_id),

    CONSTRAINT fk_dispense_executions_command
        FOREIGN KEY (command_id)
        REFERENCES dispense_commands(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_dispense_executions_status
        CHECK (
            execution_status IN ('SUCCESS', 'FAILED', 'UNCERTAIN')
        ),

    INDEX idx_dispense_executions_status (execution_status),
    INDEX idx_dispense_executions_recorded_at (recorded_at)
) ENGINE=InnoDB;
-- =========================================================
-- 13. REFILLS
-- Lịch sử tiếp nước hoa vào khe máy.
-- =========================================================

CREATE TABLE IF NOT EXISTS refills (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    slot_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    performed_by_user_id BIGINT UNSIGNED NULL,
    volume_added_ml DECIMAL(8,2) NOT NULL,
    notes TEXT NULL,
    refilled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_refills PRIMARY KEY (id),

    CONSTRAINT fk_refills_slot
        FOREIGN KEY (slot_id)
        REFERENCES machine_slots(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_refills_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_refills_user
        FOREIGN KEY (performed_by_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_refills_volume
        CHECK (volume_added_ml > 0),

    INDEX idx_refills_slot_id (slot_id),
    INDEX idx_refills_refilled_at (refilled_at)
) ENGINE=InnoDB;


-- =========================================================
-- 14. INVENTORY_TRANSACTIONS
-- Nhật ký mọi biến động tồn kho trong khe máy.
-- =========================================================

CREATE TABLE IF NOT EXISTS inventory_transactions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    slot_id BIGINT UNSIGNED NOT NULL,
    order_id BIGINT UNSIGNED NULL,
    execution_id BIGINT UNSIGNED NULL,
    refill_id BIGINT UNSIGNED NULL,
    performed_by_user_id BIGINT UNSIGNED NULL,

    transaction_type VARCHAR(30) NOT NULL,
    volume_change_ml DECIMAL(8,2) NOT NULL,
    notes TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_inventory_transactions PRIMARY KEY (id),

    CONSTRAINT fk_inventory_transactions_slot
        FOREIGN KEY (slot_id)
        REFERENCES machine_slots(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_inventory_transactions_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_inventory_transactions_execution
        FOREIGN KEY (execution_id)
        REFERENCES dispense_executions(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_inventory_transactions_refill
        FOREIGN KEY (refill_id)
        REFERENCES refills(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_inventory_transactions_user
        FOREIGN KEY (performed_by_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_inventory_transactions_type
        CHECK (
            transaction_type IN (
                'DISPENSE_SALE',
                'DIAGNOSTIC_SPRAY',
                'REFILL',
                'ADJUSTMENT',
                'REMOVAL',
                'DISPOSAL'
            )
        ),

    CONSTRAINT chk_inventory_transactions_volume
        CHECK (volume_change_ml <> 0),

    INDEX idx_inventory_transactions_slot_created
        (slot_id, created_at),

    INDEX idx_inventory_transactions_order_id (order_id),
    INDEX idx_inventory_transactions_execution_id (execution_id),
    INDEX idx_inventory_transactions_refill_id (refill_id)
) ENGINE=InnoDB;
-- =========================================================
-- 15. SENSOR_READINGS
-- Lưu dữ liệu cảm biến từ máy IoT.
-- =========================================================

CREATE TABLE IF NOT EXISTS sensor_readings (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    machine_id BIGINT UNSIGNED NOT NULL,
    slot_id BIGINT UNSIGNED NULL,
    sensor_type VARCHAR(50) NOT NULL,
    sensor_value DECIMAL(12,4) NOT NULL,
    unit VARCHAR(30) NULL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_sensor_readings PRIMARY KEY (id),

    CONSTRAINT fk_sensor_readings_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_sensor_readings_slot
        FOREIGN KEY (slot_id)
        REFERENCES machine_slots(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_sensor_readings_machine_time
        (machine_id, recorded_at),

    INDEX idx_sensor_readings_slot_time
        (slot_id, recorded_at),

    INDEX idx_sensor_readings_type (sensor_type)
) ENGINE=InnoDB;


-- =========================================================
-- 16. DEVICE_EVENTS
-- Lưu sự kiện được gửi từ thiết bị IoT.
-- =========================================================

CREATE TABLE IF NOT EXISTS device_events (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    machine_id BIGINT UNSIGNED NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    event_code VARCHAR(100) NULL,
    message TEXT NULL,
    payload JSON NULL,
    occurred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_device_events PRIMARY KEY (id),

    CONSTRAINT fk_device_events_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_device_events_severity
        CHECK (
            severity IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL')
        ),

    INDEX idx_device_events_machine_time
        (machine_id, occurred_at),

    INDEX idx_device_events_type (event_type),
    INDEX idx_device_events_severity (severity)
) ENGINE=InnoDB;


-- =========================================================
-- 17. ALERTS
-- Quản lý cảnh báo phát sinh từ máy hoặc hệ thống.
-- =========================================================

CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NOT NULL,
    machine_id BIGINT UNSIGNED NULL,
    slot_id BIGINT UNSIGNED NULL,
    device_event_id BIGINT UNSIGNED NULL,
    assigned_to_user_id BIGINT UNSIGNED NULL,

    alert_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'WARNING',
    title VARCHAR(200) NOT NULL,
    message TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',

    triggered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at DATETIME NULL,
    resolved_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_alerts PRIMARY KEY (id),

    CONSTRAINT fk_alerts_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_alerts_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_alerts_slot
        FOREIGN KEY (slot_id)
        REFERENCES machine_slots(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_alerts_device_event
        FOREIGN KEY (device_event_id)
        REFERENCES device_events(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_alerts_assigned_user
        FOREIGN KEY (assigned_to_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_alerts_severity
        CHECK (
            severity IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL')
        ),

    CONSTRAINT chk_alerts_status
        CHECK (
            status IN ('OPEN', 'ACKNOWLEDGED', 'RESOLVED', 'DISMISSED')
        ),

    INDEX idx_alerts_tenant_status (tenant_id, status),
    INDEX idx_alerts_machine_id (machine_id),
    INDEX idx_alerts_severity (severity),
    INDEX idx_alerts_triggered_at (triggered_at)
) ENGINE=InnoDB;
-- =========================================================
-- 15. SENSOR_READINGS
-- Lưu dữ liệu cảm biến từ máy IoT.
-- =========================================================

CREATE TABLE IF NOT EXISTS sensor_readings (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    machine_id BIGINT UNSIGNED NOT NULL,
    slot_id BIGINT UNSIGNED NULL,
    sensor_type VARCHAR(50) NOT NULL,
    sensor_value DECIMAL(12,4) NOT NULL,
    unit VARCHAR(30) NULL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_sensor_readings PRIMARY KEY (id),

    CONSTRAINT fk_sensor_readings_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_sensor_readings_slot
        FOREIGN KEY (slot_id)
        REFERENCES machine_slots(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_sensor_readings_machine_time
        (machine_id, recorded_at),

    INDEX idx_sensor_readings_slot_time
        (slot_id, recorded_at),

    INDEX idx_sensor_readings_type (sensor_type)
) ENGINE=InnoDB;


-- =========================================================
-- 16. DEVICE_EVENTS
-- Lưu sự kiện được gửi từ thiết bị IoT.
-- =========================================================

CREATE TABLE IF NOT EXISTS device_events (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    machine_id BIGINT UNSIGNED NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    event_code VARCHAR(100) NULL,
    message TEXT NULL,
    payload JSON NULL,
    occurred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_device_events PRIMARY KEY (id),

    CONSTRAINT fk_device_events_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_device_events_severity
        CHECK (
            severity IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL')
        ),

    INDEX idx_device_events_machine_time
        (machine_id, occurred_at),

    INDEX idx_device_events_type (event_type),
    INDEX idx_device_events_severity (severity)
) ENGINE=InnoDB;


-- =========================================================
-- 17. ALERTS
-- Quản lý cảnh báo phát sinh từ máy hoặc hệ thống.
-- =========================================================

CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NOT NULL,
    machine_id BIGINT UNSIGNED NULL,
    slot_id BIGINT UNSIGNED NULL,
    device_event_id BIGINT UNSIGNED NULL,
    assigned_to_user_id BIGINT UNSIGNED NULL,

    alert_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'WARNING',
    title VARCHAR(200) NOT NULL,
    message TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',

    triggered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at DATETIME NULL,
    resolved_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_alerts PRIMARY KEY (id),

    CONSTRAINT fk_alerts_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_alerts_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_alerts_slot
        FOREIGN KEY (slot_id)
        REFERENCES machine_slots(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_alerts_device_event
        FOREIGN KEY (device_event_id)
        REFERENCES device_events(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_alerts_assigned_user
        FOREIGN KEY (assigned_to_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_alerts_severity
        CHECK (
            severity IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL')
        ),

    CONSTRAINT chk_alerts_status
        CHECK (
            status IN ('OPEN', 'ACKNOWLEDGED', 'RESOLVED', 'DISMISSED')
        ),

    INDEX idx_alerts_tenant_status (tenant_id, status),
    INDEX idx_alerts_machine_id (machine_id),
    INDEX idx_alerts_severity (severity),
    INDEX idx_alerts_triggered_at (triggered_at)
) ENGINE=InnoDB;
-- =========================================================
-- 18. MAINTENANCE_TICKETS
-- Phiếu yêu cầu bảo trì máy IoT.
-- =========================================================

CREATE TABLE IF NOT EXISTS maintenance_tickets (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NOT NULL,
    machine_id BIGINT UNSIGNED NOT NULL,
    alert_id BIGINT UNSIGNED NULL,
    reported_by_user_id BIGINT UNSIGNED NULL,
    assigned_to_user_id BIGINT UNSIGNED NULL,

    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',

    opened_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at DATETIME NULL,
    resolved_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_maintenance_tickets PRIMARY KEY (id),

    CONSTRAINT fk_maintenance_tickets_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_maintenance_tickets_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_maintenance_tickets_alert
        FOREIGN KEY (alert_id)
        REFERENCES alerts(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_maintenance_tickets_reporter
        FOREIGN KEY (reported_by_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_maintenance_tickets_assignee
        FOREIGN KEY (assigned_to_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_maintenance_tickets_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),

    CONSTRAINT chk_maintenance_tickets_status
        CHECK (
            status IN (
                'OPEN',
                'IN_PROGRESS',
                'ON_HOLD',
                'RESOLVED',
                'CLOSED',
                'CANCELLED'
            )
        ),

    INDEX idx_maintenance_tickets_tenant_status
        (tenant_id, status),

    INDEX idx_maintenance_tickets_machine_id (machine_id),
    INDEX idx_maintenance_tickets_assignee (assigned_to_user_id)
) ENGINE=InnoDB;


-- =========================================================
-- 19. MAINTENANCE_LOGS
-- Lịch sử các hoạt động xử lý bảo trì.
-- =========================================================

CREATE TABLE IF NOT EXISTS maintenance_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ticket_id BIGINT UNSIGNED NOT NULL,
    performed_by_user_id BIGINT UNSIGNED NULL,
    action VARCHAR(100) NOT NULL,
    description TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_maintenance_logs PRIMARY KEY (id),

    CONSTRAINT fk_maintenance_logs_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES maintenance_tickets(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_maintenance_logs_user
        FOREIGN KEY (performed_by_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_maintenance_logs_ticket_time
        (ticket_id, created_at)
) ENGINE=InnoDB;


-- =========================================================
-- 20. AUDIT_LOGS
-- Nhật ký kiểm toán các thao tác quan trọng.
-- =========================================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NULL,
    user_id BIGINT UNSIGNED NULL,

    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT UNSIGNED NULL,

    old_values JSON NULL,
    new_values JSON NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_audit_logs PRIMARY KEY (id),

    CONSTRAINT fk_audit_logs_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_audit_logs_tenant_time (tenant_id, created_at),
    INDEX idx_audit_logs_user_time (user_id, created_at),
    INDEX idx_audit_logs_entity (entity_type, entity_id),
    INDEX idx_audit_logs_action (action)
) ENGINE=InnoDB;


-- =========================================================
-- 21. NOTIFICATIONS
-- Thông báo gửi đến người dùng.
-- =========================================================

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NULL,

    notification_type VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,

    reference_type VARCHAR(100) NULL,
    reference_id BIGINT UNSIGNED NULL,

    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at DATETIME NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_notifications PRIMARY KEY (id),

    CONSTRAINT fk_notifications_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_notifications_tenant_created
        (tenant_id, created_at),

    INDEX idx_notifications_user_read
        (user_id, is_read)
) ENGINE=InnoDB;
-- =========================================================
-- 18. MAINTENANCE_TICKETS
-- Phiếu yêu cầu bảo trì máy IoT.
-- =========================================================

CREATE TABLE IF NOT EXISTS maintenance_tickets (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NOT NULL,
    machine_id BIGINT UNSIGNED NOT NULL,
    alert_id BIGINT UNSIGNED NULL,
    reported_by_user_id BIGINT UNSIGNED NULL,
    assigned_to_user_id BIGINT UNSIGNED NULL,

    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',

    opened_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at DATETIME NULL,
    resolved_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_maintenance_tickets PRIMARY KEY (id),

    CONSTRAINT fk_maintenance_tickets_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_maintenance_tickets_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_maintenance_tickets_alert
        FOREIGN KEY (alert_id)
        REFERENCES alerts(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_maintenance_tickets_reporter
        FOREIGN KEY (reported_by_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_maintenance_tickets_assignee
        FOREIGN KEY (assigned_to_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_maintenance_tickets_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),

    CONSTRAINT chk_maintenance_tickets_status
        CHECK (
            status IN (
                'OPEN',
                'IN_PROGRESS',
                'ON_HOLD',
                'RESOLVED',
                'CLOSED',
                'CANCELLED'
            )
        ),

    INDEX idx_maintenance_tickets_tenant_status
        (tenant_id, status),

    INDEX idx_maintenance_tickets_machine_id (machine_id),
    INDEX idx_maintenance_tickets_assignee (assigned_to_user_id)
) ENGINE=InnoDB;


-- =========================================================
-- 19. MAINTENANCE_LOGS
-- Lịch sử các hoạt động xử lý bảo trì.
-- =========================================================

CREATE TABLE IF NOT EXISTS maintenance_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ticket_id BIGINT UNSIGNED NOT NULL,
    performed_by_user_id BIGINT UNSIGNED NULL,
    action VARCHAR(100) NOT NULL,
    description TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_maintenance_logs PRIMARY KEY (id),

    CONSTRAINT fk_maintenance_logs_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES maintenance_tickets(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_maintenance_logs_user
        FOREIGN KEY (performed_by_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_maintenance_logs_ticket_time
        (ticket_id, created_at)
) ENGINE=InnoDB;


-- =========================================================
-- 20. AUDIT_LOGS
-- Nhật ký kiểm toán các thao tác quan trọng.
-- =========================================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NULL,
    user_id BIGINT UNSIGNED NULL,

    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT UNSIGNED NULL,

    old_values JSON NULL,
    new_values JSON NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_audit_logs PRIMARY KEY (id),

    CONSTRAINT fk_audit_logs_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_audit_logs_tenant_time (tenant_id, created_at),
    INDEX idx_audit_logs_user_time (user_id, created_at),
    INDEX idx_audit_logs_entity (entity_type, entity_id),
    INDEX idx_audit_logs_action (action)
) ENGINE=InnoDB;


-- =========================================================
-- 21. NOTIFICATIONS
-- Thông báo gửi đến người dùng.
-- =========================================================

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NULL,

    notification_type VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,

    reference_type VARCHAR(100) NULL,
    reference_id BIGINT UNSIGNED NULL,

    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at DATETIME NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_notifications PRIMARY KEY (id),

    CONSTRAINT fk_notifications_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_notifications_tenant_created
        (tenant_id, created_at),

    INDEX idx_notifications_user_read
        (user_id, is_read)
) ENGINE=InnoDB;