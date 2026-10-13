package com.gatenest.repository;

import com.gatenest.model.SecurityGateAssignment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class SecurityAssignmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public SecurityAssignmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SecurityGateAssignment> assignmentRowMapper = (rs, rowNum) -> {
        SecurityGateAssignment s = new SecurityGateAssignment();
        s.setId(rs.getLong("id"));
        s.setUserId(rs.getLong("user_id"));
        s.setGateId(rs.getLong("gate_id"));
        s.setActive(rs.getBoolean("is_active"));
        s.setShiftName(rs.getString("shift_name"));
        Timestamp assignedAt = rs.getTimestamp("assigned_at");
        s.setAssignedAt(assignedAt != null ? assignedAt.toInstant() : null);
        Timestamp createdAt = rs.getTimestamp("created_at");
        s.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);

        s.setUserFullName(rs.getString("user_full_name"));
        s.setUserEmail(rs.getString("user_email"));
        s.setUserMobile(rs.getString("user_mobile"));
        s.setGateName(rs.getString("gate_name"));
        s.setGateCode(rs.getString("gate_code"));
        return s;
    };

    private static final String BASE_SELECT =
            "SELECT sga.*, u.full_name AS user_full_name, u.email AS user_email, u.mobile AS user_mobile, " +
            "g.name AS gate_name, g.code AS gate_code " +
            "FROM security_gate_assignments sga " +
            "JOIN users u ON sga.user_id = u.id " +
            "JOIN gates g ON sga.gate_id = g.id ";

    public List<SecurityGateAssignment> findAllActive() {
        String sql = BASE_SELECT + "WHERE sga.is_active = TRUE AND u.status = 'ACTIVE' ORDER BY g.name ASC, u.full_name ASC";
        return jdbcTemplate.query(sql, assignmentRowMapper);
    }

    public List<SecurityGateAssignment> findByUserId(Long userId) {
        String sql = BASE_SELECT + "WHERE sga.user_id = ? AND sga.is_active = TRUE";
        return jdbcTemplate.query(sql, assignmentRowMapper, userId);
    }

    public Optional<SecurityGateAssignment> findActiveByUserId(Long userId) {
        String sql = BASE_SELECT + "WHERE sga.user_id = ? AND sga.is_active = TRUE LIMIT 1";
        try {
            SecurityGateAssignment assignment = jdbcTemplate.queryForObject(sql, assignmentRowMapper, userId);
            return Optional.ofNullable(assignment);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<SecurityGateAssignment> findByGateId(Long gateId) {
        String sql = BASE_SELECT + "WHERE sga.gate_id = ? AND sga.is_active = TRUE AND u.status = 'ACTIVE'";
        return jdbcTemplate.query(sql, assignmentRowMapper, gateId);
    }

    public Optional<SecurityGateAssignment> findActiveByUserAndGate(Long userId, Long gateId) {
        String sql = BASE_SELECT + "WHERE sga.user_id = ? AND sga.gate_id = ? AND sga.is_active = TRUE";
        try {
            SecurityGateAssignment assignment = jdbcTemplate.queryForObject(sql, assignmentRowMapper, userId, gateId);
            return Optional.ofNullable(assignment);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public SecurityGateAssignment save(SecurityGateAssignment assignment) {
        if (assignment.getId() == null) {
            String sql = "INSERT INTO security_gate_assignments (user_id, gate_id, is_active, shift_name, assigned_at, created_at) " +
                    "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, assignment.getUserId());
                ps.setLong(2, assignment.getGateId());
                ps.setBoolean(3, assignment.isActive());
                ps.setString(4, assignment.getShiftName());
                return ps;
            }, keyHolder);

            Long generatedId = KeyHolderUtils.extractId(keyHolder);
            if (generatedId != null) {
                assignment.setId(generatedId);
            }
            return assignment;
        } else {
            String sql = "UPDATE security_gate_assignments SET gate_id = ?, is_active = ?, shift_name = ? WHERE id = ?";
            jdbcTemplate.update(sql, assignment.getGateId(), assignment.isActive(), assignment.getShiftName(), assignment.getId());
            return assignment;
        }
    }

    public void deactivateUserAssignments(Long userId) {
        String sql = "UPDATE security_gate_assignments SET is_active = FALSE WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
}
