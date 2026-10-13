package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.model.StaffAttendanceSession;
import com.gatenest.model.StaffAuthorization;
import com.gatenest.model.StaffMember;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    public static class AuthorizeStaffRequest {
        public Long staffMemberId;
        public String scheduleDescription;
    }

    public static class StaffGateActionRequest {
        public Long gateId;
        public String notes;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY', 'RESIDENT')")
    public ResponseEntity<ApiResponse<List<StaffMember>>> getAllStaff() {
        List<StaffMember> list = staffService.getAllStaff();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<StaffMember>> createOrUpdateStaff(
            @Valid @RequestBody StaffMember staff
    ) {
        StaffMember saved = staffService.createOrUpdateStaff(staff);
        return ResponseEntity.ok(ApiResponse.ok("Staff member saved", saved));
    }

    @GetMapping("/my-authorizations")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<List<StaffAuthorization>>> getMyAuthorizations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<StaffAuthorization> list = staffService.getAuthorizationsForResident(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/authorize")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<StaffAuthorization>> authorizeStaff(
            @RequestBody AuthorizeStaffRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StaffAuthorization auth = staffService.authorizeStaffForFlat(
                userDetails.getUser().getId(), request.staffMemberId, request.scheduleDescription);
        return ResponseEntity.ok(ApiResponse.ok("Staff authorization granted", auth));
    }

    @DeleteMapping("/authorizations/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<Void>> revokeAuthorization(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        staffService.revokeStaffAuthorization(userDetails.getUser().getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Staff authorization revoked", null));
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<StaffAttendanceSession>> checkInStaff(
            @PathVariable Long id,
            @RequestBody StaffGateActionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StaffAttendanceSession session = staffService.recordStaffCheckIn(
                id, request.gateId, userDetails.getUser().getId(), request.notes);
        return ResponseEntity.ok(ApiResponse.ok("Staff entry recorded", session));
    }

    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<Void>> checkOutStaff(
            @PathVariable Long id,
            @RequestBody StaffGateActionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        staffService.recordStaffCheckOut(id, request.gateId, userDetails.getUser().getId(), request.notes);
        return ResponseEntity.ok(ApiResponse.ok("Staff exit recorded", null));
    }

    @GetMapping("/attendance/active")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<List<StaffAttendanceSession>>> getActiveAttendance() {
        List<StaffAttendanceSession> list = staffService.getActiveAttendanceSessions();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}
