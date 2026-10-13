-- ==========================================================
-- Flyway Migration V4: GateNest Enterprise Seed Sample Data
-- Compatible with MySQL 8.4 LTS and H2 (MySQL Mode)
-- ==========================================================

-- Seed Parking Slots (Basement 1 & 2)
INSERT INTO parking_slots (slot_number, slot_type, block_id, floor_level, is_reserved_for_residents, is_active) VALUES
('P-B1-01', 'FOUR_WHEELER', 1, 'Basement 1', TRUE, TRUE),
('P-B1-02', 'FOUR_WHEELER', 1, 'Basement 1', TRUE, TRUE),
('P-B1-03', 'FOUR_WHEELER', 1, 'Basement 1', FALSE, TRUE), -- Visitor
('P-B1-04', 'TWO_WHEELER', 1, 'Basement 1', FALSE, TRUE),  -- Visitor
('P-B1-05', 'EV_CHARGING', 1, 'Basement 1', FALSE, TRUE),  -- Visitor / Shared
('P-B1-06', 'FOUR_WHEELER', 2, 'Basement 1', TRUE, TRUE),
('P-B1-07', 'FOUR_WHEELER', 2, 'Basement 1', FALSE, TRUE), -- Visitor
('P-B1-08', 'TWO_WHEELER', 2, 'Basement 1', FALSE, TRUE),  -- Visitor
('P-B2-01', 'FOUR_WHEELER', 3, 'Basement 2', TRUE, TRUE),
('P-B2-02', 'FOUR_WHEELER', 3, 'Basement 2', FALSE, TRUE), -- Visitor
('P-B2-03', 'FOUR_WHEELER', 3, 'Basement 2', FALSE, TRUE), -- Visitor
('P-B2-04', 'TWO_WHEELER', 3, 'Basement 2', FALSE, TRUE);  -- Visitor

-- Seed Sample Staff Members (Domestic Help)
INSERT INTO staff_members (full_name, mobile, staff_type, id_proof_type, id_proof_number, is_active, notes) VALUES
('Lakshmi Devi', '+91 98 4511 2233', 'MAID', 'AADHAAR', 'XXXX-XXXX-1122', TRUE, 'Housekeeping & Cleaning Specialist'),
('Raju Kumar', '+91 98 4522 3344', 'DRIVER', 'DRIVING_LICENSE', 'DL-04-2022-9988', TRUE, 'Personal & School Driver'),
('Murugan S', '+91 98 4533 4455', 'GARDENER', 'VOTER_ID', 'VTR-8822-11', TRUE, 'Society & Balcony Garden Maintenance'),
('Anita Roy', '+91 98 4544 5566', 'COOK', 'AADHAAR', 'XXXX-XXXX-4455', TRUE, 'North & South Indian Cuisine Cook');
