package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.dto.PagedResponse;
import com.gatenest.model.AuditLog;
import com.gatenest.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminAuditController {

    private final AuditLogService auditLogService;

    public AdminAuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        PagedResponse<AuditLog> response = auditLogService.getAuditLogs(action, entityType, query, page, limit);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
