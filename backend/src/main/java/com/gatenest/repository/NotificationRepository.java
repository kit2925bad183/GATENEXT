package com.gatenest.repository;

import com.gatenest.model.Notification;
import com.gatenest.model.NotificationType;
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
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Notification> notificationRowMapper = (rs, rowNum) -> {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setUserId(rs.getLong("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setLinkUrl(rs.getString("link_url"));
        n.setNotificationType(NotificationType.valueOf(rs.getString("notification_type")));
        n.setRead(rs.getBoolean("is_read"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        n.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        return n;
    };

    public List<Notification> findByUserId(Long userId, int limit) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, notificationRowMapper, userId, limit);
    }

    public int countUnreadByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    public Notification save(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, title, message, link_url, notification_type, is_read, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, notification.getUserId());
            ps.setString(2, notification.getTitle().trim());
            ps.setString(3, notification.getMessage().trim());
            ps.setString(4, notification.getLinkUrl());
            ps.setString(5, notification.getNotificationType().name());
            ps.setBoolean(6, notification.isRead());
            return ps;
        }, keyHolder);

        Long generatedId = KeyHolderUtils.extractId(keyHolder);
        if (generatedId != null) {
            notification.setId(generatedId);
        }
        return notification;
    }

    public void markAsRead(Long notificationId, Long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ? AND user_id = ?";
        jdbcTemplate.update(sql, notificationId, userId);
    }

    public void markAllAsRead(Long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        jdbcTemplate.update(sql, userId);
    }
}
