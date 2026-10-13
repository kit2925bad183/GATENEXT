package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.dto.CreateSecurityRequest;
import com.gatenest.dto.PagedResponse;
import com.gatenest.dto.UserDto;
import com.gatenest.model.UserStatus;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.SecurityStaffService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/security")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminSecurityController {

    private final SecurityStaffService securityStaffService;

    public AdminSecurityController(SecurityStaffService securityStaffService) {
        this.securityStaffService = securityStaffService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> getSecurityStaff(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int limit
    ) {
        PagedResponse<UserDto> response = securityStaffService.getSecurityStaff(query, status, page, limit);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createSecurityStaff(
            @Valid @RequestBody CreateSecurityRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        UserDto created = securityStaffService.createSecurityStaff(request, userDetails.getUser(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Security staff account created", created));
    }

    @PostMapping("/{userId}/assign-gate")
    public ResponseEntity<ApiResponse<Void>> assignGate(
            @PathVariable Long userId,
            @RequestParam Long gateId,
            @RequestParam(required = false) String shiftName,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        securityStaffService.assignGate(userId, gateId, shiftName, userDetails.getUser(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Gate assignment updated", null));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> setStatus(
            @PathVariable Long userId,
            @RequestParam UserStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        securityStaffService.setSecurityStatus(userId, status, userDetails.getUser(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Security account status updated to " + status, null));
    }
}
