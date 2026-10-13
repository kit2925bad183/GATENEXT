-- ==========================================================
-- Flyway Migration V3: GateNest Enterprise Enhancements Schema
-- Compatible with MySQL 8.4 LTS and H2 (MySQL Mode)
-- ==========================================================

-- 1. Saved Visitor Contacts (Owned per Resident)
CREATE TABLE IF NOT EXISTS saved_visitor_contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resident_user_id BIGINT NOT NULL,
    visitor_name VARCHAR(120) NOT NULL,
    visitor_mobile VARCHAR(30) NOT NULL,
    visitor_category VARCHAR(30) NOT NULL DEFAULT 'GUEST',
    default_purpose VARCHAR(255) NULL,
    default_vehicle_number VARCHAR(30) NULL,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_saved_contact_resident FOREIGN KEY (resident_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_saved_contacts_resident ON saved_visitor_contacts(resident_user_id);

-- 2. Recurring Visit Rules (Authorized Service Visitors)
CREATE TABLE IF NOT EXISTS recurring_visit_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resident_user_id BIGINT NOT NULL,
    destination_flat_id BIGINT NOT NULL,
    visitor_name VARCHAR(120) NOT NULL,
    visitor_mobile VARCHAR(30) NOT NULL,
    visitor_category VARCHAR(30) NOT NULL DEFAULT 'SERVICE',
    purpose VARCHAR(255) NOT NULL,
    allowed_weekdays VARCHAR(50) NOT NULL DEFAULT 'MON,TUE,WED,THU,FRI,SAT,SUN', -- Comma separated day codes
    valid_from_date DATE NOT NULL,
    valid_to_date DATE NOT NULL,
    allowed_start_time VARCHAR(10) NOT NULL DEFAULT '08:00',
    allowed_end_time VARCHAR(10) NOT NULL DEFAULT '20:00',
    expected_duration_minutes INT NOT NULL DEFAULT 60,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_recurring_rules_resident FOREIGN KEY (resident_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_rules_flat FOREIGN KEY (destination_flat_id) REFERENCES flats(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_recurring_rules_resident ON recurring_visit_rules(resident_user_id);
CREATE INDEX idx_recurring_rules_flat ON recurring_visit_rules(destination_flat_id);

-- 3. Group Invitations (Event & Party Visits)
CREATE TABLE IF NOT EXISTS group_invitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    host_user_id BIGINT NOT NULL,
    destination_flat_id BIGINT NOT NULL,
    event_title VARCHAR(150) NOT NULL,
    group_reference VARCHAR(50) NOT NULL UNIQUE,
    arrival_window_start TIMESTAMP NOT NULL,
    arrival_window_end TIMESTAMP NOT NULL,
    max_guests INT NOT NULL DEFAULT 10,
    notes VARCHAR(500) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'APPROVED', -- APPROVED, CANCELLED, EXPIRED, COMPLETED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_group_inv_host FOREIGN KEY (host_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_inv_flat FOREIGN KEY (destination_flat_id) REFERENCES flats(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_group_inv_host ON group_invitations(host_user_id);
CREATE INDEX idx_group_inv_status ON group_invitations(status);

-- 4. Group Invitation Members (Individual Named Guests)
CREATE TABLE IF NOT EXISTS group_invitation_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_invitation_id BIGINT NOT NULL,
    guest_name VARCHAR(120) NOT NULL,
    guest_mobile VARCHAR(30) NULL,
    vehicle_number VARCHAR(30) NULL,
    pass_credential_hash VARCHAR(64) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'EXPECTED', -- EXPECTED, INSIDE, CHECKED_OUT, CANCELLED
    check_in_time TIMESTAMP NULL,
    check_out_time TIMESTAMP NULL,
    entry_gate_id BIGINT NULL,
    exit_gate_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_group_mem_inv FOREIGN KEY (group_invitation_id) REFERENCES group_invitations(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_mem_entry_gate FOREIGN KEY (entry_gate_id) REFERENCES gates(id) ON DELETE SET NULL,
    CONSTRAINT fk_group_mem_exit_gate FOREIGN KEY (exit_gate_id) REFERENCES gates(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_group_mem_inv_id ON group_invitation_members(group_invitation_id);
CREATE INDEX idx_group_mem_status ON group_invitation_members(status);

-- 5. Parcels Management
CREATE TABLE IF NOT EXISTS parcels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_user_id BIGINT NOT NULL,
    destination_flat_id BIGINT NOT NULL,
    courier_name VARCHAR(100) NOT NULL,
    tracking_number VARCHAR(100) NULL,
    description VARCHAR(255) NULL,
    storage_location VARCHAR(100) NOT NULL DEFAULT 'Gate Security Room',
    received_by_user_id BIGINT NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'READY_FOR_COLLECTION', -- RECEIVED, READY_FOR_COLLECTION, COLLECTED, RETURNED
    collected_by_name VARCHAR(120) NULL,
    collected_by_user_id BIGINT NULL,
    verified_by_user_id BIGINT NULL,
    collected_at TIMESTAMP NULL,
    delegate_collector_name VARCHAR(120) NULL,
    delegate_notes VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_parcel_recipient FOREIGN KEY (recipient_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_parcel_flat FOREIGN KEY (destination_flat_id) REFERENCES flats(id) ON DELETE RESTRICT,
    CONSTRAINT fk_parcel_receiver FOREIGN KEY (received_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_parcel_verifier FOREIGN KEY (verified_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_parcels_recipient ON parcels(recipient_user_id);
CREATE INDEX idx_parcels_status ON parcels(status);
CREATE INDEX idx_parcels_flat ON parcels(destination_flat_id);

-- 6. Parcel Events Log
CREATE TABLE IF NOT EXISTS parcel_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parcel_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL, -- RECEIVED, READY_FOR_COLLECTION, DELEGATE_ASSIGNED, COLLECTED, RETURNED
    actor_user_id BIGINT NOT NULL,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parcel_event_parcel FOREIGN KEY (parcel_id) REFERENCES parcels(id) ON DELETE CASCADE,
    CONSTRAINT fk_parcel_event_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_parcel_events_parcel ON parcel_events(parcel_id);

-- 7. Staff Members (Domestic Help: Cleaners, Cooks, Drivers, etc.)
CREATE TABLE IF NOT EXISTS staff_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    mobile VARCHAR(30) NOT NULL UNIQUE,
    staff_type VARCHAR(50) NOT NULL, -- MAID, COOK, DRIVER, CLEANER, GARDENER, TUTOR, OTHER
    id_proof_type VARCHAR(50) NULL,
    id_proof_number VARCHAR(100) NULL,
    photo_url VARCHAR(255) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_staff_mobile ON staff_members(mobile);
CREATE INDEX idx_staff_type ON staff_members(staff_type);

-- 8. Staff Authorizations (Resident flat linkages)
CREATE TABLE IF NOT EXISTS staff_authorizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_member_id BIGINT NOT NULL,
    resident_user_id BIGINT NOT NULL,
    flat_id BIGINT NOT NULL,
    schedule_description VARCHAR(255) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    authorized_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_auth_staff FOREIGN KEY (staff_member_id) REFERENCES staff_members(id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_auth_resident FOREIGN KEY (resident_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_auth_flat FOREIGN KEY (flat_id) REFERENCES flats(id) ON DELETE CASCADE,
    CONSTRAINT uq_staff_resident_flat UNIQUE (staff_member_id, resident_user_id, flat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_staff_auth_resident ON staff_authorizations(resident_user_id);
CREATE INDEX idx_staff_auth_staff ON staff_authorizations(staff_member_id);

-- 9. Staff Attendance Sessions (Single active session enforced)
CREATE TABLE IF NOT EXISTS staff_attendance_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_member_id BIGINT NOT NULL,
    check_in_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    check_out_time TIMESTAMP NULL,
    entry_gate_id BIGINT NOT NULL,
    exit_gate_id BIGINT NULL,
    entry_guard_user_id BIGINT NOT NULL,
    exit_guard_user_id BIGINT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_att_staff FOREIGN KEY (staff_member_id) REFERENCES staff_members(id) ON DELETE RESTRICT,
    CONSTRAINT fk_staff_att_entry_gate FOREIGN KEY (entry_gate_id) REFERENCES gates(id) ON DELETE RESTRICT,
    CONSTRAINT fk_staff_att_exit_gate FOREIGN KEY (exit_gate_id) REFERENCES gates(id) ON DELETE SET NULL,
    CONSTRAINT fk_staff_att_entry_guard FOREIGN KEY (entry_guard_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_staff_att_exit_guard FOREIGN KEY (exit_guard_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_staff_att_staff ON staff_attendance_sessions(staff_member_id);
CREATE INDEX idx_staff_att_active ON staff_attendance_sessions(is_active);

-- 10. Parking Slots
CREATE TABLE IF NOT EXISTS parking_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_number VARCHAR(30) NOT NULL UNIQUE,
    slot_type VARCHAR(30) NOT NULL DEFAULT 'FOUR_WHEELER', -- TWO_WHEELER, FOUR_WHEELER, EV_CHARGING, VISITOR
    block_id BIGINT NULL,
    floor_level VARCHAR(20) NOT NULL DEFAULT 'Basement 1',
    is_reserved_for_residents BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_parking_slot_block FOREIGN KEY (block_id) REFERENCES blocks(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_parking_slots_type ON parking_slots(slot_type);

-- 11. Registered Resident Vehicles
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    flat_id BIGINT NOT NULL,
    vehicle_number VARCHAR(30) NOT NULL UNIQUE,
    vehicle_type VARCHAR(30) NOT NULL DEFAULT 'FOUR_WHEELER', -- TWO_WHEELER, FOUR_WHEELER, EV
    make_model VARCHAR(100) NULL,
    color VARCHAR(30) NULL,
    sticker_number VARCHAR(50) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_vehicle_owner FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_vehicle_flat FOREIGN KEY (flat_id) REFERENCES flats(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_vehicles_owner ON vehicles(owner_user_id);
CREATE INDEX idx_vehicles_flat ON vehicles(flat_id);

-- 12. Parking Assignments (Resident & Visitor allocations)
CREATE TABLE IF NOT EXISTS parking_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parking_slot_id BIGINT NOT NULL,
    vehicle_id BIGINT NULL,
    visit_id BIGINT NULL,
    resident_user_id BIGINT NULL,
    assigned_by_user_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_at TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_park_assign_slot FOREIGN KEY (parking_slot_id) REFERENCES parking_slots(id) ON DELETE RESTRICT,
    CONSTRAINT fk_park_assign_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL,
    CONSTRAINT fk_park_assign_visit FOREIGN KEY (visit_id) REFERENCES visits(id) ON DELETE SET NULL,
    CONSTRAINT fk_park_assign_resident FOREIGN KEY (resident_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_park_assign_assigner FOREIGN KEY (assigned_by_user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_parking_assign_slot ON parking_assignments(parking_slot_id);
CREATE INDEX idx_parking_assign_active ON parking_assignments(is_active);

-- 13. Complaints and Maintenance Requests
CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resident_user_id BIGINT NOT NULL,
    flat_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL, -- PLUMBING, ELECTRICAL, CARPENTRY, SECURITY, NOISE, COMMON_AREA, LIFT, OTHER
    subject VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    location_details VARCHAR(200) NULL,
    priority VARCHAR(30) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, URGENT
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
    assigned_to_user_id BIGINT NULL,
    resolved_at TIMESTAMP NULL,
    closed_at TIMESTAMP NULL,
    reopened_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaint_resident FOREIGN KEY (resident_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_complaint_flat FOREIGN KEY (flat_id) REFERENCES flats(id) ON DELETE RESTRICT,
    CONSTRAINT fk_complaint_assignee FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_complaints_resident ON complaints(resident_user_id);
CREATE INDEX idx_complaints_status ON complaints(status);
CREATE INDEX idx_complaints_priority ON complaints(priority);

-- 14. Complaint Comments & Timeline
CREATE TABLE IF NOT EXISTS complaint_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    author_user_id BIGINT NOT NULL,
    author_name_snapshot VARCHAR(120) NOT NULL,
    author_role VARCHAR(30) NOT NULL,
    comment_text TEXT NOT NULL,
    is_internal_note BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaint_comment_comp FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaint_comment_author FOREIGN KEY (author_user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_complaint_comments_comp ON complaint_comments(complaint_id);

-- 15. Complaint Attachments
CREATE TABLE IF NOT EXISTS complaint_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    uploaded_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaint_att_comp FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaint_att_uploader FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_complaint_att_comp ON complaint_attachments(complaint_id);

-- 16. Announcements & Notice Board
CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    announcement_type VARCHAR(50) NOT NULL DEFAULT 'GENERAL', -- GENERAL, MAINTENANCE, WATER_POWER, EVENT, EMERGENCY
    priority VARCHAR(30) NOT NULL DEFAULT 'NORMAL', -- NORMAL, IMPORTANT, URGENT
    audience_type VARCHAR(30) NOT NULL DEFAULT 'COMMUNITY', -- COMMUNITY, BLOCK
    target_block_id BIGINT NULL,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    published_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_announcement_author FOREIGN KEY (author_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_announcement_block FOREIGN KEY (target_block_id) REFERENCES blocks(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_announcements_published ON announcements(is_published, is_archived);
CREATE INDEX idx_announcements_audience ON announcements(audience_type, target_block_id);

-- 17. Announcement Read Receipts
CREATE TABLE IF NOT EXISTS announcement_reads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    announcement_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ann_read_ann FOREIGN KEY (announcement_id) REFERENCES announcements(id) ON DELETE CASCADE,
    CONSTRAINT fk_ann_read_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_ann_user_read UNIQUE (announcement_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. Security Assistance Requests (Resident Panic/Help alerts)
CREATE TABLE IF NOT EXISTS security_assistance_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resident_user_id BIGINT NOT NULL,
    flat_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'GENERAL', -- MEDICAL_ALERT, NOISE_DISTURBANCE, SUSPICIOUS_ACTIVITY, PARKING_BLOCK, EMERGENCY_HELP, GENERAL
    location_details VARCHAR(200) NULL,
    description TEXT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED', -- SUBMITTED, ACKNOWLEDGED, RESOLVED, CANCELLED
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_by_user_id BIGINT NULL,
    acknowledged_at TIMESTAMP NULL,
    resolved_by_user_id BIGINT NULL,
    resolved_at TIMESTAMP NULL,
    resolution_notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_assist_resident FOREIGN KEY (resident_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_assist_flat FOREIGN KEY (flat_id) REFERENCES flats(id) ON DELETE RESTRICT,
    CONSTRAINT fk_assist_ack_user FOREIGN KEY (acknowledged_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_assist_res_user FOREIGN KEY (resolved_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_assist_status ON security_assistance_requests(status);
CREATE INDEX idx_assist_resident ON security_assistance_requests(resident_user_id);

-- 19. Security Handover Notes (Guard shift-to-shift logs)
CREATE TABLE IF NOT EXISTS security_handover_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_name VARCHAR(100) NOT NULL,
    gate_id BIGINT NOT NULL,
    author_user_id BIGINT NOT NULL,
    note_text TEXT NOT NULL,
    is_acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_by_user_id BIGINT NULL,
    acknowledged_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_handover_gate FOREIGN KEY (gate_id) REFERENCES gates(id) ON DELETE RESTRICT,
    CONSTRAINT fk_handover_author FOREIGN KEY (author_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_handover_ack_user FOREIGN KEY (acknowledged_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_handover_gate ON security_handover_notes(gate_id);

-- 20. User Notification Preferences
CREATE TABLE IF NOT EXISTS user_notification_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    visitor_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    parcel_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    complaint_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    notice_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    assistance_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_pref_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
