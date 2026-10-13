package com.gatenest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FlatDto {
    private Long id;

    @NotNull(message = "Block ID is required")
    private Long blockId;

    private String blockName;

    @NotBlank(message = "Flat number is required")
    private String flatNumber;

    @Positive(message = "Floor number must be at least 1")
    private int floorNumber = 1;

    private String extensionNumber;
    private boolean active = true;
    private int activeResidentsCount;

    public FlatDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBlockId() { return blockId; }
    public void setBlockId(Long blockId) { this.blockId = blockId; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public String getExtensionNumber() { return extensionNumber; }
    public void setExtensionNumber(String extensionNumber) { this.extensionNumber = extensionNumber; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getActiveResidentsCount() { return activeResidentsCount; }
    public void setActiveResidentsCount(int activeResidentsCount) { this.activeResidentsCount = activeResidentsCount; }
}
