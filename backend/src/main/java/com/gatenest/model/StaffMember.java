package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class StaffMember implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String fullName;
    private String mobile;
    private String staffType; // MAID, COOK, DRIVER, CLEANER, GARDENER, TUTOR, OTHER
    private String idProofType;
    private String idProofNumber;
    private String photoUrl;
    private boolean active;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private boolean currentlyInside;
    private Long activeSessionId;
    private Instant checkInTime;
    private String entryGateName;
    private int authorizedFlatsCount;

    public StaffMember() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getStaffType() { return staffType; }
    public void setStaffType(String staffType) { this.staffType = staffType; }

    public String getIdProofType() { return idProofType; }
    public void setIdProofType(String idProofType) { this.idProofType = idProofType; }

    public String getIdProofNumber() { return idProofNumber; }
    public void setIdProofNumber(String idProofNumber) { this.idProofNumber = idProofNumber; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public boolean isCurrentlyInside() { return currentlyInside; }
    public void setCurrentlyInside(boolean currentlyInside) { this.currentlyInside = currentlyInside; }

    public Long getActiveSessionId() { return activeSessionId; }
    public void setActiveSessionId(Long activeSessionId) { this.activeSessionId = activeSessionId; }

    public Instant getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Instant checkInTime) { this.checkInTime = checkInTime; }

    public String getEntryGateName() { return entryGateName; }
    public void setEntryGateName(String entryGateName) { this.entryGateName = entryGateName; }

    public int getAuthorizedFlatsCount() { return authorizedFlatsCount; }
    public void setAuthorizedFlatsCount(int authorizedFlatsCount) { this.authorizedFlatsCount = authorizedFlatsCount; }
}
