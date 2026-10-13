package com.gatenest.model;

import java.time.Instant;

public class Visit {
    private Long id;
    private String visitReference;
    private Long hostUserId;
    private Long destinationFlatId;

    // Destination snapshot
    private String destinationBlockName;
    private String destinationFlatNumber;
    private String hostNameSnapshot;
    private String hostMobileSnapshot;

    // Visitor Details
    private String visitorName;
    private String visitorMobile;
    private VisitorCategory visitorCategory;
    private String purpose;
    private String vehicleNumber;
    private String notes;

    // Timings & State
    private Instant arrivalWindowStart;
    private Instant arrivalWindowEnd;
    private int expectedDurationMinutes;
    private VisitStatus status;

    // Pass Credential Security
    private String passCredentialHash;
    private Instant passGeneratedAt;

    // Gate & Check-in Details
    private Long entryGateId;
    private Long exitGateId;
    private Instant checkInTime;
    private Instant checkOutTime;

    // Workflow Tracking
    private Long registeredByUserId;
    private Long approvedByUserId;
    private Instant approvedAt;
    private String rejectionReason;
    private String adminOverrideReason;
    private Long adminOverrideByUserId;

    private Instant createdAt;
    private Instant updatedAt;

    // Enriched Joins for Display
    private String entryGateName;
    private String exitGateName;
    private String registeredByUserName;
    private String approvedByUserName;
    private boolean isOverstay;

    public Visit() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVisitReference() { return visitReference; }
    public void setVisitReference(String visitReference) { this.visitReference = visitReference; }

    public Long getHostUserId() { return hostUserId; }
    public void setHostUserId(Long hostUserId) { this.hostUserId = hostUserId; }

    public Long getDestinationFlatId() { return destinationFlatId; }
    public void setDestinationFlatId(Long destinationFlatId) { this.destinationFlatId = destinationFlatId; }

    public String getDestinationBlockName() { return destinationBlockName; }
    public void setDestinationBlockName(String destinationBlockName) { this.destinationBlockName = destinationBlockName; }

    public String getDestinationFlatNumber() { return destinationFlatNumber; }
    public void setDestinationFlatNumber(String destinationFlatNumber) { this.destinationFlatNumber = destinationFlatNumber; }

    public String getHostNameSnapshot() { return hostNameSnapshot; }
    public void setHostNameSnapshot(String hostNameSnapshot) { this.hostNameSnapshot = hostNameSnapshot; }

    public String getHostMobileSnapshot() { return hostMobileSnapshot; }
    public void setHostMobileSnapshot(String hostMobileSnapshot) { this.hostMobileSnapshot = hostMobileSnapshot; }

    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }

    public String getVisitorMobile() { return visitorMobile; }
    public void setVisitorMobile(String visitorMobile) { this.visitorMobile = visitorMobile; }

    public VisitorCategory getVisitorCategory() { return visitorCategory; }
    public void setVisitorCategory(VisitorCategory visitorCategory) { this.visitorCategory = visitorCategory; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getArrivalWindowStart() { return arrivalWindowStart; }
    public void setArrivalWindowStart(Instant arrivalWindowStart) { this.arrivalWindowStart = arrivalWindowStart; }

    public Instant getArrivalWindowEnd() { return arrivalWindowEnd; }
    public void setArrivalWindowEnd(Instant arrivalWindowEnd) { this.arrivalWindowEnd = arrivalWindowEnd; }

    public int getExpectedDurationMinutes() { return expectedDurationMinutes; }
    public void setExpectedDurationMinutes(int expectedDurationMinutes) { this.expectedDurationMinutes = expectedDurationMinutes; }

    public VisitStatus getStatus() { return status; }
    public void setStatus(VisitStatus status) { this.status = status; }

    public String getPassCredentialHash() { return passCredentialHash; }
    public void setPassCredentialHash(String passCredentialHash) { this.passCredentialHash = passCredentialHash; }

    public Instant getPassGeneratedAt() { return passGeneratedAt; }
    public void setPassGeneratedAt(Instant passGeneratedAt) { this.passGeneratedAt = passGeneratedAt; }

    public Long getEntryGateId() { return entryGateId; }
    public void setEntryGateId(Long entryGateId) { this.entryGateId = entryGateId; }

    public Long getExitGateId() { return exitGateId; }
    public void setExitGateId(Long exitGateId) { this.exitGateId = exitGateId; }

    public Instant getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Instant checkInTime) { this.checkInTime = checkInTime; }

    public Instant getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Instant checkOutTime) { this.checkOutTime = checkOutTime; }

    public Long getRegisteredByUserId() { return registeredByUserId; }
    public void setRegisteredByUserId(Long registeredByUserId) { this.registeredByUserId = registeredByUserId; }

    public Long getApprovedByUserId() { return approvedByUserId; }
    public void setApprovedByUserId(Long approvedByUserId) { this.approvedByUserId = approvedByUserId; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getAdminOverrideReason() { return adminOverrideReason; }
    public void setAdminOverrideReason(String adminOverrideReason) { this.adminOverrideReason = adminOverrideReason; }

    public Long getAdminOverrideByUserId() { return adminOverrideByUserId; }
    public void setAdminOverrideByUserId(Long adminOverrideByUserId) { this.adminOverrideByUserId = adminOverrideByUserId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getEntryGateName() { return entryGateName; }
    public void setEntryGateName(String entryGateName) { this.entryGateName = entryGateName; }

    public String getExitGateName() { return exitGateName; }
    public void setExitGateName(String exitGateName) { this.exitGateName = exitGateName; }

    public String getRegisteredByUserName() { return registeredByUserName; }
    public void setRegisteredByUserName(String registeredByUserName) { this.registeredByUserName = registeredByUserName; }

    public String getApprovedByUserName() { return approvedByUserName; }
    public void setApprovedByUserName(String approvedByUserName) { this.approvedByUserName = approvedByUserName; }

    public boolean isOverstay() { return isOverstay; }
    public void setOverstay(boolean overstay) { isOverstay = overstay; }
}
