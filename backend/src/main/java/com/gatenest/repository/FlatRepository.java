package com.gatenest.repository;

import com.gatenest.model.Flat;
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
public class FlatRepository {

    private final JdbcTemplate jdbcTemplate;

    public FlatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Flat> flatRowMapper = (rs, rowNum) -> {
        Flat f = new Flat();
        f.setId(rs.getLong("id"));
        f.setBlockId(rs.getLong("block_id"));
        f.setFlatNumber(rs.getString("flat_number"));
        f.setFloorNumber(rs.getInt("floor_number"));
        f.setExtensionNumber(rs.getString("extension_number"));
        f.setActive(rs.getBoolean("is_active"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        f.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        f.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            f.setBlockName(rs.getString("block_name"));
        } catch (Exception ignored) {}
        try {
            f.setActiveResidentsCount(rs.getInt("active_residents_count"));
        } catch (Exception ignored) {}
        return f;
    };

    private static final String BASE_SELECT =
            "SELECT f.*, b.name AS block_name, " +
            "(SELECT COUNT(*) FROM resident_profiles rp JOIN users u ON rp.user_id = u.id WHERE rp.flat_id = f.id AND rp.verification_status = 'APPROVED' AND u.status = 'ACTIVE') AS active_residents_count " +
            "FROM flats f JOIN blocks b ON f.block_id = b.id ";

    public List<Flat> findAll() {
        String sql = BASE_SELECT + "ORDER BY b.name ASC, f.floor_number ASC, f.flat_number ASC";
        return jdbcTemplate.query(sql, flatRowMapper);
    }

    public List<Flat> findByBlockId(Long blockId) {
        String sql = BASE_SELECT + "WHERE f.block_id = ? ORDER BY f.floor_number ASC, f.flat_number ASC";
        return jdbcTemplate.query(sql, flatRowMapper, blockId);
    }

    public Optional<Flat> findById(Long id) {
        String sql = BASE_SELECT + "WHERE f.id = ?";
        try {
            Flat flat = jdbcTemplate.queryForObject(sql, flatRowMapper, id);
            return Optional.ofNullable(flat);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Flat> findByBlockIdAndFlatNumber(Long blockId, String flatNumber) {
        String sql = BASE_SELECT + "WHERE f.block_id = ? AND LOWER(f.flat_number) = LOWER(?)";
        try {
            Flat flat = jdbcTemplate.queryForObject(sql, flatRowMapper, blockId, flatNumber.trim());
            return Optional.ofNullable(flat);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Flat save(Flat flat) {
        if (flat.getId() == null) {
            String sql = "INSERT INTO flats (block_id, flat_number, floor_number, extension_number, is_active, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, flat.getBlockId());
                ps.setString(2, flat.getFlatNumber().trim());
                ps.setInt(3, flat.getFloorNumber());
                ps.setString(4, flat.getExtensionNumber());
                ps.setBoolean(5, flat.isActive());
                return ps;
            }, keyHolder);

            Long generatedId = KeyHolderUtils.extractId(keyHolder);
            if (generatedId != null) {
                flat.setId(generatedId);
            }
            return flat;
        } else {
            String sql = "UPDATE flats SET block_id = ?, flat_number = ?, floor_number = ?, extension_number = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, flat.getBlockId(), flat.getFlatNumber().trim(), flat.getFloorNumber(), flat.getExtensionNumber(), flat.isActive(), flat.getId());
            return flat;
        }
    }
}
