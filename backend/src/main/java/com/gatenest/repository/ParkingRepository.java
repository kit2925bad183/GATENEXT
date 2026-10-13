package com.gatenest.repository;

import com.gatenest.model.ParkingAssignment;
import com.gatenest.model.ParkingSlot;
import com.gatenest.model.Vehicle;
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
public class ParkingRepository {

    private final JdbcTemplate jdbcTemplate;

    public ParkingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ParkingSlot> slotRowMapper = (rs, rowNum) -> {
        ParkingSlot s = new ParkingSlot();
        s.setId(rs.getLong("id"));
        s.setSlotNumber(rs.getString("slot_number"));
        s.setSlotType(rs.getString("slot_type"));
        long blockId = rs.getLong("block_id");
        s.setBlockId(rs.wasNull() ? null : blockId);
        s.setFloorLevel(rs.getString("floor_level"));
        s.setReservedForResidents(rs.getBoolean("is_reserved_for_residents"));
        s.setActive(rs.getBoolean("is_active"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        s.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        s.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            s.setBlockName(rs.getString("block_name"));
            s.setOccupied(rs.getInt("occupied_count") > 0);
            long assignId = rs.getLong("active_assignment_id");
            s.setCurrentAssignmentId(rs.wasNull() ? null : assignId);
            s.setOccupiedByVehicleNumber(rs.getString("assigned_vehicle_number"));
            s.setOccupiedByResidentName(rs.getString("assigned_resident_name"));
            s.setOccupiedByVisitorName(rs.getString("assigned_visitor_name"));
            s.setCurrentVisitReference(rs.getString("assigned_visit_reference"));
        } catch (Exception ignored) {}

        return s;
    };

    private final RowMapper<Vehicle> vehicleRowMapper = (rs, rowNum) -> {
        Vehicle v = new Vehicle();
        v.setId(rs.getLong("id"));
        v.setOwnerUserId(rs.getLong("owner_user_id"));
        v.setFlatId(rs.getLong("flat_id"));
        v.setVehicleNumber(rs.getString("vehicle_number"));
        v.setVehicleType(rs.getString("vehicle_type"));
        v.setMakeModel(rs.getString("make_model"));
        v.setColor(rs.getString("color"));
        v.setStickerNumber(rs.getString("sticker_number"));
        v.setActive(rs.getBoolean("is_active"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        v.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        v.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            v.setOwnerName(rs.getString("owner_name"));
            v.setFlatNumber(rs.getString("flat_number"));
            v.setBlockName(rs.getString("block_name"));
            v.setAssignedSlotNumber(rs.getString("assigned_slot_number"));
        } catch (Exception ignored) {}

        return v;
    };

    private final RowMapper<ParkingAssignment> assignmentRowMapper = (rs, rowNum) -> {
        ParkingAssignment a = new ParkingAssignment();
        a.setId(rs.getLong("id"));
        a.setParkingSlotId(rs.getLong("parking_slot_id"));
        long vId = rs.getLong("vehicle_id");
        a.setVehicleId(rs.wasNull() ? null : vId);
        long visId = rs.getLong("visit_id");
        a.setVisitId(rs.wasNull() ? null : visId);
        long rId = rs.getLong("resident_user_id");
        a.setResidentUserId(rs.wasNull() ? null : rId);
        a.setAssignedByUserId(rs.getLong("assigned_by_user_id"));
        Timestamp aAt = rs.getTimestamp("assigned_at");
        a.setAssignedAt(aAt != null ? aAt.toInstant() : null);
        Timestamp rAt = rs.getTimestamp("released_at");
        a.setReleasedAt(rAt != null ? rAt.toInstant() : null);
        a.setActive(rs.getBoolean("is_active"));
        a.setNotes(rs.getString("notes"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        a.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        a.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            a.setSlotNumber(rs.getString("slot_number"));
            a.setSlotType(rs.getString("slot_type"));
            a.setVehicleNumber(rs.getString("vehicle_number"));
            a.setVisitReference(rs.getString("visit_reference"));
            a.setVisitorName(rs.getString("visitor_name"));
            a.setResidentName(rs.getString("resident_name"));
            a.setAssignedByName(rs.getString("assigned_by_name"));
        } catch (Exception ignored) {}

        return a;
    };

    // Slots
    public List<ParkingSlot> findAllSlots() {
        String sql = "SELECT ps.*, b.name AS block_name, " +
                "(SELECT COUNT(*) FROM parking_assignments pa WHERE pa.parking_slot_id = ps.id AND pa.is_active = TRUE) AS occupied_count, " +
                "(SELECT pa.id FROM parking_assignments pa WHERE pa.parking_slot_id = ps.id AND pa.is_active = TRUE LIMIT 1) AS active_assignment_id, " +
                "(SELECT COALESCE(v.vehicle_number, vis.vehicle_number) FROM parking_assignments pa " +
                " LEFT JOIN vehicles v ON pa.vehicle_id = v.id " +
                " LEFT JOIN visits vis ON pa.visit_id = vis.id " +
                " WHERE pa.parking_slot_id = ps.id AND pa.is_active = TRUE LIMIT 1) AS assigned_vehicle_number, " +
                "(SELECT u.full_name FROM parking_assignments pa " +
                " JOIN users u ON pa.resident_user_id = u.id " +
                " WHERE pa.parking_slot_id = ps.id AND pa.is_active = TRUE LIMIT 1) AS assigned_resident_name, " +
                "(SELECT vis.visitor_name FROM parking_assignments pa " +
                " JOIN visits vis ON pa.visit_id = vis.id " +
                " WHERE pa.parking_slot_id = ps.id AND pa.is_active = TRUE LIMIT 1) AS assigned_visitor_name, " +
                "(SELECT vis.visit_reference FROM parking_assignments pa " +
                " JOIN visits vis ON pa.visit_id = vis.id " +
                " WHERE pa.parking_slot_id = ps.id AND pa.is_active = TRUE LIMIT 1) AS assigned_visit_reference " +
                "FROM parking_slots ps " +
                "LEFT JOIN blocks b ON ps.block_id = b.id " +
                "WHERE ps.is_active = TRUE ORDER BY ps.slot_number ASC";
        return jdbcTemplate.query(sql, slotRowMapper);
    }

    public List<ParkingSlot> findAvailableVisitorSlots() {
        String sql = "SELECT ps.*, b.name AS block_name, 0 AS occupied_count, " +
                "NULL AS active_assignment_id, NULL AS assigned_vehicle_number, NULL AS assigned_resident_name, " +
                "NULL AS assigned_visitor_name, NULL AS assigned_visit_reference " +
                "FROM parking_slots ps " +
                "LEFT JOIN blocks b ON ps.block_id = b.id " +
                "WHERE ps.is_active = TRUE AND ps.is_reserved_for_residents = FALSE " +
                "AND ps.id NOT IN (SELECT pa.parking_slot_id FROM parking_assignments pa WHERE pa.is_active = TRUE) " +
                "ORDER BY ps.slot_number ASC";
        return jdbcTemplate.query(sql, slotRowMapper);
    }

    public Optional<ParkingSlot> findSlotById(Long id) {
        String sql = "SELECT ps.*, b.name AS block_name, 0 AS occupied_count, " +
                "NULL AS active_assignment_id, NULL AS assigned_vehicle_number, NULL AS assigned_resident_name, " +
                "NULL AS assigned_visitor_name, NULL AS assigned_visit_reference " +
                "FROM parking_slots ps " +
                "LEFT JOIN blocks b ON ps.block_id = b.id WHERE ps.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, slotRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public ParkingSlot saveSlot(ParkingSlot slot) {
        if (slot.getId() == null) {
            String sql = "INSERT INTO parking_slots (slot_number, slot_type, block_id, floor_level, is_reserved_for_residents, is_active, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, slot.getSlotNumber().trim().toUpperCase());
                ps.setString(2, slot.getSlotType());
                if (slot.getBlockId() != null) ps.setLong(3, slot.getBlockId()); else ps.setNull(3, java.sql.Types.BIGINT);
                ps.setString(4, slot.getFloorLevel());
                ps.setBoolean(5, slot.isReservedForResidents());
                ps.setBoolean(6, slot.isActive());
                return ps;
            }, keyHolder);
            Long id = KeyHolderUtils.extractId(keyHolder);
            if (id != null) {
                slot.setId(id);
            }
        } else {
            String sql = "UPDATE parking_slots SET slot_number = ?, slot_type = ?, block_id = ?, floor_level = ?, is_reserved_for_residents = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, slot.getSlotNumber().trim().toUpperCase(), slot.getSlotType(),
                    slot.getBlockId(), slot.getFloorLevel(), slot.isReservedForResidents(), slot.isActive(), slot.getId());
        }
        return slot;
    }

    // Vehicles
    public List<Vehicle> findVehiclesByOwner(Long ownerUserId) {
        String sql = "SELECT v.*, u.full_name AS owner_name, f.flat_number, b.name AS block_name, " +
                "(SELECT ps.slot_number FROM parking_assignments pa JOIN parking_slots ps ON pa.parking_slot_id = ps.id WHERE pa.vehicle_id = v.id AND pa.is_active = TRUE LIMIT 1) AS assigned_slot_number " +
                "FROM vehicles v " +
                "JOIN users u ON v.owner_user_id = u.id " +
                "JOIN flats f ON v.flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "WHERE v.owner_user_id = ? AND v.is_active = TRUE ORDER BY v.created_at DESC";
        return jdbcTemplate.query(sql, vehicleRowMapper, ownerUserId);
    }

    public List<Vehicle> findAllVehicles() {
        String sql = "SELECT v.*, u.full_name AS owner_name, f.flat_number, b.name AS block_name, " +
                "(SELECT ps.slot_number FROM parking_assignments pa JOIN parking_slots ps ON pa.parking_slot_id = ps.id WHERE pa.vehicle_id = v.id AND pa.is_active = TRUE LIMIT 1) AS assigned_slot_number " +
                "FROM vehicles v " +
                "JOIN users u ON v.owner_user_id = u.id " +
                "JOIN flats f ON v.flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "WHERE v.is_active = TRUE ORDER BY v.vehicle_number ASC";
        return jdbcTemplate.query(sql, vehicleRowMapper);
    }

    public Optional<Vehicle> findVehicleById(Long id) {
        String sql = "SELECT v.*, u.full_name AS owner_name, f.flat_number, b.name AS block_name, NULL AS assigned_slot_number " +
                "FROM vehicles v " +
                "JOIN users u ON v.owner_user_id = u.id " +
                "JOIN flats f ON v.flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "WHERE v.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, vehicleRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Vehicle saveVehicle(Vehicle v) {
        if (v.getId() == null) {
            String sql = "INSERT INTO vehicles (owner_user_id, flat_id, vehicle_number, vehicle_type, make_model, color, sticker_number, is_active, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, v.getOwnerUserId());
                ps.setLong(2, v.getFlatId());
                ps.setString(3, v.getVehicleNumber().trim().toUpperCase());
                ps.setString(4, v.getVehicleType());
                ps.setString(5, v.getMakeModel());
                ps.setString(6, v.getColor());
                ps.setString(7, v.getStickerNumber());
                return ps;
            }, keyHolder);
            Long id = KeyHolderUtils.extractId(keyHolder);
            if (id != null) {
                v.setId(id);
            }
        } else {
            String sql = "UPDATE vehicles SET vehicle_number = ?, vehicle_type = ?, make_model = ?, color = ?, sticker_number = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, v.getVehicleNumber().trim().toUpperCase(), v.getVehicleType(),
                    v.getMakeModel(), v.getColor(), v.getStickerNumber(), v.isActive(), v.getId());
        }
        return v;
    }

    // Parking assignments
    public Optional<ParkingAssignment> findActiveAssignmentForSlot(Long slotId) {
        String sql = "SELECT pa.*, ps.slot_number, ps.slot_type, v.vehicle_number, " +
                "vis.visit_reference, vis.visitor_name, u.full_name AS resident_name, " +
                "a.full_name AS assigned_by_name " +
                "FROM parking_assignments pa " +
                "JOIN parking_slots ps ON pa.parking_slot_id = ps.id " +
                "LEFT JOIN vehicles v ON pa.vehicle_id = v.id " +
                "LEFT JOIN visits vis ON pa.visit_id = vis.id " +
                "LEFT JOIN users u ON pa.resident_user_id = u.id " +
                "JOIN users a ON pa.assigned_by_user_id = a.id " +
                "WHERE pa.parking_slot_id = ? AND pa.is_active = TRUE";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, assignmentRowMapper, slotId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<ParkingAssignment> findActiveAssignmentForVisit(Long visitId) {
        String sql = "SELECT pa.*, ps.slot_number, ps.slot_type, v.vehicle_number, " +
                "vis.visit_reference, vis.visitor_name, u.full_name AS resident_name, " +
                "a.full_name AS assigned_by_name " +
                "FROM parking_assignments pa " +
                "JOIN parking_slots ps ON pa.parking_slot_id = ps.id " +
                "LEFT JOIN vehicles v ON pa.vehicle_id = v.id " +
                "LEFT JOIN visits vis ON pa.visit_id = vis.id " +
                "LEFT JOIN users u ON pa.resident_user_id = u.id " +
                "JOIN users a ON pa.assigned_by_user_id = a.id " +
                "WHERE pa.visit_id = ? AND pa.is_active = TRUE";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, assignmentRowMapper, visitId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public ParkingAssignment assignSlot(Long slotId, Long vehicleId, Long visitId, Long residentUserId, Long assignedByUserId, String notes) {
        String sql = "INSERT INTO parking_assignments (parking_slot_id, vehicle_id, visit_id, resident_user_id, assigned_by_user_id, assigned_at, is_active, notes, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, TRUE, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, slotId);
            if (vehicleId != null) ps.setLong(2, vehicleId); else ps.setNull(2, java.sql.Types.BIGINT);
            if (visitId != null) ps.setLong(3, visitId); else ps.setNull(3, java.sql.Types.BIGINT);
            if (residentUserId != null) ps.setLong(4, residentUserId); else ps.setNull(4, java.sql.Types.BIGINT);
            ps.setLong(5, assignedByUserId);
            ps.setString(6, notes);
            return ps;
        }, keyHolder);
        Long id = KeyHolderUtils.extractId(keyHolder);

        ParkingAssignment pa = new ParkingAssignment();
        pa.setId(id);
        pa.setParkingSlotId(slotId);
        pa.setVehicleId(vehicleId);
        pa.setVisitId(visitId);
        pa.setResidentUserId(residentUserId);
        pa.setAssignedByUserId(assignedByUserId);
        pa.setActive(true);
        pa.setNotes(notes);
        return pa;
    }

    public boolean releaseAssignment(Long assignmentId) {
        String sql = "UPDATE parking_assignments SET is_active = FALSE, released_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND is_active = TRUE";
        return jdbcTemplate.update(sql, assignmentId) > 0;
    }

    public boolean releaseAssignmentByVisitId(Long visitId) {
        String sql = "UPDATE parking_assignments SET is_active = FALSE, released_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE visit_id = ? AND is_active = TRUE";
        return jdbcTemplate.update(sql, visitId) > 0;
    }
}
