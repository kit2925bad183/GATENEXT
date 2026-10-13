package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.model.GroupInvitation;
import com.gatenest.model.GroupInvitationMember;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.GroupInvitationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/group-invitations")
public class GroupInvitationController {

    private final GroupInvitationService groupService;

    public GroupInvitationController(GroupInvitationService groupService) {
        this.groupService = groupService;
    }

    public static class CreateGroupRequest {
        public GroupInvitation invitation;
        public List<String> guestNames;
    }

    @GetMapping("/my-groups")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<List<GroupInvitation>>> getMyGroups(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<GroupInvitation> list = groupService.getInvitationsForHost(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY', 'RESIDENT')")
    public ResponseEntity<ApiResponse<GroupInvitation>> getById(@PathVariable Long id) {
        GroupInvitation inv = groupService.getInvitationById(id);
        return ResponseEntity.ok(ApiResponse.ok(inv));
    }

    @GetMapping("/by-ref/{ref}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY', 'RESIDENT')")
    public ResponseEntity<ApiResponse<GroupInvitation>> getByReference(@PathVariable String ref) {
        GroupInvitation inv = groupService.getInvitationByReference(ref);
        return ResponseEntity.ok(ApiResponse.ok(inv));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<GroupInvitation>> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GroupInvitation created = groupService.createGroupInvitation(
                userDetails.getUser().getId(), request.invitation, request.guestNames);
        return ResponseEntity.ok(ApiResponse.ok("Group event invitation created", created));
    }

    @PostMapping("/members/{memberId}/check-in")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<GroupInvitationMember>> checkInGuest(
            @PathVariable Long memberId,
            @RequestParam Long gateId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GroupInvitationMember member = groupService.checkInGuest(memberId, gateId, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok("Guest checked in successfully", member));
    }

    @PostMapping("/members/{memberId}/check-out")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<GroupInvitationMember>> checkOutGuest(
            @PathVariable Long memberId,
            @RequestParam Long gateId
    ) {
        GroupInvitationMember member = groupService.checkOutGuest(memberId, gateId);
        return ResponseEntity.ok(ApiResponse.ok("Guest checked out successfully", member));
    }
}
