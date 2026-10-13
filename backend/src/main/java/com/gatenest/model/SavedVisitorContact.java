package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class SavedVisitorContact implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long residentUserId;
    private String visitorName;
    private String visitorMobile;
    private VisitorCategory visitorCategory;
    private String defaultPurpose;
    private String defaultVehicleNumber;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public SavedVisitorContact() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResidentUserId() { return residentUserId; }
    public void setResidentUserId(Long residentUserId) { this.residentUserId = residentUserId; }

    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }

    public String getVisitorMobile() { return visitorMobile; }
    public void setVisitorMobile(String visitorMobile) { this.visitorMobile = visitorMobile; }

    public VisitorCategory getVisitorCategory() { return visitorCategory; }
    public void setVisitorCategory(VisitorCategory visitorCategory) { this.visitorCategory = visitorCategory; }

    public String getDefaultPurpose() { return defaultPurpose; }
    public void setDefaultPurpose(String defaultPurpose) { this.defaultPurpose = defaultPurpose; }

    public String getDefaultVehicleNumber() { return defaultVehicleNumber; }
    public void setDefaultVehicleNumber(String defaultVehicleNumber) { this.defaultVehicleNumber = defaultVehicleNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
