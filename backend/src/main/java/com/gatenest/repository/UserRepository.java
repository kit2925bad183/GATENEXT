package com.gatenest.repository;

import com.gatenest.model.Role;
import com.gatenest.model.User;
import com.gatenest.model.UserStatus;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setMobile(rs.getString("mobile"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setStatus(UserStatus.valueOf(rs.getString("status")));
        u.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        Timestamp lastLogin = rs.getTimestamp("last_login_at");
        u.setLastLoginAt(lastLogin != null ? lastLogin.toInstant() : null);
        Timestamp createdAt = rs.getTimestamp("created_at");
        u.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        u.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);
        return u;
    };

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public boolean existsByRole(Role role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, role.name());
        return count != null && count > 0;
    }

    public User save(User user) {
        if (user.getId() == null) {
            String sql = "INSERT INTO users (email, password_hash, full_name, mobile, role, status, failed_login_attempts, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getEmail().toLowerCase().trim());
                ps.setString(2, user.getPasswordHash());
                ps.setString(3, user.getFullName().trim());
                ps.setString(4, user.getMobile().trim());
                ps.setString(5, user.getRole().name());
                ps.setString(6, user.getStatus().name());
                ps.setInt(7, user.getFailedLoginAttempts());
                return ps;
            }, keyHolder);

            Long generatedId = KeyHolderUtils.extractId(keyHolder);
            if (generatedId != null) {
                user.setId(generatedId);
            }
            return user;
        } else {
            String sql = "UPDATE users SET email = ?, password_hash = ?, full_name = ?, mobile = ?, role = ?, status = ?, failed_login_attempts = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql,
                    user.getEmail().toLowerCase().trim(),
                    user.getPasswordHash(),
                    user.getFullName().trim(),
                    user.getMobile().trim(),
                    user.getRole().name(),
                    user.getStatus().name(),
                    user.getFailedLoginAttempts(),
                    user.getId());
            return user;
        }
    }

    public void updatePassword(Long userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, newPasswordHash, userId);
    }

    public void updateStatus(Long userId, UserStatus status) {
        String sql = "UPDATE users SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), userId);
    }

    public void updateLastLogin(Long userId) {
        String sql = "UPDATE users SET last_login_at = CURRENT_TIMESTAMP, failed_login_attempts = 0, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void recordFailedLogin(Long userId) {
        String sql = "UPDATE users SET failed_login_attempts = failed_login_attempts + 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public List<User> findByRole(Role role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY full_name ASC";
        return jdbcTemplate.query(sql, userRowMapper, role.name());
    }

    public List<User> searchUsers(String query, Role role, UserStatus status, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1 ");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (role != null) {
            sql.append(" AND role = ?");
            params.add(role.name());
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }
        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(full_name) LIKE ? OR LOWER(email) LIKE ? OR mobile LIKE ?)");
            String term = "%" + query.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), userRowMapper, params.toArray());
    }

    public int countUsers(String query, Role role, UserStatus status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1 ");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (role != null) {
            sql.append(" AND role = ?");
            params.add(role.name());
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }
        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(full_name) LIKE ? OR LOWER(email) LIKE ? OR mobile LIKE ?)");
            String term = "%" + query.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    public int countActiveResidents() {
        String sql = "SELECT COUNT(*) FROM users u JOIN resident_profiles rp ON u.id = rp.user_id WHERE u.role = 'RESIDENT' AND u.status = 'ACTIVE' AND rp.verification_status = 'APPROVED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countPendingResidents() {
        String sql = "SELECT COUNT(*) FROM resident_profiles WHERE verification_status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countActiveAdmins() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMINISTRATOR' AND status = 'ACTIVE'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
