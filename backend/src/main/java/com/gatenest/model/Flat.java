package com.gatenest.model;

import java.time.Instant;

public class Flat {
    private Long id;
    private Long blockId;
    private String flatNumber;
    private int floorNumber;
    private String extensionNumber;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    // Join field
    private String blockName;
    private int activeResidentsCount;

    public Flat() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBlockId() { return blockId; }
    public void setBlockId(Long blockId) { this.blockId = blockId; }

    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public String getExtensionNumber() { return extensionNumber; }
    public void setExtensionNumber(String extensionNumber) { this.extensionNumber = extensionNumber; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    public int getActiveResidentsCount() { return activeResidentsCount; }
    public void setActiveResidentsCount(int activeResidentsCount) { this.activeResidentsCount = activeResidentsCount; }
}
