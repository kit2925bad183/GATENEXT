package com.gatenest.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class VisitCorrectionRepository {

    private final JdbcTemplate jdbcTemplate;

    public VisitCorrectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void logCorrection(Long visitId, Long actorUserId, String fieldName, String oldValue, String newValue, String reason) {
        String sql = "INSERT INTO visit_corrections (visit_id, actor_user_id, field_name, old_value, new_value, reason) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, visitId, actorUserId, fieldName, oldValue, newValue, reason);
    }

    public List<Map<String, Object>> findByVisitId(Long visitId) {
        String sql = "SELECT c.*, u.full_name AS actor_name " +
                "FROM visit_corrections c " +
                "JOIN users u ON c.actor_user_id = u.id " +
                "WHERE c.visit_id = ? " +
                "ORDER BY c.created_at DESC";
        return jdbcTemplate.queryForList(sql, visitId);
    }
}
