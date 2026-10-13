package com.gatenest.repository;

import com.gatenest.model.VisitEvent;
import com.gatenest.model.VisitEventType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class VisitEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public VisitEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<VisitEvent> eventRowMapper = (rs, rowNum) -> {
        VisitEvent e = new VisitEvent();
        e.setId(rs.getLong("id"));
        e.setVisitId(rs.getLong("visit_id"));
        e.setEventType(VisitEventType.valueOf(rs.getString("event_type")));
        long actor = rs.getLong("actor_user_id");
        e.setActorUserId(rs.wasNull() ? null : actor);
        long gate = rs.getLong("gate_id");
        e.setGateId(rs.wasNull() ? null : gate);
        e.setNotes(rs.getString("notes"));
        e.setMetadataJson(rs.getString("metadata_json"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        e.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);

        try {
            e.setActorName(rs.getString("actor_name"));
        } catch (Exception ignored) {}
        try {
            e.setActorRole(rs.getString("actor_role"));
        } catch (Exception ignored) {}
        try {
            e.setGateName(rs.getString("gate_name"));
        } catch (Exception ignored) {}
        return e;
    };

    public List<VisitEvent> findByVisitId(Long visitId) {
        String sql = "SELECT ve.*, u.full_name AS actor_name, u.role AS actor_role, g.name AS gate_name " +
                "FROM visit_events ve " +
                "LEFT JOIN users u ON ve.actor_user_id = u.id " +
                "LEFT JOIN gates g ON ve.gate_id = g.id " +
                "WHERE ve.visit_id = ? " +
                "ORDER BY ve.created_at ASC, ve.id ASC";
        return jdbcTemplate.query(sql, eventRowMapper, visitId);
    }

    public VisitEvent save(VisitEvent event) {
        String sql = "INSERT INTO visit_events (visit_id, event_type, actor_user_id, gate_id, notes, metadata_json, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, event.getVisitId());
            ps.setString(2, event.getEventType().name());
            if (event.getActorUserId() != null) {
                ps.setLong(3, event.getActorUserId());
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            if (event.getGateId() != null) {
                ps.setLong(4, event.getGateId());
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }
            ps.setString(5, event.getNotes());
            ps.setString(6, event.getMetadataJson());
            return ps;
        }, keyHolder);

        Long generatedId = KeyHolderUtils.extractId(keyHolder);
        if (generatedId != null) {
            event.setId(generatedId);
        }
        return event;
    }
}
