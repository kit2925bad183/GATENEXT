package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.dto.PagedResponse;
import com.gatenest.dto.RejectVisitRequest;
import com.gatenest.model.ResidentProfile;
import com.gatenest.model.UserStatus;
import com.gatenest.model.VerificationStatus;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.ResidentManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/residents")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminResidentController {

    private final ResidentManagementService residentManagementService;

    public AdminResidentController(ResidentManagementService residentManagementService) {
        this.residentManagementService = residentManagementService;
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ResidentProfile>>> getPendingRegistrations(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit
    ) {
        List<ResidentProfile> pending = residentManagementService.getPendingRegistrations(offset, limit);
        return ResponseEntity.ok(ApiResponse.ok(pending));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ResidentProfile>>> searchResidents(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long blockId,
            @RequestParam(required = false) Long flatId,
            @RequestParam(required = false) VerificationStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int limit
    ) {
        PagedResponse<ResidentProfile> response = residentManagementService.searchResidents(query, blockId, flatId, status, page, limit);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{profileId}/approve")
    public ResponseEntity<ApiResponse<ResidentProfile>> approveResident(
            @PathVariable Long profileId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        ResidentProfile profile = residentManagementService.approveResident(profileId, userDetails.getUser(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Resident registration approved", profile));
    }

    @PostMapping("/{profileId}/reject")
    public ResponseEntity<ApiResponse<ResidentProfile>> rejectResident(
            @PathVariable Long profileId,
            @Valid @RequestBody RejectVisitRequest rejectReq,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        ResidentProfile profile = residentManagementService.rejectResident(profileId, rejectReq.getReason(), userDetails.getUser(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Resident registration rejected", profile));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> setStatus(
            @PathVariable Long userId,
            @RequestParam UserStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        residentManagementService.setResidentStatus(userId, status, userDetails.getUser(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Resident account status updated to " + status, null));
    }
}
