package com.gatenest.controller;

import com.gatenest.dto.*;
import com.gatenest.model.User;
import com.gatenest.model.Visit;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.VisitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/security")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
public class SecurityGateController {

    private final VisitService visitService;

    public SecurityGateController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping("/walk-in")
    public ResponseEntity<ApiResponse<Visit>> registerWalkIn(
            @Valid @RequestBody RegisterWalkInRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        Visit visit = visitService.registerWalkIn(request, userDetails.getUser(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Walk-in visitor registered. Approval notification sent to resident.", visit));
    }

    @PostMapping("/verify-pass")
    public ResponseEntity<ApiResponse<PassVerificationResponse>> verifyPass(
            @Valid @RequestBody VerifyPassRequest request
    ) {
        PassVerificationResponse response = visitService.verifyPass(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/check-in/{visitId}")
    public ResponseEntity<ApiResponse<Visit>> checkIn(
            @PathVariable Long visitId,
            @Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        Visit visit = visitService.checkIn(visitId, request, userDetails.getUser(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Visitor successfully checked in", visit));
    }

    @PostMapping("/check-out/{visitId}")
    public ResponseEntity<ApiResponse<Visit>> checkOut(
            @PathVariable Long visitId,
            @Valid @RequestBody CheckOutRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        Visit visit = visitService.checkOut(visitId, request, userDetails.getUser(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Visitor successfully checked out", visit));
    }

    @GetMapping("/waiting-approvals")
    public ResponseEntity<ApiResponse<List<Visit>>> getWaitingApprovals(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Visit> list = visitService.getWaitingApprovals(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/inside")
    public ResponseEntity<ApiResponse<List<Visit>>> getCurrentlyInside(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Visit> list = visitService.getCurrentlyInside(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}
