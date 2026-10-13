package com.gatenest.repository;

import com.gatenest.model.Block;
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
public class BlockRepository {

    private final JdbcTemplate jdbcTemplate;

    public BlockRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Block> blockRowMapper = (rs, rowNum) -> {
        Block b = new Block();
        b.setId(rs.getLong("id"));
        b.setName(rs.getString("name"));
        b.setDescription(rs.getString("description"));
        b.setTotalFloors(rs.getInt("total_floors"));
        b.setActive(rs.getBoolean("is_active"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        b.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        b.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);
        
        try {
            b.setTotalFlats(rs.getInt("total_flats"));
        } catch (Exception ignored) {}
        return b;
    };

    public List<Block> findAll() {
        String sql = "SELECT b.*, COUNT(f.id) AS total_flats FROM blocks b LEFT JOIN flats f ON b.id = f.block_id AND f.is_active = TRUE GROUP BY b.id ORDER BY b.name ASC";
        return jdbcTemplate.query(sql, blockRowMapper);
    }

    public Optional<Block> findById(Long id) {
        String sql = "SELECT b.*, COUNT(f.id) AS total_flats FROM blocks b LEFT JOIN flats f ON b.id = f.block_id AND f.is_active = TRUE WHERE b.id = ? GROUP BY b.id";
        try {
            Block block = jdbcTemplate.queryForObject(sql, blockRowMapper, id);
            return Optional.ofNullable(block);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Block> findByName(String name) {
        String sql = "SELECT b.*, 0 AS total_flats FROM blocks b WHERE LOWER(name) = LOWER(?)";
        try {
            Block block = jdbcTemplate.queryForObject(sql, blockRowMapper, name.trim());
            return Optional.ofNullable(block);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Block save(Block block) {
        if (block.getId() == null) {
            String sql = "INSERT INTO blocks (name, description, total_floors, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, block.getName().trim());
                ps.setString(2, block.getDescription());
                ps.setInt(3, block.getTotalFloors());
                ps.setBoolean(4, block.isActive());
                return ps;
            }, keyHolder);

            Long generatedId = KeyHolderUtils.extractId(keyHolder);
            if (generatedId != null) {
                block.setId(generatedId);
            }
            return block;
        } else {
            String sql = "UPDATE blocks SET name = ?, description = ?, total_floors = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, block.getName().trim(), block.getDescription(), block.getTotalFloors(), block.isActive(), block.getId());
            return block;
        }
    }
}
