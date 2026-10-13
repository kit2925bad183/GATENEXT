-- ==========================================================
-- Flyway Migration V1: GateNest Core Schema
-- Target Database: MySQL 8.4 LTS
-- ==========================================================

-- 1. Community Settings
CREATE TABLE IF NOT EXISTS community_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    community_name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    default_timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Kolkata',
    contact_phone VARCHAR(30) NULL,
    contact_email VARCHAR(120) NULL,
    visitor_pass_validity_hours INT NOT NULL DEFAULT 12,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Blocks
CREATE TABLE IF NOT EXISTS blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    total_floors INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Flats
CREATE TABLE IF NOT EXISTS flats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    block_id BIGINT NOT NULL,
    flat_number VARCHAR(30) NOT NULL,
    floor_number INT NOT NULL DEFAULT 1,
    extension_number VARCHAR(20) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_flats_block FOREIGN KEY (block_id) REFERENCES blocks(id) ON DELETE RESTRICT,
    CONSTRAINT uq_block_flat UNIQUE (block_id, flat_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_flats_block_id ON flats(block_id);

-- 4. Gates
CREATE TABLE IF NOT EXISTS gates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    gate_type VARCHAR(30) NOT NULL DEFAULT 'GENERAL', -- GENERAL, PEDESTRIAN, VEHICLE, SERVICE
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    mobile VARCHAR(30) NOT NULL,
    role VARCHAR(30) NOT NULL, -- ADMINISTRATOR, SECURITY, RESIDENT
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, PENDING, SUSPENDED, DEACTIVATED
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_status ON users(role, status);

-- 6. Resident Profiles
CREATE TABLE IF NOT EXISTS resident_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    flat_id BIGINT NOT NULL,
    occupancy_type VARCHAR(30) NOT NULL DEFAULT 'OWNER', -- OWNER, TENANT
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    rejection_reason VARCHAR(500) NULL,
    reviewed_by_user_id BIGINT NULL,
    reviewed_at TIMESTAMP NULL,
    emergency_contact VARCHAR(30) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_resident_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_resident_flat FOREIGN KEY (flat_id) REFERENCES flats(id) ON DELETE RESTRICT,
    CONSTRAINT fk_resident_reviewer FOREIGN KEY (reviewed_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_resident_flat_id ON resident_profiles(flat_id);
CREATE INDEX idx_resident_verification ON resident_profiles(verification_status);

-- 7. Security Gate Assignments
CREATE TABLE IF NOT EXISTS security_gate_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    gate_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    shift_name VARCHAR(50) NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sec_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sec_gate FOREIGN KEY (gate_id) REFERENCES gates(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sec_user_active ON security_gate_assignments(user_id, is_active);

-- 8. Visits
CREATE TABLE IF NOT EXISTS visits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visit_reference VARCHAR(30) NOT NULL UNIQUE,
    host_user_id BIGINT NOT NULL,
    destination_flat_id BIGINT NOT NULL,
    
    -- Historical Snapshot of Destination
    destination_block_name VARCHAR(50) NOT NULL,
    destination_flat_number VARCHAR(30) NOT NULL,
    host_name_snapshot VARCHAR(120) NOT NULL,
    host_mobile_snapshot VARCHAR(30) NOT NULL,

    -- Visitor Info
    visitor_name VARCHAR(120) NOT NULL,
    visitor_mobile VARCHAR(30) NOT NULL,
    visitor_category VARCHAR(30) NOT NULL DEFAULT 'GUEST', -- GUEST, DELIVERY, SERVICE, CAB, OTHER
    purpose VARCHAR(255) NOT NULL,
    vehicle_number VARCHAR(40) NULL,
    notes VARCHAR(500) NULL,

    -- Scheduling & State
    arrival_window_start TIMESTAMP NOT NULL,
    arrival_window_end TIMESTAMP NOT NULL,
    expected_duration_minutes INT NOT NULL DEFAULT 120,
    status VARCHAR(30) NOT NULL DEFAULT 'APPROVED', -- PENDING_APPROVAL, APPROVED, INSIDE, EXITED, REJECTED, CANCELLED, EXPIRED

    -- Pass Security
    pass_credential_hash VARCHAR(128) NULL,
    pass_generated_at TIMESTAMP NULL,

    -- Gate & Flow
    entry_gate_id BIGINT NULL,
    exit_gate_id BIGINT NULL,
    check_in_time TIMESTAMP NULL,
    check_out_time TIMESTAMP NULL,
    
    -- Workflow actors
    registered_by_user_id BIGINT NOT NULL, -- Host resident or Security staff
    approved_by_user_id BIGINT NULL,
    approved_at TIMESTAMP NULL,
    rejection_reason VARCHAR(500) NULL,
    admin_override_reason VARCHAR(500) NULL,
    admin_override_by_user_id BIGINT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_visits_host FOREIGN KEY (host_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_visits_flat FOREIGN KEY (destination_flat_id) REFERENCES flats(id) ON DELETE RESTRICT,
    CONSTRAINT fk_visits_entry_gate FOREIGN KEY (entry_gate_id) REFERENCES gates(id) ON DELETE SET NULL,
    CONSTRAINT fk_visits_exit_gate FOREIGN KEY (exit_gate_id) REFERENCES gates(id) ON DELETE SET NULL,
    CONSTRAINT fk_visits_registrar FOREIGN KEY (registered_by_user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_visits_ref ON visits(visit_reference);
CREATE INDEX idx_visits_status ON visits(status);
CREATE INDEX idx_visits_host ON visits(host_user_id);
CREATE INDEX idx_visits_flat ON visits(destination_flat_id);
CREATE INDEX idx_visits_window ON visits(arrival_window_start, arrival_window_end);
CREATE INDEX idx_visits_checkin ON visits(check_in_time);
CREATE INDEX idx_visits_cred_hash ON visits(pass_credential_hash);

-- 9. Visit Events (Immutable Timeline Log)
CREATE TABLE IF NOT EXISTS visit_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visit_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL, -- CREATED, APPROVED, REJECTED, CHECKED_IN, CHECKED_OUT, CANCELLED, EXPIRED, PASS_REGENERATED, OVERRIDDEN, NOTE_ADDED
    actor_user_id BIGINT NULL,
    gate_id BIGINT NULL,
    notes VARCHAR(500) NULL,
    metadata_json TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_events_visit FOREIGN KEY (visit_id) REFERENCES visits(id) ON DELETE RESTRICT,
    CONSTRAINT fk_events_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_events_gate FOREIGN KEY (gate_id) REFERENCES gates(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_events_visit_id ON visit_events(visit_id);

-- 10. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    link_url VARCHAR(255) NULL,
    notification_type VARCHAR(50) NOT NULL, -- VISITOR_REQUEST, VISITOR_APPROVED, VISITOR_REJECTED, VISITOR_CHECKED_IN, VISITOR_CHECKED_OUT, RESIDENT_APPROVED, RESIDENT_REJECTED
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notif_user_unread ON notifications(user_id, is_read, created_at);

-- 11. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id BIGINT NULL,
    actor_email VARCHAR(150) NULL,
    actor_role VARCHAR(30) NULL,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NULL,
    ip_address VARCHAR(50) NULL,
    details_json TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_actor ON audit_logs(actor_user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

-- 12. Idempotency Records
CREATE TABLE IF NOT EXISTS idempotency_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    handler_name VARCHAR(100) NOT NULL,
    status_code INT NOT NULL,
    response_payload MEDIUMTEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_idempotency_key ON idempotency_records(idempotency_key);

-- 13. Spring Session JDBC Tables
CREATE TABLE IF NOT EXISTS SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BLOB NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
