-- ============================================================================
-- STOCK MANAGEMENT SYSTEM - CORE SCHEMA
-- Version: 1.0
-- Description: Core tables for users, authentication, and audit logging
-- ============================================================================

-- Users Table
CREATE TABLE IF NOT EXISTS users
(
    user_id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    username
    VARCHAR
(
    50
) NOT NULL UNIQUE,
    email VARCHAR
(
    100
) NOT NULL UNIQUE,
    password_hash VARCHAR
(
    255
) NOT NULL,
    first_name VARCHAR
(
    50
) NOT NULL,
    last_name VARCHAR
(
    50
) NOT NULL,
    role VARCHAR
(
    50
) NOT NULL,
    is_active TINYINT
(
    1
) NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_users_created_by FOREIGN KEY
(
    created_by
) REFERENCES users
(
    user_id
)
                                                  ON DELETE SET NULL,
    CONSTRAINT fk_users_updated_by FOREIGN KEY
(
    updated_by
) REFERENCES users
(
    user_id
)
                                                  ON DELETE SET NULL
    );

-- Spring Security Remember-Me Table
CREATE TABLE IF NOT EXISTS persistent_logins
(
    username
    VARCHAR
(
    64
) NOT NULL,
    series VARCHAR
(
    64
) PRIMARY KEY,
    token VARCHAR
(
    64
) NOT NULL,
    last_used TIMESTAMP NOT NULL
    );

-- User Sessions Table (for remember-me functionality)
CREATE TABLE IF NOT EXISTS user_sessions
(
    session_id
    VARCHAR
(
    128
) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ip_address VARCHAR
(
    45
),
    user_agent TEXT,
    created_at DATETIME,
    expires_at DATETIME,
    is_active TINYINT
(
    1
) DEFAULT 1,
    CONSTRAINT fk_user_sessions_user FOREIGN KEY
(
    user_id
) REFERENCES users
(
    user_id
) ON DELETE CASCADE
    );

-- Password Reset Tokens Table
CREATE TABLE IF NOT EXISTS password_reset_tokens
(
    id
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT,
    token
    VARCHAR
(
    255
) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    PRIMARY KEY
(
    id
),
    CONSTRAINT fk_password_reset_user FOREIGN KEY
(
    user_id
) REFERENCES users
(
    user_id
) ON DELETE CASCADE
    );

-- Audit Log Table
CREATE TABLE IF NOT EXISTS audit_log
(
    log_id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT,
    user_id
    BIGINT,
    action_type
    VARCHAR
(
    50
) NOT NULL,
    table_name VARCHAR
(
    50
),
    record_id BIGINT,
    old_values JSON NULL,
    new_values JSON NULL,
    ip_address VARCHAR
(
    45
),
    user_agent TEXT,
    created_at DATETIME,
    CONSTRAINT fk_audit_user FOREIGN KEY
(
    user_id
) REFERENCES users
(
    user_id
) ON DELETE SET NULL
    );

-- Indexes for Core Tables
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_audit_log_user ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_table ON audit_log(table_name);
CREATE INDEX IF NOT EXISTS idx_audit_log_created ON audit_log(created_at);
