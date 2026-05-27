package com.stockmanagement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseIndexInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseIndexInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {
        logger.info("Creating database indexes if they don't exist...");

        try {
            // Check and create index for bills(customer_id)
            createIndexIfNotExists("bills", "customer_id", "idx_bills_customer_id");

            // Check and create index for bills(bill_date)
            createIndexIfNotExists("bills", "bill_date", "idx_bills_bill_date");

            // Check and create index for bills(status)
            createIndexIfNotExists("bills", "status", "idx_bills_status");

            // Check and create index for bill_items(bill_id)
            createIndexIfNotExists("bill_items", "bill_id", "idx_bill_items_bill_id");

            // Check and create index for bill_items(product_id)
            createIndexIfNotExists("bill_items", "product_id", "idx_bill_items_product_id");

            logger.info("Database indexes created successfully");
        } catch (Exception e) {
            logger.error("Error creating database indexes", e);
        }
    }

    private void createIndexIfNotExists(String tableName, String columnName, String indexName) {
        try {
            // Check if the index exists
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS " +
                            "WHERE table_schema = DATABASE() " +
                            "AND table_name = ? " +
                            "AND index_name = ?",
                    Integer.class,
                    tableName,
                    indexName
            );

            // If the index doesn't exist, create it
            if (count != null && count == 0) {
                String sql = String.format("CREATE INDEX %s ON %s(%s)", indexName, tableName, columnName);
                jdbcTemplate.execute(sql);
                logger.info("Created index {} on table {}", indexName, tableName);
            } else {
                logger.info("Index {} already exists on table {}", indexName, tableName);
            }
        } catch (Exception e) {
            logger.warn("Error checking/creating index {} on table {}: {}", indexName, tableName, e.getMessage());
        }
    }
}