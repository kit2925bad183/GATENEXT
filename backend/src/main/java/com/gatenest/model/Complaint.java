package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public class Complaint implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long residentUserId;
    private Long flatId;
    private String category; // PLUMBING, ELECTRICAL, CARPENTRY, SECURITY, NOISE, COMMON_AREA, LIFT, OTHER
    private String subject;
    private String description;
    private String locationDetails;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private String status; // OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
    private Long assignedToUserId;
    private Instant resolvedAt;
    private Instant closedAt;
    private int reopenedCount;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private String residentName;
    private String residentMobile;
    private String flatNumber;
    private String blockName;
    private String assignedToName;
    private List<ComplaintComment> comments;
    private List<ComplaintAttachment> attachments;

    public Complaint() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResidentUserId() { return residentUserId; }
    public void setResidentUserId(Long residentUserId) { this.residentUserId = residentUserId; }

    public Long getFlatId() { return flatId; }
    public void setFlatId(Long flatId) { this.flatId = flatId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocationDetails() { return locationDetails; }
    public void setLocationDetails(String locationDetails) { this.locationDetails = locationDetails; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(Long assignedToUserId) { this.assignedToUserId = assignedToUserId; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }

    public int getReopenedCount() { return reopenedCount; }
    public void setReopenedCount(int reopenedCount) { this.reopenedCount = reopenedCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getResidentMobile() { return residentMobile; }
    public void setResidentMobile(String residentMobile) { this.residentMobile = residentMobile; }

    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public List<ComplaintComment> getComments() { return comments; }
    public void setComments(List<ComplaintComment> comments) { this.comments = comments; }

    public List<ComplaintAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<ComplaintAttachment> attachments) { this.attachments = attachments; }
}
