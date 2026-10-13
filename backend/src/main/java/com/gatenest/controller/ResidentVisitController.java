package com.gatenest.controller;

import com.gatenest.dto.*;
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
@RequestMapping("/api/v1/resident")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
public class ResidentVisitController {

    private final VisitService visitService;

    public ResidentVisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping("/invitations")
    public ResponseEntity<ApiResponse<PassCredentialDto>> createInvitation(
            @Valid @RequestBody CreateInvitationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        PassCredentialDto cred = visitService.createInvitation(request, userDetails.getUser(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Visitor invitation created successfully", cred));
    }

    @GetMapping("/invitations/upcoming")
    public ResponseEntity<ApiResponse<List<Visit>>> getUpcomingInvitations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Visit> list = visitService.getUpcomingInvitations(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/approval-inbox")
    public ResponseEntity<ApiResponse<List<Visit>>> getApprovalInbox(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Visit> list = visitService.getWaitingApprovals(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/visits/{visitId}/approve")
    public ResponseEntity<ApiResponse<Visit>> approveVisit(
            @PathVariable Long visitId,
            @RequestBody(required = false) ApproveVisitRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        Visit visit = visitService.approveVisit(visitId, request, userDetails.getUser(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Visitor request approved", visit));
    }

    @PostMapping("/visits/{visitId}/reject")
    public ResponseEntity<ApiResponse<Visit>> rejectVisit(
            @PathVariable Long visitId,
            @Valid @RequestBody RejectVisitRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        Visit visit = visitService.rejectVisit(visitId, request, userDetails.getUser(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Visitor request rejected", visit));
    }

    @PostMapping("/visits/{visitId}/cancel")
    public ResponseEntity<ApiResponse<Visit>> cancelInvitation(
            @PathVariable Long visitId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        Visit visit = visitService.cancelInvitation(visitId, userDetails.getUser(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Invitation cancelled", visit));
    }

    @PostMapping("/visits/{visitId}/regenerate-pass")
    public ResponseEntity<ApiResponse<PassCredentialDto>> regeneratePass(
            @PathVariable Long visitId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        PassCredentialDto cred = visitService.regeneratePass(visitId, userDetails.getUser(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Pass regenerated and new QR code issued", cred));
    }

    @GetMapping("/my-visitors-inside")
    public ResponseEntity<ApiResponse<List<Visit>>> getMyVisitorsInside(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Visit> list = visitService.getCurrentlyInside(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}
