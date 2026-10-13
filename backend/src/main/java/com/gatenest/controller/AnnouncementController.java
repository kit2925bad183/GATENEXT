package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.model.Announcement;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.AnnouncementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("/feed")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY', 'RESIDENT')")
    public ResponseEntity<ApiResponse<List<Announcement>>> getFeed(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Announcement> list = announcementService.getFeedForUser(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/admin-all")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<Announcement>>> getAllAdmin(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        List<Announcement> list = announcementService.getAllAnnouncementsAdmin(limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Announcement>> createAnnouncement(
            @Valid @RequestBody Announcement announcement,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Announcement saved = announcementService.createOrUpdateAnnouncement(
                announcement, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok("Announcement published successfully", saved));
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY', 'RESIDENT')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        announcementService.markAsRead(id, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok("Marked as read", null));
    }
}
