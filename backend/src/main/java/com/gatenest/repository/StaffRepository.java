package com.gatenest.repository;

import com.gatenest.model.StaffAttendanceSession;
import com.gatenest.model.StaffAuthorization;
import com.gatenest.model.StaffMember;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class StaffRepository {

    private final JdbcTemplate jdbcTemplate;

    public StaffRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<StaffMember> staffRowMapper = (rs, rowNum) -> {
        StaffMember s = new StaffMember();
        s.setId(rs.getLong("id"));
        s.setFullName(rs.getString("full_name"));
        s.setMobile(rs.getString("mobile"));
        s.setStaffType(rs.getString("staff_type"));
        s.setIdProofType(rs.getString("id_proof_type"));
        s.setIdProofNumber(rs.getString("id_proof_number"));
        s.setPhotoUrl(rs.getString("photo_url"));
        s.setActive(rs.getBoolean("is_active"));
        s.setNotes(rs.getString("notes"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        s.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        s.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);
        return s;
    };

    private final RowMapper<StaffAuthorization> authRowMapper = (rs, rowNum) -> {
        StaffAuthorization a = new StaffAuthorization();
        a.setId(rs.getLong("id"));
        a.setStaffMemberId(rs.getLong("staff_member_id"));
        a.setResidentUserId(rs.getLong("resident_user_id"));
        a.setFlatId(rs.getLong("flat_id"));
        a.setScheduleDescription(rs.getString("schedule_description"));
        a.setActive(rs.getBoolean("is_active"));
        Timestamp authAt = rs.getTimestamp("authorized_at");
        a.setAuthorizedAt(authAt != null ? authAt.toInstant() : null);
        Timestamp revAt = rs.getTimestamp("revoked_at");
        a.setRevokedAt(revAt != null ? revAt.toInstant() : null);
        Timestamp createdAt = rs.getTimestamp("created_at");
        a.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        a.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            a.setStaffName(rs.getString("staff_name"));
            a.setStaffMobile(rs.getString("staff_mobile"));
            a.setStaffType(rs.getString("staff_type"));
            a.setResidentName(rs.getString("resident_name"));
            a.setFlatNumber(rs.getString("flat_number"));
            a.setBlockName(rs.getString("block_name"));
        } catch (Exception ignored) {}

        return a;
    };

    private final RowMapper<StaffAttendanceSession> sessionRowMapper = (rs, rowNum) -> {
        StaffAttendanceSession s = new StaffAttendanceSession();
        s.setId(rs.getLong("id"));
        s.setStaffMemberId(rs.getLong("staff_member_id"));
        Timestamp inAt = rs.getTimestamp("check_in_time");
        s.setCheckInTime(inAt != null ? inAt.toInstant() : null);
        Timestamp outAt = rs.getTimestamp("check_out_time");
        s.setCheckOutTime(outAt != null ? outAt.toInstant() : null);
        s.setEntryGateId(rs.getLong("entry_gate_id"));
        long exitG = rs.getLong("exit_gate_id");
        s.setExitGateId(rs.wasNull() ? null : exitG);
        s.setEntryGuardUserId(rs.getLong("entry_guard_user_id"));
        long exitGu = rs.getLong("exit_guard_user_id");
        s.setExitGuardUserId(rs.wasNull() ? null : exitGu);
        s.setActive(rs.getBoolean("is_active"));
        s.setNotes(rs.getString("notes"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        s.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        s.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            s.setStaffName(rs.getString("staff_name"));
            s.setStaffMobile(rs.getString("staff_mobile"));
            s.setStaffType(rs.getString("staff_type"));
            s.setEntryGateName(rs.getString("entry_gate_name"));
            s.setExitGateName(rs.getString("exit_gate_name"));
            s.setEntryGuardName(rs.getString("entry_guard_name"));
            s.setExitGuardName(rs.getString("exit_guard_name"));
        } catch (Exception ignored) {}

        return s;
    };

    // Staff Member methods
    public List<StaffMember> findAllStaff() {
        String sql = "SELECT s.*, " +
                "(SELECT COUNT(*) FROM staff_authorizations sa WHERE sa.staff_member_id = s.id AND sa.is_active = TRUE) AS auth_count, " +
                "(SELECT COUNT(*) FROM staff_attendance_sessions ses WHERE ses.staff_member_id = s.id AND ses.is_active = TRUE) AS inside_count, " +
                "(SELECT ses.id FROM staff_attendance_sessions ses WHERE ses.staff_member_id = s.id AND ses.is_active = TRUE LIMIT 1) AS active_session_id, " +
                "(SELECT ses.check_in_time FROM staff_attendance_sessions ses WHERE ses.staff_member_id = s.id AND ses.is_active = TRUE LIMIT 1) AS active_in_time, " +
                "(SELECT g.name FROM staff_attendance_sessions ses JOIN gates g ON ses.entry_gate_id = g.id WHERE ses.staff_member_id = s.id AND ses.is_active = TRUE LIMIT 1) AS active_gate_name " +
                "FROM staff_members s ORDER BY s.full_name ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            StaffMember m = staffRowMapper.mapRow(rs, rowNum);
            m.setAuthorizedFlatsCount(rs.getInt("auth_count"));
            m.setCurrentlyInside(rs.getInt("inside_count") > 0);
            long sessId = rs.getLong("active_session_id");
            m.setActiveSessionId(rs.wasNull() ? null : sessId);
            Timestamp inTime = rs.getTimestamp("active_in_time");
            m.setCheckInTime(inTime != null ? inTime.toInstant() : null);
            m.setEntryGateName(rs.getString("active_gate_name"));
            return m;
        });
    }

    public Optional<StaffMember> findStaffById(Long id) {
        String sql = "SELECT * FROM staff_members WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, staffRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<StaffMember> findStaffByMobile(String mobile) {
        String sql = "SELECT * FROM staff_members WHERE mobile = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, staffRowMapper, mobile.trim()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public StaffMember saveStaff(StaffMember s) {
        if (s.getId() == null) {
            String sql = "INSERT INTO staff_members (full_name, mobile, staff_type, id_proof_type, id_proof_number, photo_url, is_active, notes, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, s.getFullName().trim());
                ps.setString(2, s.getMobile().trim());
                ps.setString(3, s.getStaffType());
                ps.setString(4, s.getIdProofType());
                ps.setString(5, s.getIdProofNumber());
                ps.setString(6, s.getPhotoUrl());
                ps.setBoolean(7, s.isActive());
                ps.setString(8, s.getNotes());
                return ps;
            }, keyHolder);
            Long id = KeyHolderUtils.extractId(keyHolder);
            if (id != null) {
                s.setId(id);
            }
        } else {
            String sql = "UPDATE staff_members SET full_name = ?, mobile = ?, staff_type = ?, id_proof_type = ?, id_proof_number = ?, photo_url = ?, is_active = ?, notes = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, s.getFullName().trim(), s.getMobile().trim(), s.getStaffType(),
                    s.getIdProofType(), s.getIdProofNumber(), s.getPhotoUrl(), s.isActive(), s.getNotes(), s.getId());
        }
        return s;
    }

    // Authorizations
    public List<StaffAuthorization> findAuthorizationsByResident(Long residentUserId) {
        String sql = "SELECT sa.*, s.full_name AS staff_name, s.mobile AS staff_mobile, s.staff_type, " +
                "u.full_name AS resident_name, f.flat_number, b.name AS block_name " +
                "FROM staff_authorizations sa " +
                "JOIN staff_members s ON sa.staff_member_id = s.id " +
                "JOIN users u ON sa.resident_user_id = u.id " +
                "JOIN flats f ON sa.flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "WHERE sa.resident_user_id = ? ORDER BY sa.is_active DESC, sa.authorized_at DESC";
        return jdbcTemplate.query(sql, authRowMapper, residentUserId);
    }

    public List<StaffAuthorization> findAuthorizationsByStaff(Long staffMemberId) {
        String sql = "SELECT sa.*, s.full_name AS staff_name, s.mobile AS staff_mobile, s.staff_type, " +
                "u.full_name AS resident_name, f.flat_number, b.name AS block_name " +
                "FROM staff_authorizations sa " +
                "JOIN staff_members s ON sa.staff_member_id = s.id " +
                "JOIN users u ON sa.resident_user_id = u.id " +
                "JOIN flats f ON sa.flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "WHERE sa.staff_member_id = ? AND sa.is_active = TRUE";
        return jdbcTemplate.query(sql, authRowMapper, staffMemberId);
    }

    public Optional<StaffAuthorization> findAuthorization(Long staffMemberId, Long residentUserId, Long flatId) {
        String sql = "SELECT sa.*, s.full_name AS staff_name, s.mobile AS staff_mobile, s.staff_type, " +
                "u.full_name AS resident_name, f.flat_number, b.name AS block_name " +
                "FROM staff_authorizations sa " +
                "JOIN staff_members s ON sa.staff_member_id = s.id " +
                "JOIN users u ON sa.resident_user_id = u.id " +
                "JOIN flats f ON sa.flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "WHERE sa.staff_member_id = ? AND sa.resident_user_id = ? AND sa.flat_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, authRowMapper, staffMemberId, residentUserId, flatId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public StaffAuthorization saveAuthorization(StaffAuthorization a) {
        if (a.getId() == null) {
            String sql = "INSERT INTO staff_authorizations (staff_member_id, resident_user_id, flat_id, schedule_description, is_active, authorized_at, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, a.getStaffMemberId());
                ps.setLong(2, a.getResidentUserId());
                ps.setLong(3, a.getFlatId());
                ps.setString(4, a.getScheduleDescription());
                return ps;
            }, keyHolder);
            Long id = KeyHolderUtils.extractId(keyHolder);
            if (id != null) {
                a.setId(id);
            }
        } else {
            String sql = "UPDATE staff_authorizations SET schedule_description = ?, is_active = ?, revoked_at = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, a.getScheduleDescription(), a.isActive(),
                    a.getRevokedAt() != null ? Timestamp.from(a.getRevokedAt()) : null, a.getId());
        }
        return a;
    }

    // Attendance sessions
    public Optional<StaffAttendanceSession> findActiveSessionForStaff(Long staffMemberId) {
        String sql = "SELECT ses.*, s.full_name AS staff_name, s.mobile AS staff_mobile, s.staff_type, " +
                "g1.name AS entry_gate_name, g2.name AS exit_gate_name, " +
                "u1.full_name AS entry_guard_name, u2.full_name AS exit_guard_name " +
                "FROM staff_attendance_sessions ses " +
                "JOIN staff_members s ON ses.staff_member_id = s.id " +
                "JOIN gates g1 ON ses.entry_gate_id = g1.id " +
                "LEFT JOIN gates g2 ON ses.exit_gate_id = g2.id " +
                "JOIN users u1 ON ses.entry_guard_user_id = u1.id " +
                "LEFT JOIN users u2 ON ses.exit_guard_user_id = u2.id " +
                "WHERE ses.staff_member_id = ? AND ses.is_active = TRUE";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, sessionRowMapper, staffMemberId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<StaffAttendanceSession> findAllActiveSessions() {
        String sql = "SELECT ses.*, s.full_name AS staff_name, s.mobile AS staff_mobile, s.staff_type, " +
                "g1.name AS entry_gate_name, g2.name AS exit_gate_name, " +
                "u1.full_name AS entry_guard_name, u2.full_name AS exit_guard_name " +
                "FROM staff_attendance_sessions ses " +
                "JOIN staff_members s ON ses.staff_member_id = s.id " +
                "JOIN gates g1 ON ses.entry_gate_id = g1.id " +
                "LEFT JOIN gates g2 ON ses.exit_gate_id = g2.id " +
                "JOIN users u1 ON ses.entry_guard_user_id = u1.id " +
                "LEFT JOIN users u2 ON ses.exit_guard_user_id = u2.id " +
                "WHERE ses.is_active = TRUE ORDER BY ses.check_in_time DESC";
        return jdbcTemplate.query(sql, sessionRowMapper);
    }

    public List<StaffAttendanceSession> findAttendanceHistory(int limit, int offset) {
        String sql = "SELECT ses.*, s.full_name AS staff_name, s.mobile AS staff_mobile, s.staff_type, " +
                "g1.name AS entry_gate_name, g2.name AS exit_gate_name, " +
                "u1.full_name AS entry_guard_name, u2.full_name AS exit_guard_name " +
                "FROM staff_attendance_sessions ses " +
                "JOIN staff_members s ON ses.staff_member_id = s.id " +
                "JOIN gates g1 ON ses.entry_gate_id = g1.id " +
                "LEFT JOIN gates g2 ON ses.exit_gate_id = g2.id " +
                "JOIN users u1 ON ses.entry_guard_user_id = u1.id " +
                "LEFT JOIN users u2 ON ses.exit_guard_user_id = u2.id " +
                "ORDER BY ses.check_in_time DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, sessionRowMapper, limit, offset);
    }

    public StaffAttendanceSession createCheckIn(Long staffMemberId, Long entryGateId, Long entryGuardUserId, String notes) {
        String sql = "INSERT INTO staff_attendance_sessions (staff_member_id, check_in_time, entry_gate_id, entry_guard_user_id, is_active, notes, created_at, updated_at) " +
                "VALUES (?, CURRENT_TIMESTAMP, ?, ?, TRUE, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, staffMemberId);
            ps.setLong(2, entryGateId);
            ps.setLong(3, entryGuardUserId);
            ps.setString(4, notes);
            return ps;
        }, keyHolder);
        Long id = KeyHolderUtils.extractId(keyHolder);

        StaffAttendanceSession s = new StaffAttendanceSession();
        s.setId(id);
        s.setStaffMemberId(staffMemberId);
        s.setEntryGateId(entryGateId);
        s.setEntryGuardUserId(entryGuardUserId);
        s.setActive(true);
        s.setNotes(notes);
        s.setCheckInTime(Instant.now());
        return s;
    }

    public boolean recordCheckOut(Long sessionId, Long exitGateId, Long exitGuardUserId, String notes) {
        String sql = "UPDATE staff_attendance_sessions SET is_active = FALSE, check_out_time = CURRENT_TIMESTAMP, " +
                "exit_gate_id = ?, exit_guard_user_id = ?, notes = COALESCE(?, notes), updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND is_active = TRUE";
        return jdbcTemplate.update(sql, exitGateId, exitGuardUserId, notes, sessionId) > 0;
    }
}
