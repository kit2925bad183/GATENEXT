package com.gatenest.repository;

import com.gatenest.model.AuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AuditLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuditLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AuditLog> auditLogRowMapper = (rs, rowNum) -> {
        AuditLog a = new AuditLog();
        a.setId(rs.getLong("id"));
        long actor = rs.getLong("actor_user_id");
        a.setActorUserId(rs.wasNull() ? null : actor);
        a.setActorEmail(rs.getString("actor_email"));
        a.setActorRole(rs.getString("actor_role"));
        a.setAction(rs.getString("action"));
        a.setEntityType(rs.getString("entity_type"));
        a.setEntityId(rs.getString("entity_id"));
        a.setIpAddress(rs.getString("ip_address"));
        a.setDetailsJson(rs.getString("details_json"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        a.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        return a;
    };

    public AuditLog save(AuditLog log) {
        String sql = "INSERT INTO audit_logs (actor_user_id, actor_email, actor_role, action, entity_type, entity_id, ip_address, details_json, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (log.getActorUserId() != null) {
                ps.setLong(1, log.getActorUserId());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            ps.setString(2, log.getActorEmail());
            ps.setString(3, log.getActorRole());
            ps.setString(4, log.getAction());
            ps.setString(5, log.getEntityType());
            ps.setString(6, log.getEntityId());
            ps.setString(7, log.getIpAddress());
            ps.setString(8, log.getDetailsJson());
            return ps;
        }, keyHolder);

        Long generatedId = KeyHolderUtils.extractId(keyHolder);
        if (generatedId != null) {
            log.setId(generatedId);
        }
        return log;
    }

    public List<AuditLog> searchLogs(String action, String entityType, String query, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_logs WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (action != null && !action.trim().isEmpty()) {
            sql.append(" AND action = ?");
            params.add(action.trim());
        }
        if (entityType != null && !entityType.trim().isEmpty()) {
            sql.append(" AND entity_type = ?");
            params.add(entityType.trim());
        }
        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(actor_email) LIKE ? OR LOWER(action) LIKE ? OR entity_id LIKE ? OR LOWER(details_json) LIKE ?)");
            String term = "%" + query.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        sql.append(" ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), auditLogRowMapper, params.toArray());
    }

    public int countLogs(String action, String entityType, String query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_logs WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (action != null && !action.trim().isEmpty()) {
            sql.append(" AND action = ?");
            params.add(action.trim());
        }
        if (entityType != null && !entityType.trim().isEmpty()) {
            sql.append(" AND entity_type = ?");
            params.add(entityType.trim());
        }
        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(actor_email) LIKE ? OR LOWER(action) LIKE ? OR entity_id LIKE ? OR LOWER(details_json) LIKE ?)");
            String term = "%" + query.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }
}
