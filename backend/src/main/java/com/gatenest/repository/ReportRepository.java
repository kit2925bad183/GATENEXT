package com.gatenest.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getOverallSummary() {
        Map<String, Object> map = new HashMap<>();

        Integer totalVisitsToday = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM visits WHERE check_in_time >= CURRENT_DATE", Integer.class);
        map.put("totalVisitsToday", totalVisitsToday != null ? totalVisitsToday : 0);

        Integer currentlyInside = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM visits WHERE status = 'INSIDE'", Integer.class);
        map.put("currentlyInside", currentlyInside != null ? currentlyInside : 0);

        Integer pendingParcels = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM parcels WHERE status IN ('RECEIVED', 'READY_FOR_COLLECTION')", Integer.class);
        map.put("pendingParcels", pendingParcels != null ? pendingParcels : 0);

        Integer openComplaints = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM complaints WHERE status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS')", Integer.class);
        map.put("openComplaints", openComplaints != null ? openComplaints : 0);

        Integer activeAssistance = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM security_assistance_requests WHERE status IN ('SUBMITTED', 'ACKNOWLEDGED')", Integer.class);
        map.put("activeAssistance", activeAssistance != null ? activeAssistance : 0);

        Integer staffInside = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM staff_attendance_sessions WHERE is_active = TRUE", Integer.class);
        map.put("staffInside", staffInside != null ? staffInside : 0);

        Integer overdueVisits = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM visits WHERE status = 'INSIDE' AND arrival_window_end IS NOT NULL AND arrival_window_end < CURRENT_TIMESTAMP", Integer.class);
        map.put("overdueVisits", overdueVisits != null ? overdueVisits : 0);

        return map;
    }

    public List<Map<String, Object>> getBlockWiseVisitorTotals() {
        String sql = "SELECT destination_block_name AS block_name, COUNT(*) AS total_visits " +
                "FROM visits GROUP BY destination_block_name ORDER BY total_visits DESC";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getVisitorCategoryBreakdown() {
        String sql = "SELECT visitor_category AS category, COUNT(*) AS count " +
                "FROM visits GROUP BY visitor_category ORDER BY count DESC";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getGateTrafficStats() {
        String sql = "SELECT g.name AS gate_name, " +
                "COUNT(CASE WHEN v.entry_gate_id = g.id THEN 1 END) AS entry_count, " +
                "COUNT(CASE WHEN v.exit_gate_id = g.id THEN 1 END) AS exit_count " +
                "FROM gates g " +
                "LEFT JOIN visits v ON v.entry_gate_id = g.id OR v.exit_gate_id = g.id " +
                "GROUP BY g.id, g.name ORDER BY entry_count DESC";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getDailyVisitTrends(int days) {
        String sql = "SELECT CAST(check_in_time AS DATE) AS visit_date, " +
                "COUNT(CASE WHEN check_in_time IS NOT NULL THEN 1 END) AS total_entries, " +
                "COUNT(CASE WHEN check_out_time IS NOT NULL THEN 1 END) AS total_exits " +
                "FROM visits WHERE check_in_time >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY) " +
                "GROUP BY CAST(check_in_time AS DATE) ORDER BY visit_date ASC";
        try {
            return jdbcTemplate.queryForList(sql, days);
        } catch (Exception e) {
            String fallbackSql = "SELECT CAST(check_in_time AS DATE) AS visit_date, " +
                    "COUNT(*) AS total_entries, " +
                    "COUNT(check_out_time) AS total_exits " +
                    "FROM visits WHERE check_in_time IS NOT NULL " +
                    "GROUP BY CAST(check_in_time AS DATE) ORDER BY visit_date DESC LIMIT ?";
            return jdbcTemplate.queryForList(fallbackSql, days);
        }
    }

    public List<Map<String, Object>> getWeekWiseVisitorTotals() {
        String sql = "SELECT YEAR(check_in_time) AS visit_year, WEEK(check_in_time, 1) AS visit_week, " +
                "COUNT(CASE WHEN check_in_time IS NOT NULL THEN 1 END) AS entry_count, " +
                "COUNT(CASE WHEN check_out_time IS NOT NULL THEN 1 END) AS exit_count " +
                "FROM visits WHERE check_in_time IS NOT NULL " +
                "GROUP BY YEAR(check_in_time), WEEK(check_in_time, 1) " +
                "ORDER BY visit_year DESC, visit_week DESC LIMIT 12";
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Map<String, Object>> getMonthWiseVisitorTrends() {
        String sql = "SELECT YEAR(check_in_time) AS visit_year, MONTH(check_in_time) AS visit_month, " +
                "COUNT(*) AS total_visits " +
                "FROM visits WHERE check_in_time IS NOT NULL " +
                "GROUP BY YEAR(check_in_time), MONTH(check_in_time) " +
                "ORDER BY visit_year DESC, visit_month DESC LIMIT 12";
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return List.of();
        }
    }

    public Map<String, Object> getAverageCompletedVisitDuration() {
        Map<String, Object> result = new HashMap<>();
        String sql = "SELECT COUNT(*) AS completed_visits_count, " +
                "AVG(TIMESTAMPDIFF(MINUTE, check_in_time, check_out_time)) AS avg_duration_minutes " +
                "FROM visits " +
                "WHERE status = 'EXITED' AND check_in_time IS NOT NULL AND check_out_time IS NOT NULL " +
                "AND check_out_time >= check_in_time";
        try {
            Map<String, Object> map = jdbcTemplate.queryForMap(sql);
            result.put("completedVisitsCount", map.get("completed_visits_count"));
            result.put("avgDurationMinutes", map.get("avg_duration_minutes") != null ? ((Number) map.get("avg_duration_minutes")).doubleValue() : 0.0);
        } catch (Exception e) {
            result.put("completedVisitsCount", 0);
            result.put("avgDurationMinutes", 0.0);
        }
        return result;
    }

    public List<Map<String, Object>> getBusiestArrivalHours() {
        String sql = "SELECT HOUR(check_in_time) AS arrival_hour, COUNT(*) AS count " +
                "FROM visits WHERE check_in_time IS NOT NULL " +
                "GROUP BY HOUR(check_in_time) ORDER BY arrival_hour ASC";
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Map<String, Object>> getOverdueVisitsList() {
        String sql = "SELECT v.id, v.visit_reference, v.visitor_name, v.visitor_mobile, " +
                "v.destination_block_name, v.destination_flat_number, v.host_name_snapshot, " +
                "v.check_in_time, v.arrival_window_end, v.expected_duration_minutes, " +
                "g.name AS entry_gate_name " +
                "FROM visits v " +
                "LEFT JOIN gates g ON v.entry_gate_id = g.id " +
                "WHERE v.status = 'INSIDE' AND v.arrival_window_end < CURRENT_TIMESTAMP " +
                "ORDER BY v.arrival_window_end ASC LIMIT 50";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getComplaintStatusBreakdown() {
        String sql = "SELECT status, COUNT(*) AS count FROM complaints GROUP BY status";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getParcelStatusBreakdown() {
        String sql = "SELECT status, COUNT(*) AS count FROM parcels GROUP BY status";
        return jdbcTemplate.queryForList(sql);
    }
}
