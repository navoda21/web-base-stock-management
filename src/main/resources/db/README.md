# Database Structure Documentation

## Overview

This directory contains all SQL scripts for the Stock Management System database, organized in a clean, maintainable
structure.

## Directory Structure

```
db/
├── schema/          # Database schema definitions (DDL)
│   ├── 01-core-tables.sql                    # Users, authentication, audit logging
│   ├── 02-inventory-tables.sql               # Products, items, suppliers
│   ├── 03-people-management-tables.sql       # Staff and customers
│   ├── 04-sales-tables.sql                   # Bills and transactions
│   └── 05-promotions-discounts-tables.sql    # Promotions and discounts
│
└── data/            # Initial seed data (DML)
    └── initial-data.sql                       # All initial/seed data
```

## Execution Order

The SQL files should be executed in this order:

### 1. Schema Creation (Automatic)

The schema files are executed automatically by Spring Boot in alphabetical order:

1. `01-core-tables.sql` - Creates users, authentication tables
2. `02-inventory-tables.sql` - Creates products and supplier tables
3. `03-people-management-tables.sql` - Creates staff and customer tables
4. `04-sales-tables.sql` - Creates bills and transactions tables
5. `05-promotions-discounts-tables.sql` - Creates promotions and discounts tables

### 2. Data Population (Automatic)

- `initial-data.sql` - Populates all tables with seed data

## Configuration

### Application Properties

Add this to your `application.properties`:

```properties
# Database initialization
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:db/schema/*.sql
spring.sql.init.data-locations=classpath:db/data/initial-data.sql
spring.sql.init.continue-on-error=false
spring.sql.init.separator=;
```

## Table Relationships

### Core Tables

- `users` - System users with authentication
- `persistent_logins` - Remember-me functionality
- `user_sessions` - Active user sessions
- `password_reset_tokens` - Password reset tokens
- `audit_log` - System audit trail

### Inventory Tables

- `products` - Product catalog
- `item` - Legacy items table (backward compatibility)
- `supplier` - Supplier information

### People Management

- `staff` - Employee records
- `customers` - Customer records

### Sales Tables

- `bills` - Sales transactions
- `bill_items` - Line items for bills
- FK: `bills.customer_id` → `customers.id`
- FK: `bill_items.bill_id` → `bills.id`
- FK: `bill_items.product_id` → `products.id`

### Promotions & Discounts

- `promotions` - Marketing promotions
- `discounts` - Product discounts
- `discount_types` - Discount type enum (PERCENTAGE, FIXED_AMOUNT)
- `promotion_items` - Many-to-many: promotions ↔ products
- FK: `discounts.item_id` → `products.id`

## Indexes

All tables have appropriate indexes for:

- Primary keys (auto-created)
- Foreign keys (auto-created)
- Frequently queried columns (explicitly created)
- Date range queries (composite indexes)

## Data Seeding

The `initial-data.sql` file contains:

- 1 admin user (username: `admin`, password: `password123`)
- 5 staff members
- 5 customers
- 8 products
- 5 bills with line items
- 5 promotions
- 4 discounts

All INSERT statements use `ON DUPLICATE KEY UPDATE` for idempotency.

## Migration Notes

### Legacy Files (To be removed)

The following files in `src/main/resources/` are now obsolete:

- `schema.sql` - Replaced by `db/schema/*.sql`
- `data.sql` - Replaced by `db/data/initial-data.sql`
- `promotions-schema.sql` - Merged into `05-promotions-discounts-tables.sql`
- `promotion-discount-data.sql` - Merged into `initial-data.sql`
- `migration-fix-column-names.sql` - No longer needed
- `fix-promotion-discount-columns.sql` - No longer needed
- `test-table-structure.sql` - Remove after testing
- `create-indexes.sql` - Indexes now in schema files

## Best Practices

1. **Schema Changes**: Add new schema changes in numbered sequence files
2. **Data Changes**: Update `initial-data.sql` or create new data files
3. **Idempotency**: Always use `IF NOT EXISTS` for CREATE and `ON DUPLICATE KEY UPDATE` for INSERT
4. **Comments**: Document all changes with version and description headers
5. **Testing**: Test all scripts on a clean database before committing

## Troubleshooting

### Common Issues

1. **Foreign Key Errors**: Ensure tables are created in correct order (use numbering)
2. **Duplicate Key Errors**: Use `ON DUPLICATE KEY UPDATE` for data inserts
3. **Column Not Found**: Check if migration ran successfully
4. **Index Exists**: Use `IF NOT EXISTS` clause

### Reset Database

To completely reset the database:

```sql
DROP DATABASE IF EXISTS stock_management;
CREATE DATABASE stock_management;
USE stock_management;
-- Then run all schema and data files
```

## Version History

- **v1.0** (2025-10-15): Initial organized structure
    - Separated schema from data
    - Organized into logical modules
    - Added comprehensive documentation
    - Fixed column naming consistency (active vs is_active)
