package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.model.Notification;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "30") int limit
    ) {
        List<Notification> notifications = notificationService.getNotificationsForUser(userDetails.getId(), limit);
        return ResponseEntity.ok(ApiResponse.ok(notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        int count = notificationService.getUnreadCount(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("unreadCount", count)));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.markAsRead(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("Marked as read", null));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.markAllAsRead(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("All marked as read", null));
    }
}
