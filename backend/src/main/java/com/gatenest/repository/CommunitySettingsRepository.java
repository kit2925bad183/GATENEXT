package com.gatenest.repository;

import com.gatenest.model.CommunitySettings;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class CommunitySettingsRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommunitySettingsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CommunitySettings> settingsRowMapper = (rs, rowNum) -> {
        CommunitySettings s = new CommunitySettings();
        s.setId(rs.getLong("id"));
        s.setCommunityName(rs.getString("community_name"));
        s.setAddress(rs.getString("address"));
        s.setCity(rs.getString("city"));
        s.setPostalCode(rs.getString("postal_code"));
        s.setDefaultTimezone(rs.getString("default_timezone"));
        s.setContactPhone(rs.getString("contact_phone"));
        s.setContactEmail(rs.getString("contact_email"));
        s.setVisitorPassValidityHours(rs.getInt("visitor_pass_validity_hours"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        s.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        s.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);
        return s;
    };

    public Optional<CommunitySettings> getSettings() {
        String sql = "SELECT * FROM community_settings ORDER BY id ASC LIMIT 1";
        try {
            CommunitySettings s = jdbcTemplate.queryForObject(sql, settingsRowMapper);
            return Optional.ofNullable(s);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public CommunitySettings updateSettings(CommunitySettings settings) {
        String sql = "UPDATE community_settings SET community_name = ?, address = ?, city = ?, postal_code = ?, default_timezone = ?, contact_phone = ?, contact_email = ?, visitor_pass_validity_hours = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql,
                settings.getCommunityName().trim(),
                settings.getAddress().trim(),
                settings.getCity().trim(),
                settings.getPostalCode().trim(),
                settings.getDefaultTimezone().trim(),
                settings.getContactPhone(),
                settings.getContactEmail(),
                settings.getVisitorPassValidityHours(),
                settings.getId());
        return settings;
    }
}
