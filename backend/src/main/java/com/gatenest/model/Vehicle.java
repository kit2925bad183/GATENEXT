package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long ownerUserId;
    private Long flatId;
    private String vehicleNumber;
    private String vehicleType; // TWO_WHEELER, FOUR_WHEELER, EV
    private String makeModel;
    private String color;
    private String stickerNumber;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private String ownerName;
    private String flatNumber;
    private String blockName;
    private String assignedSlotNumber;

    public Vehicle() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }

    public Long getFlatId() { return flatId; }
    public void setFlatId(Long flatId) { this.flatId = flatId; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getMakeModel() { return makeModel; }
    public void setMakeModel(String makeModel) { this.makeModel = makeModel; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getStickerNumber() { return stickerNumber; }
    public void setStickerNumber(String stickerNumber) { this.stickerNumber = stickerNumber; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    public String getAssignedSlotNumber() { return assignedSlotNumber; }
    public void setAssignedSlotNumber(String assignedSlotNumber) { this.assignedSlotNumber = assignedSlotNumber; }
}
