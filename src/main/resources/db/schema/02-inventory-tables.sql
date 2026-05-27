-- ============================================================================
-- STOCK MANAGEMENT SYSTEM - INVENTORY SCHEMA
-- Version: 2.0
-- Description: Tables for items (inventory) and suppliers
-- ============================================================================

-- Items Table (Primary Inventory Table)
CREATE TABLE IF NOT EXISTS item
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT,
    name
    VARCHAR
(
    255
) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    price DECIMAL
(
    10,
    2
) NOT NULL,
    category VARCHAR
(
    100
),
    sku VARCHAR
(
    50
) UNIQUE,
    description TEXT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    id
)
    );

-- Suppliers Table
CREATE TABLE IF NOT EXISTS supplier
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT,
    name
    VARCHAR
(
    255
) NOT NULL,
    address VARCHAR
(
    255
) NOT NULL,
    contact_no VARCHAR
(
    20
) NOT NULL,
    email VARCHAR
(
    100
) NOT NULL,
    rating INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    id
)
    );

-- Supplier Requests Table (Low Stock Notifications & Supplier Orders)
CREATE TABLE IF NOT EXISTS supplier_requests
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT,
    item_id
    BIGINT
    NOT
    NULL,
    requested_quantity
    INT
    NOT
    NULL,
    current_stock
    INT
    NOT
    NULL,
    status
    VARCHAR
(
    20
) NOT NULL DEFAULT 'PENDING',
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    requested_by VARCHAR
(
    100
),
    notes TEXT,
    processed_at DATETIME,
    processed_by VARCHAR
(
    100
),
    PRIMARY KEY
(
    id
),
    FOREIGN KEY
(
    item_id
) REFERENCES item
(
    id
) ON DELETE CASCADE
    );

-- Indexes for Inventory Tables
CREATE INDEX IF NOT EXISTS idx_item_sku ON item(sku);
CREATE INDEX IF NOT EXISTS idx_item_category ON item(category);
CREATE INDEX IF NOT EXISTS idx_item_name ON item(name);
CREATE INDEX IF NOT EXISTS idx_supplier_name ON supplier(name);
CREATE INDEX IF NOT EXISTS idx_supplier_email ON supplier(email);
CREATE INDEX IF NOT EXISTS idx_supplier_requests_status ON supplier_requests(status);
CREATE INDEX IF NOT EXISTS idx_supplier_requests_item_id ON supplier_requests(item_id);
CREATE INDEX IF NOT EXISTS idx_supplier_requests_requested_at ON supplier_requests(requested_at);
