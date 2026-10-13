package com.gatenest.service;

import com.gatenest.model.*;
import com.gatenest.repository.KeyHolderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DemoDataSeederService {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeederService.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final Environment environment;
    private final PasswordEncoder passwordEncoder;
    private final PassCredentialService passCredentialService;

    @Value("${gatenest.demo-mode:false}")
    private boolean demoModeConfigured;

    public DemoDataSeederService(
            JdbcTemplate jdbcTemplate,
            DataSource dataSource,
            Environment environment,
            PasswordEncoder passwordEncoder,
            PassCredentialService passCredentialService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.environment = environment;
        this.passwordEncoder = passwordEncoder;
        this.passCredentialService = passCredentialService;
    }

    /**
     * Safety check to ensure we only ever run seeding/reset against an approved demo database.
     */
    public void verifyDemoDatabaseGuard() {
        boolean isDemoProfile = Arrays.asList(environment.getActiveProfiles()).contains("demo");
        String dbUrl = "";
        try (Connection conn = dataSource.getConnection()) {
            dbUrl = conn.getMetaData().getURL();
        } catch (SQLException e) {
            log.warn("Could not inspect database metadata URL: {}", e.getMessage());
        }

        boolean urlHasDemo = dbUrl.toLowerCase().contains("demo");
        if (!isDemoProfile && !urlHasDemo && !demoModeConfigured) {
            throw new SecurityException("SAFETY GUARD TRIGGERED: Refusing to run demo seeding against non-demo database (" + dbUrl + "). " +
                    "Active profiles must contain 'demo' or database URL must contain 'demo'.");
        }
    }

    public boolean isDemoEnvironment() {
        boolean isDemoProfile = Arrays.asList(environment.getActiveProfiles()).contains("demo");
        return isDemoProfile || demoModeConfigured;
    }

    @Transactional
    public Map<String, Object> seedDemoDataset(boolean resetFirst) {
        verifyDemoDatabaseGuard();

        Instant referenceNow = Instant.now();
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        Random rng = new Random(20260408L); // Deterministic seed for reproducible variation

        // Obtain demo password from environment variable DEMO_ACCOUNT_PASSWORD
        String envPassword = System.getenv("DEMO_ACCOUNT_PASSWORD");
        String demoPassword = (envPassword != null && !envPassword.trim().isEmpty())
                ? envPassword.trim()
                : "DemoPass2026!";
        String encodedPassword = passwordEncoder.encode(demoPassword);

        log.info("Resetting demo database tables prior to seeding...");
        resetDemoTables();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("referenceTimestamp", referenceNow.toString());
        stats.put("timezone", zoneId.getId());

        // 1. Community Settings
        seedCommunitySettings();
        stats.put("communityName", "Bluehaven Residency — Demo");

        // 2. Blocks & Flats & Gates
        Map<String, Long> blockIds = seedBlocks();
        Map<String, Long> flatIds = seedFlats(blockIds);
        Map<String, Long> gateIds = seedGates();
        stats.put("blocksCount", blockIds.size());
        stats.put("flatsCount", flatIds.size());
        stats.put("gatesCount", gateIds.size());

        // 3. Presentation Users & Residents
        Map<String, Long> userIds = seedPresentationAndResidentUsers(encodedPassword, flatIds, gateIds);
        stats.put("totalUsersCount", userIds.size());

        // 4. Visit History & Active Live Demo Scenario
        int visitsCount = seedVisits(userIds, flatIds, gateIds, referenceNow, rng, zoneId);
        stats.put("visitsCount", visitsCount);

        // 5. Additional Implemented Modules
        int parcelsCount = seedParcels(userIds, flatIds, referenceNow, rng);
        int complaintsCount = seedComplaints(userIds, flatIds, referenceNow, rng);
        int announcementsCount = seedAnnouncements(userIds.get("admin@gatenest.example"), blockIds);
        int staffCount = seedDomesticStaff(userIds, flatIds, gateIds, referenceNow, rng);
        int parkingCount = seedParkingAndVehicles(userIds, flatIds, blockIds, referenceNow, rng);
        seedSavedContactsAndRules(userIds, flatIds, gateIds, referenceNow);
        seedInAppNotifications(userIds, referenceNow);

        stats.put("parcelsCount", parcelsCount);
        stats.put("complaintsCount", complaintsCount);
        stats.put("announcementsCount", announcementsCount);
        stats.put("staffCount", staffCount);
        stats.put("parkingSlotsCount", 12);
        stats.put("status", "SUCCESS");

        log.info("Demo dataset seeded successfully! Summary: {}", stats);
        return stats;
    }

    private void resetDemoTables() {
        try {
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE;");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");
        } catch (Exception ignored) {}

        String[] tables = {
                "audit_logs",
                "notifications",
                "user_notification_preferences",
                "security_handover_notes",
                "security_assistance_requests",
                "announcement_reads",
                "announcements",
                "complaint_attachments",
                "complaint_comments",
                "complaints",
                "parking_assignments",
                "vehicles",
                "parking_slots",
                "staff_attendance_sessions",
                "staff_authorizations",
                "staff_members",
                "parcel_events",
                "parcels",
                "group_invitation_members",
                "group_invitations",
                "recurring_visit_rules",
                "saved_visitor_contacts",
                "visit_corrections",
                "visit_events",
                "visits",
                "security_gate_assignments",
                "resident_moves",
                "resident_profiles",
                "user_activations",
                "password_resets",
                "user_sessions",
                "users",
                "flats",
                "blocks",
                "gates",
                "community_settings"
        };

        for (String table : tables) {
            try {
                jdbcTemplate.update("DELETE FROM " + table);
            } catch (Exception e) {
                log.debug("Notice on clearing table {}: {}", table, e.getMessage());
            }
        }

        try {
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE;");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
        } catch (Exception ignored) {}
    }

    private void seedCommunitySettings() {
        jdbcTemplate.update("DELETE FROM community_settings");
        String sql = """
            INSERT INTO community_settings (
                community_name, address, city, postal_code, default_timezone, contact_phone, contact_email, visitor_pass_validity_hours
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
                "Bluehaven Residency — Demo",
                "108 Serene Coastal Boulevard, Bay View Enclave",
                "Chennai",
                "600041",
                "Asia/Kolkata",
                "+91 44 2490 0001",
                "management@bluehaven-demo.example",
                12
        );
    }

    private Map<String, Long> seedBlocks() {
        Map<String, Long> map = new HashMap<>();
        String[] names = {"Block A", "Block B", "Block C"};
        String[] desc = {
                "Tower A - Coastal View Wing",
                "Tower B - Central Greens Promenade",
                "Tower C - Bay Breeze Promenade"
        };

        for (int i = 0; i < names.length; i++) {
            KeyHolder kh = new GeneratedKeyHolder();
            final String name = names[i];
            final String description = desc[i];
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO blocks (name, description, total_floors, is_active) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, name);
                ps.setString(2, description);
                ps.setInt(3, 4);
                ps.setBoolean(4, true);
                return ps;
            }, kh);
            map.put(name, KeyHolderUtils.extractId(kh));
        }
        return map;
    }

    private Map<String, Long> seedFlats(Map<String, Long> blockIds) {
        Map<String, Long> map = new HashMap<>();
        char[] blockLetters = {'A', 'B', 'C'};

        for (char blk : blockLetters) {
            String blockName = "Block " + blk;
            Long blockId = blockIds.get(blockName);

            for (int floor = 1; floor <= 4; floor++) {
                for (int flatNum = 1; flatNum <= 4; flatNum++) {
                    String flatNumber = String.format("%c-%d%02d", blk, floor, flatNum);
                    String extension = String.format("%d%d%02d", blk - 'A' + 1, floor, flatNum);

                    KeyHolder kh = new GeneratedKeyHolder();
                    final int fFloor = floor;
                    jdbcTemplate.update(connection -> {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO flats (block_id, flat_number, floor_number, extension_number, is_active) VALUES (?, ?, ?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setLong(1, blockId);
                        ps.setString(2, flatNumber);
                        ps.setInt(3, fFloor);
                        ps.setString(4, extension);
                        ps.setBoolean(5, true);
                        return ps;
                    }, kh);
                    map.put(flatNumber, KeyHolderUtils.extractId(kh));
                }
            }
        }
        return map;
    }

    private Map<String, Long> seedGates() {
        Map<String, Long> map = new HashMap<>();

        String[][] gateDefs = {
                {"Main Gate", "MG-01", "Primary North Entrance for Residents and Visitors", "GENERAL"},
                {"Service Gate", "SG-02", "East Logistics, Logistics & Contractor Gate", "SERVICE"}
        };

        for (String[] g : gateDefs) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO gates (name, code, description, gate_type, is_active) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, g[0]);
                ps.setString(2, g[1]);
                ps.setString(3, g[2]);
                ps.setString(4, g[3]);
                ps.setBoolean(5, true);
                return ps;
            }, kh);
            map.put(g[0], KeyHolderUtils.extractId(kh));
        }
        return map;
    }

    private Map<String, Long> seedPresentationAndResidentUsers(
            String encodedPassword,
            Map<String, Long> flatIds,
            Map<String, Long> gateIds
    ) {
        Map<String, Long> userMap = new HashMap<>();

        // 1. Primary Admin
        Long admin1Id = insertUser("admin@gatenest.example", "Elena Vance (Lead Administrator)", "+91 98765 00001", "ADMINISTRATOR", "ACTIVE", encodedPassword);
        userMap.put("admin@gatenest.example", admin1Id);

        // 2. Additional Admin
        Long admin2Id = insertUser("admin2@gatenest.example", "Marcus Sterling (Operations Administrator)", "+91 98765 00002", "ADMINISTRATOR", "ACTIVE", encodedPassword);
        userMap.put("admin2@gatenest.example", admin2Id);

        // 3. Security Guard Main Gate
        Long sec1Id = insertUser("guard.main@gatenest.example", "Vikram Singh (Main Gate Lead Officer)", "+91 98765 00003", "SECURITY", "ACTIVE", encodedPassword);
        userMap.put("guard.main@gatenest.example", sec1Id);
        assignSecurityGate(sec1Id, gateIds.get("Main Gate"), "Day Shift (06:00 - 18:00)");

        // 4. Security Guard Service Gate
        Long sec2Id = insertUser("guard.service@gatenest.example", "Rajesh Nair (Service Gate Officer)", "+91 98765 00004", "SECURITY", "ACTIVE", encodedPassword);
        userMap.put("guard.service@gatenest.example", sec2Id);
        assignSecurityGate(sec2Id, gateIds.get("Service Gate"), "Day Shift (06:00 - 18:00)");

        // 5. Presenter Resident Account (Flat A-101)
        Long residentPresenterId = insertUser("resident@gatenest.example", "Dr. Ananya Roy", "+91 98765 00005", "RESIDENT", "ACTIVE", encodedPassword);
        userMap.put("resident@gatenest.example", residentPresenterId);
        insertResidentProfile(residentPresenterId, flatIds.get("A-101"), "OWNER", "APPROVED", null, admin1Id, "+91 98765 00000");

        // Fictional Resident Distribution: Total 36 residents
        // 28 Approved (mix of owners and tenants, including co-residents in A-101, A-201, B-102, C-301)
        // 5 Pending applications
        // 2 Rejected applications with realistic reasons
        // 1 Suspended resident

        String[][] approvedResidents = {
                {"ananya.spouse@gatenest.example", "Dr. Subhash Roy", "+91 98765 00006", "A-101", "OWNER", "+91 98765 00005"},
                {"priya.sharma@gatenest.example", "Priya Sharma", "+91 98765 00007", "A-102", "OWNER", "+91 98765 00091"},
                {"arun.patel@gatenest.example", "Arun Patel", "+91 98765 00008", "A-103", "TENANT", "+91 98765 00092"},
                {"deepa.iyer@gatenest.example", "Deepa Iyer", "+91 98765 00009", "A-104", "OWNER", "+91 98765 00093"},
                {"karthik.ram@gatenest.example", "Karthik Ramanathan", "+91 98765 00010", "A-201", "OWNER", "+91 98765 00094"},
                {"karthik.partner@gatenest.example", "Meera Ramanathan", "+91 98765 00011", "A-201", "OWNER", "+91 98765 00010"},
                {"sunil.verma@gatenest.example", "Sunil Verma", "+91 98765 00012", "A-202", "TENANT", "+91 98765 00095"},
                {"neha.kapoor@gatenest.example", "Neha Kapoor", "+91 98765 00013", "A-203", "OWNER", "+91 98765 00096"},
                {"tarun.joshi@gatenest.example", "Tarun Joshi", "+91 98765 00014", "A-204", "TENANT", "+91 98765 00097"},
                {"rohit.deshmukh@gatenest.example", "Rohit Deshmukh", "+91 98765 00015", "A-301", "OWNER", "+91 98765 00098"},
                {"anita.menon@gatenest.example", "Anita Menon", "+91 98765 00016", "A-302", "TENANT", "+91 98765 00099"},
                {"vijay.reddy@gatenest.example", "Vijay Reddy", "+91 98765 00017", "A-303", "OWNER", "+91 98765 00100"},
                {"siddharth.nair@gatenest.example", "Siddharth Nair", "+91 98765 00018", "B-101", "OWNER", "+91 98765 00101"},
                {"pooja.hegde@gatenest.example", "Pooja Hegde", "+91 98765 00019", "B-102", "TENANT", "+91 98765 00102"},
                {"pooja.roommate@gatenest.example", "Kavya Hegde", "+91 98765 00020", "B-102", "TENANT", "+91 98765 00019"},
                {"manish.gupta@gatenest.example", "Manish Gupta", "+91 98765 00021", "B-103", "OWNER", "+91 98765 00103"},
                {"divya.krishnan@gatenest.example", "Divya Krishnan", "+91 98765 00022", "B-201", "OWNER", "+91 98765 00104"},
                {"suresh.bhat@gatenest.example", "Suresh Bhat", "+91 98765 00023", "B-202", "TENANT", "+91 98765 00105"},
                {"anuradha.rao@gatenest.example", "Anuradha Rao", "+91 98765 00024", "B-203", "OWNER", "+91 98765 00106"},
                {"harish.chandran@gatenest.example", "Harish Chandran", "+91 98765 00025", "B-301", "TENANT", "+91 98765 00107"},
                {"gautam.sethi@gatenest.example", "Gautam Sethi", "+91 98765 00026", "C-101", "OWNER", "+91 98765 00108"},
                {"shweta.tiwari@gatenest.example", "Shweta Tiwari", "+91 98765 00027", "C-102", "TENANT", "+91 98765 00109"},
                {"madhavan.s@gatenest.example", "S. Madhavan", "+91 98765 00028", "C-201", "OWNER", "+91 98765 00110"},
                {"archana.k@gatenest.example", "Archana Kulkarni", "+91 98765 00029", "C-202", "TENANT", "+91 98765 00111"},
                {"vivek.singhania@gatenest.example", "Vivek Singhania", "+91 98765 00030", "C-301", "OWNER", "+91 98765 00112"},
                {"vivek.coowner@gatenest.example", "Ritu Singhania", "+91 98765 00031", "C-301", "OWNER", "+91 98765 00030"},
                {"kiran.v@gatenest.example", "Kiran Varma", "+91 98765 00032", "C-302", "TENANT", "+91 98765 00113"}
        };

        for (String[] r : approvedResidents) {
            Long uid = insertUser(r[0], r[1], r[2], "RESIDENT", "ACTIVE", encodedPassword);
            userMap.put(r[0], uid);
            insertResidentProfile(uid, flatIds.get(r[3]), r[4], "APPROVED", null, admin1Id, r[5]);
        }

        // 5 Pending resident registrations
        String[][] pendingResidents = {
                {"pending.res1@gatenest.example", "Nikhil Chawla (Pending Verification)", "+91 98765 00033", "B-302", "TENANT", "+91 98765 00114"},
                {"pending.res2@gatenest.example", "Gayathri Sundar (Pending Verification)", "+91 98765 00034", "B-401", "OWNER", "+91 98765 00115"},
                {"pending.res3@gatenest.example", "Abhishek Saxena (Pending Verification)", "+91 98765 00035", "C-204", "TENANT", "+91 98765 00116"},
                {"pending.res4@gatenest.example", "Pallavi Jadhav (Pending Verification)", "+91 98765 00036", "C-303", "OWNER", "+91 98765 00117"},
                {"pending.res5@gatenest.example", "Tanmay Sen (Pending Verification)", "+91 98765 00037", "A-304", "TENANT", "+91 98765 00118"}
        };
        for (String[] p : pendingResidents) {
            Long uid = insertUser(p[0], p[1], p[2], "RESIDENT", "PENDING", encodedPassword);
            userMap.put(p[0], uid);
            insertResidentProfile(uid, flatIds.get(p[3]), p[4], "PENDING", null, null, p[5]);
        }

        // 2 Rejected applications
        String[][] rejectedResidents = {
                {"rejected.res1@gatenest.example", "Rameshwar Prasad", "+91 98765 00038", "B-104", "TENANT", "+91 98765 00119", "Tenancy lease agreement expired; unverified notarization."},
                {"rejected.res2@gatenest.example", "Kavita Somani", "+91 98765 00039", "C-402", "OWNER", "+91 98765 00120", "Ownership deed name mismatch with government ID proof."}
        };
        for (String[] r : rejectedResidents) {
            Long uid = insertUser(r[0], r[1], r[2], "RESIDENT", "DEACTIVATED", encodedPassword);
            userMap.put(r[0], uid);
            insertResidentProfile(uid, flatIds.get(r[3]), r[4], "REJECTED", r[6], admin1Id, r[5]);
        }

        // 1 Suspended resident
        Long suspendedUid = insertUser("suspended.res@gatenest.example", "Vikramaditya Bose (Suspended Account)", "+91 98765 00040", "RESIDENT", "SUSPENDED", encodedPassword);
        userMap.put("suspended.res@gatenest.example", suspendedUid);
        insertResidentProfile(suspendedUid, flatIds.get("A-404"), "TENANT", "APPROVED", null, admin1Id, "+91 98765 00121");

        return userMap;
    }

    private Long insertUser(String email, String fullName, String mobile, String role, String status, String passwordHash) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (email, password_hash, full_name, mobile, role, status) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            ps.setString(4, mobile);
            ps.setString(5, role);
            ps.setString(6, status);
            return ps;
        }, kh);
        return KeyHolderUtils.extractId(kh);
    }

    private void insertResidentProfile(
            Long userId,
            Long flatId,
            String occupancyType,
            String verificationStatus,
            String rejectionReason,
            Long reviewedByUserId,
            String emergencyContact
    ) {
        String sql = """
            INSERT INTO resident_profiles (
                user_id, flat_id, occupancy_type, verification_status, rejection_reason, reviewed_by_user_id, reviewed_at, emergency_contact
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        Timestamp reviewedAt = "APPROVED".equals(verificationStatus) || "REJECTED".equals(verificationStatus)
                ? Timestamp.from(Instant.now().minus(5, ChronoUnit.DAYS))
                : null;

        jdbcTemplate.update(sql, userId, flatId, occupancyType, verificationStatus, rejectionReason, reviewedByUserId, reviewedAt, emergencyContact);
    }

    private void assignSecurityGate(Long userId, Long gateId, String shiftName) {
        String sql = """
            INSERT INTO security_gate_assignments (user_id, gate_id, is_active, shift_name, assigned_at)
            VALUES (?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql, userId, gateId, true, shiftName, Timestamp.from(Instant.now().minus(30, ChronoUnit.DAYS)));
    }

    private int seedVisits(
            Map<String, Long> userIds,
            Map<String, Long> flatIds,
            Map<String, Long> gateIds,
            Instant referenceNow,
            Random rng,
            ZoneId zoneId
    ) {
        Long guardMainId = userIds.get("guard.main@gatenest.example");
        Long guardServiceId = userIds.get("guard.service@gatenest.example");
        Long presenterId = userIds.get("resident@gatenest.example");
        Long presenterFlatId = flatIds.get("A-101");
        Long mainGateId = gateIds.get("Main Gate");
        Long serviceGateId = gateIds.get("Service Gate");

        int totalVisits = 0;

        // -------------------------------------------------------------
        // A. PRESENTER DEMONSTRATION SCENARIO (Dr. Ananya Roy, Flat A-101)
        // -------------------------------------------------------------

        // 1. Pending Walk-in 1: BlueDart Courier waiting at Main Gate
        insertCustomVisit(
                "GN-DEMO-WALKIN-01",
                presenterId,
                presenterFlatId,
                "Block A",
                "A-101",
                "Dr. Ananya Roy",
                "+91 98765 00005",
                "Deepak Sharma (BlueDart Express)",
                "+91 98401 11001",
                "DELIVERY",
                "BlueDart Urgent Medical Journal Courier",
                "TN-09-BD-1024",
                "Courier waiting at Main Gate security cabin for host resident approval",
                referenceNow.minus(15, ChronoUnit.MINUTES),
                referenceNow.plus(45, ChronoUnit.MINUTES),
                60,
                "PENDING_APPROVAL",
                null,
                null,
                null,
                null,
                null,
                null,
                guardMainId,
                null,
                null,
                null,
                referenceNow.minus(15, ChronoUnit.MINUTES),
                List.of(
                        new EventSpec("CREATED", guardMainId, mainGateId, "Walk-in delivery entry request submitted by Main Gate security", referenceNow.minus(15, ChronoUnit.MINUTES))
                )
        );
        totalVisits++;

        // 2. Pending Walk-in 2: Interior Consultant at Main Gate
        insertCustomVisit(
                "GN-DEMO-WALKIN-02",
                presenterId,
                presenterFlatId,
                "Block A",
                "A-101",
                "Dr. Ananya Roy",
                "+91 98765 00005",
                "Kavita Rao (Studio Arc Interior)",
                "+91 98401 11002",
                "SERVICE",
                "Interior Renovation Layout Discussion",
                "KA-03-MR-4412",
                "Visitor arrived for consultation; waiting for resident approval",
                referenceNow.minus(5, ChronoUnit.MINUTES),
                referenceNow.plus(55, ChronoUnit.MINUTES),
                90,
                "PENDING_APPROVAL",
                null,
                null,
                null,
                null,
                null,
                null,
                guardMainId,
                null,
                null,
                null,
                referenceNow.minus(5, ChronoUnit.MINUTES),
                List.of(
                        new EventSpec("CREATED", guardMainId, mainGateId, "Walk-in visitor registration logged at Main Gate", referenceNow.minus(5, ChronoUnit.MINUTES))
                )
        );
        totalVisits++;

        // 3. Valid Upcoming Invitation 1 for Today (Usable for Live QR / Manual Verification)
        String passToken1 = "PASS-BLUEHAVEN-DEMO-01";
        String passHash1 = passCredentialService.hashCredential(passToken1);
        insertCustomVisit(
                "GN-DEMO-INV-01",
                presenterId,
                presenterFlatId,
                "Block A",
                "A-101",
                "Dr. Ananya Roy",
                "+91 98765 00005",
                "Siddharth Verma",
                "+91 98401 11003",
                "GUEST",
                "Research Collaboration & Afternoon Tea",
                "TN-07-CS-9901",
                "Invited guest. Presentation pass token: PASS-BLUEHAVEN-DEMO-01",
                referenceNow.minus(2, ChronoUnit.HOURS),
                referenceNow.plus(6, ChronoUnit.HOURS),
                180,
                "APPROVED",
                passHash1,
                referenceNow.minus(1, ChronoUnit.DAYS),
                null,
                null,
                null,
                null,
                presenterId,
                presenterId,
                referenceNow.minus(1, ChronoUnit.DAYS),
                null,
                referenceNow.minus(1, ChronoUnit.DAYS),
                List.of(
                        new EventSpec("CREATED", presenterId, null, "Invitation created by host resident", referenceNow.minus(1, ChronoUnit.DAYS)),
                        new EventSpec("APPROVED", presenterId, null, "Pre-approved by host resident", referenceNow.minus(1, ChronoUnit.DAYS))
                )
        );
        totalVisits++;

        // 4. Valid Upcoming Invitation 2 for Today (Swiggy Delivery)
        String passToken2 = "PASS-BLUEHAVEN-DEMO-02";
        String passHash2 = passCredentialService.hashCredential(passToken2);
        insertCustomVisit(
                "GN-DEMO-INV-02",
                presenterId,
                presenterFlatId,
                "Block A",
                "A-101",
                "Dr. Ananya Roy",
                "+91 98765 00005",
                "Swiggy Delivery Partner",
                "+91 98401 11004",
                "DELIVERY",
                "Lunch Food Delivery from Gourmet Bowl",
                "TN-01-SW-5532",
                "Pre-approved delivery pass. Presentation pass token: PASS-BLUEHAVEN-DEMO-02",
                referenceNow.minus(1, ChronoUnit.HOURS),
                referenceNow.plus(4, ChronoUnit.HOURS),
                60,
                "APPROVED",
                passHash2,
                referenceNow.minus(3, ChronoUnit.HOURS),
                null,
                null,
                null,
                null,
                presenterId,
                presenterId,
                referenceNow.minus(3, ChronoUnit.HOURS),
                null,
                referenceNow.minus(3, ChronoUnit.HOURS),
                List.of(
                        new EventSpec("CREATED", presenterId, null, "Pre-approved delivery pass created", referenceNow.minus(3, ChronoUnit.HOURS)),
                        new EventSpec("APPROVED", presenterId, null, "Pre-approved by host resident", referenceNow.minus(3, ChronoUnit.HOURS))
                )
        );
        totalVisits++;

        // 5. Visitor Currently Inside Flat A-101 (Ready for live Check-Out demonstration)
        insertCustomVisit(
                "GN-DEMO-INSIDE-01",
                presenterId,
                presenterFlatId,
                "Block A",
                "A-101",
                "Dr. Ananya Roy",
                "+91 98765 00005",
                "Rohan Mehta",
                "+91 98401 11005",
                "GUEST",
                "Family Weekend Visit",
                "TN-09-RM-7721",
                "Entered via Main Gate, currently visiting Flat A-101",
                referenceNow.minus(2, ChronoUnit.HOURS),
                referenceNow.plus(4, ChronoUnit.HOURS),
                180,
                "INSIDE",
                passCredentialService.hashCredential("PASS-RM-INSIDE"),
                referenceNow.minus(1, ChronoUnit.DAYS),
                mainGateId,
                null,
                referenceNow.minus(45, ChronoUnit.MINUTES),
                null,
                presenterId,
                presenterId,
                referenceNow.minus(1, ChronoUnit.DAYS),
                null,
                referenceNow.minus(1, ChronoUnit.DAYS),
                List.of(
                        new EventSpec("CREATED", presenterId, null, "Invitation created by host", referenceNow.minus(1, ChronoUnit.DAYS)),
                        new EventSpec("APPROVED", presenterId, null, "Approved by host", referenceNow.minus(1, ChronoUnit.DAYS)),
                        new EventSpec("CHECKED_IN", guardMainId, mainGateId, "Checked in at Main Gate by Vikram Singh", referenceNow.minus(45, ChronoUnit.MINUTES))
                )
        );
        totalVisits++;

        // -------------------------------------------------------------
        // B. SYNTHETIC REALISTIC VISITOR DATASET (~175 additional visits)
        // Spanning Past 90 Days to Next 7 Days
        // -------------------------------------------------------------

        List<String> residentEmails = new ArrayList<>(userIds.keySet());
        residentEmails.remove("admin@gatenest.example");
        residentEmails.remove("admin2@gatenest.example");
        residentEmails.remove("guard.main@gatenest.example");
        residentEmails.remove("guard.service@gatenest.example");
        residentEmails.removeIf(e -> e.startsWith("pending") || e.startsWith("rejected") || e.startsWith("suspended"));

        String[] visitorNames = {
                "Aditya Joshi", "Bhavna Trivedi", "Chetan Bhagat", "Darshan Lal", "Eshwar Chand",
                "Fatima Sheikh", "Girish Karnad", "Hemant Karkare", "Indira Goswami", "Jitendra Kumar",
                "Komal Nath", "Laxman Rao", "Manoj Bajpayee", "Naveen Polishetty", "Om Puri",
                "Pankaj Tripathi", "Qasim Ali", "Raghubir Yadav", "Sanjay Mishra", "Tapasya Sen",
                "Uday Chopra", "Varun Grover", "Wahid Rehman", "Yashpal Sharma", "Zakir Khan",
                "Amazon Logistics Partner", "Flipkart Quick Courier", "Zomato Delivery Driver",
                "Urban Company Electrician", "Airtel Fiber Technician", "Eureka Forbes RO Service",
                "Water Tanker Operator", "Pest Control Specialist", "Furniture Assembly Crew",
                "Uber Premier Driver", "Ola Electric Cabs", "Blinkit Instant Delivery", "Zepto Courier"
        };

        String[] purposes = {
                "Social & Family Visit", "Weekend Dinner & Catchup", "Business Document Delivery",
                "Package Drop-off", "Fiber Optic Line Repair", "Appliance Maintenance & Servicing",
                "Cab Pickup / Drop", "Plumbing Leak Fix", "Pest Control Routine Treatment",
                "Interior Carpentry Work", "Birthday Celebration Guest", "Study Group Session"
        };

        String[] categories = {"GUEST", "DELIVERY", "SERVICE", "CAB", "OTHER"};
        String[] vehiclePrefixes = {"TN-09", "TN-07", "TN-01", "TN-22", "KA-01", "KA-03", "DL-01", "MH-02"};

        List<String> flatNumbers = new ArrayList<>(flatIds.keySet());

        // Generate date relative visits across 90 days
        // Day -90 to Day -61: ~35 visits (Older past)
        // Day -60 to Day -31: ~40 visits (Last month)
        // Day -30 to Day -8:  ~45 visits (Earlier this month)
        // Day -7 to Day -2:   ~25 visits (Last week)
        // Day -1 (Yesterday): ~12 visits (Yesterday)
        // Day 0 (Today):      ~15 visits (Today: completed, inside, overdue)
        // Day +1 to +7:       ~8 visits (Upcoming invitations)

        int visitSeq = 10;

        // 1. Past completed visits (Days -90 to -2)
        for (int dayOffset = -90; dayOffset <= -2; dayOffset++) {
            // Pick a variable number of visits per day (1 to 3 visits)
            int dailyVisits = 1 + rng.nextInt(3);
            if (dayOffset >= -7) dailyVisits = 2 + rng.nextInt(3); // denser in recent week

            for (int v = 0; v < dailyVisits; v++) {
                visitSeq++;
                String ref = String.format("GN-%04d", visitSeq);
                String flatNum = flatNumbers.get(rng.nextInt(flatNumbers.size()));
                Long flatId = flatIds.get(flatNum);
                String blockName = "Block " + flatNum.charAt(0);
                String resEmail = residentEmails.get(rng.nextInt(residentEmails.size()));
                Long hostId = userIds.get(resEmail);

                String vName = visitorNames[rng.nextInt(visitorNames.length)];
                String vCategory = categories[rng.nextInt(categories.length)];
                String purpose = purposes[rng.nextInt(purposes.length)];
                String vehicle = rng.nextBoolean()
                        ? String.format("%s-%c%c-%04d", vehiclePrefixes[rng.nextInt(vehiclePrefixes.length)], (char)('A' + rng.nextInt(26)), (char)('A' + rng.nextInt(26)), 1000 + rng.nextInt(9000))
                        : null;

                int startHour = 7 + rng.nextInt(13); // 07:00 to 20:00
                int startMinute = rng.nextInt(60);
                int durationMinutes = 30 + rng.nextInt(180);

                Instant dayBase = referenceNow.plus(dayOffset, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
                Instant checkIn = dayBase.plus(startHour, ChronoUnit.HOURS).plus(startMinute, ChronoUnit.MINUTES);
                Instant checkOut = checkIn.plus(durationMinutes, ChronoUnit.MINUTES);
                Instant windowStart = checkIn.minus(1, ChronoUnit.HOURS);
                Instant windowEnd = checkIn.plus(3, ChronoUnit.HOURS);

                Long gateIn = (rng.nextBoolean() || "SERVICE".equals(vCategory) || "DELIVERY".equals(vCategory)) ? serviceGateId : mainGateId;
                Long gateOut = rng.nextBoolean() ? mainGateId : serviceGateId;
                Long guardIn = gateIn.equals(mainGateId) ? guardMainId : guardServiceId;
                Long guardOut = gateOut.equals(mainGateId) ? guardMainId : guardServiceId;

                // Handle occasional midnight crossover visits
                if (visitSeq % 25 == 0) {
                    checkIn = dayBase.plus(22, ChronoUnit.HOURS).plus(45, ChronoUnit.MINUTES);
                    checkOut = checkIn.plus(2, ChronoUnit.HOURS).plus(40, ChronoUnit.MINUTES); // crosses into next day 01:25
                }

                // Status variation: mostly EXITED, occasional REJECTED, CANCELLED, EXPIRED
                String status = "EXITED";
                if (visitSeq % 17 == 0) {
                    status = "REJECTED";
                    checkIn = null;
                    checkOut = null;
                } else if (visitSeq % 23 == 0) {
                    status = "CANCELLED";
                    checkIn = null;
                    checkOut = null;
                } else if (visitSeq % 29 == 0) {
                    status = "EXPIRED";
                    checkIn = null;
                    checkOut = null;
                }

                String token = "TOKEN-" + ref;
                String tokenHash = passCredentialService.hashCredential(token);

                List<EventSpec> events = new ArrayList<>();
                events.add(new EventSpec("CREATED", hostId, null, "Visit logged", windowStart));
                if ("REJECTED".equals(status)) {
                    events.add(new EventSpec("REJECTED", hostId, null, "Host declined the visit request", windowStart.plus(10, ChronoUnit.MINUTES)));
                } else if ("CANCELLED".equals(status)) {
                    events.add(new EventSpec("CANCELLED", hostId, null, "Host cancelled invitation", windowStart.plus(15, ChronoUnit.MINUTES)));
                } else if ("EXPIRED".equals(status)) {
                    events.add(new EventSpec("APPROVED", hostId, null, "Approved by host", windowStart));
                    events.add(new EventSpec("EXPIRED", null, null, "Arrival window expired without entry", windowEnd));
                } else {
                    events.add(new EventSpec("APPROVED", hostId, null, "Approved by host", windowStart));
                    events.add(new EventSpec("CHECKED_IN", guardIn, gateIn, "Entry validated at gate", checkIn));
                    events.add(new EventSpec("CHECKED_OUT", guardOut, gateOut, "Exit recorded at gate", checkOut));
                }

                insertCustomVisit(
                        ref,
                        hostId,
                        flatId,
                        blockName,
                        flatNum,
                        "Resident Snapshot",
                        "+91 98765 00000",
                        vName,
                        String.format("+91 98401 %05d", 20000 + visitSeq),
                        vCategory,
                        purpose,
                        vehicle,
                        "Historical presentation record",
                        windowStart,
                        windowEnd,
                        durationMinutes,
                        status,
                        tokenHash,
                        windowStart,
                        gateIn,
                        ("EXITED".equals(status) ? gateOut : null),
                        checkIn,
                        checkOut,
                        hostId,
                        hostId,
                        windowStart,
                        ("REJECTED".equals(status) ? "Resident unavailable at scheduled time" : null),
                        windowStart,
                        events
                );
                totalVisits++;
            }
        }

        // 2. Yesterday Visits (Day -1): 8 completed visits + 1 midnight crossover
        for (int i = 1; i <= 8; i++) {
            visitSeq++;
            String ref = String.format("GN-YEST-%02d", i);
            String flatNum = flatNumbers.get(rng.nextInt(flatNumbers.size()));
            Long flatId = flatIds.get(flatNum);
            String blockName = "Block " + flatNum.charAt(0);
            Long hostId = userIds.get(residentEmails.get(rng.nextInt(residentEmails.size())));

            Instant dayBase = referenceNow.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
            Instant checkIn = dayBase.plus(8 + (i * 1), ChronoUnit.HOURS).plus(rng.nextInt(40), ChronoUnit.MINUTES);
            Instant checkOut = checkIn.plus(45 + rng.nextInt(90), ChronoUnit.MINUTES);

            Long gateIn = i % 2 == 0 ? mainGateId : serviceGateId;
            Long gateOut = mainGateId;
            Long guardIn = gateIn.equals(mainGateId) ? guardMainId : guardServiceId;
            Long guardOut = guardMainId;

            String vCat = categories[i % categories.length];
            String vName = visitorNames[i % visitorNames.length];

            List<EventSpec> events = List.of(
                    new EventSpec("CREATED", hostId, null, "Visit logged", checkIn.minus(2, ChronoUnit.HOURS)),
                    new EventSpec("APPROVED", hostId, null, "Approved by host", checkIn.minus(2, ChronoUnit.HOURS)),
                    new EventSpec("CHECKED_IN", guardIn, gateIn, "Entry validated", checkIn),
                    new EventSpec("CHECKED_OUT", guardOut, gateOut, "Exit recorded", checkOut)
            );

            insertCustomVisit(
                    ref, hostId, flatId, blockName, flatNum, "Resident Snapshot", "+91 98765 00000",
                    vName, String.format("+91 98402 %05d", 30000 + i), vCat,
                    purposes[i % purposes.length], "TN-07-YEST-" + i, "Yesterday visitor record",
                    checkIn.minus(2, ChronoUnit.HOURS), checkIn.plus(4, ChronoUnit.HOURS), 120,
                    "EXITED", passCredentialService.hashCredential("PASS-" + ref),
                    checkIn.minus(2, ChronoUnit.HOURS), gateIn, gateOut, checkIn, checkOut,
                    hostId, hostId, checkIn.minus(2, ChronoUnit.HOURS), null,
                    checkIn.minus(2, ChronoUnit.HOURS), events
            );
            totalVisits++;
        }

        // 3. Today's Completed Visits (Day 0): 6 completed visits earlier today
        for (int i = 1; i <= 6; i++) {
            visitSeq++;
            String ref = String.format("GN-TODAY-EXIT-%02d", i);
            String flatNum = flatNumbers.get(rng.nextInt(flatNumbers.size()));
            Long flatId = flatIds.get(flatNum);
            String blockName = "Block " + flatNum.charAt(0);
            Long hostId = userIds.get(residentEmails.get(rng.nextInt(residentEmails.size())));

            Instant checkIn = referenceNow.minus(6 - (i * 45), ChronoUnit.MINUTES);
            Instant checkOut = checkIn.plus(25 + rng.nextInt(35), ChronoUnit.MINUTES);

            Long gateIn = i % 2 == 0 ? mainGateId : serviceGateId;
            Long gateOut = gateIn;
            Long guardIn = gateIn.equals(mainGateId) ? guardMainId : guardServiceId;

            String vCat = categories[i % categories.length];
            String vName = visitorNames[(10 + i) % visitorNames.length];

            List<EventSpec> events = List.of(
                    new EventSpec("CREATED", hostId, null, "Created today", checkIn.minus(30, ChronoUnit.MINUTES)),
                    new EventSpec("APPROVED", hostId, null, "Approved", checkIn.minus(30, ChronoUnit.MINUTES)),
                    new EventSpec("CHECKED_IN", guardIn, gateIn, "Entry validated", checkIn),
                    new EventSpec("CHECKED_OUT", guardIn, gateOut, "Exit recorded", checkOut)
            );

            insertCustomVisit(
                    ref, hostId, flatId, blockName, flatNum, "Resident Snapshot", "+91 98765 00000",
                    vName, String.format("+91 98403 %05d", 40000 + i), vCat,
                    purposes[i % purposes.length], "TN-09-TDY-" + i, "Today completed visit",
                    checkIn.minus(1, ChronoUnit.HOURS), checkIn.plus(3, ChronoUnit.HOURS), 60,
                    "EXITED", passCredentialService.hashCredential("PASS-" + ref),
                    checkIn.minus(1, ChronoUnit.HOURS), gateIn, gateOut, checkIn, checkOut,
                    hostId, hostId, checkIn.minus(1, ChronoUnit.HOURS), null,
                    checkIn.minus(1, ChronoUnit.HOURS), events
            );
            totalVisits++;
        }

        // 4. Visitors Currently Inside Across the Community (7 visitors inside + 2 overdue)
        String[][] insideVisitors = {
                {"B-201", "Sunita Agarwal", "GUEST", "Weekend Family Lunch", "TN-01-SA-1100", "90", "45", "Main Gate"},
                {"B-301", "Urban Company AC Tech", "SERVICE", "AC Deep Cleaning & Gas Refill", "TN-09-UC-2200", "120", "80", "Service Gate"},
                {"C-101", "Amazon Logistics delivery team", "DELIVERY", "Bulk Home Delivery", "KA-01-AZ-3300", "45", "20", "Service Gate"},
                {"C-201", "Dr. K. S. Murthy", "GUEST", "Medical Consultation & Home Visit", "TN-07-DR-4400", "90", "30", "Main Gate"},
                {"A-302", "Pest Control Specialist Team", "SERVICE", "Quarterly Organic Pest Control", "TN-22-PC-5500", "120", "110", "Service Gate"},
                // Overdue visitors (Entered long ago, expected duration exceeded)
                {"B-103", "Aman Verma (Overstay Contractor)", "SERVICE", "Floor Tile Masonry Work", "TN-09-OV-9901", "120", "340", "Service Gate"}, // 340 mins inside vs 120 expected!
                {"C-301", "Balaji Krishnan (Overstay Guest)", "GUEST", "Social Visit & Game Afternoon", "TN-07-OV-9902", "120", "310", "Main Gate"}  // 310 mins inside vs 120 expected!
        };

        for (int i = 0; i < insideVisitors.length; i++) {
            visitSeq++;
            String[] iv = insideVisitors[i];
            String flatNum = iv[0];
            Long flatId = flatIds.get(flatNum);
            String blockName = "Block " + flatNum.charAt(0);
            Long hostId = userIds.get(residentEmails.get(i % residentEmails.size()));

            int expDuration = Integer.parseInt(iv[5]);
            int minsAgo = Integer.parseInt(iv[6]);
            Instant checkIn = referenceNow.minus(minsAgo, ChronoUnit.MINUTES);

            Long gateId = "Main Gate".equals(iv[7]) ? mainGateId : serviceGateId;
            Long guardId = gateId.equals(mainGateId) ? guardMainId : guardServiceId;

            String ref = String.format("GN-INSIDE-%02d", i + 2);
            List<EventSpec> events = List.of(
                    new EventSpec("CREATED", hostId, null, "Logged", checkIn.minus(1, ChronoUnit.HOURS)),
                    new EventSpec("APPROVED", hostId, null, "Approved", checkIn.minus(1, ChronoUnit.HOURS)),
                    new EventSpec("CHECKED_IN", guardId, gateId, "Checked in at gate", checkIn)
            );

            insertCustomVisit(
                    ref, hostId, flatId, blockName, flatNum, "Resident Snapshot", "+91 98765 00000",
                    iv[1], String.format("+91 98404 %05d", 50000 + i), iv[2], iv[3], iv[4],
                    (minsAgo > expDuration ? "OVERSTAY VISITOR: Duration exceeded" : "Currently on premises"),
                    checkIn.minus(1, ChronoUnit.HOURS), checkIn.plus(6, ChronoUnit.HOURS), expDuration,
                    "INSIDE", passCredentialService.hashCredential("PASS-" + ref),
                    checkIn.minus(1, ChronoUnit.HOURS), gateId, null, checkIn, null,
                    hostId, hostId, checkIn.minus(1, ChronoUnit.HOURS), null,
                    checkIn.minus(1, ChronoUnit.HOURS), events
            );
            totalVisits++;
        }

        // 5. Additional Walk-in Approvals in Other Blocks (4 pending walk-ins)
        String[][] otherWalkins = {
                {"B-202", "Blinkit Grocery Delivery Partner", "DELIVERY", "Instant Grocery Delivery", "TN-01-BK-8812", "Main Gate"},
                {"C-102", "Kent RO Water Purifier Technician", "SERVICE", "Annual Water Filter Replacement", "TN-09-RO-1928", "Service Gate"},
                {"A-203", "Harish Natarajan", "GUEST", "Family Birthday Visit", "KA-03-HN-7711", "Main Gate"},
                {"B-101", "FedEx Courier Dispatch", "DELIVERY", "International Document Envelope", "TN-07-FX-3399", "Main Gate"}
        };
        for (int i = 0; i < otherWalkins.length; i++) {
            visitSeq++;
            String[] ow = otherWalkins[i];
            String flatNum = ow[0];
            Long flatId = flatIds.get(flatNum);
            String blockName = "Block " + flatNum.charAt(0);
            Long hostId = userIds.get(residentEmails.get(i % residentEmails.size()));

            Long gateId = "Main Gate".equals(ow[5]) ? mainGateId : serviceGateId;
            Long guardId = gateId.equals(mainGateId) ? guardMainId : guardServiceId;

            String ref = String.format("GN-WALKIN-%02d", i + 3);
            Instant regTime = referenceNow.minus(8 + (i * 5), ChronoUnit.MINUTES);

            List<EventSpec> events = List.of(
                    new EventSpec("CREATED", guardId, gateId, "Walk-in logged at gate", regTime)
            );

            insertCustomVisit(
                    ref, hostId, flatId, blockName, flatNum, "Resident Snapshot", "+91 98765 00000",
                    ow[1], String.format("+91 98405 %05d", 60000 + i), ow[2], ow[3], ow[4],
                    "Waiting for resident approval", regTime, regTime.plus(1, ChronoUnit.HOURS), 60,
                    "PENDING_APPROVAL", null, null, null, null, null, null,
                    guardId, null, null, null, regTime, events
            );
            totalVisits++;
        }

        // 6. Upcoming Approved Invitations for Next 7 Days (~10 visits)
        for (int day = 1; day <= 7; day++) {
            visitSeq++;
            String ref = String.format("GN-UPCOMING-D%d-%02d", day, visitSeq % 10);
            String flatNum = flatNumbers.get(rng.nextInt(flatNumbers.size()));
            Long flatId = flatIds.get(flatNum);
            String blockName = "Block " + flatNum.charAt(0);
            Long hostId = userIds.get(residentEmails.get(rng.nextInt(residentEmails.size())));

            Instant winStart = referenceNow.plus(day, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS).plus(10, ChronoUnit.HOURS);
            Instant winEnd = winStart.plus(6, ChronoUnit.HOURS);

            String vCat = categories[day % categories.length];
            String vName = visitorNames[(20 + day) % visitorNames.length];
            String token = "TOKEN-" + ref;

            List<EventSpec> events = List.of(
                    new EventSpec("CREATED", hostId, null, "Scheduled invitation created", referenceNow.minus(1, ChronoUnit.DAYS)),
                    new EventSpec("APPROVED", hostId, null, "Pre-approved by host", referenceNow.minus(1, ChronoUnit.DAYS))
            );

            insertCustomVisit(
                    ref, hostId, flatId, blockName, flatNum, "Resident Snapshot", "+91 98765 00000",
                    vName, String.format("+91 98406 %05d", 70000 + day), vCat,
                    purposes[day % purposes.length], "TN-09-UP-" + day, "Upcoming scheduled invitation",
                    winStart, winEnd, 120, "APPROVED",
                    passCredentialService.hashCredential(token), referenceNow.minus(1, ChronoUnit.DAYS),
                    null, null, null, null,
                    hostId, hostId, referenceNow.minus(1, ChronoUnit.DAYS), null,
                    referenceNow.minus(1, ChronoUnit.DAYS), events
            );
            totalVisits++;
        }

        return totalVisits;
    }

    private void insertCustomVisit(
            String visitRef,
            Long hostUserId,
            Long flatId,
            String blockName,
            String flatNumber,
            String hostName,
            String hostMobile,
            String visitorName,
            String visitorMobile,
            String visitorCategory,
            String purpose,
            String vehicleNumber,
            String notes,
            Instant winStart,
            Instant winEnd,
            int durationMinutes,
            String status,
            String passHash,
            Instant passGeneratedAt,
            Long entryGateId,
            Long exitGateId,
            Instant checkInTime,
            Instant checkOutTime,
            Long registeredByUserId,
            Long approvedByUserId,
            Instant approvedAt,
            String rejectionReason,
            Instant createdAt,
            List<EventSpec> events
    ) {
        String sql = """
            INSERT INTO visits (
                visit_reference, host_user_id, destination_flat_id,
                destination_block_name, destination_flat_number, host_name_snapshot, host_mobile_snapshot,
                visitor_name, visitor_mobile, visitor_category, purpose, vehicle_number, notes,
                arrival_window_start, arrival_window_end, expected_duration_minutes, status,
                pass_credential_hash, pass_generated_at,
                entry_gate_id, exit_gate_id, check_in_time, check_out_time,
                registered_by_user_id, approved_by_user_id, approved_at, rejection_reason,
                created_at, updated_at
            ) VALUES (
                ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?,
                ?, ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?
            )
        """;

        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, visitRef);
            ps.setLong(2, hostUserId);
            ps.setLong(3, flatId);
            ps.setString(4, blockName);
            ps.setString(5, flatNumber);
            ps.setString(6, hostName);
            ps.setString(7, hostMobile);
            ps.setString(8, visitorName);
            ps.setString(9, visitorMobile);
            ps.setString(10, visitorCategory);
            ps.setString(11, purpose);
            ps.setString(12, vehicleNumber);
            ps.setString(13, notes);
            ps.setTimestamp(14, Timestamp.from(winStart));
            ps.setTimestamp(15, Timestamp.from(winEnd));
            ps.setInt(16, durationMinutes);
            ps.setString(17, status);
            ps.setString(18, passHash);
            ps.setTimestamp(19, passGeneratedAt != null ? Timestamp.from(passGeneratedAt) : null);
            if (entryGateId != null) ps.setLong(20, entryGateId); else ps.setNull(20, Types.BIGINT);
            if (exitGateId != null) ps.setLong(21, exitGateId); else ps.setNull(21, Types.BIGINT);
            ps.setTimestamp(22, checkInTime != null ? Timestamp.from(checkInTime) : null);
            ps.setTimestamp(23, checkOutTime != null ? Timestamp.from(checkOutTime) : null);
            ps.setLong(24, registeredByUserId);
            if (approvedByUserId != null) ps.setLong(25, approvedByUserId); else ps.setNull(25, Types.BIGINT);
            ps.setTimestamp(26, approvedAt != null ? Timestamp.from(approvedAt) : null);
            ps.setString(27, rejectionReason);
            ps.setTimestamp(28, Timestamp.from(createdAt));
            ps.setTimestamp(29, Timestamp.from(createdAt));
            return ps;
        }, kh);

        Long visitId = KeyHolderUtils.extractId(kh);

        if (events != null) {
            String eventSql = "INSERT INTO visit_events (visit_id, event_type, actor_user_id, gate_id, notes, created_at) VALUES (?, ?, ?, ?, ?, ?)";
            for (EventSpec e : events) {
                jdbcTemplate.update(eventSql, visitId, e.eventType, e.actorUserId, e.gateId, e.notes, Timestamp.from(e.createdAt));
            }
        }
    }

    private static class EventSpec {
        String eventType;
        Long actorUserId;
        Long gateId;
        String notes;
        Instant createdAt;

        public EventSpec(String eventType, Long actorUserId, Long gateId, String notes, Instant createdAt) {
            this.eventType = eventType;
            this.actorUserId = actorUserId;
            this.gateId = gateId;
            this.notes = notes;
            this.createdAt = createdAt;
        }
    }

    private int seedParcels(Map<String, Long> userIds, Map<String, Long> flatIds, Instant referenceNow, Random rng) {
        Long guardId = userIds.get("guard.main@gatenest.example");
        Long presenterId = userIds.get("resident@gatenest.example");
        Long presenterFlatId = flatIds.get("A-101");

        String[][] parcelData = {
                // Flat A-101 (Presenter)
                {"A-101", "resident@gatenest.example", "Amazon Logistics", "AMZ-IN-98741029", "Ergonomic Desk Monitor Arm", "Gate Security Parcel Room - Shelf A1", "READY_FOR_COLLECTION"},
                {"A-101", "resident@gatenest.example", "BlueDart Express", "BD-77192834", "Medical Research Journals & Stationery", "Gate Security Parcel Room - Shelf A1", "COLLECTED"},
                // Other Flats
                {"A-102", "priya.sharma@gatenest.example", "Flipkart Quick", "FK-55192837", "Kitchen Espresso Maker", "Gate Security Room", "READY_FOR_COLLECTION"},
                {"A-201", "karthik.ram@gatenest.example", "DHL Express", "DHL-99182736", "Passport & Visa Return Document", "Gate Security Room - Secure Locker 4", "READY_FOR_COLLECTION"},
                {"B-102", "pooja.hegde@gatenest.example", "Zomato Blinkit", "BK-11029384", "Organic Grocery Hamper", "Gate Security Room", "COLLECTED"},
                {"B-201", "divya.krishnan@gatenest.example", "DTDC Express", "DTDC-44192830", "Handloom Cotton Sarees", "Gate Security Room", "COLLECTED"},
                {"B-301", "harish.chandran@gatenest.example", "Delhivery Logistics", "DEL-88192847", "Wireless Noise-Canceling Headphones", "Gate Security Room", "READY_FOR_COLLECTION"},
                {"C-101", "gautam.sethi@gatenest.example", "Amazon Logistics", "AMZ-88129031", "Smart Home Air Purifier Filter", "Gate Security Room", "RECEIVED"},
                {"C-201", "madhavan.s@gatenest.example", "Speed Post India", "SP-22019384", "Bank Security Cheque Book", "Gate Security Room", "RECEIVED"},
                {"C-301", "vivek.singhania@gatenest.example", "BlueDart Express", "BD-33918274", "High-End Mechanical Keyboard", "Gate Security Room", "COLLECTED"},
                {"A-203", "neha.kapoor@gatenest.example", "Myntra Logistics", "MYN-99182740", "Apparel & Footwear Package", "Gate Security Room", "RETURNED"},
                {"B-103", "manish.gupta@gatenest.example", "FedEx Express", "FDX-77102938", "Computer Spare Parts", "Gate Security Room", "RECEIVED"}
        };

        String parcelSql = """
            INSERT INTO parcels (
                recipient_user_id, destination_flat_id, courier_name, tracking_number, description,
                storage_location, received_by_user_id, received_at, status,
                collected_by_name, collected_by_user_id, verified_by_user_id, collected_at, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        String eventSql = "INSERT INTO parcel_events (parcel_id, event_type, actor_user_id, notes, created_at) VALUES (?, ?, ?, ?, ?)";

        for (int i = 0; i < parcelData.length; i++) {
            String[] p = parcelData[i];
            Long flatId = flatIds.get(p[0]);
            Long recUser = userIds.getOrDefault(p[1], presenterId);
            String status = p[6];

            Instant recAt = referenceNow.minus(1 + i, ChronoUnit.HOURS);
            Instant collAt = "COLLECTED".equals(status) ? recAt.plus(45, ChronoUnit.MINUTES) : null;
            String collName = "COLLECTED".equals(status) ? "Resident Self" : null;

            KeyHolder kh = new GeneratedKeyHolder();
            final int fIdx = i;
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(parcelSql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, recUser);
                ps.setLong(2, flatId);
                ps.setString(3, p[2]);
                ps.setString(4, p[3]);
                ps.setString(5, p[4]);
                ps.setString(6, p[5]);
                ps.setLong(7, guardId);
                ps.setTimestamp(8, Timestamp.from(recAt));
                ps.setString(9, status);
                ps.setString(10, collName);
                if (collAt != null) ps.setLong(11, recUser); else ps.setNull(11, Types.BIGINT);
                if (collAt != null) ps.setLong(12, guardId); else ps.setNull(12, Types.BIGINT);
                ps.setTimestamp(13, collAt != null ? Timestamp.from(collAt) : null);
                ps.setTimestamp(14, Timestamp.from(recAt));
                ps.setTimestamp(15, Timestamp.from(recAt));
                return ps;
            }, kh);

            Long parcelId = KeyHolderUtils.extractId(kh);
            jdbcTemplate.update(eventSql, parcelId, "RECEIVED", guardId, "Parcel received at gate", Timestamp.from(recAt));
            if ("READY_FOR_COLLECTION".equals(status)) {
                jdbcTemplate.update(eventSql, parcelId, "READY_FOR_COLLECTION", guardId, "Placed in parcel holding storage", Timestamp.from(recAt.plus(5, ChronoUnit.MINUTES)));
            } else if ("COLLECTED".equals(status)) {
                jdbcTemplate.update(eventSql, parcelId, "COLLECTED", guardId, "Collected and verified by recipient", Timestamp.from(collAt));
            } else if ("RETURNED".equals(status)) {
                jdbcTemplate.update(eventSql, parcelId, "RETURNED", guardId, "Returned to courier (delivery cancelled by buyer)", Timestamp.from(recAt.plus(2, ChronoUnit.HOURS)));
            }
        }
        return parcelData.length;
    }

    private int seedComplaints(Map<String, Long> userIds, Map<String, Long> flatIds, Instant referenceNow, Random rng) {
        Long adminId = userIds.get("admin@gatenest.example");
        Long presenterId = userIds.get("resident@gatenest.example");

        String[][] compData = {
                {"A-101", "PLUMBING", "Balcony drain slow drainage during rain", "Water accumulates slightly during heavy rain near outer grate.", "MEDIUM", "IN_PROGRESS"},
                {"A-201", "ELECTRICAL", "Corridor motion light sensor delay on 2nd floor", "The LED corridor light turns on 4-5 seconds late after exiting lift.", "LOW", "OPEN"},
                {"B-102", "LIFT", "Lift 2 slight jerk on stopping at Floor 1", "Elevator car experiences a perceptible bump when leveling with floor.", "HIGH", "RESOLVED"},
                {"B-201", "COMMON_AREA", "Clubhouse treadmill display error code E03", "Gym treadmill 2 stops after 10 mins with motor communication error.", "MEDIUM", "ASSIGNED"},
                {"C-101", "SECURITY", "Intercom audio hum from Service Gate", "Background static audible when guard speaks from Service Gate intercom.", "LOW", "RESOLVED"},
                {"C-201", "PLUMBING", "Basement overhead pipe minor seepage near Slot P-B1-04", "Small water droplet drip spotted near rainwater harvesting pipe.", "HIGH", "RESOLVED"},
                {"A-302", "NOISE", "Balcony AC outdoor unit vibration on 4th floor", "Rattling noise from neighboring outdoor unit during night hours.", "MEDIUM", "CLOSED"},
                {"B-301", "COMMON_AREA", "Children's playground swing chain maintenance", "Right swing chain links showing superficial surface rust.", "LOW", "OPEN"}
        };

        String compSql = """
            INSERT INTO complaints (
                resident_user_id, flat_id, category, subject, description, priority, status,
                assigned_to_user_id, resolved_at, closed_at, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        String commentSql = """
            INSERT INTO complaint_comments (
                complaint_id, author_user_id, author_name_snapshot, author_role, comment_text, is_internal_note, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        for (int i = 0; i < compData.length; i++) {
            String[] c = compData[i];
            Long flatId = flatIds.get(c[0]);
            String category = c[1];
            String subject = c[2];
            String desc = c[3];
            String priority = c[4];
            String status = c[5];

            Instant createdAt = referenceNow.minus(10 - i, ChronoUnit.DAYS);
            Instant resolvedAt = "RESOLVED".equals(status) || "CLOSED".equals(status) ? createdAt.plus(2, ChronoUnit.DAYS) : null;
            Instant closedAt = "CLOSED".equals(status) ? createdAt.plus(3, ChronoUnit.DAYS) : null;

            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(compSql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, presenterId);
                ps.setLong(2, flatId);
                ps.setString(3, category);
                ps.setString(4, subject);
                ps.setString(5, desc);
                ps.setString(6, priority);
                ps.setString(7, status);
                ps.setLong(8, adminId);
                ps.setTimestamp(9, resolvedAt != null ? Timestamp.from(resolvedAt) : null);
                ps.setTimestamp(10, closedAt != null ? Timestamp.from(closedAt) : null);
                ps.setTimestamp(11, Timestamp.from(createdAt));
                ps.setTimestamp(12, Timestamp.from(createdAt));
                return ps;
            }, kh);

            Long compId = KeyHolderUtils.extractId(kh);
            jdbcTemplate.update(commentSql, compId, presenterId, "Dr. Ananya Roy", "RESIDENT", "Complaint raised with detailed photos.", false, Timestamp.from(createdAt));
            jdbcTemplate.update(commentSql, compId, adminId, "Elena Vance", "ADMINISTRATOR", "Assigned to facility maintenance vendor for prompt inspection.", false, Timestamp.from(createdAt.plus(3, ChronoUnit.HOURS)));

            if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
                jdbcTemplate.update(commentSql, compId, adminId, "Elena Vance", "ADMINISTRATOR", "Vendor attended and certified fix. Tested successfully.", false, Timestamp.from(resolvedAt));
            }
        }

        return compData.length;
    }

    private int seedAnnouncements(Long adminUserId, Map<String, Long> blockIds) {
        String sql = """
            INSERT INTO announcements (
                author_user_id, title, content, announcement_type, priority, audience_type,
                target_block_id, is_pinned, is_published, published_at, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        Instant now = Instant.now();

        // 1. AGM Meeting
        jdbcTemplate.update(sql,
                adminUserId,
                "Bluehaven Residency Annual General Meeting (AGM) 2026",
                "The Annual General Meeting will convene this Sunday at 10:30 AM in the Grand Clubhouse Hall. Key agenda items: Solar rooftop review, EV charging expansion, and security system upgrade report.",
                "EVENT", "IMPORTANT", "COMMUNITY", null, true, true, Timestamp.from(now.minus(2, ChronoUnit.DAYS)), Timestamp.from(now.minus(2, ChronoUnit.DAYS)), Timestamp.from(now.minus(2, ChronoUnit.DAYS))
        );

        // 2. Elevator Maintenance
        jdbcTemplate.update(sql,
                adminUserId,
                "Scheduled Elevator Preventive Maintenance - Block A & B",
                "Otis Elevator engineers will carry out quarterly preventive servicing on Thursday between 2:00 PM and 4:30 PM. One elevator per block will remain operational throughout.",
                "MAINTENANCE", "NORMAL", "COMMUNITY", null, false, true, Timestamp.from(now.minus(1, ChronoUnit.DAYS)), Timestamp.from(now.minus(1, ChronoUnit.DAYS)), Timestamp.from(now.minus(1, ChronoUnit.DAYS))
        );

        // 3. Welcome Presentation Notice
        jdbcTemplate.update(sql,
                adminUserId,
                "Welcome to Bluehaven Residency — Live System Presentation",
                "Welcome to the official presentation demo environment of GateNest Apartment Visitor Management System. All resident profiles, gates, and visitor workflows reflect real production logic with date-relative sample data.",
                "GENERAL", "NORMAL", "COMMUNITY", null, false, true, Timestamp.from(now.minus(3, ChronoUnit.HOURS)), Timestamp.from(now.minus(3, ChronoUnit.HOURS)), Timestamp.from(now.minus(3, ChronoUnit.HOURS))
        );

        return 3;
    }

    private int seedDomesticStaff(
            Map<String, Long> userIds,
            Map<String, Long> flatIds,
            Map<String, Long> gateIds,
            Instant referenceNow,
            Random rng
    ) {
        Long guardId = userIds.get("guard.main@gatenest.example");
        Long mainGateId = gateIds.get("Main Gate");
        Long presenterId = userIds.get("resident@gatenest.example");
        Long presenterFlatId = flatIds.get("A-101");

        String[][] staffData = {
                {"Meenakshi Sundaram", "+91 98408 10001", "MAID", "AADHAAR", "XXXX-XXXX-9012", "Mon-Sat (07:30 - 11:30)"},
                {"Ramesh Kumar", "+91 98408 10002", "COOK", "AADHAAR", "XXXX-XXXX-9013", "Daily (06:30 - 09:30, 18:00 - 20:30)"},
                {"Suresh Babu", "+91 98408 10003", "DRIVER", "DRIVING_LICENSE", "DL-TN-09-2018-0912", "Mon-Sat (08:30 - 19:30)"},
                {"Lakshmi Priya", "+91 98408 10004", "CLEANER", "AADHAAR", "XXXX-XXXX-9014", "Mon-Fri (09:00 - 13:00)"},
                {"Murugan P", "+91 98408 10005", "GARDENER", "VOTER_ID", "TN-CH-8829102", "Tue, Thu, Sat (08:00 - 12:00)"}
        };

        String staffSql = "INSERT INTO staff_members (full_name, mobile, staff_type, id_proof_type, id_proof_number, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        String authSql = "INSERT INTO staff_authorizations (staff_member_id, resident_user_id, flat_id, schedule_description, is_active) VALUES (?, ?, ?, ?, ?)";
        String attSql = """
            INSERT INTO staff_attendance_sessions (
                staff_member_id, check_in_time, check_out_time, entry_gate_id, exit_gate_id, entry_guard_user_id, exit_guard_user_id, is_active
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        for (int i = 0; i < staffData.length; i++) {
            String[] s = staffData[i];
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(staffSql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, s[0]);
                ps.setString(2, s[1]);
                ps.setString(3, s[2]);
                ps.setString(4, s[3]);
                ps.setString(5, s[4]);
                ps.setBoolean(6, true);
                return ps;
            }, kh);

            Long staffId = KeyHolderUtils.extractId(kh);

            // Link to Flat A-101 and one other flat
            jdbcTemplate.update(authSql, staffId, presenterId, presenterFlatId, s[5], true);
            if (flatIds.containsKey("B-102")) {
                jdbcTemplate.update(authSql, staffId, userIds.getOrDefault("pooja.hegde@gatenest.example", presenterId), flatIds.get("B-102"), s[5], true);
            }

            // Record attendance session: Staff 1 (Cook) currently inside today, others checked out
            if (i == 1) { // Ramesh Kumar (Cook) currently on premises
                Instant inTime = referenceNow.minus(50, ChronoUnit.MINUTES);
                jdbcTemplate.update(attSql, staffId, Timestamp.from(inTime), null, mainGateId, null, guardId, null, true);
            } else {
                Instant inTime = referenceNow.minus(4, ChronoUnit.HOURS);
                Instant outTime = referenceNow.minus(1, ChronoUnit.HOURS);
                jdbcTemplate.update(attSql, staffId, Timestamp.from(inTime), Timestamp.from(outTime), mainGateId, mainGateId, guardId, guardId, false);
            }
        }

        return staffData.length;
    }

    private int seedParkingAndVehicles(
            Map<String, Long> userIds,
            Map<String, Long> flatIds,
            Map<String, Long> blockIds,
            Instant referenceNow,
            Random rng
    ) {
        Long adminId = userIds.get("admin@gatenest.example");
        Long presenterId = userIds.get("resident@gatenest.example");
        Long flatA101 = flatIds.get("A-101");
        Long blockAId = blockIds.get("Block A");

        // 1. Parking slots
        String slotSql = """
            INSERT INTO parking_slots (slot_number, slot_type, block_id, floor_level, is_reserved_for_residents, is_active)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        String[][] slots = {
                {"P-B1-01", "FOUR_WHEELER", "Basement 1", "true"},
                {"P-B1-02", "FOUR_WHEELER", "Basement 1", "true"},
                {"P-B1-03", "EV_CHARGING", "Basement 1", "true"},
                {"P-B1-04", "FOUR_WHEELER", "Basement 1", "true"},
                {"P-B1-05", "VISITOR", "Basement 1", "false"},
                {"P-B1-06", "VISITOR", "Basement 1", "false"},
                {"P-B2-01", "FOUR_WHEELER", "Basement 2", "true"},
                {"P-B2-02", "TWO_WHEELER", "Basement 2", "true"},
                {"P-B2-03", "TWO_WHEELER", "Basement 2", "true"},
                {"P-B2-04", "VISITOR", "Basement 2", "false"},
                {"P-B2-05", "VISITOR", "Basement 2", "false"},
                {"P-B2-06", "EV_CHARGING", "Basement 2", "false"}
        };

        Map<String, Long> slotIds = new HashMap<>();
        for (String[] s : slots) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(slotSql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, s[0]);
                ps.setString(2, s[1]);
                ps.setLong(3, blockAId);
                ps.setString(4, s[2]);
                ps.setBoolean(5, Boolean.parseBoolean(s[3]));
                ps.setBoolean(6, true);
                return ps;
            }, kh);
            slotIds.put(s[0], KeyHolderUtils.extractId(kh));
        }

        // 2. Resident vehicles
        String vehSql = """
            INSERT INTO vehicles (owner_user_id, flat_id, vehicle_number, vehicle_type, make_model, color, sticker_number, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        KeyHolder vkh1 = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(vehSql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, presenterId);
            ps.setLong(2, flatA101);
            ps.setString(3, "TN-09-AR-2026");
            ps.setString(4, "FOUR_WHEELER");
            ps.setString(5, "Hyundai Ioniq 5 (EV)");
            ps.setString(6, "Midnight Pearl Blue");
            ps.setString(7, "BH-RES-0101");
            ps.setBoolean(8, true);
            return ps;
        }, vkh1);
        Long veh1Id = KeyHolderUtils.extractId(vkh1);

        // 3. Parking assignments
        String assignSql = """
            INSERT INTO parking_assignments (parking_slot_id, vehicle_id, resident_user_id, assigned_by_user_id, assigned_at, is_active, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(assignSql, slotIds.get("P-B1-03"), veh1Id, presenterId, adminId, Timestamp.from(referenceNow.minus(30, ChronoUnit.DAYS)), true, "Assigned dedicated EV resident slot for Flat A-101");

        return slots.length;
    }

    private void seedSavedContactsAndRules(
            Map<String, Long> userIds,
            Map<String, Long> flatIds,
            Map<String, Long> gateIds,
            Instant referenceNow
    ) {
        Long presenterId = userIds.get("resident@gatenest.example");
        Long flatA101 = flatIds.get("A-101");

        // 1. Saved Contacts
        String contactSql = """
            INSERT INTO saved_visitor_contacts (resident_user_id, visitor_name, visitor_mobile, visitor_category, default_purpose, default_vehicle_number, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(contactSql, presenterId, "Rohan Mehta", "+91 98401 11005", "GUEST", "Family Weekend Visit", "TN-09-RM-7721", "Cousin - Frequent weekend guest");
        jdbcTemplate.update(contactSql, presenterId, "Daily Fresh Organic Milk", "+91 98401 99001", "DELIVERY", "Morning Fresh Farm Milk Delivery", null, "Daily morning milk supply");
        jdbcTemplate.update(contactSql, presenterId, "Dr. S. K. Narayanan", "+91 98401 99002", "GUEST", "Medical Advisory & Research Consultation", "TN-07-SK-4491", "University Colleague");

        // 2. Recurring Visit Rule
        String recRuleSql = """
            INSERT INTO recurring_visit_rules (
                resident_user_id, destination_flat_id, visitor_name, visitor_mobile, visitor_category,
                purpose, allowed_weekdays, valid_from_date, valid_to_date, allowed_start_time, allowed_end_time,
                expected_duration_minutes, is_active, notes
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        LocalDate today = LocalDate.now();
        jdbcTemplate.update(recRuleSql,
                presenterId,
                flatA101,
                "Daily Morning Milk & Newspaper Delivery",
                "+91 98401 99001",
                "DELIVERY",
                "Daily Dairy & Newspaper Drop",
                "MON,TUE,WED,THU,FRI,SAT,SUN",
                java.sql.Date.valueOf(today.minusMonths(1)),
                java.sql.Date.valueOf(today.plusMonths(6)),
                "06:00",
                "08:30",
                30,
                true,
                "Authorized daily morning service entry"
        );

        // 3. Group Invitation for Upcoming Weekend
        String grpSql = """
            INSERT INTO group_invitations (
                host_user_id, destination_flat_id, event_title, group_reference, arrival_window_start, arrival_window_end, max_guests, notes, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        Instant partyStart = referenceNow.plus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS).plus(16, ChronoUnit.HOURS);
        Instant partyEnd = partyStart.plus(6, ChronoUnit.HOURS);

        KeyHolder gkh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(grpSql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, presenterId);
            ps.setLong(2, flatA101);
            ps.setString(3, "Dr. Ananya's Academic Research Team High-Tea");
            ps.setString(4, "GRP-BLUEHAVEN-2026");
            ps.setTimestamp(5, Timestamp.from(partyStart));
            ps.setTimestamp(6, Timestamp.from(partyEnd));
            ps.setInt(7, 6);
            ps.setString(8, "Group high-tea and academic paper review");
            ps.setString(9, "APPROVED");
            return ps;
        }, gkh);
        Long grpId = KeyHolderUtils.extractId(gkh);

        String memberSql = "INSERT INTO group_invitation_members (group_invitation_id, guest_name, guest_mobile, status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(memberSql, grpId, "Prof. Alistair Finch", "+91 98409 00001", "EXPECTED");
        jdbcTemplate.update(memberSql, grpId, "Dr. Suniti Banerjee", "+91 98409 00002", "EXPECTED");
        jdbcTemplate.update(memberSql, grpId, "Research Associate Tariq", "+91 98409 00003", "EXPECTED");
    }

    private void seedInAppNotifications(Map<String, Long> userIds, Instant referenceNow) {
        Long presenterId = userIds.get("resident@gatenest.example");

        String notifSql = """
            INSERT INTO notifications (user_id, title, message, link_url, notification_type, is_read, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        // Unread notification 1: Walk-in waiting
        jdbcTemplate.update(notifSql,
                presenterId,
                "Visitor Waiting at Main Gate",
                "Deepak Sharma (BlueDart Courier) has arrived for Flat A-101. Please approve or decline.",
                "/resident/approval-inbox",
                "VISITOR_REQUEST",
                false,
                Timestamp.from(referenceNow.minus(15, ChronoUnit.MINUTES))
        );

        // Unread notification 2: Parcel ready
        jdbcTemplate.update(notifSql,
                presenterId,
                "Parcel Ready for Collection",
                "Your parcel from Amazon Logistics (Tracking: AMZ-IN-98741029) is ready at Gate Security Room.",
                "/parcels",
                "VISITOR_CHECKED_IN",
                false,
                Timestamp.from(referenceNow.minus(45, ChronoUnit.MINUTES))
        );

        // Read notification 3: Visitor checked in earlier
        jdbcTemplate.update(notifSql,
                presenterId,
                "Visitor Entered Premises",
                "Rohan Mehta has checked in at Main Gate and is on the way to Flat A-101.",
                "/resident/visitors-inside",
                "VISITOR_CHECKED_IN",
                true,
                Timestamp.from(referenceNow.minus(45, ChronoUnit.MINUTES))
        );

        // Read notification 4: Community announcement
        jdbcTemplate.update(notifSql,
                presenterId,
                "Notice: Annual General Meeting (AGM)",
                "Society Annual General Meeting has been scheduled for this Sunday at 10:30 AM in Clubhouse.",
                "/announcements",
                "RESIDENT_APPROVED",
                true,
                Timestamp.from(referenceNow.minus(2, ChronoUnit.DAYS))
        );
    }
}
