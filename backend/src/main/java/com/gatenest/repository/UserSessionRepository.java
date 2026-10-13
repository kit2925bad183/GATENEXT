package com.gatenest.repository;

import com.gatenest.dto.UserSessionDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class UserSessionRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<UserSessionDto> sessionRowMapper = (rs, rowNum) -> {
        UserSessionDto dto = new UserSessionDto();
        dto.setId(rs.getLong("id"));
        dto.setDeviceInfo(rs.getString("device_info"));
        dto.setIpAddress(rs.getString("ip_address"));
        Timestamp lastAct = rs.getTimestamp("last_activity_at");
        dto.setLastActivityAt(lastAct != null ? lastAct.toInstant() : null);
        Timestamp created = rs.getTimestamp("created_at");
        dto.setCreatedAt(created != null ? created.toInstant() : null);
        return dto;
    };

    public void registerOrUpdateSession(Long userId, String sessionIdHash, String deviceInfo, String ipAddress) {
        String checkSql = "SELECT COUNT(*) FROM user_sessions WHERE session_id_hash = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, sessionIdHash);
        if (count != null && count > 0) {
            String updateSql = "UPDATE user_sessions SET last_activity_at = CURRENT_TIMESTAMP, device_info = ?, ip_address = ?, is_revoked = FALSE WHERE session_id_hash = ?";
            jdbcTemplate.update(updateSql, deviceInfo, ipAddress, sessionIdHash);
        } else {
            String insertSql = "INSERT INTO user_sessions (user_id, session_id_hash, device_info, ip_address, is_revoked) VALUES (?, ?, ?, ?, FALSE)";
            jdbcTemplate.update(insertSql, userId, sessionIdHash, deviceInfo, ipAddress);
        }
    }

    public List<UserSessionDto> findActiveSessions(Long userId, String currentSessionHash) {
        String sql = "SELECT id, device_info, ip_address, last_activity_at, created_at, session_id_hash " +
                "FROM user_sessions " +
                "WHERE user_id = ? AND is_revoked = FALSE " +
                "ORDER BY last_activity_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserSessionDto dto = sessionRowMapper.mapRow(rs, rowNum);
            String hash = rs.getString("session_id_hash");
            if (dto != null) {
                dto.setCurrentSession(hash != null && hash.equals(currentSessionHash));
            }
            return dto;
        }, userId);
    }

    public void revokeSession(Long userId, Long sessionId) {
        String sql = "UPDATE user_sessions SET is_revoked = TRUE WHERE id = ? AND user_id = ?";
        jdbcTemplate.update(sql, sessionId, userId);
    }

    public void revokeAllOtherSessions(Long userId, String currentSessionHash) {
        String sql = "UPDATE user_sessions SET is_revoked = TRUE WHERE user_id = ? AND session_id_hash != ?";
        jdbcTemplate.update(sql, userId, currentSessionHash);
    }

    public void revokeAllUserSessions(Long userId) {
        String sql = "UPDATE user_sessions SET is_revoked = TRUE WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public boolean isSessionRevoked(String sessionIdHash) {
        String sql = "SELECT is_revoked FROM user_sessions WHERE session_id_hash = ?";
        try {
            Boolean revoked = jdbcTemplate.queryForObject(sql, Boolean.class, sessionIdHash);
            return revoked != null && revoked;
        } catch (Exception e) {
            return false;
        }
    }
}
