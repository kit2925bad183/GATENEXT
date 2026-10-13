-- ==========================================================
-- Flyway Migration V5: GateNest Advanced Filtering & Admin Security
-- Compatible with MySQL 8.4 LTS and H2 (MySQL Mode)
-- ==========================================================

-- 1. User Account Activations (Single-use secure tokens)
CREATE TABLE IF NOT EXISTS user_activations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP NULL,
    created_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_act_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_act_creator FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_act_token ON user_activations(token_hash);
CREATE INDEX idx_user_act_user_id ON user_activations(user_id);

-- 2. Password Reset Requests
CREATE TABLE IF NOT EXISTS password_resets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP NULL,
    ip_address VARCHAR(45) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pw_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pw_reset_token ON password_resets(token_hash);
CREATE INDEX idx_pw_reset_user_id ON password_resets(user_id);

-- 3. User Active Sessions Tracking
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id_hash VARCHAR(128) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    device_info VARCHAR(255) NULL,
    ip_address VARCHAR(45) NULL,
    last_activity_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_sess_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_sess_user_id ON user_sessions(user_id, is_revoked);

-- 4. Historical Visit Time Corrections
CREATE TABLE IF NOT EXISTS visit_corrections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visit_id BIGINT NOT NULL,
    actor_user_id BIGINT NOT NULL,
    field_name VARCHAR(50) NOT NULL, -- e.g. check_in_time, check_out_time
    old_value VARCHAR(100) NULL,
    new_value VARCHAR(100) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_correction_visit FOREIGN KEY (visit_id) REFERENCES visits(id) ON DELETE CASCADE,
    CONSTRAINT fk_correction_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_correction_visit_id ON visit_corrections(visit_id);

-- 5. Resident Flat Movement Audit Log
CREATE TABLE IF NOT EXISTS resident_moves (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resident_user_id BIGINT NOT NULL,
    old_flat_id BIGINT NOT NULL,
    new_flat_id BIGINT NOT NULL,
    authorized_by_user_id BIGINT NOT NULL,
    reason VARCHAR(500) NULL,
    move_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    future_invitations_invalidated_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_res_move_user FOREIGN KEY (resident_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_res_move_old_flat FOREIGN KEY (old_flat_id) REFERENCES flats(id) ON DELETE RESTRICT,
    CONSTRAINT fk_res_move_new_flat FOREIGN KEY (new_flat_id) REFERENCES flats(id) ON DELETE RESTRICT,
    CONSTRAINT fk_res_move_authorizer FOREIGN KEY (authorized_by_user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_res_move_resident ON resident_moves(resident_user_id);

-- 6. High-Performance Compound Indexes for Filtering
CREATE INDEX idx_visits_comp_status_checkin ON visits(status, check_in_time);
CREATE INDEX idx_visits_comp_flat_checkin ON visits(destination_flat_id, check_in_time);
CREATE INDEX idx_visits_comp_gate_checkin ON visits(entry_gate_id, check_in_time);
CREATE INDEX idx_visits_comp_sched_window ON visits(arrival_window_start, arrival_window_end, status);
CREATE INDEX idx_visits_comp_checkout ON visits(check_out_time);
CREATE INDEX idx_visits_comp_created ON visits(created_at);
