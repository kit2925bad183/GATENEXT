package com.gatenest.service;

import com.gatenest.model.Visit;
import com.gatenest.repository.ReportRepository;
import com.gatenest.repository.VisitRepository;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final VisitRepository visitRepository;

    public ReportService(ReportRepository reportRepository, VisitRepository visitRepository) {
        this.reportRepository = reportRepository;
        this.visitRepository = visitRepository;
    }

    public Map<String, Object> getOverallDashboardStats() {
        return reportRepository.getOverallSummary();
    }

    public List<Map<String, Object>> getBlockWiseVisitorTotals() {
        return reportRepository.getBlockWiseVisitorTotals();
    }

    public List<Map<String, Object>> getVisitorCategoryBreakdown() {
        return reportRepository.getVisitorCategoryBreakdown();
    }

    public List<Map<String, Object>> getGateTrafficStats() {
        return reportRepository.getGateTrafficStats();
    }

    public List<Map<String, Object>> getDailyVisitTrends(int days) {
        return reportRepository.getDailyVisitTrends(days);
    }

    public List<Map<String, Object>> getWeekWiseVisitorTotals() {
        return reportRepository.getWeekWiseVisitorTotals();
    }

    public List<Map<String, Object>> getMonthWiseVisitorTrends() {
        return reportRepository.getMonthWiseVisitorTrends();
    }

    public Map<String, Object> getAverageCompletedVisitDuration() {
        return reportRepository.getAverageCompletedVisitDuration();
    }

    public List<Map<String, Object>> getBusiestArrivalHours() {
        return reportRepository.getBusiestArrivalHours();
    }

    public List<Map<String, Object>> getOverdueVisitsList() {
        return reportRepository.getOverdueVisitsList();
    }

    public List<Map<String, Object>> getComplaintStatusBreakdown() {
        return reportRepository.getComplaintStatusBreakdown();
    }

    public List<Map<String, Object>> getParcelStatusBreakdown() {
        return reportRepository.getParcelStatusBreakdown();
    }

    public String exportVisitsToCsv(List<Visit> visits, String appliedFiltersSummary) {
        StringWriter sw = new StringWriter();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Kolkata"));

        // Metadata header
        sw.append("# GateNest Apartment Visitor Management System - Official Export\n");
        sw.append("# Generated At: ").append(dtf.format(Instant.now())).append(" (Asia/Kolkata)\n");
        if (appliedFiltersSummary != null && !appliedFiltersSummary.isBlank()) {
            sw.append("# Applied Filters: ").append(sanitizeCsvField(appliedFiltersSummary)).append("\n");
        }
        sw.append("# Total Records: ").append(String.valueOf(visits.size())).append("\n#\n");

        // Column Header
        sw.append("Reference,Visitor Name,Mobile,Category,Purpose,Flat,Block,Status,Arrival Window Start,Check In,Check Out,Duration (Mins),Vehicle Number\n");

        for (Visit v : visits) {
            sw.append(sanitizeCsvField(v.getVisitReference())).append(",");
            sw.append(sanitizeCsvField(v.getVisitorName())).append(",");
            sw.append(sanitizeCsvField(v.getVisitorMobile())).append(",");
            sw.append(sanitizeCsvField(v.getVisitorCategory() != null ? v.getVisitorCategory().name() : "")).append(",");
            sw.append(sanitizeCsvField(v.getPurpose())).append(",");
            sw.append(sanitizeCsvField(v.getDestinationFlatNumber())).append(",");
            sw.append(sanitizeCsvField(v.getDestinationBlockName())).append(",");
            sw.append(sanitizeCsvField(v.getStatus() != null ? v.getStatus().name() : "")).append(",");
            sw.append(sanitizeCsvField(v.getArrivalWindowStart() != null ? dtf.format(v.getArrivalWindowStart()) : "")).append(",");
            sw.append(sanitizeCsvField(v.getCheckInTime() != null ? dtf.format(v.getCheckInTime()) : "")).append(",");
            sw.append(sanitizeCsvField(v.getCheckOutTime() != null ? dtf.format(v.getCheckOutTime()) : "")).append(",");
            sw.append(sanitizeCsvField(String.valueOf(v.getExpectedDurationMinutes()))).append(",");
            sw.append(sanitizeCsvField(v.getVehicleNumber())).append("\n");
        }

        return sw.toString();
    }

    public String sanitizeCsvField(String value) {
        if (value == null) return "\"\"";
        String clean = value.replace("\"", "\"\"");
        // Prevent formula injection (=, +, -, @, \t, \r)
        if (clean.startsWith("=") || clean.startsWith("+") || clean.startsWith("-") || clean.startsWith("@") || clean.startsWith("\t") || clean.startsWith("\r")) {
            clean = "'" + clean;
        }
        return "\"" + clean + "\"";
    }
}
