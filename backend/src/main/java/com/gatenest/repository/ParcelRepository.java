package com.gatenest.repository;

import com.gatenest.model.Parcel;
import com.gatenest.model.ParcelEvent;
import com.gatenest.model.ParcelStatus;
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
public class ParcelRepository {

    private final JdbcTemplate jdbcTemplate;

    public ParcelRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Parcel> parcelRowMapper = (rs, rowNum) -> {
        Parcel p = new Parcel();
        p.setId(rs.getLong("id"));
        p.setRecipientUserId(rs.getLong("recipient_user_id"));
        p.setDestinationFlatId(rs.getLong("destination_flat_id"));
        p.setCourierName(rs.getString("courier_name"));
        p.setTrackingNumber(rs.getString("tracking_number"));
        p.setDescription(rs.getString("description"));
        p.setStorageLocation(rs.getString("storage_location"));
        p.setReceivedByUserId(rs.getLong("received_by_user_id"));
        Timestamp rxAt = rs.getTimestamp("received_at");
        p.setReceivedAt(rxAt != null ? rxAt.toInstant() : null);
        p.setStatus(ParcelStatus.valueOf(rs.getString("status")));
        p.setCollectedByName(rs.getString("collected_by_name"));
        long colUser = rs.getLong("collected_by_user_id");
        p.setCollectedByUserId(rs.wasNull() ? null : colUser);
        long verUser = rs.getLong("verified_by_user_id");
        p.setVerifiedByUserId(rs.wasNull() ? null : verUser);
        Timestamp colAt = rs.getTimestamp("collected_at");
        p.setCollectedAt(colAt != null ? colAt.toInstant() : null);
        p.setDelegateCollectorName(rs.getString("delegate_collector_name"));
        p.setDelegateNotes(rs.getString("delegate_notes"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        p.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        p.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            p.setRecipientName(rs.getString("recipient_name"));
            p.setRecipientMobile(rs.getString("recipient_mobile"));
            p.setFlatNumber(rs.getString("flat_number"));
            p.setBlockName(rs.getString("block_name"));
            p.setReceivedByName(rs.getString("received_by_name"));
            p.setVerifiedByName(rs.getString("verified_by_name"));
        } catch (Exception ignored) {}

        return p;
    };

    private final RowMapper<ParcelEvent> eventRowMapper = (rs, rowNum) -> {
        ParcelEvent e = new ParcelEvent();
        e.setId(rs.getLong("id"));
        e.setParcelId(rs.getLong("parcel_id"));
        e.setEventType(rs.getString("event_type"));
        e.setActorUserId(rs.getLong("actor_user_id"));
        e.setNotes(rs.getString("notes"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        e.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        try {
            e.setActorName(rs.getString("actor_name"));
        } catch (Exception ignored) {}
        return e;
    };

    private String baseSelectSql() {
        return "SELECT p.*, " +
                "u.full_name AS recipient_name, u.mobile AS recipient_mobile, " +
                "f.flat_number, b.name AS block_name, " +
                "r.full_name AS received_by_name, " +
                "v.full_name AS verified_by_name " +
                "FROM parcels p " +
                "JOIN users u ON p.recipient_user_id = u.id " +
                "JOIN flats f ON p.destination_flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "JOIN users r ON p.received_by_user_id = r.id " +
                "LEFT JOIN users v ON p.verified_by_user_id = v.id ";
    }

    public List<Parcel> findByRecipientUserId(Long recipientUserId) {
        String sql = baseSelectSql() + "WHERE p.recipient_user_id = ? ORDER BY p.created_at DESC";
        return jdbcTemplate.query(sql, parcelRowMapper, recipientUserId);
    }

    public List<Parcel> findAllPending() {
        String sql = baseSelectSql() + "WHERE p.status IN ('RECEIVED', 'READY_FOR_COLLECTION') ORDER BY p.received_at ASC";
        return jdbcTemplate.query(sql, parcelRowMapper);
    }

    public List<Parcel> findAllHistory(int limit, int offset) {
        String sql = baseSelectSql() + "ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, parcelRowMapper, limit, offset);
    }

    public Optional<Parcel> findById(Long id) {
        String sql = baseSelectSql() + "WHERE p.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, parcelRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Parcel save(Parcel p) {
        if (p.getId() == null) {
            String sql = "INSERT INTO parcels (recipient_user_id, destination_flat_id, courier_name, tracking_number, description, storage_location, received_by_user_id, received_at, status, delegate_collector_name, delegate_notes, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, p.getRecipientUserId());
                ps.setLong(2, p.getDestinationFlatId());
                ps.setString(3, p.getCourierName().trim());
                ps.setString(4, p.getTrackingNumber());
                ps.setString(5, p.getDescription());
                ps.setString(6, p.getStorageLocation() != null ? p.getStorageLocation() : "Gate Security Room");
                ps.setLong(7, p.getReceivedByUserId());
                ps.setString(8, p.getStatus() != null ? p.getStatus().name() : ParcelStatus.READY_FOR_COLLECTION.name());
                ps.setString(9, p.getDelegateCollectorName());
                ps.setString(10, p.getDelegateNotes());
                return ps;
            }, keyHolder);
            Long id = KeyHolderUtils.extractId(keyHolder);
            if (id != null) {
                p.setId(id);
            }
        } else {
            String sql = "UPDATE parcels SET courier_name = ?, tracking_number = ?, description = ?, storage_location = ?, status = ?, collected_by_name = ?, collected_by_user_id = ?, verified_by_user_id = ?, collected_at = ?, delegate_collector_name = ?, delegate_notes = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, p.getCourierName().trim(), p.getTrackingNumber(), p.getDescription(),
                    p.getStorageLocation(), p.getStatus().name(), p.getCollectedByName(), p.getCollectedByUserId(),
                    p.getVerifiedByUserId(),
                    p.getCollectedAt() != null ? Timestamp.from(p.getCollectedAt()) : null,
                    p.getDelegateCollectorName(), p.getDelegateNotes(), p.getId());
        }
        return p;
    }

    public boolean updateDelegate(Long parcelId, Long recipientUserId, String delegateName, String notes) {
        String sql = "UPDATE parcels SET delegate_collector_name = ?, delegate_notes = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND recipient_user_id = ?";
        return jdbcTemplate.update(sql, delegateName != null ? delegateName.trim() : null, notes, parcelId, recipientUserId) > 0;
    }

    public boolean markCollectedAtomic(Long parcelId, String collectorName, Long collectorUserId, Long verifierUserId) {
        String sql = "UPDATE parcels SET status = 'COLLECTED', collected_by_name = ?, collected_by_user_id = ?, verified_by_user_id = ?, collected_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND status IN ('RECEIVED', 'READY_FOR_COLLECTION')";
        return jdbcTemplate.update(sql, collectorName.trim(), collectorUserId, verifierUserId, parcelId) > 0;
    }

    public void recordEvent(Long parcelId, String eventType, Long actorUserId, String notes) {
        String sql = "INSERT INTO parcel_events (parcel_id, event_type, actor_user_id, notes, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(sql, parcelId, eventType, actorUserId, notes);
    }

    public List<ParcelEvent> findEventsByParcelId(Long parcelId) {
        String sql = "SELECT pe.*, u.full_name AS actor_name FROM parcel_events pe " +
                "JOIN users u ON pe.actor_user_id = u.id " +
                "WHERE pe.parcel_id = ? ORDER BY pe.created_at ASC";
        return jdbcTemplate.query(sql, eventRowMapper, parcelId);
    }

    public int countPendingForResident(Long residentUserId) {
        String sql = "SELECT COUNT(*) FROM parcels WHERE recipient_user_id = ? AND status IN ('RECEIVED', 'READY_FOR_COLLECTION')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, residentUserId);
        return count != null ? count : 0;
    }

    public int countPendingTotal() {
        String sql = "SELECT COUNT(*) FROM parcels WHERE status IN ('RECEIVED', 'READY_FOR_COLLECTION')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
