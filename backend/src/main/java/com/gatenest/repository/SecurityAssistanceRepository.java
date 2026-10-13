package com.gatenest.repository;

import com.gatenest.model.SecurityAssistanceRequest;
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
public class SecurityAssistanceRepository {

    private final JdbcTemplate jdbcTemplate;

    public SecurityAssistanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SecurityAssistanceRequest> rowMapper = (rs, rowNum) -> {
        SecurityAssistanceRequest r = new SecurityAssistanceRequest();
        r.setId(rs.getLong("id"));
        r.setResidentUserId(rs.getLong("resident_user_id"));
        r.setFlatId(rs.getLong("flat_id"));
        r.setCategory(rs.getString("category"));
        r.setLocationDetails(rs.getString("location_details"));
        r.setDescription(rs.getString("description"));
        r.setStatus(rs.getString("status"));
        Timestamp subAt = rs.getTimestamp("submitted_at");
        r.setSubmittedAt(subAt != null ? subAt.toInstant() : null);
        long ackUser = rs.getLong("acknowledged_by_user_id");
        r.setAcknowledgedByUserId(rs.wasNull() ? null : ackUser);
        Timestamp ackAt = rs.getTimestamp("acknowledged_at");
        r.setAcknowledgedAt(ackAt != null ? ackAt.toInstant() : null);
        long resUser = rs.getLong("resolved_by_user_id");
        r.setResolvedByUserId(rs.wasNull() ? null : resUser);
        Timestamp resAt = rs.getTimestamp("resolved_at");
        r.setResolvedAt(resAt != null ? resAt.toInstant() : null);
        r.setResolutionNotes(rs.getString("resolution_notes"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        r.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        r.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            r.setResidentName(rs.getString("resident_name"));
            r.setResidentMobile(rs.getString("resident_mobile"));
            r.setFlatNumber(rs.getString("flat_number"));
            r.setBlockName(rs.getString("block_name"));
            r.setAcknowledgedByName(rs.getString("acknowledged_by_name"));
            r.setResolvedByName(rs.getString("resolved_by_name"));
        } catch (Exception ignored) {}

        return r;
    };

    private String baseSelectSql() {
        return "SELECT sar.*, u.full_name AS resident_name, u.mobile AS resident_mobile, " +
                "f.flat_number, b.name AS block_name, " +
                "a.full_name AS acknowledged_by_name, " +
                "res.full_name AS resolved_by_name " +
                "FROM security_assistance_requests sar " +
                "JOIN users u ON sar.resident_user_id = u.id " +
                "JOIN flats f ON sar.flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "LEFT JOIN users a ON sar.acknowledged_by_user_id = a.id " +
                "LEFT JOIN users res ON sar.resolved_by_user_id = res.id ";
    }

    public List<SecurityAssistanceRequest> findByResidentUserId(Long residentUserId) {
        String sql = baseSelectSql() + "WHERE sar.resident_user_id = ? ORDER BY sar.submitted_at DESC";
        return jdbcTemplate.query(sql, rowMapper, residentUserId);
    }

    public List<SecurityAssistanceRequest> findActiveRequests() {
        String sql = baseSelectSql() + "WHERE sar.status IN ('SUBMITTED', 'ACKNOWLEDGED') ORDER BY sar.submitted_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<SecurityAssistanceRequest> findAllHistory(int limit, int offset) {
        String sql = baseSelectSql() + "ORDER BY sar.submitted_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, rowMapper, limit, offset);
    }

    public Optional<SecurityAssistanceRequest> findById(Long id) {
        String sql = baseSelectSql() + "WHERE sar.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public SecurityAssistanceRequest save(SecurityAssistanceRequest r) {
        if (r.getId() == null) {
            String sql = "INSERT INTO security_assistance_requests (resident_user_id, flat_id, category, location_details, description, status, submitted_at, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, 'SUBMITTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, r.getResidentUserId());
                ps.setLong(2, r.getFlatId());
                ps.setString(3, r.getCategory() != null ? r.getCategory() : "GENERAL");
                ps.setString(4, r.getLocationDetails());
                ps.setString(5, r.getDescription());
                return ps;
            }, keyHolder);
            Long id = KeyHolderUtils.extractId(keyHolder);
            if (id != null) {
                r.setId(id);
            }
        } else {
            String sql = "UPDATE security_assistance_requests SET category = ?, location_details = ?, description = ?, status = ?, acknowledged_by_user_id = ?, acknowledged_at = ?, resolved_by_user_id = ?, resolved_at = ?, resolution_notes = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, r.getCategory(), r.getLocationDetails(), r.getDescription(),
                    r.getStatus(), r.getAcknowledgedByUserId(),
                    r.getAcknowledgedAt() != null ? Timestamp.from(r.getAcknowledgedAt()) : null,
                    r.getResolvedByUserId(),
                    r.getResolvedAt() != null ? Timestamp.from(r.getResolvedAt()) : null,
                    r.getResolutionNotes(), r.getId());
        }
        return r;
    }

    public boolean acknowledgeRequest(Long id, Long guardUserId) {
        String sql = "UPDATE security_assistance_requests SET status = 'ACKNOWLEDGED', acknowledged_by_user_id = ?, acknowledged_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND status = 'SUBMITTED'";
        return jdbcTemplate.update(sql, guardUserId, id) > 0;
    }

    public boolean resolveRequest(Long id, Long guardUserId, String resolutionNotes) {
        String sql = "UPDATE security_assistance_requests SET status = 'RESOLVED', resolved_by_user_id = ?, resolved_at = CURRENT_TIMESTAMP, resolution_notes = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND status IN ('SUBMITTED', 'ACKNOWLEDGED')";
        return jdbcTemplate.update(sql, guardUserId, resolutionNotes, id) > 0;
    }

    public int countActiveAssistanceRequests() {
        String sql = "SELECT COUNT(*) FROM security_assistance_requests WHERE status IN ('SUBMITTED', 'ACKNOWLEDGED')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
