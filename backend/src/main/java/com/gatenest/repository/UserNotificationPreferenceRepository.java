package com.gatenest.repository;

import com.gatenest.model.UserNotificationPreferences;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class UserNotificationPreferenceRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserNotificationPreferenceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<UserNotificationPreferences> rowMapper = (rs, rowNum) -> {
        UserNotificationPreferences p = new UserNotificationPreferences();
        p.setId(rs.getLong("id"));
        p.setUserId(rs.getLong("user_id"));
        p.setVisitorAlerts(rs.getBoolean("visitor_alerts"));
        p.setParcelAlerts(rs.getBoolean("parcel_alerts"));
        p.setComplaintAlerts(rs.getBoolean("complaint_alerts"));
        p.setNoticeAlerts(rs.getBoolean("notice_alerts"));
        p.setAssistanceAlerts(rs.getBoolean("assistance_alerts"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        p.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        p.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);
        return p;
    };

    public Optional<UserNotificationPreferences> findByUserId(Long userId) {
        String sql = "SELECT * FROM user_notification_preferences WHERE user_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public UserNotificationPreferences saveOrUpdate(UserNotificationPreferences p) {
        Optional<UserNotificationPreferences> existing = findByUserId(p.getUserId());
        if (existing.isEmpty()) {
            String sql = "INSERT INTO user_notification_preferences (user_id, visitor_alerts, parcel_alerts, complaint_alerts, notice_alerts, assistance_alerts, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            jdbcTemplate.update(sql, p.getUserId(), p.isVisitorAlerts(), p.isParcelAlerts(),
                    p.isComplaintAlerts(), p.isNoticeAlerts(), p.isAssistanceAlerts());
        } else {
            String sql = "UPDATE user_notification_preferences SET visitor_alerts = ?, parcel_alerts = ?, complaint_alerts = ?, notice_alerts = ?, assistance_alerts = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
            jdbcTemplate.update(sql, p.isVisitorAlerts(), p.isParcelAlerts(),
                    p.isComplaintAlerts(), p.isNoticeAlerts(), p.isAssistanceAlerts(), p.getUserId());
        }
        return findByUserId(p.getUserId()).orElse(p);
    }
}
