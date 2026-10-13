package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.model.SecurityHandoverNote;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.SecurityHandoverService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/security/handover")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
public class SecurityHandoverController {

    private final SecurityHandoverService handoverService;

    public SecurityHandoverController(SecurityHandoverService handoverService) {
        this.handoverService = handoverService;
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<SecurityHandoverNote>>> getRecentNotes(
            @RequestParam(defaultValue = "20") int limit
    ) {
        List<SecurityHandoverNote> list = handoverService.getRecentNotes(limit);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SecurityHandoverNote>> createNote(
            @Valid @RequestBody SecurityHandoverNote note,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SecurityHandoverNote saved = handoverService.createNote(note, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok("Handover note created", saved));
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<Void>> acknowledgeNote(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        handoverService.acknowledgeNote(id, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok("Handover note acknowledged", null));
    }
}
