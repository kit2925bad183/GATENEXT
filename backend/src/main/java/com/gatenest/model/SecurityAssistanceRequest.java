package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class SecurityAssistanceRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long residentUserId;
    private Long flatId;
    private String category; // MEDICAL_ALERT, NOISE_DISTURBANCE, SUSPICIOUS_ACTIVITY, PARKING_BLOCK, EMERGENCY_HELP, GENERAL
    private String locationDetails;
    private String description;
    private String status; // SUBMITTED, ACKNOWLEDGED, RESOLVED, CANCELLED
    private Instant submittedAt;
    private Long acknowledgedByUserId;
    private Instant acknowledgedAt;
    private Long resolvedByUserId;
    private Instant resolvedAt;
    private String resolutionNotes;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private String residentName;
    private String residentMobile;
    private String flatNumber;
    private String blockName;
    private String acknowledgedByName;
    private String resolvedByName;

    public SecurityAssistanceRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResidentUserId() { return residentUserId; }
    public void setResidentUserId(Long residentUserId) { this.residentUserId = residentUserId; }

    public Long getFlatId() { return flatId; }
    public void setFlatId(Long flatId) { this.flatId = flatId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocationDetails() { return locationDetails; }
    public void setLocationDetails(String locationDetails) { this.locationDetails = locationDetails; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public Long getAcknowledgedByUserId() { return acknowledgedByUserId; }
    public void setAcknowledgedByUserId(Long acknowledgedByUserId) { this.acknowledgedByUserId = acknowledgedByUserId; }

    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Instant acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

    public Long getResolvedByUserId() { return resolvedByUserId; }
    public void setResolvedByUserId(Long resolvedByUserId) { this.resolvedByUserId = resolvedByUserId; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

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

    public String getAcknowledgedByName() { return acknowledgedByName; }
    public void setAcknowledgedByName(String acknowledgedByName) { this.acknowledgedByName = acknowledgedByName; }

    public String getResolvedByName() { return resolvedByName; }
    public void setResolvedByName(String resolvedByName) { this.resolvedByName = resolvedByName; }
}
