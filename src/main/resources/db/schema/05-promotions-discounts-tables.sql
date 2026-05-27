-- ============================================================================
-- STOCK MANAGEMENT SYSTEM - PROMOTIONS & DISCOUNTS SCHEMA
-- Version: 1.0
-- Description: Tables for promotions and discounts management
-- ============================================================================

-- Promotions Table
CREATE TABLE IF NOT EXISTS promotions
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT,
    name
    VARCHAR
(
    100
) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active TINYINT
(
    1
) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    id
)
    );

-- Discount Types Enum Table
CREATE TABLE IF NOT EXISTS discount_types
(
    id
    VARCHAR
(
    20
) NOT NULL,
    description VARCHAR
(
    100
) NOT NULL,
    PRIMARY KEY
(
    id
)
    );

-- Insert discount types
INSERT INTO discount_types (id, description)
VALUES ('PERCENTAGE', 'Percentage off the original price'),
       ('FIXED_AMOUNT', 'Fixed amount off the original price') ON DUPLICATE KEY
UPDATE description =
VALUES (description);

-- Discounts Table
CREATE TABLE IF NOT EXISTS discounts
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT,
    name
    VARCHAR
(
    100
) NOT NULL,
    description TEXT,
    type VARCHAR
(
    20
) NOT NULL,
    value DECIMAL
(
    10,
    2
) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active TINYINT
(
    1
) DEFAULT 1,
    item_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    id
),
    CONSTRAINT fk_discounts_type FOREIGN KEY
(
    type
) REFERENCES discount_types
(
    id
),
    CONSTRAINT fk_discounts_item FOREIGN KEY
(
    item_id
) REFERENCES item
(
    id
)
                                                  ON DELETE CASCADE
    );

-- Promotion Items Junction Table
CREATE TABLE IF NOT EXISTS promotion_items
(
    promotion_id
    BIGINT
    NOT
    NULL,
    item_id
    BIGINT
    NOT
    NULL,
    PRIMARY
    KEY
(
    promotion_id,
    item_id
),
    CONSTRAINT fk_promotion_items_promotion FOREIGN KEY
(
    promotion_id
) REFERENCES promotions
(
    id
) ON DELETE CASCADE,
    CONSTRAINT fk_promotion_items_item FOREIGN KEY
(
    item_id
) REFERENCES item
(
    id
)
  ON DELETE CASCADE
    );

-- Indexes for Promotions & Discounts Tables
CREATE INDEX IF NOT EXISTS idx_discounts_active_dates ON discounts (active, start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_promotions_active_dates ON promotions (active, start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_discounts_item ON discounts (item_id);
CREATE INDEX IF NOT EXISTS idx_promotion_items_promotion ON promotion_items (promotion_id);
CREATE INDEX IF NOT EXISTS idx_promotion_items_item ON promotion_items (item_id);
