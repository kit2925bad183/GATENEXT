package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.model.RecurringVisitRule;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.RecurringVisitService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recurring-rules")
public class RecurringVisitController {

    private final RecurringVisitService recurringVisitService;

    public RecurringVisitController(RecurringVisitService recurringVisitService) {
        this.recurringVisitService = recurringVisitService;
    }

    @GetMapping("/my-rules")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<List<RecurringVisitRule>>> getMyRules(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<RecurringVisitRule> list = recurringVisitService.getRulesForResident(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<List<RecurringVisitRule>>> getAllActiveRules() {
        List<RecurringVisitRule> list = recurringVisitService.getAllActiveRules();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<RecurringVisitRule>> createRule(
            @Valid @RequestBody RecurringVisitRule rule,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RecurringVisitRule saved = recurringVisitService.createRule(userDetails.getUser().getId(), rule);
        return ResponseEntity.ok(ApiResponse.ok("Recurring visit rule created", saved));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<RecurringVisitRule>> toggleStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RecurringVisitRule updated = recurringVisitService.toggleRuleStatus(userDetails.getUser().getId(), id, active);
        return ResponseEntity.ok(ApiResponse.ok("Rule status updated", updated));
    }
}
