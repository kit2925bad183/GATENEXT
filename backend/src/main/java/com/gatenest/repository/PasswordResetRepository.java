package com.gatenest.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Repository
public class PasswordResetRepository {

    private final JdbcTemplate jdbcTemplate;

    public PasswordResetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createPasswordReset(Long userId, String tokenHash, Instant expiresAt, String ipAddress) {
        // Invalidate older tokens for this user
        jdbcTemplate.update("UPDATE password_resets SET is_used = TRUE, used_at = CURRENT_TIMESTAMP WHERE user_id = ? AND is_used = FALSE", userId);

        String sql = "INSERT INTO password_resets (user_id, token_hash, expires_at, is_used, ip_address) " +
                "VALUES (?, ?, ?, FALSE, ?)";
        jdbcTemplate.update(sql, userId, tokenHash, Timestamp.from(expiresAt), ipAddress);
    }

    public Optional<Map<String, Object>> findValidByTokenHash(String tokenHash) {
        String sql = "SELECT pr.*, u.email, u.full_name, u.role, u.status " +
                "FROM password_resets pr " +
                "JOIN users u ON pr.user_id = u.id " +
                "WHERE pr.token_hash = ? AND pr.is_used = FALSE AND pr.expires_at > CURRENT_TIMESTAMP";
        try {
            return Optional.of(jdbcTemplate.queryForMap(sql, tokenHash));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean markConsumed(Long resetId) {
        String sql = "UPDATE password_resets SET is_used = TRUE, used_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND is_used = FALSE";
        return jdbcTemplate.update(sql, resetId) > 0;
    }
}
