package com.gatenest.repository;

import com.gatenest.model.RecurringVisitRule;
import com.gatenest.model.VisitorCategory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class RecurringVisitRuleRepository {

    private final JdbcTemplate jdbcTemplate;

    public RecurringVisitRuleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RecurringVisitRule> rowMapper = (rs, rowNum) -> {
        RecurringVisitRule r = new RecurringVisitRule();
        r.setId(rs.getLong("id"));
        r.setResidentUserId(rs.getLong("resident_user_id"));
        r.setDestinationFlatId(rs.getLong("destination_flat_id"));
        r.setVisitorName(rs.getString("visitor_name"));
        r.setVisitorMobile(rs.getString("visitor_mobile"));
        String catStr = rs.getString("visitor_category");
        r.setVisitorCategory(catStr != null ? VisitorCategory.valueOf(catStr) : VisitorCategory.SERVICE);
        r.setPurpose(rs.getString("purpose"));
        r.setAllowedWeekdays(rs.getString("allowed_weekdays"));
        Date fromDate = rs.getDate("valid_from_date");
        r.setValidFromDate(fromDate != null ? fromDate.toLocalDate() : null);
        Date toDate = rs.getDate("valid_to_date");
        r.setValidToDate(toDate != null ? toDate.toLocalDate() : null);
        r.setAllowedStartTime(rs.getString("allowed_start_time"));
        r.setAllowedEndTime(rs.getString("allowed_end_time"));
        r.setExpectedDurationMinutes(rs.getInt("expected_duration_minutes"));
        r.setActive(rs.getBoolean("is_active"));
        r.setNotes(rs.getString("notes"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        r.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        r.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            r.setDestinationFlatNumber(rs.getString("flat_number"));
            r.setDestinationBlockName(rs.getString("block_name"));
            r.setResidentFullName(rs.getString("resident_name"));
        } catch (Exception ignored) {}

        return r;
    };

    public List<RecurringVisitRule> findByResidentUserId(Long residentUserId) {
        String sql = "SELECT r.*, f.flat_number, b.name AS block_name, u.full_name AS resident_name " +
                "FROM recurring_visit_rules r " +
                "JOIN flats f ON r.destination_flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "JOIN users u ON r.resident_user_id = u.id " +
                "WHERE r.resident_user_id = ? ORDER BY r.created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, residentUserId);
    }

    public List<RecurringVisitRule> findAllActive() {
        String sql = "SELECT r.*, f.flat_number, b.name AS block_name, u.full_name AS resident_name " +
                "FROM recurring_visit_rules r " +
                "JOIN flats f ON r.destination_flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "JOIN users u ON r.resident_user_id = u.id " +
                "WHERE r.is_active = TRUE ORDER BY r.visitor_name ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<RecurringVisitRule> findById(Long id) {
        String sql = "SELECT r.*, f.flat_number, b.name AS block_name, u.full_name AS resident_name " +
                "FROM recurring_visit_rules r " +
                "JOIN flats f ON r.destination_flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "JOIN users u ON r.resident_user_id = u.id " +
                "WHERE r.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<RecurringVisitRule> findActiveByMobileAndFlat(String mobile, Long flatId, LocalDate date) {
        String sql = "SELECT r.*, f.flat_number, b.name AS block_name, u.full_name AS resident_name " +
                "FROM recurring_visit_rules r " +
                "JOIN flats f ON r.destination_flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "JOIN users u ON r.resident_user_id = u.id " +
                "WHERE r.visitor_mobile = ? AND r.destination_flat_id = ? AND r.is_active = TRUE " +
                "AND ? BETWEEN r.valid_from_date AND r.valid_to_date";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, mobile.trim(), flatId, Date.valueOf(date)));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public RecurringVisitRule save(RecurringVisitRule rule) {
        if (rule.getId() == null) {
            String sql = "INSERT INTO recurring_visit_rules (resident_user_id, destination_flat_id, visitor_name, visitor_mobile, visitor_category, purpose, allowed_weekdays, valid_from_date, valid_to_date, allowed_start_time, allowed_end_time, expected_duration_minutes, is_active, notes, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, rule.getResidentUserId());
                ps.setLong(2, rule.getDestinationFlatId());
                ps.setString(3, rule.getVisitorName().trim());
                ps.setString(4, rule.getVisitorMobile().trim());
                ps.setString(5, rule.getVisitorCategory() != null ? rule.getVisitorCategory().name() : VisitorCategory.SERVICE.name());
                ps.setString(6, rule.getPurpose());
                ps.setString(7, rule.getAllowedWeekdays());
                ps.setDate(8, Date.valueOf(rule.getValidFromDate()));
                ps.setDate(9, Date.valueOf(rule.getValidToDate()));
                ps.setString(10, rule.getAllowedStartTime());
                ps.setString(11, rule.getAllowedEndTime());
                ps.setInt(12, rule.getExpectedDurationMinutes());
                ps.setBoolean(13, rule.isActive());
                ps.setString(14, rule.getNotes());
                return ps;
            }, keyHolder);
            Long id = KeyHolderUtils.extractId(keyHolder);
            if (id != null) {
                rule.setId(id);
            }
        } else {
            String sql = "UPDATE recurring_visit_rules SET visitor_name = ?, visitor_mobile = ?, visitor_category = ?, purpose = ?, allowed_weekdays = ?, valid_from_date = ?, valid_to_date = ?, allowed_start_time = ?, allowed_end_time = ?, expected_duration_minutes = ?, is_active = ?, notes = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ?";
            jdbcTemplate.update(sql, rule.getVisitorName().trim(), rule.getVisitorMobile().trim(),
                    rule.getVisitorCategory() != null ? rule.getVisitorCategory().name() : VisitorCategory.SERVICE.name(),
                    rule.getPurpose(), rule.getAllowedWeekdays(),
                    Date.valueOf(rule.getValidFromDate()), Date.valueOf(rule.getValidToDate()),
                    rule.getAllowedStartTime(), rule.getAllowedEndTime(), rule.getExpectedDurationMinutes(),
                    rule.isActive(), rule.getNotes(), rule.getId());
        }
        return rule;
    }

    public boolean updateStatus(Long id, boolean active) {
        String sql = "UPDATE recurring_visit_rules SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return jdbcTemplate.update(sql, active, id) > 0;
    }
}
