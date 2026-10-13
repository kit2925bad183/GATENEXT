package com.gatenest.repository;

import com.gatenest.model.Gate;
import com.gatenest.model.GateType;
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
public class GateRepository {

    private final JdbcTemplate jdbcTemplate;

    public GateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Gate> gateRowMapper = (rs, rowNum) -> {
        Gate g = new Gate();
        g.setId(rs.getLong("id"));
        g.setName(rs.getString("name"));
        g.setCode(rs.getString("code"));
        g.setDescription(rs.getString("description"));
        g.setGateType(GateType.valueOf(rs.getString("gate_type")));
        g.setActive(rs.getBoolean("is_active"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        g.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        g.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            g.setActiveGuardsCount(rs.getInt("active_guards_count"));
        } catch (Exception ignored) {}
        return g;
    };

    private static final String BASE_SELECT =
            "SELECT g.*, " +
            "(SELECT COUNT(*) FROM security_gate_assignments sga JOIN users u ON sga.user_id = u.id WHERE sga.gate_id = g.id AND sga.is_active = TRUE AND u.status = 'ACTIVE') AS active_guards_count " +
            "FROM gates g ";

    public List<Gate> findAll() {
        String sql = BASE_SELECT + "ORDER BY g.name ASC";
        return jdbcTemplate.query(sql, gateRowMapper);
    }

    public List<Gate> findActiveGates() {
        String sql = BASE_SELECT + "WHERE g.is_active = TRUE ORDER BY g.name ASC";
        return jdbcTemplate.query(sql, gateRowMapper);
    }

    public Optional<Gate> findById(Long id) {
        String sql = BASE_SELECT + "WHERE g.id = ?";
        try {
            Gate gate = jdbcTemplate.queryForObject(sql, gateRowMapper, id);
            return Optional.ofNullable(gate);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Gate> findByCode(String code) {
        String sql = BASE_SELECT + "WHERE LOWER(g.code) = LOWER(?)";
        try {
            Gate gate = jdbcTemplate.queryForObject(sql, gateRowMapper, code.trim());
            return Optional.ofNullable(gate);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Gate save(Gate gate) {
        if (gate.getId() == null) {
            String sql = "INSERT INTO gates (name, code, description, gate_type, is_active, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, gate.getName().trim());
                ps.setString(2, gate.getCode().trim().toUpperCase());
                ps.setString(3, gate.getDescription());
                ps.setString(4, gate.getGateType().name());
                ps.setBoolean(5, gate.isActive());
                return ps;
            }, keyHolder);

            Long generatedId = KeyHolderUtils.extractId(keyHolder);
            if (generatedId != null) {
                gate.setId(generatedId);
            }
            return gate;
        } else {
            String sql = "UPDATE gates SET name = ?, code = ?, description = ?, gate_type = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, gate.getName().trim(), gate.getCode().trim().toUpperCase(), gate.getDescription(), gate.getGateType().name(), gate.isActive(), gate.getId());
            return gate;
        }
    }
}
