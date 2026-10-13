package com.gatenest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatenest.dto.PagedResponse;
import com.gatenest.model.AuditLog;
import com.gatenest.model.User;
import com.gatenest.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(User actor, String action, String entityType, String entityId, String ipAddress, Map<String, Object> details) {
        try {
            AuditLog auditLog = new AuditLog();
            if (actor != null) {
                auditLog.setActorUserId(actor.getId());
                auditLog.setActorEmail(actor.getEmail());
                auditLog.setActorRole(actor.getRole().name());
            } else {
                auditLog.setActorEmail("SYSTEM/BOOTSTRAP");
                auditLog.setActorRole("SYSTEM");
            }
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setIpAddress(ipAddress);

            if (details != null && !details.isEmpty()) {
                auditLog.setDetailsJson(objectMapper.writeValueAsString(details));
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to persist audit log", e);
        }
    }

    public PagedResponse<AuditLog> getAuditLogs(String action, String entityType, String query, int page, int limit) {
        int offset = (page - 1) * limit;
        List<AuditLog> items = auditLogRepository.searchLogs(action, entityType, query, offset, limit);
        int total = auditLogRepository.countLogs(action, entityType, query);
        return new PagedResponse<>(items, total, page, limit);
    }
}
