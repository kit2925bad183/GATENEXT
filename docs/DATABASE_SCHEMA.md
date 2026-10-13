# GateNest Database Schema & Migration Architecture

GateNest uses a relational schema with **Flyway Versioned Migrations (V1 to V4)**.
All persistence operations are handled via **Spring JDBC (`JdbcTemplate` / `NamedParameterJdbcTemplate`)** with parameterized SQL queries (no JPA/Hibernate).

---

## 📊 Flyway Migrations Overview

| Migration Version | Description | Key Tables Created / Modified |
| :--- | :--- | :--- |
| **V1__init_schema.sql** | Core community foundation, authentication, users, blocks, flats, gates, visits, visit events, audit logs, and sessions. | `users`, `resident_profiles`, `blocks`, `flats`, `gates`, `security_gate_assignments`, `visits`, `visit_events`, `notifications`, `audit_logs`, `SPRING_SESSION`, `SPRING_SESSION_ATTRIBUTES` |
| **V2__seed_initial_data.sql** | Seed data for Greenfield Heights community. | Seed records for Block A, B, C, Flats 101–504, Gates North, South, East, and initial roles. |
| **V3__enterprise_features_schema.sql** | Complete enterprise table schema. | `saved_visitor_contacts`, `recurring_visit_rules`, `group_invitations`, `group_invitation_members`, `parcels`, `parcel_events`, `staff_members`, `staff_authorizations`, `staff_attendance_sessions`, `parking_slots`, `vehicles`, `parking_assignments`, `complaints`, `complaint_comments`, `complaint_attachments`, `announcements`, `announcement_reads`, `security_assistance_requests`, `security_handover_notes`, `user_notification_preferences` |
| **V4__seed_enterprise_sample_data.sql** | Seed data for parking slots and initial domestic staff members. | Parking slots `P-B1-01` to `P-B2-04`, initial staff members. |

---

## 🗄️ Detailed Table Specifications

### 1. `saved_visitor_contacts`
- `id` (BIGINT PRIMARY KEY AUTO_INCREMENT)
- `resident_user_id` (BIGINT NOT NULL, FK to `users(id)`)
- `visitor_name` (VARCHAR(150) NOT NULL)
- `visitor_mobile` (VARCHAR(30) NOT NULL)
- `visitor_category` (VARCHAR(50) NOT NULL)
- `default_purpose` (VARCHAR(255))
- `default_vehicle_number` (VARCHAR(50))
- `notes` (TEXT)
- `created_at`, `updated_at` (DATETIME)

### 2. `recurring_visit_rules`
- `id` (BIGINT PRIMARY KEY AUTO_INCREMENT)
- `resident_user_id` (BIGINT NOT NULL, FK to `users(id)`)
- `destination_flat_id` (BIGINT NOT NULL, FK to `flats(id)`)
- `visitor_name` (VARCHAR(150) NOT NULL)
- `visitor_mobile` (VARCHAR(30) NOT NULL)
- `visitor_category` (VARCHAR(50) NOT NULL)
- `purpose` (VARCHAR(255) NOT NULL)
- `allowed_weekdays` (VARCHAR(100) NOT NULL) -- e.g. "MON,WED,FRI"
- `valid_from_date` (DATE NOT NULL)
- `valid_to_date` (DATE NOT NULL)
- `allowed_start_time` (VARCHAR(10) NOT NULL) -- "08:00"
- `allowed_end_time` (VARCHAR(10) NOT NULL) -- "18:00"
- `expected_duration_minutes` (INT DEFAULT 60)
- `is_active` (BOOLEAN DEFAULT TRUE)

### 3. `group_invitations` & `group_invitation_members`
- `group_invitations`:
  - `id` (BIGINT PRIMARY KEY AUTO_INCREMENT)
  - `host_user_id` (BIGINT NOT NULL, FK to `users(id)`)
  - `destination_flat_id` (BIGINT NOT NULL, FK to `flats(id)`)
  - `event_title` (VARCHAR(150) NOT NULL)
  - `group_reference` (VARCHAR(50) UNIQUE NOT NULL)
  - `arrival_window_start`, `arrival_window_end` (DATETIME NOT NULL)
  - `max_guests` (INT NOT NULL)
  - `status` (VARCHAR(30) DEFAULT 'APPROVED')
- `group_invitation_members`:
  - `id` (BIGINT PRIMARY KEY AUTO_INCREMENT)
  - `group_invitation_id` (BIGINT NOT NULL, FK to `group_invitations(id)`)
  - `guest_name` (VARCHAR(150) NOT NULL)
  - `guest_mobile` (VARCHAR(30))
  - `vehicle_number` (VARCHAR(50))
  - `pass_credential_hash` (VARCHAR(255))
  - `status` (VARCHAR(30) DEFAULT 'EXPECTED')
  - `check_in_time`, `check_out_time` (DATETIME)
  - `entry_gate_id`, `exit_gate_id` (BIGINT, FK to `gates(id)`)

### 4. `parcels` & `parcel_events`
- `parcels`:
  - `id` (BIGINT PRIMARY KEY AUTO_INCREMENT)
  - `recipient_user_id` (BIGINT NOT NULL, FK to `users(id)`)
  - `destination_flat_id` (BIGINT NOT NULL, FK to `flats(id)`)
  - `courier_name` (VARCHAR(100) NOT NULL)
  - `tracking_number` (VARCHAR(100))
  - `description` (VARCHAR(255))
  - `storage_location` (VARCHAR(100) NOT NULL)
  - `received_by_user_id` (BIGINT NOT NULL, FK to `users(id)`)
  - `received_at` (DATETIME NOT NULL)
  - `status` (VARCHAR(30) DEFAULT 'RECEIVED')
  - `collected_by_name` (VARCHAR(150))
  - `verified_by_user_id` (BIGINT, FK to `users(id)`)
  - `collected_at` (DATETIME)
  - `delegate_collector_name` (VARCHAR(150))
  - `delegate_notes` (VARCHAR(255))
- `parcel_events`:
  - `id` (BIGINT PRIMARY KEY AUTO_INCREMENT)
  - `parcel_id` (BIGINT NOT NULL, FK to `parcels(id)`)
  - `event_type` (VARCHAR(50) NOT NULL)
  - `actor_user_id` (BIGINT NOT NULL, FK to `users(id)`)
  - `notes` (VARCHAR(255))
  - `created_at` (DATETIME NOT NULL)

### 5. `staff_members`, `staff_authorizations`, `staff_attendance_sessions`
- `staff_members`: `id`, `full_name`, `phone`, `service_category`, `is_active`
- `staff_authorizations`: `id`, `staff_member_id`, `resident_user_id`, `destination_flat_id`, `schedule_notes`, `is_active`
- `staff_attendance_sessions`: `id`, `staff_member_id`, `entry_gate_id`, `exit_gate_id`, `check_in_time`, `check_out_time`, `status` (`ACTIVE`/`COMPLETED`)

### 6. `parking_slots`, `vehicles`, `parking_assignments`
- `parking_slots`: `id`, `slot_number`, `slot_type` (`RESIDENT_2W`, `RESIDENT_4W`, `VISITOR_4W`, `VISITOR_2W`), `is_occupied`
- `vehicles`: `id`, `resident_user_id`, `flat_id`, `vehicle_number`, `vehicle_type`, `make_model`
- `parking_assignments`: `id`, `parking_slot_id`, `vehicle_id`, `visit_id`, `assigned_at`, `released_at`

### 7. `complaints`, `complaint_comments`, `complaint_attachments`
- `complaints`: `id`, `resident_user_id`, `flat_id`, `category`, `subject`, `description`, `location_details`, `priority`, `status` (`OPEN`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`), `assigned_to_user_id`
- `complaint_comments`: `id`, `complaint_id`, `author_user_id`, `comment_text`, `created_at`
- `complaint_attachments`: `id`, `complaint_id`, `file_name`, `file_path`, `file_type`, `file_size_bytes`

### 8. `announcements` & `announcement_reads`
- `announcements`: `id`, `author_user_id`, `title`, `content`, `category`, `priority`, `target_audience` (`ALL`, `BLOCK`), `target_block_id`, `status` (`DRAFT`, `PUBLISHED`, `ARCHIVED`), `is_pinned`, `publish_at`, `expires_at`
- `announcement_reads`: `id`, `announcement_id`, `user_id`, `read_at`

### 9. `security_assistance_requests` & `security_handover_notes`
- `security_assistance_requests`: `id`, `resident_user_id`, `flat_id`, `category`, `location_details`, `description`, `status` (`SUBMITTED`, `ACKNOWLEDGED`, `RESOLVED`, `CANCELLED`), `acknowledged_by_user_id`, `acknowledged_at`
- `security_handover_notes`: `id`, `author_user_id`, `gate_id`, `shift_name`, `note_content`, `unresolved_issues`, `acknowledged_by_user_id`, `acknowledged_at`
