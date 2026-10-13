package com.gatenest.controller;

import com.gatenest.dto.*;
import com.gatenest.model.*;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.VisitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @GetMapping(value = {"", "/advanced"})
    public ResponseEntity<ApiResponse<PagedResponse<Visit>>> searchVisits(
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
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long resolvedEntryGate = (entryGateId != null) ? entryGateId : gateId;
        PagedResponse<Visit> response = visitService.searchVisitsAdvanced(
                userDetails.getUser(), blockId, floorNumber, flatId,
                resolvedEntryGate, exitGateId, securityStaffId,
                status, category, isWalkIn, isOverdue,
                query, dateBasis, fromDate, toDate,
                sortBy, sortDirection, page, limit
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Visit>> getVisit(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Visit visit = visitService.getVisitById(id);
        if (userDetails.getRole() == Role.RESIDENT && !visit.getHostUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("You cannot view details of visits addressed to another resident."));
        }
        return ResponseEntity.ok(ApiResponse.ok(visit));
    }

    @GetMapping("/by-reference/{reference}")
    public ResponseEntity<ApiResponse<Visit>> getVisitByReference(
            @PathVariable String reference,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Visit visit = visitService.getVisitByReference(reference);
        if (userDetails.getRole() == Role.RESIDENT && !visit.getHostUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("You cannot view details of visits addressed to another resident."));
        }
        return ResponseEntity.ok(ApiResponse.ok(visit));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<ApiResponse<List<VisitEvent>>> getVisitTimeline(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Visit visit = visitService.getVisitById(id);
        if (userDetails.getRole() == Role.RESIDENT && !visit.getHostUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }
        List<VisitEvent> timeline = visitService.getVisitTimeline(id);
        return ResponseEntity.ok(ApiResponse.ok(timeline));
    }

    @PostMapping("/{id}/correct-times")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<Visit>> correctVisitTimes(
            @PathVariable Long id,
            @Valid @RequestBody VisitCorrectionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        Visit updated = visitService.correctVisitTimes(id, request, userDetails.getUser(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Visit timestamp corrected and logged to audit trail.", updated));
    }

    @GetMapping("/{id}/corrections")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVisitCorrections(
            @PathVariable Long id
    ) {
        List<Map<String, Object>> corrections = visitService.getVisitCorrections(id);
        return ResponseEntity.ok(ApiResponse.ok(corrections));
    }

    @PostMapping("/{id}/override")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Visit>> adminOverride(
            @PathVariable Long id,
            @Valid @RequestBody AdminOverrideRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        Visit visit = visitService.adminOverride(id, request, userDetails.getUser(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Visit status overridden by administrator", visit));
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        DashboardStatsDto stats = visitService.getDashboardStats(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/export")
    public void exportCsv(
            @RequestParam(required = false) VisitStatus status,
            @RequestParam(required = false) VisitorCategory category,
            @RequestParam(required = false) Long gateId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) throws IOException {
        String csvContent = visitService.exportVisitsCsv(userDetails.getUser(), status, category, gateId, query, fromDate, toDate);

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"gatenest-visits-" + System.currentTimeMillis() + ".csv\"");
        response.getWriter().write(csvContent);
    }
}
