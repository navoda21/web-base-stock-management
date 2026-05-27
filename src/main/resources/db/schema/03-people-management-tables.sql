-- ============================================================================
-- STOCK MANAGEMENT SYSTEM - PEOPLE MANAGEMENT SCHEMA
-- Version: 1.0
-- Description: Tables for staff and customer management
-- ============================================================================

-- Staff Table
CREATE TABLE IF NOT EXISTS staff
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT,
    employee_id
    VARCHAR
(
    20
) NOT NULL UNIQUE,
    first_name VARCHAR
(
    50
) NOT NULL,
    last_name VARCHAR
(
    50
) NOT NULL,
    email VARCHAR
(
    100
) NOT NULL UNIQUE,
    phone VARCHAR
(
    20
),
    role VARCHAR
(
    50
) NOT NULL,
    department VARCHAR
(
    50
),
    hire_date DATE NOT NULL,
    salary DOUBLE,
    shift_schedule VARCHAR
(
    100
),
    performance_rating DOUBLE,
    is_active TINYINT
(
    1
) NOT NULL DEFAULT 1,
    photo_url VARCHAR
(
    255
),
    photo_thumbnail_url VARCHAR
(
    255
),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    id
)
    );

-- Customers Table
CREATE TABLE IF NOT EXISTS customers
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT,
    customer_id
    VARCHAR
(
    20
) UNIQUE,
    first_name VARCHAR
(
    50
) NOT NULL,
    last_name VARCHAR
(
    50
) NOT NULL,
    email VARCHAR
(
    100
) UNIQUE,
    phone VARCHAR
(
    20
) NOT NULL,
    address VARCHAR
(
    255
),
    city VARCHAR
(
    100
),
    postal_code VARCHAR
(
    20
),
    country VARCHAR
(
    100
),
    loyalty_points INT DEFAULT 0,
    membership_level VARCHAR
(
    50
) DEFAULT 'Standard',
    notes TEXT,
    is_active TINYINT
(
    1
) NOT NULL DEFAULT 1,
    photo_url VARCHAR
(
    255
),
    photo_thumbnail_url VARCHAR
(
    255
),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    id
)
    );

-- Indexes for People Management Tables
CREATE INDEX IF NOT EXISTS idx_staff_employee_id ON staff(employee_id);
CREATE INDEX IF NOT EXISTS idx_staff_email ON staff(email);
CREATE INDEX IF NOT EXISTS idx_staff_department ON staff(department);
CREATE INDEX IF NOT EXISTS idx_staff_active ON staff(is_active);
CREATE INDEX IF NOT EXISTS idx_customers_customer_id ON customers(customer_id);
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);
CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone);
CREATE INDEX IF NOT EXISTS idx_customers_membership ON customers(membership_level);
