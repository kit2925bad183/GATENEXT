package com.gatenest.repository;

import com.gatenest.model.IdempotencyRecord;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class IdempotencyRepository {

    private final JdbcTemplate jdbcTemplate;

    public IdempotencyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<IdempotencyRecord> rowMapper = (rs, rowNum) -> {
        IdempotencyRecord r = new IdempotencyRecord();
        r.setId(rs.getLong("id"));
        r.setIdempotencyKey(rs.getString("idempotency_key"));
        r.setHandlerName(rs.getString("handler_name"));
        r.setStatusCode(rs.getInt("status_code"));
        r.setResponsePayload(rs.getString("response_payload"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        r.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        return r;
    };

    public Optional<IdempotencyRecord> findByKey(String key) {
        String sql = "SELECT * FROM idempotency_records WHERE idempotency_key = ?";
        try {
            IdempotencyRecord record = jdbcTemplate.queryForObject(sql, rowMapper, key);
            return Optional.ofNullable(record);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void save(String key, String handlerName, int statusCode, String responsePayload) {
        String sql = "INSERT INTO idempotency_records (idempotency_key, handler_name, status_code, response_payload, created_at) " +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(sql, key, handlerName, statusCode, responsePayload);
    }
}
