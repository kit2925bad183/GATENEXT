package com.gatenest.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ResidentMoveRepository {

    private final JdbcTemplate jdbcTemplate;

    public ResidentMoveRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void logResidentMove(Long residentUserId, Long oldFlatId, Long newFlatId, Long authorizedByUserId, String reason, int invalidatedCount) {
        String sql = "INSERT INTO resident_moves (resident_user_id, old_flat_id, new_flat_id, authorized_by_user_id, reason, future_invitations_invalidated_count) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, residentUserId, oldFlatId, newFlatId, authorizedByUserId, reason, invalidatedCount);
    }

    public List<Map<String, Object>> findByResidentUserId(Long residentUserId) {
        String sql = "SELECT m.*, u.full_name AS resident_name, au.full_name AS authorizer_name, " +
                "oflat.flat_number AS old_flat_number, ob.name AS old_block_name, " +
                "nflat.flat_number AS new_flat_number, nb.name AS new_block_name " +
                "FROM resident_moves m " +
                "JOIN users u ON m.resident_user_id = u.id " +
                "JOIN users au ON m.authorized_by_user_id = au.id " +
                "JOIN flats oflat ON m.old_flat_id = oflat.id " +
                "JOIN blocks ob ON oflat.block_id = ob.id " +
                "JOIN flats nflat ON m.new_flat_id = nflat.id " +
                "JOIN blocks nb ON nflat.block_id = nb.id " +
                "WHERE m.resident_user_id = ? " +
                "ORDER BY m.created_at DESC";
        return jdbcTemplate.queryForList(sql, residentUserId);
    }
}
