package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class StaffAttendanceSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long staffMemberId;
    private Instant checkInTime;
    private Instant checkOutTime;
    private Long entryGateId;
    private Long exitGateId;
    private Long entryGuardUserId;
    private Long exitGuardUserId;
    private boolean active;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private String staffName;
    private String staffMobile;
    private String staffType;
    private String entryGateName;
    private String exitGateName;
    private String entryGuardName;
    private String exitGuardName;

    public StaffAttendanceSession() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStaffMemberId() { return staffMemberId; }
    public void setStaffMemberId(Long staffMemberId) { this.staffMemberId = staffMemberId; }

    public Instant getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Instant checkInTime) { this.checkInTime = checkInTime; }

    public Instant getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Instant checkOutTime) { this.checkOutTime = checkOutTime; }

    public Long getEntryGateId() { return entryGateId; }
    public void setEntryGateId(Long entryGateId) { this.entryGateId = entryGateId; }

    public Long getExitGateId() { return exitGateId; }
    public void setExitGateId(Long exitGateId) { this.exitGateId = exitGateId; }

    public Long getEntryGuardUserId() { return entryGuardUserId; }
    public void setEntryGuardUserId(Long entryGuardUserId) { this.entryGuardUserId = entryGuardUserId; }

    public Long getExitGuardUserId() { return exitGuardUserId; }
    public void setExitGuardUserId(Long exitGuardUserId) { this.exitGuardUserId = exitGuardUserId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

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

    public String getEntryGateName() { return entryGateName; }
    public void setEntryGateName(String entryGateName) { this.entryGateName = entryGateName; }

    public String getExitGateName() { return exitGateName; }
    public void setExitGateName(String exitGateName) { this.exitGateName = exitGateName; }

    public String getEntryGuardName() { return entryGuardName; }
    public void setEntryGuardName(String entryGuardName) { this.entryGuardName = entryGuardName; }

    public String getExitGuardName() { return exitGuardName; }
    public void setExitGuardName(String exitGuardName) { this.exitGuardName = exitGuardName; }
}
