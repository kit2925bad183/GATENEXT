package com.gatenest.repository;

import com.gatenest.model.OccupancyType;
import com.gatenest.model.ResidentProfile;
import com.gatenest.model.VerificationStatus;
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
public class ResidentProfileRepository {

    private final JdbcTemplate jdbcTemplate;

    public ResidentProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ResidentProfile> enrichedRowMapper = (rs, rowNum) -> {
        ResidentProfile p = new ResidentProfile();
        p.setId(rs.getLong("id"));
        p.setUserId(rs.getLong("user_id"));
        p.setFlatId(rs.getLong("flat_id"));
        p.setOccupancyType(OccupancyType.valueOf(rs.getString("occupancy_type")));
        p.setVerificationStatus(VerificationStatus.valueOf(rs.getString("verification_status")));
        p.setRejectionReason(rs.getString("rejection_reason"));
        long reviewer = rs.getLong("reviewed_by_user_id");
        p.setReviewedByUserId(rs.wasNull() ? null : reviewer);
        Timestamp revAt = rs.getTimestamp("reviewed_at");
        p.setReviewedAt(revAt != null ? revAt.toInstant() : null);
        p.setEmergencyContact(rs.getString("emergency_contact"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        p.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        p.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        // Joined fields
        p.setUserFullName(rs.getString("user_full_name"));
        p.setUserEmail(rs.getString("user_email"));
        p.setUserMobile(rs.getString("user_mobile"));
        p.setBlockName(rs.getString("block_name"));
        p.setFlatNumber(rs.getString("flat_number"));
        p.setFloorNumber(rs.getInt("floor_number"));
        return p;
    };

    private static final String BASE_SELECT =
            "SELECT rp.*, u.full_name AS user_full_name, u.email AS user_email, u.mobile AS user_mobile, " +
            "b.name AS block_name, f.flat_number AS flat_number, f.floor_number AS floor_number " +
            "FROM resident_profiles rp " +
            "JOIN users u ON rp.user_id = u.id " +
            "JOIN flats f ON rp.flat_id = f.id " +
            "JOIN blocks b ON f.block_id = b.id ";

    public Optional<ResidentProfile> findById(Long id) {
        String sql = BASE_SELECT + "WHERE rp.id = ?";
        try {
            ResidentProfile profile = jdbcTemplate.queryForObject(sql, enrichedRowMapper, id);
            return Optional.ofNullable(profile);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<ResidentProfile> findByUserId(Long userId) {
        String sql = BASE_SELECT + "WHERE rp.user_id = ?";
        try {
            ResidentProfile profile = jdbcTemplate.queryForObject(sql, enrichedRowMapper, userId);
            return Optional.ofNullable(profile);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public ResidentProfile save(ResidentProfile profile) {
        if (profile.getId() == null) {
            String sql = "INSERT INTO resident_profiles (user_id, flat_id, occupancy_type, verification_status, rejection_reason, reviewed_by_user_id, reviewed_at, emergency_contact, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, profile.getUserId());
                ps.setLong(2, profile.getFlatId());
                ps.setString(3, profile.getOccupancyType().name());
                ps.setString(4, profile.getVerificationStatus().name());
                ps.setString(5, profile.getRejectionReason());
                if (profile.getReviewedByUserId() != null) {
                    ps.setLong(6, profile.getReviewedByUserId());
                } else {
                    ps.setNull(6, java.sql.Types.BIGINT);
                }
                if (profile.getReviewedAt() != null) {
                    ps.setTimestamp(7, Timestamp.from(profile.getReviewedAt()));
                } else {
                    ps.setNull(7, java.sql.Types.TIMESTAMP);
                }
                ps.setString(8, profile.getEmergencyContact());
                return ps;
            }, keyHolder);

            Long generatedId = KeyHolderUtils.extractId(keyHolder);
            if (generatedId != null) {
                profile.setId(generatedId);
            }
            return profile;
        } else {
            String sql = "UPDATE resident_profiles SET flat_id = ?, occupancy_type = ?, verification_status = ?, rejection_reason = ?, reviewed_by_user_id = ?, reviewed_at = ?, emergency_contact = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql,
                    profile.getFlatId(),
                    profile.getOccupancyType().name(),
                    profile.getVerificationStatus().name(),
                    profile.getRejectionReason(),
                    profile.getReviewedByUserId(),
                    profile.getReviewedAt() != null ? Timestamp.from(profile.getReviewedAt()) : null,
                    profile.getEmergencyContact(),
                    profile.getId());
            return profile;
        }
    }

    public List<ResidentProfile> findPendingProfiles(int offset, int limit) {
        String sql = BASE_SELECT + "WHERE rp.verification_status = 'PENDING' ORDER BY rp.id ASC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, enrichedRowMapper, limit, offset);
    }

    public List<ResidentProfile> searchResidents(String query, Long blockId, Long flatId, VerificationStatus status, int offset, int limit) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (status != null) {
            sql.append(" AND rp.verification_status = ?");
            params.add(status.name());
        }
        if (blockId != null) {
            sql.append(" AND f.block_id = ?");
            params.add(blockId);
        }
        if (flatId != null) {
            sql.append(" AND rp.flat_id = ?");
            params.add(flatId);
        }
        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(u.full_name) LIKE ? OR LOWER(u.email) LIKE ? OR u.mobile LIKE ? OR LOWER(f.flat_number) LIKE ?)");
            String term = "%" + query.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        sql.append(" ORDER BY rp.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), enrichedRowMapper, params.toArray());
    }

    public int countResidents(String query, Long blockId, Long flatId, VerificationStatus status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM resident_profiles rp " +
                "JOIN users u ON rp.user_id = u.id " +
                "JOIN flats f ON rp.flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id WHERE 1=1 ");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (status != null) {
            sql.append(" AND rp.verification_status = ?");
            params.add(status.name());
        }
        if (blockId != null) {
            sql.append(" AND f.block_id = ?");
            params.add(blockId);
        }
        if (flatId != null) {
            sql.append(" AND rp.flat_id = ?");
            params.add(flatId);
        }
        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(u.full_name) LIKE ? OR LOWER(u.email) LIKE ? OR u.mobile LIKE ? OR LOWER(f.flat_number) LIKE ?)");
            String term = "%" + query.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    public List<ResidentProfile> findApprovedByFlatId(Long flatId) {
        String sql = BASE_SELECT + "WHERE rp.flat_id = ? AND rp.verification_status = 'APPROVED' AND u.status = 'ACTIVE' ORDER BY u.full_name ASC";
        return jdbcTemplate.query(sql, enrichedRowMapper, flatId);
    }
}
