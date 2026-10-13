package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class GroupInvitationMember implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long groupInvitationId;
    private String guestName;
    private String guestMobile;
    private String vehicleNumber;
    private String passCredentialHash;
    private String status; // EXPECTED, INSIDE, CHECKED_OUT, CANCELLED
    private Instant checkInTime;
    private Instant checkOutTime;
    private Long entryGateId;
    private Long exitGateId;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private String entryGateName;
    private String exitGateName;
    private String rawPassToken;

    public GroupInvitationMember() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGroupInvitationId() { return groupInvitationId; }
    public void setGroupInvitationId(Long groupInvitationId) { this.groupInvitationId = groupInvitationId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestMobile() { return guestMobile; }
    public void setGuestMobile(String guestMobile) { this.guestMobile = guestMobile; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getPassCredentialHash() { return passCredentialHash; }
    public void setPassCredentialHash(String passCredentialHash) { this.passCredentialHash = passCredentialHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Instant checkInTime) { this.checkInTime = checkInTime; }

    public Instant getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Instant checkOutTime) { this.checkOutTime = checkOutTime; }

    public Long getEntryGateId() { return entryGateId; }
    public void setEntryGateId(Long entryGateId) { this.entryGateId = entryGateId; }

    public Long getExitGateId() { return exitGateId; }
    public void setExitGateId(Long exitGateId) { this.exitGateId = exitGateId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getEntryGateName() { return entryGateName; }
    public void setEntryGateName(String entryGateName) { this.entryGateName = entryGateName; }

    public String getExitGateName() { return exitGateName; }
    public void setExitGateName(String exitGateName) { this.exitGateName = exitGateName; }

    public String getRawPassToken() { return rawPassToken; }
    public void setRawPassToken(String rawPassToken) { this.rawPassToken = rawPassToken; }
}
