package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class ParkingSlot implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String slotNumber;
    private String slotType; // TWO_WHEELER, FOUR_WHEELER, EV_CHARGING, VISITOR
    private Long blockId;
    private String floorLevel;
    private boolean reservedForResidents;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private String blockName;
    private boolean occupied;
    private Long currentAssignmentId;
    private String occupiedByVehicleNumber;
    private String occupiedByResidentName;
    private String occupiedByVisitorName;
    private String currentVisitReference;

    public ParkingSlot() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public String getSlotType() { return slotType; }
    public void setSlotType(String slotType) { this.slotType = slotType; }

    public Long getBlockId() { return blockId; }
    public void setBlockId(Long blockId) { this.blockId = blockId; }

    public String getFloorLevel() { return floorLevel; }
    public void setFloorLevel(String floorLevel) { this.floorLevel = floorLevel; }

    public boolean isReservedForResidents() { return reservedForResidents; }
    public void setReservedForResidents(boolean reservedForResidents) { this.reservedForResidents = reservedForResidents; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }

    public Long getCurrentAssignmentId() { return currentAssignmentId; }
    public void setCurrentAssignmentId(Long currentAssignmentId) { this.currentAssignmentId = currentAssignmentId; }

    public String getOccupiedByVehicleNumber() { return occupiedByVehicleNumber; }
    public void setOccupiedByVehicleNumber(String occupiedByVehicleNumber) { this.occupiedByVehicleNumber = occupiedByVehicleNumber; }

    public String getOccupiedByResidentName() { return occupiedByResidentName; }
    public void setOccupiedByResidentName(String occupiedByResidentName) { this.occupiedByResidentName = occupiedByResidentName; }

    public String getOccupiedByVisitorName() { return occupiedByVisitorName; }
    public void setOccupiedByVisitorName(String occupiedByVisitorName) { this.occupiedByVisitorName = occupiedByVisitorName; }

    public String getCurrentVisitReference() { return currentVisitReference; }
    public void setCurrentVisitReference(String currentVisitReference) { this.currentVisitReference = currentVisitReference; }
}
