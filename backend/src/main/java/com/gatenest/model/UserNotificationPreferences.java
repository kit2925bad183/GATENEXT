package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class UserNotificationPreferences implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private boolean visitorAlerts = true;
    private boolean parcelAlerts = true;
    private boolean complaintAlerts = true;
    private boolean noticeAlerts = true;
    private boolean assistanceAlerts = true;
    private Instant createdAt;
    private Instant updatedAt;

    public UserNotificationPreferences() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public boolean isVisitorAlerts() { return visitorAlerts; }
    public void setVisitorAlerts(boolean visitorAlerts) { this.visitorAlerts = visitorAlerts; }

    public boolean isParcelAlerts() { return parcelAlerts; }
    public void setParcelAlerts(boolean parcelAlerts) { this.parcelAlerts = parcelAlerts; }

    public boolean isComplaintAlerts() { return complaintAlerts; }
    public void setComplaintAlerts(boolean complaintAlerts) { this.complaintAlerts = complaintAlerts; }

    public boolean isNoticeAlerts() { return noticeAlerts; }
    public void setNoticeAlerts(boolean noticeAlerts) { this.noticeAlerts = noticeAlerts; }

    public boolean isAssistanceAlerts() { return assistanceAlerts; }
    public void setAssistanceAlerts(boolean assistanceAlerts) { this.assistanceAlerts = assistanceAlerts; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
