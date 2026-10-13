package com.gatenest.repository;

import com.gatenest.model.Announcement;
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
public class AnnouncementRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnnouncementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Announcement> rowMapper = (rs, rowNum) -> {
        Announcement a = new Announcement();
        a.setId(rs.getLong("id"));
        a.setAuthorUserId(rs.getLong("author_user_id"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setAnnouncementType(rs.getString("announcement_type"));
        a.setPriority(rs.getString("priority"));
        a.setAudienceType(rs.getString("audience_type"));
        long blockId = rs.getLong("target_block_id");
        a.setTargetBlockId(rs.wasNull() ? null : blockId);
        a.setPinned(rs.getBoolean("is_pinned"));
        a.setPublished(rs.getBoolean("is_published"));
        Timestamp pubAt = rs.getTimestamp("published_at");
        a.setPublishedAt(pubAt != null ? pubAt.toInstant() : null);
        Timestamp expAt = rs.getTimestamp("expires_at");
        a.setExpiresAt(expAt != null ? expAt.toInstant() : null);
        a.setArchived(rs.getBoolean("is_archived"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        a.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        a.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            a.setAuthorName(rs.getString("author_name"));
            a.setTargetBlockName(rs.getString("target_block_name"));
            a.setReadByCurrentUser(rs.getInt("read_count") > 0);
        } catch (Exception ignored) {}

        return a;
    };

    public List<Announcement> findFeedForUser(Long userId, Long userBlockId) {
        String sql = "SELECT a.*, u.full_name AS author_name, b.name AS target_block_name, " +
                "(SELECT COUNT(*) FROM announcement_reads ar WHERE ar.announcement_id = a.id AND ar.user_id = ?) AS read_count " +
                "FROM announcements a " +
                "JOIN users u ON a.author_user_id = u.id " +
                "LEFT JOIN blocks b ON a.target_block_id = b.id " +
                "WHERE a.is_published = TRUE AND a.is_archived = FALSE " +
                "AND (a.audience_type = 'COMMUNITY' OR (a.audience_type = 'BLOCK' AND a.target_block_id = ?)) " +
                "AND (a.expires_at IS NULL OR a.expires_at > CURRENT_TIMESTAMP) " +
                "ORDER BY a.is_pinned DESC, a.published_at DESC";
        return jdbcTemplate.query(sql, rowMapper, userId, userBlockId != null ? userBlockId : -1L);
    }

    public List<Announcement> findAllAdmin(int limit, int offset) {
        String sql = "SELECT a.*, u.full_name AS author_name, b.name AS target_block_name, 0 AS read_count " +
                "FROM announcements a " +
                "JOIN users u ON a.author_user_id = u.id " +
                "LEFT JOIN blocks b ON a.target_block_id = b.id " +
                "ORDER BY a.is_pinned DESC, a.created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, rowMapper, limit, offset);
    }

    public Optional<Announcement> findById(Long id) {
        String sql = "SELECT a.*, u.full_name AS author_name, b.name AS target_block_name, 0 AS read_count " +
                "FROM announcements a " +
                "JOIN users u ON a.author_user_id = u.id " +
                "LEFT JOIN blocks b ON a.target_block_id = b.id " +
                "WHERE a.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Announcement save(Announcement a) {
        if (a.getId() == null) {
            String sql = "INSERT INTO announcements (author_user_id, title, content, announcement_type, priority, audience_type, target_block_id, is_pinned, is_published, published_at, expires_at, is_archived, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, a.getAuthorUserId());
                ps.setString(2, a.getTitle().trim());
                ps.setString(3, a.getContent());
                ps.setString(4, a.getAnnouncementType() != null ? a.getAnnouncementType() : "GENERAL");
                ps.setString(5, a.getPriority() != null ? a.getPriority() : "NORMAL");
                ps.setString(6, a.getAudienceType() != null ? a.getAudienceType() : "COMMUNITY");
                if (a.getTargetBlockId() != null) ps.setLong(7, a.getTargetBlockId()); else ps.setNull(7, java.sql.Types.BIGINT);
                ps.setBoolean(8, a.isPinned());
                ps.setBoolean(9, a.isPublished());
                if (a.getExpiresAt() != null) ps.setTimestamp(10, Timestamp.from(a.getExpiresAt())); else ps.setNull(10, java.sql.Types.TIMESTAMP);
                return ps;
            }, keyHolder);
            Long id = KeyHolderUtils.extractId(keyHolder);
            if (id != null) {
                a.setId(id);
            }
        } else {
            String sql = "UPDATE announcements SET title = ?, content = ?, announcement_type = ?, priority = ?, audience_type = ?, target_block_id = ?, is_pinned = ?, is_published = ?, expires_at = ?, is_archived = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, a.getTitle().trim(), a.getContent(), a.getAnnouncementType(),
                    a.getPriority(), a.getAudienceType(), a.getTargetBlockId(), a.isPinned(), a.isPublished(),
                    a.getExpiresAt() != null ? Timestamp.from(a.getExpiresAt()) : null,
                    a.isArchived(), a.getId());
        }
        return a;
    }

    public void markAsRead(Long announcementId, Long userId) {
        String sql = "INSERT INTO announcement_reads (announcement_id, user_id, read_at) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP) " +
                "ON DUPLICATE KEY UPDATE read_at = CURRENT_TIMESTAMP";
        try {
            jdbcTemplate.update(sql, announcementId, userId);
        } catch (Exception e) {
            // Fallback for H2 if needed
            String h2Sql = "MERGE INTO announcement_reads (announcement_id, user_id, read_at) KEY(announcement_id, user_id) VALUES (?, ?, CURRENT_TIMESTAMP)";
            try {
                jdbcTemplate.update(h2Sql, announcementId, userId);
            } catch (Exception ignored) {}
        }
    }
}
