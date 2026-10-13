package com.gatenest.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserActivationRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserActivationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createActivation(Long userId, String tokenHash, Instant expiresAt, Long createdByUserId) {
        // Invalidate any older pending activations for this user
        jdbcTemplate.update("UPDATE user_activations SET is_used = TRUE, used_at = CURRENT_TIMESTAMP WHERE user_id = ? AND is_used = FALSE", userId);

        String sql = "INSERT INTO user_activations (user_id, token_hash, expires_at, is_used, created_by_user_id) " +
                "VALUES (?, ?, ?, FALSE, ?)";
        jdbcTemplate.update(sql, userId, tokenHash, Timestamp.from(expiresAt), createdByUserId);
    }

    public Optional<Map<String, Object>> findValidByTokenHash(String tokenHash) {
        String sql = "SELECT a.*, u.email, u.full_name, u.role, u.status " +
                "FROM user_activations a " +
                "JOIN users u ON a.user_id = u.id " +
                "WHERE a.token_hash = ? AND a.is_used = FALSE AND a.expires_at > CURRENT_TIMESTAMP";
        try {
            return Optional.of(jdbcTemplate.queryForMap(sql, tokenHash));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean markConsumed(Long activationId) {
        String sql = "UPDATE user_activations SET is_used = TRUE, used_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND is_used = FALSE";
        return jdbcTemplate.update(sql, activationId) > 0;
    }

    public Optional<String> findLatestTokenHashForUser(Long userId) {
        String sql = "SELECT token_hash FROM user_activations " +
                "WHERE user_id = ? AND is_used = FALSE AND expires_at > CURRENT_TIMESTAMP " +
                "ORDER BY id DESC LIMIT 1";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, String.class, userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
