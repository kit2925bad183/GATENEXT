package com.gatenest.repository;

import com.gatenest.model.SavedVisitorContact;
import com.gatenest.model.VisitorCategory;
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
public class SavedVisitorContactRepository {

    private final JdbcTemplate jdbcTemplate;

    public SavedVisitorContactRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SavedVisitorContact> rowMapper = (rs, rowNum) -> {
        SavedVisitorContact c = new SavedVisitorContact();
        c.setId(rs.getLong("id"));
        c.setResidentUserId(rs.getLong("resident_user_id"));
        c.setVisitorName(rs.getString("visitor_name"));
        c.setVisitorMobile(rs.getString("visitor_mobile"));
        String catStr = rs.getString("visitor_category");
        c.setVisitorCategory(catStr != null ? VisitorCategory.valueOf(catStr) : VisitorCategory.GUEST);
        c.setDefaultPurpose(rs.getString("default_purpose"));
        c.setDefaultVehicleNumber(rs.getString("default_vehicle_number"));
        c.setNotes(rs.getString("notes"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        c.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        c.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);
        return c;
    };

    public List<SavedVisitorContact> findByResidentUserId(Long residentUserId) {
        String sql = "SELECT * FROM saved_visitor_contacts WHERE resident_user_id = ? ORDER BY visitor_name ASC";
        return jdbcTemplate.query(sql, rowMapper, residentUserId);
    }

    public Optional<SavedVisitorContact> findByIdAndResidentUserId(Long id, Long residentUserId) {
        String sql = "SELECT * FROM saved_visitor_contacts WHERE id = ? AND resident_user_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id, residentUserId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<SavedVisitorContact> findById(Long id) {
        String sql = "SELECT * FROM saved_visitor_contacts WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public SavedVisitorContact save(SavedVisitorContact contact) {
        if (contact.getId() == null) {
            String sql = "INSERT INTO saved_visitor_contacts (resident_user_id, visitor_name, visitor_mobile, visitor_category, default_purpose, default_vehicle_number, notes, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, contact.getResidentUserId());
                ps.setString(2, contact.getVisitorName().trim());
                ps.setString(3, contact.getVisitorMobile().trim());
                ps.setString(4, contact.getVisitorCategory() != null ? contact.getVisitorCategory().name() : VisitorCategory.GUEST.name());
                ps.setString(5, contact.getDefaultPurpose());
                ps.setString(6, contact.getDefaultVehicleNumber());
                ps.setString(7, contact.getNotes());
                return ps;
            }, keyHolder);
            Long id = KeyHolderUtils.extractId(keyHolder);
            if (id != null) {
                contact.setId(id);
            }
        } else {
            String sql = "UPDATE saved_visitor_contacts SET visitor_name = ?, visitor_mobile = ?, visitor_category = ?, default_purpose = ?, default_vehicle_number = ?, notes = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ? AND resident_user_id = ?";
            jdbcTemplate.update(sql, contact.getVisitorName().trim(), contact.getVisitorMobile().trim(),
                    contact.getVisitorCategory() != null ? contact.getVisitorCategory().name() : VisitorCategory.GUEST.name(),
                    contact.getDefaultPurpose(), contact.getDefaultVehicleNumber(),
                    contact.getNotes(), contact.getId(), contact.getResidentUserId());
        }
        return contact;
    }

    public boolean deleteByIdAndResidentUserId(Long id, Long residentUserId) {
        String sql = "DELETE FROM saved_visitor_contacts WHERE id = ? AND resident_user_id = ?";
        return jdbcTemplate.update(sql, id, residentUserId) > 0;
    }
}
