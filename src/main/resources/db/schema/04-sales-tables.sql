-- ============================================================================
-- STOCK MANAGEMENT SYSTEM - SALES SCHEMA
-- Version: 1.0
-- Description: Tables for bills and transactions
-- ============================================================================

-- Bills Table
CREATE TABLE IF NOT EXISTS bills
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT,
    bill_number
    VARCHAR
(
    50
) NOT NULL UNIQUE,
    customer_id BIGINT,
    bill_date DATETIME NOT NULL,
    total_amount DECIMAL
(
    10,
    2
) NOT NULL DEFAULT 0.00,
    subtotal_amount DECIMAL
(
    10,
    2
) DEFAULT 0.00,
    tax_amount DECIMAL
(
    10,
    2
) DEFAULT 0.00,
    amount_paid DECIMAL
(
    10,
    2
) DEFAULT 0.00,
    payment_method VARCHAR
(
    50
),
    payment_reference VARCHAR
(
    100
),
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    id
),
    CONSTRAINT fk_bills_customer FOREIGN KEY
(
    customer_id
) REFERENCES customers
(
    id
)
                                                    ON DELETE SET NULL
    );

-- Bill Items Table
CREATE TABLE IF NOT EXISTS bill_items
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT,
    bill_id
    BIGINT
    NOT
    NULL,
    product_id
    BIGINT
    NOT
    NULL,
    quantity
    INT
    NOT
    NULL,
    unit_price
    DECIMAL
(
    10,
    2
) NOT NULL,
    total_price DECIMAL
(
    10,
    2
) NOT NULL,
    PRIMARY KEY
(
    id
),
    CONSTRAINT fk_bill_items_bill FOREIGN KEY
(
    bill_id
) REFERENCES bills
(
    id
) ON DELETE CASCADE,
    CONSTRAINT fk_bill_items_product FOREIGN KEY
(
    product_id
) REFERENCES item
(
    id
)
    );

-- Indexes for Sales Tables
CREATE INDEX IF NOT EXISTS idx_bills_customer_id ON bills(customer_id);
CREATE INDEX IF NOT EXISTS idx_bills_bill_date ON bills(bill_date);
CREATE INDEX IF NOT EXISTS idx_bills_status ON bills(status);
CREATE INDEX IF NOT EXISTS idx_bills_bill_number ON bills(bill_number);
CREATE INDEX IF NOT EXISTS idx_bill_items_bill_id ON bill_items(bill_id);
CREATE INDEX IF NOT EXISTS idx_bill_items_product_id ON bill_items(product_id);
