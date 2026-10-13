package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class ParkingAssignment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parkingSlotId;
    private Long vehicleId;
    private Long visitId;
    private Long residentUserId;
    private Long assignedByUserId;
    private Instant assignedAt;
    private Instant releasedAt;
    private boolean active;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private String slotNumber;
    private String slotType;
    private String vehicleNumber;
    private String visitReference;
    private String visitorName;
    private String residentName;
    private String assignedByName;

    public ParkingAssignment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getParkingSlotId() { return parkingSlotId; }
    public void setParkingSlotId(Long parkingSlotId) { this.parkingSlotId = parkingSlotId; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public Long getVisitId() { return visitId; }
    public void setVisitId(Long visitId) { this.visitId = visitId; }

    public Long getResidentUserId() { return residentUserId; }
    public void setResidentUserId(Long residentUserId) { this.residentUserId = residentUserId; }

    public Long getAssignedByUserId() { return assignedByUserId; }
    public void setAssignedByUserId(Long assignedByUserId) { this.assignedByUserId = assignedByUserId; }

    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }

    public Instant getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public String getSlotType() { return slotType; }
    public void setSlotType(String slotType) { this.slotType = slotType; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVisitReference() { return visitReference; }
    public void setVisitReference(String visitReference) { this.visitReference = visitReference; }

    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getAssignedByName() { return assignedByName; }
    public void setAssignedByName(String assignedByName) { this.assignedByName = assignedByName; }
}
