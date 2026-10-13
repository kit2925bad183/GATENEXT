package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.dto.DateBasis;
import com.gatenest.dto.PagedResponse;
import com.gatenest.model.Visit;
import com.gatenest.model.VisitStatus;
import com.gatenest.model.VisitorCategory;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.ReportService;
import com.gatenest.service.VisitService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
public class ReportController {

    private final ReportService reportService;
    private final VisitService visitService;

    public ReportController(ReportService reportService, VisitService visitService) {
        this.reportService = reportService;
        this.visitService = visitService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = reportService.getOverallDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/block-wise")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBlockWiseTotals() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getBlockWiseVisitorTotals()));
    }

    @GetMapping("/visitor-categories")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVisitorCategoryBreakdown() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getVisitorCategoryBreakdown()));
    }

    @GetMapping("/gate-traffic")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGateTraffic() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getGateTrafficStats()));
    }

    @GetMapping("/daily-trends")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDailyTrends(
            @RequestParam(defaultValue = "14") int days
    ) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getDailyVisitTrends(days)));
    }

    @GetMapping("/week-wise")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWeekWiseTotals() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getWeekWiseVisitorTotals()));
    }

    @GetMapping("/month-wise")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMonthWiseTrends() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getMonthWiseVisitorTrends()));
    }

    @GetMapping("/average-duration")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAverageDuration() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getAverageCompletedVisitDuration()));
    }

    @GetMapping("/busiest-hours")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBusiestHours() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getBusiestArrivalHours()));
    }

    @GetMapping("/overdue-visits")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOverdueVisits() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getOverdueVisitsList()));
    }

    @GetMapping("/complaints-summary")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getComplaintsSummary() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getComplaintStatusBreakdown()));
    }

    @GetMapping("/parcels-summary")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getParcelsSummary() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getParcelStatusBreakdown()));
    }

    @GetMapping(value = {"/export/visits.csv", "/export/visits"}, produces = "text/csv")
    public void exportVisitsCsv(
            @RequestParam(required = false) Long blockId,
            @RequestParam(required = false) Integer floorNumber,
            @RequestParam(required = false) Long flatId,
            @RequestParam(required = false) Long entryGateId,
            @RequestParam(required = false) Long exitGateId,
            @RequestParam(required = false) Long securityStaffId,
            @RequestParam(required = false) VisitStatus status,
            @RequestParam(required = false) VisitorCategory category,
            @RequestParam(required = false) Long gateId,
            @RequestParam(required = false) Boolean isWalkIn,
            @RequestParam(required = false) Boolean isOverdue,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) DateBasis dateBasis,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,
            @RequestParam(required = false) String filtersSummary,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) throws IOException {
        Long resolvedEntry = (entryGateId != null) ? entryGateId : gateId;
        PagedResponse<Visit> paged = visitService.searchVisitsAdvanced(
                userDetails.getUser(), blockId, floorNumber, flatId,
                resolvedEntry, exitGateId, securityStaffId,
                status, category, isWalkIn, isOverdue,
                query, dateBasis, fromDate, toDate,
                "id", "DESC", 1, 5000
        );

        String csvData = reportService.exportVisitsToCsv(paged.getItems(), filtersSummary);

        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"gatenest-visits-report-" + System.currentTimeMillis() + ".csv\"");
        response.getWriter().write(csvData);
        response.getWriter().flush();
    }
}
