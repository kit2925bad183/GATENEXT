package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.model.SecurityAssistanceRequest;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.SecurityAssistanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assistance")
public class SecurityAssistanceController {

    private final SecurityAssistanceService assistanceService;

    public SecurityAssistanceController(SecurityAssistanceService assistanceService) {
        this.assistanceService = assistanceService;
    }

    public static class ResolveAssistanceRequest {
        public String resolutionNotes;
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<List<SecurityAssistanceRequest>>> getMyRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<SecurityAssistanceRequest> list = assistanceService.getRequestsForResident(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<List<SecurityAssistanceRequest>>> getActiveRequests() {
        List<SecurityAssistanceRequest> list = assistanceService.getActiveRequests();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<SecurityAssistanceRequest>> submitRequest(
            @Valid @RequestBody SecurityAssistanceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SecurityAssistanceRequest saved = assistanceService.submitAssistanceRequest(
                userDetails.getUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Assistance request triggered and sent to on-duty security", saved));
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<Void>> acknowledgeRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        assistanceService.acknowledgeRequest(id, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok("Assistance request acknowledged by security", null));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<Void>> resolveRequest(
            @PathVariable Long id,
            @RequestBody ResolveAssistanceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        assistanceService.resolveRequest(id, userDetails.getUser().getId(), request.resolutionNotes);
        return ResponseEntity.ok(ApiResponse.ok("Assistance request resolved", null));
    }
}
