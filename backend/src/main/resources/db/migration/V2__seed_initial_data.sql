-- ==========================================================
-- Flyway Migration V2: GateNest Seed Data
-- ==========================================================

-- Seed Community Settings
INSERT INTO community_settings (
    community_name, address, city, postal_code, default_timezone, contact_phone, contact_email, visitor_pass_validity_hours
) VALUES (
    'Greenfield Heights Community',
    '42 Orchid Boulevard, Silicon Corridor',
    'Bengaluru',
    '560103',
    'Asia/Kolkata',
    '+91 80 4912 0000',
    'community@greenfieldheights.example.com',
    12
);

-- Seed Blocks
INSERT INTO blocks (name, description, total_floors) VALUES
('Block A', 'Tower 1 - North Wing (Premium Residencies)', 14),
('Block B', 'Tower 2 - Central Park Facing', 14),
('Block C', 'Tower 3 - South Garden Facing', 12);

-- Seed Flats
-- Block A
INSERT INTO flats (block_id, flat_number, floor_number, extension_number) VALUES
(1, 'A-101', 1, '1101'),
(1, 'A-102', 1, '1102'),
(1, 'A-201', 2, '1201'),
(1, 'A-202', 2, '1202'),
(1, 'A-301', 3, '1301'),
(1, 'A-302', 3, '1302'),
(1, 'A-401', 4, '1401'),
(1, 'A-402', 4, '1402');

-- Block B
INSERT INTO flats (block_id, flat_number, floor_number, extension_number) VALUES
(2, 'B-101', 1, '2101'),
(2, 'B-102', 1, '2102'),
(2, 'B-201', 2, '2201'),
(2, 'B-202', 2, '2202'),
(2, 'B-301', 3, '2301'),
(2, 'B-302', 3, '2302');

-- Block C
INSERT INTO flats (block_id, flat_number, floor_number, extension_number) VALUES
(3, 'C-101', 1, '3101'),
(3, 'C-102', 1, '3102'),
(3, 'C-201', 2, '3201'),
(3, 'C-202', 2, '3202');

-- Seed Gates
INSERT INTO gates (name, code, description, gate_type, is_active) VALUES
('Main North Gate', 'GATE-NORTH-01', 'Primary entry for residents and verified visitors', 'GENERAL', TRUE),
('South Residents Gate', 'GATE-SOUTH-02', 'Dedicated automated RFID gate for registered residents', 'GENERAL', TRUE),
('East Service & Logistics Gate', 'GATE-EAST-03', 'Deliveries, maintenance trucks, and contractor services', 'SERVICE', TRUE);
