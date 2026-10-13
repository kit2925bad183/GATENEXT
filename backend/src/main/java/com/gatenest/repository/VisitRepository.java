package com.gatenest.repository;

import com.gatenest.model.Visit;
import com.gatenest.model.VisitStatus;
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
import java.time.Instant;
import java.util.*;

@Repository
public class VisitRepository {

    private final JdbcTemplate jdbcTemplate;

    public VisitRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "created_at", "arrival_window_start", "arrival_window_end",
            "check_in_time", "check_out_time", "visitor_name", "visit_reference", "status"
    );

    private final RowMapper<Visit> visitRowMapper = (rs, rowNum) -> {
        Visit v = new Visit();
        v.setId(rs.getLong("id"));
        v.setVisitReference(rs.getString("visit_reference"));
        v.setHostUserId(rs.getLong("host_user_id"));
        v.setDestinationFlatId(rs.getLong("destination_flat_id"));

        v.setDestinationBlockName(rs.getString("destination_block_name"));
        v.setDestinationFlatNumber(rs.getString("destination_flat_number"));
        v.setHostNameSnapshot(rs.getString("host_name_snapshot"));
        v.setHostMobileSnapshot(rs.getString("host_mobile_snapshot"));

        v.setVisitorName(rs.getString("visitor_name"));
        v.setVisitorMobile(rs.getString("visitor_mobile"));
        v.setVisitorCategory(VisitorCategory.valueOf(rs.getString("visitor_category")));
        v.setPurpose(rs.getString("purpose"));
        v.setVehicleNumber(rs.getString("vehicle_number"));
        v.setNotes(rs.getString("notes"));

        Timestamp winStart = rs.getTimestamp("arrival_window_start");
        v.setArrivalWindowStart(winStart != null ? winStart.toInstant() : null);
        Timestamp winEnd = rs.getTimestamp("arrival_window_end");
        v.setArrivalWindowEnd(winEnd != null ? winEnd.toInstant() : null);
        v.setExpectedDurationMinutes(rs.getInt("expected_duration_minutes"));
        v.setStatus(VisitStatus.valueOf(rs.getString("status")));

        v.setPassCredentialHash(rs.getString("pass_credential_hash"));
        Timestamp passGen = rs.getTimestamp("pass_generated_at");
        v.setPassGeneratedAt(passGen != null ? passGen.toInstant() : null);

        long entryGate = rs.getLong("entry_gate_id");
        v.setEntryGateId(rs.wasNull() ? null : entryGate);
        long exitGate = rs.getLong("exit_gate_id");
        v.setExitGateId(rs.wasNull() ? null : exitGate);

        Timestamp checkIn = rs.getTimestamp("check_in_time");
        v.setCheckInTime(checkIn != null ? checkIn.toInstant() : null);
        Timestamp checkOut = rs.getTimestamp("check_out_time");
        v.setCheckOutTime(checkOut != null ? checkOut.toInstant() : null);

        v.setRegisteredByUserId(rs.getLong("registered_by_user_id"));
        long approver = rs.getLong("approved_by_user_id");
        v.setApprovedByUserId(rs.wasNull() ? null : approver);
        Timestamp approvedAt = rs.getTimestamp("approved_at");
        v.setApprovedAt(approvedAt != null ? approvedAt.toInstant() : null);

        v.setRejectionReason(rs.getString("rejection_reason"));
        v.setAdminOverrideReason(rs.getString("admin_override_reason"));
        long overrideBy = rs.getLong("admin_override_by_user_id");
        v.setAdminOverrideByUserId(rs.wasNull() ? null : overrideBy);

        Timestamp createdAt = rs.getTimestamp("created_at");
        v.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        v.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        // Joined fields
        try {
            v.setEntryGateName(rs.getString("entry_gate_name"));
        } catch (Exception ignored) {}
        try {
            v.setExitGateName(rs.getString("exit_gate_name"));
        } catch (Exception ignored) {}
        try {
            v.setRegisteredByUserName(rs.getString("registered_by_user_name"));
        } catch (Exception ignored) {}
        try {
            v.setApprovedByUserName(rs.getString("approved_by_user_name"));
        } catch (Exception ignored) {}

        // Overstay calculation
        if (v.getStatus() == VisitStatus.INSIDE && v.getCheckInTime() != null) {
            Instant expectedExit = v.getCheckInTime().plusSeconds((long) v.getExpectedDurationMinutes() * 60);
            v.setOverstay(Instant.now().isAfter(expectedExit));
        }

        return v;
    };

    private static final String BASE_SELECT =
            "SELECT v.*, " +
            "eg.name AS entry_gate_name, " +
            "xg.name AS exit_gate_name, " +
            "ru.full_name AS registered_by_user_name, " +
            "au.full_name AS approved_by_user_name " +
            "FROM visits v " +
            "LEFT JOIN gates eg ON v.entry_gate_id = eg.id " +
            "LEFT JOIN gates xg ON v.exit_gate_id = xg.id " +
            "LEFT JOIN users ru ON v.registered_by_user_id = ru.id " +
            "LEFT JOIN users au ON v.approved_by_user_id = au.id ";

    public Optional<Visit> findById(Long id) {
        String sql = BASE_SELECT + "WHERE v.id = ?";
        try {
            Visit visit = jdbcTemplate.queryForObject(sql, visitRowMapper, id);
            return Optional.ofNullable(visit);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Visit> findByReference(String reference) {
        String sql = BASE_SELECT + "WHERE UPPER(v.visit_reference) = UPPER(?)";
        try {
            Visit visit = jdbcTemplate.queryForObject(sql, visitRowMapper, reference.trim());
            return Optional.ofNullable(visit);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Visit> findByCredentialHash(String hash) {
        String sql = BASE_SELECT + "WHERE v.pass_credential_hash = ?";
        try {
            Visit visit = jdbcTemplate.queryForObject(sql, visitRowMapper, hash.trim());
            return Optional.ofNullable(visit);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Visit save(Visit visit) {
        if (visit.getId() == null) {
            String sql = "INSERT INTO visits (" +
                    "visit_reference, host_user_id, destination_flat_id, destination_block_name, destination_flat_number, " +
                    "host_name_snapshot, host_mobile_snapshot, visitor_name, visitor_mobile, visitor_category, purpose, " +
                    "vehicle_number, notes, arrival_window_start, arrival_window_end, expected_duration_minutes, status, " +
                    "pass_credential_hash, pass_generated_at, registered_by_user_id, approved_by_user_id, approved_at, " +
                    "created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, visit.getVisitReference());
                ps.setLong(2, visit.getHostUserId());
                ps.setLong(3, visit.getDestinationFlatId());
                ps.setString(4, visit.getDestinationBlockName());
                ps.setString(5, visit.getDestinationFlatNumber());
                ps.setString(6, visit.getHostNameSnapshot());
                ps.setString(7, visit.getHostMobileSnapshot());
                ps.setString(8, visit.getVisitorName());
                ps.setString(9, visit.getVisitorMobile());
                ps.setString(10, visit.getVisitorCategory().name());
                ps.setString(11, visit.getPurpose());
                ps.setString(12, visit.getVehicleNumber());
                ps.setString(13, visit.getNotes());
                ps.setTimestamp(14, Timestamp.from(visit.getArrivalWindowStart()));
                ps.setTimestamp(15, Timestamp.from(visit.getArrivalWindowEnd()));
                ps.setInt(16, visit.getExpectedDurationMinutes());
                ps.setString(17, visit.getStatus().name());
                ps.setString(18, visit.getPassCredentialHash());
                ps.setTimestamp(19, visit.getPassGeneratedAt() != null ? Timestamp.from(visit.getPassGeneratedAt()) : null);
                ps.setLong(20, visit.getRegisteredByUserId());
                if (visit.getApprovedByUserId() != null) {
                    ps.setLong(21, visit.getApprovedByUserId());
                } else {
                    ps.setNull(21, java.sql.Types.BIGINT);
                }
                ps.setTimestamp(22, visit.getApprovedAt() != null ? Timestamp.from(visit.getApprovedAt()) : null);
                return ps;
            }, keyHolder);

            Long generatedId = KeyHolderUtils.extractId(keyHolder);
            if (generatedId != null) {
                visit.setId(generatedId);
            }
            return visit;
        } else {
            String sql = "UPDATE visits SET " +
                    "destination_flat_id = ?, destination_block_name = ?, destination_flat_number = ?, " +
                    "host_name_snapshot = ?, host_mobile_snapshot = ?, visitor_name = ?, visitor_mobile = ?, " +
                    "visitor_category = ?, purpose = ?, vehicle_number = ?, notes = ?, " +
                    "arrival_window_start = ?, arrival_window_end = ?, expected_duration_minutes = ?, status = ?, " +
                    "pass_credential_hash = ?, pass_generated_at = ?, entry_gate_id = ?, exit_gate_id = ?, " +
                    "check_in_time = ?, check_out_time = ?, approved_by_user_id = ?, approved_at = ?, " +
                    "rejection_reason = ?, admin_override_reason = ?, admin_override_by_user_id = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ?";

            jdbcTemplate.update(sql,
                    visit.getDestinationFlatId(),
                    visit.getDestinationBlockName(),
                    visit.getDestinationFlatNumber(),
                    visit.getHostNameSnapshot(),
                    visit.getHostMobileSnapshot(),
                    visit.getVisitorName(),
                    visit.getVisitorMobile(),
                    visit.getVisitorCategory().name(),
                    visit.getPurpose(),
                    visit.getVehicleNumber(),
                    visit.getNotes(),
                    Timestamp.from(visit.getArrivalWindowStart()),
                    Timestamp.from(visit.getArrivalWindowEnd()),
                    visit.getExpectedDurationMinutes(),
                    visit.getStatus().name(),
                    visit.getPassCredentialHash(),
                    visit.getPassGeneratedAt() != null ? Timestamp.from(visit.getPassGeneratedAt()) : null,
                    visit.getEntryGateId(),
                    visit.getExitGateId(),
                    visit.getCheckInTime() != null ? Timestamp.from(visit.getCheckInTime()) : null,
                    visit.getCheckOutTime() != null ? Timestamp.from(visit.getCheckOutTime()) : null,
                    visit.getApprovedByUserId(),
                    visit.getApprovedAt() != null ? Timestamp.from(visit.getApprovedAt()) : null,
                    visit.getRejectionReason(),
                    visit.getAdminOverrideReason(),
                    visit.getAdminOverrideByUserId(),
                    visit.getId());
            return visit;
        }
    }

    public List<Visit> searchVisits(
            Long hostUserId,
            VisitStatus status,
            VisitorCategory category,
            Long gateId,
            String query,
            Instant fromDate,
            Instant toDate,
            String sortBy,
            String sortDirection,
            int offset,
            int limit
    ) {
        return searchVisitsAdvanced(
                hostUserId, null, null, null,
                gateId, null, null,
                status, category, null, null,
                query, com.gatenest.dto.DateBasis.SCHEDULED_ARRIVAL,
                fromDate, toDate,
                sortBy, sortDirection, offset, limit
        );
    }

    public List<Visit> searchVisitsAdvanced(
            Long hostUserId,
            Long blockId,
            Integer floorNumber,
            Long flatId,
            Long entryGateId,
            Long exitGateId,
            Long securityStaffId,
            VisitStatus status,
            VisitorCategory category,
            Boolean isWalkIn,
            Boolean isOverdue,
            String query,
            com.gatenest.dto.DateBasis dateBasis,
            Instant fromDate,
            Instant toDate,
            String sortBy,
            String sortDirection,
            int offset,
            int limit
    ) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("LEFT JOIN flats f ON v.destination_flat_id = f.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        buildFilterWhereClause(sql, params, hostUserId, blockId, floorNumber, flatId,
                entryGateId, exitGateId, securityStaffId, status, category, isWalkIn, isOverdue, query, dateBasis, fromDate, toDate);

        String validSortBy = (sortBy != null && ALLOWED_SORT_FIELDS.contains(sortBy.toLowerCase())) ? sortBy.toLowerCase() : "id";
        String validDirection = ("ASC".equalsIgnoreCase(sortDirection)) ? "ASC" : "DESC";

        sql.append(" ORDER BY v.").append(validSortBy).append(" ").append(validDirection);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), visitRowMapper, params.toArray());
    }

    public int countSearchVisits(
            Long hostUserId,
            VisitStatus status,
            VisitorCategory category,
            Long gateId,
            String query,
            Instant fromDate,
            Instant toDate
    ) {
        return countSearchVisitsAdvanced(
                hostUserId, null, null, null,
                gateId, null, null,
                status, category, null, null,
                query, com.gatenest.dto.DateBasis.SCHEDULED_ARRIVAL,
                fromDate, toDate
        );
    }

    public int countSearchVisitsAdvanced(
            Long hostUserId,
            Long blockId,
            Integer floorNumber,
            Long flatId,
            Long entryGateId,
            Long exitGateId,
            Long securityStaffId,
            VisitStatus status,
            VisitorCategory category,
            Boolean isWalkIn,
            Boolean isOverdue,
            String query,
            com.gatenest.dto.DateBasis dateBasis,
            Instant fromDate,
            Instant toDate
    ) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM visits v LEFT JOIN flats f ON v.destination_flat_id = f.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        buildFilterWhereClause(sql, params, hostUserId, blockId, floorNumber, flatId,
                entryGateId, exitGateId, securityStaffId, status, category, isWalkIn, isOverdue, query, dateBasis, fromDate, toDate);

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    private void buildFilterWhereClause(
            StringBuilder sql,
            List<Object> params,
            Long hostUserId,
            Long blockId,
            Integer floorNumber,
            Long flatId,
            Long entryGateId,
            Long exitGateId,
            Long securityStaffId,
            VisitStatus status,
            VisitorCategory category,
            Boolean isWalkIn,
            Boolean isOverdue,
            String query,
            com.gatenest.dto.DateBasis dateBasis,
            Instant fromDate,
            Instant toDate
    ) {
        if (hostUserId != null) {
            sql.append(" AND v.host_user_id = ?");
            params.add(hostUserId);
        }
        if (blockId != null) {
            sql.append(" AND (f.block_id = ? OR v.destination_block_name = (SELECT name FROM blocks WHERE id = ?))");
            params.add(blockId);
            params.add(blockId);
        }
        if (floorNumber != null) {
            sql.append(" AND f.floor_number = ?");
            params.add(floorNumber);
        }
        if (flatId != null) {
            sql.append(" AND v.destination_flat_id = ?");
            params.add(flatId);
        }
        if (entryGateId != null) {
            sql.append(" AND v.entry_gate_id = ?");
            params.add(entryGateId);
        }
        if (exitGateId != null) {
            sql.append(" AND v.exit_gate_id = ?");
            params.add(exitGateId);
        }
        if (securityStaffId != null) {
            sql.append(" AND (v.registered_by_user_id = ? OR v.approved_by_user_id = ?)");
            params.add(securityStaffId);
            params.add(securityStaffId);
        }
        if (status != null) {
            sql.append(" AND v.status = ?");
            params.add(status.name());
        }
        if (category != null) {
            sql.append(" AND v.visitor_category = ?");
            params.add(category.name());
        }
        if (isWalkIn != null) {
            if (isWalkIn) {
                sql.append(" AND v.registered_by_user_id != v.host_user_id");
            } else {
                sql.append(" AND v.registered_by_user_id = v.host_user_id");
            }
        }
        if (Boolean.TRUE.equals(isOverdue)) {
            sql.append(" AND v.status = 'INSIDE' AND v.check_in_time IS NOT NULL AND v.arrival_window_end < CURRENT_TIMESTAMP");
        }

        // Date Basis handling
        String dateColumn = "v.check_in_time";
        if (dateBasis != null) {
            switch (dateBasis) {
                case SCHEDULED_ARRIVAL -> dateColumn = "v.arrival_window_start";
                case ACTUAL_CHECK_OUT -> dateColumn = "v.check_out_time";
                case RECORD_CREATION -> dateColumn = "v.created_at";
                case ACTUAL_CHECK_IN -> dateColumn = "v.check_in_time";
            }
        } else {
            dateColumn = "v.arrival_window_start";
        }

        if (fromDate != null) {
            sql.append(" AND ").append(dateColumn).append(" >= ?");
            params.add(Timestamp.from(fromDate));
        }
        if (toDate != null) {
            sql.append(" AND ").append(dateColumn).append(" < ?");
            params.add(Timestamp.from(toDate));
        }

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(v.visitor_name) LIKE ? OR v.visitor_mobile LIKE ? OR LOWER(v.visit_reference) LIKE ? OR LOWER(v.vehicle_number) LIKE ? OR LOWER(v.destination_flat_number) LIKE ? OR LOWER(v.host_name_snapshot) LIKE ?)");
            String term = "%" + query.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }
    }

    public void updateCheckInTime(Long visitId, Instant checkInTime) {
        String sql = "UPDATE visits SET check_in_time = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, checkInTime != null ? Timestamp.from(checkInTime) : null, visitId);
    }

    public void updateCheckOutTime(Long visitId, Instant checkOutTime) {
        String sql = "UPDATE visits SET check_out_time = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, checkOutTime != null ? Timestamp.from(checkOutTime) : null, visitId);
    }

    public int invalidateFutureInvitationsForResident(Long hostUserId) {
        String sql = "UPDATE visits SET status = 'CANCELLED', rejection_reason = 'Resident relocated flat', updated_at = CURRENT_TIMESTAMP " +
                "WHERE host_user_id = ? AND status IN ('PENDING_APPROVAL', 'APPROVED') AND arrival_window_start > CURRENT_TIMESTAMP";
        return jdbcTemplate.update(sql, hostUserId);
    }

    public List<Visit> findCurrentlyInside(Long hostUserId) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE v.status = 'INSIDE' ");
        List<Object> params = new ArrayList<>();
        if (hostUserId != null) {
            sql.append(" AND v.host_user_id = ?");
            params.add(hostUserId);
        }
        sql.append(" ORDER BY v.check_in_time DESC");
        return jdbcTemplate.query(sql.toString(), visitRowMapper, params.toArray());
    }

    public List<Visit> findWaitingApprovals(Long hostUserId) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE v.status = 'PENDING_APPROVAL' ");
        List<Object> params = new ArrayList<>();
        if (hostUserId != null) {
            sql.append(" AND v.host_user_id = ?");
            params.add(hostUserId);
        }
        sql.append(" ORDER BY v.created_at ASC");
        return jdbcTemplate.query(sql.toString(), visitRowMapper, params.toArray());
    }

    public List<Visit> findUpcomingInvitations(Long hostUserId) {
        String sql = BASE_SELECT + "WHERE v.host_user_id = ? AND v.status = 'APPROVED' AND v.arrival_window_end >= CURRENT_TIMESTAMP ORDER BY v.arrival_window_start ASC";
        return jdbcTemplate.query(sql, visitRowMapper, hostUserId);
    }

    public int countInsideVisits() {
        String sql = "SELECT COUNT(*) FROM visits WHERE status = 'INSIDE'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countTodayEntries() {
        String sql = "SELECT COUNT(*) FROM visits WHERE check_in_time >= CURRENT_DATE()";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countTodayExits() {
        String sql = "SELECT COUNT(*) FROM visits WHERE check_out_time >= CURRENT_DATE()";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countPendingApprovals() {
        String sql = "SELECT COUNT(*) FROM visits WHERE status = 'PENDING_APPROVAL'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countOverdueVisits() {
        try {
            List<Visit> insideVisits = searchVisits(null, VisitStatus.INSIDE, null, null, null, null, null, "id", "DESC", 1, 1000);
            int overdue = 0;
            Instant now = Instant.now();
            for (Visit v : insideVisits) {
                if (v.getCheckInTime() != null && v.getExpectedDurationMinutes() > 0) {
                    Instant expectedExit = v.getCheckInTime().plusSeconds(v.getExpectedDurationMinutes() * 60L);
                    if (expectedExit.isBefore(now)) {
                        overdue++;
                    }
                }
            }
            return overdue;
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Map<String, Object>> getDailyEntryCounts(int days) {
        Instant cutoff = Instant.now().minus(days, java.time.temporal.ChronoUnit.DAYS);
        String sql = "SELECT CAST(check_in_time AS DATE) AS entry_date, COUNT(*) AS total_count " +
                "FROM visits " +
                "WHERE check_in_time >= ? " +
                "GROUP BY CAST(check_in_time AS DATE) " +
                "ORDER BY entry_date ASC";
        try {
            return jdbcTemplate.queryForList(sql, Timestamp.from(cutoff));
        } catch (Exception e) {
            String fallbackSql = "SELECT DATE(check_in_time) AS entry_date, COUNT(*) AS total_count " +
                    "FROM visits WHERE check_in_time >= ? GROUP BY DATE(check_in_time) ORDER BY entry_date ASC";
            try {
                return jdbcTemplate.queryForList(fallbackSql, Timestamp.from(cutoff));
            } catch (Exception ex) {
                return List.of();
            }
        }
    }
}
