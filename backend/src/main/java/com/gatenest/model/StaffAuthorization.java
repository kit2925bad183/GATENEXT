package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class StaffAuthorization implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long staffMemberId;
    private Long residentUserId;
    private Long flatId;
    private String scheduleDescription;
    private boolean active;
    private Instant authorizedAt;
    private Instant revokedAt;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private String staffName;
    private String staffMobile;
    private String staffType;
    private String residentName;
    private String flatNumber;
    private String blockName;

    public StaffAuthorization() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStaffMemberId() { return staffMemberId; }
    public void setStaffMemberId(Long staffMemberId) { this.staffMemberId = staffMemberId; }

    public Long getResidentUserId() { return residentUserId; }
    public void setResidentUserId(Long residentUserId) { this.residentUserId = residentUserId; }

    public Long getFlatId() { return flatId; }
    public void setFlatId(Long flatId) { this.flatId = flatId; }

    public String getScheduleDescription() { return scheduleDescription; }
    public void setScheduleDescription(String scheduleDescription) { this.scheduleDescription = scheduleDescription; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getAuthorizedAt() { return authorizedAt; }
    public void setAuthorizedAt(Instant authorizedAt) { this.authorizedAt = authorizedAt; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getStaffMobile() { return staffMobile; }
    public void setStaffMobile(String staffMobile) { this.staffMobile = staffMobile; }

    public String getStaffType() { return staffType; }
    public void setStaffType(String staffType) { this.staffType = staffType; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }
}
