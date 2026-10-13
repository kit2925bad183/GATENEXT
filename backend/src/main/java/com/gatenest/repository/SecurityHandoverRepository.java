package com.gatenest.repository;

import com.gatenest.model.SecurityHandoverNote;
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
public class SecurityHandoverRepository {

    private final JdbcTemplate jdbcTemplate;

    public SecurityHandoverRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SecurityHandoverNote> rowMapper = (rs, rowNum) -> {
        SecurityHandoverNote n = new SecurityHandoverNote();
        n.setId(rs.getLong("id"));
        n.setShiftName(rs.getString("shift_name"));
        n.setGateId(rs.getLong("gate_id"));
        n.setAuthorUserId(rs.getLong("author_user_id"));
        n.setNoteText(rs.getString("note_text"));
        n.setAcknowledged(rs.getBoolean("is_acknowledged"));
        long ackUser = rs.getLong("acknowledged_by_user_id");
        n.setAcknowledgedByUserId(rs.wasNull() ? null : ackUser);
        Timestamp ackAt = rs.getTimestamp("acknowledged_at");
        n.setAcknowledgedAt(ackAt != null ? ackAt.toInstant() : null);
        Timestamp createdAt = rs.getTimestamp("created_at");
        n.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        n.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            n.setGateName(rs.getString("gate_name"));
            n.setAuthorName(rs.getString("author_name"));
            n.setAcknowledgedByName(rs.getString("acknowledged_by_name"));
        } catch (Exception ignored) {}

        return n;
    };

    public List<SecurityHandoverNote> findRecentNotes(int limit) {
        String sql = "SELECT n.*, g.name AS gate_name, u.full_name AS author_name, " +
                "a.full_name AS acknowledged_by_name " +
                "FROM security_handover_notes n " +
                "JOIN gates g ON n.gate_id = g.id " +
                "JOIN users u ON n.author_user_id = u.id " +
                "LEFT JOIN users a ON n.acknowledged_by_user_id = a.id " +
                "ORDER BY n.created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public Optional<SecurityHandoverNote> findById(Long id) {
        String sql = "SELECT n.*, g.name AS gate_name, u.full_name AS author_name, " +
                "a.full_name AS acknowledged_by_name " +
                "FROM security_handover_notes n " +
                "JOIN gates g ON n.gate_id = g.id " +
                "JOIN users u ON n.author_user_id = u.id " +
                "LEFT JOIN users a ON n.acknowledged_by_user_id = a.id " +
                "WHERE n.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public SecurityHandoverNote save(SecurityHandoverNote n) {
        if (n.getId() == null) {
            String sql = "INSERT INTO security_handover_notes (shift_name, gate_id, author_user_id, note_text, is_acknowledged, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, n.getShiftName() != null ? n.getShiftName() : "General Shift");
                if (n.getGateId() != null) {
                    ps.setLong(2, n.getGateId());
                } else {
                    ps.setNull(2, java.sql.Types.BIGINT);
                }
                ps.setLong(3, n.getAuthorUserId());
                ps.setString(4, n.getNoteText());
                return ps;
            }, keyHolder);
            Long id = KeyHolderUtils.extractId(keyHolder);
            if (id != null) {
                n.setId(id);
            }
        } else {
            String sql = "UPDATE security_handover_notes SET shift_name = ?, gate_id = ?, note_text = ?, is_acknowledged = ?, acknowledged_by_user_id = ?, acknowledged_at = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, n.getShiftName() != null ? n.getShiftName() : "General Shift", n.getGateId(), n.getNoteText(),
                    n.isAcknowledged(), n.getAcknowledgedByUserId(),
                    n.getAcknowledgedAt() != null ? Timestamp.from(n.getAcknowledgedAt()) : null, n.getId());
        }
        return n;
    }

    public boolean acknowledgeNote(Long noteId, Long guardUserId) {
        String sql = "UPDATE security_handover_notes SET is_acknowledged = TRUE, acknowledged_by_user_id = ?, acknowledged_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return jdbcTemplate.update(sql, guardUserId, noteId) > 0;
    }

    public int countUnacknowledgedNotes() {
        String sql = "SELECT COUNT(*) FROM security_handover_notes WHERE is_acknowledged = FALSE";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
