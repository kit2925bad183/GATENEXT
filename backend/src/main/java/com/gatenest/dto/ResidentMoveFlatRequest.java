package com.gatenest.dto;

import jakarta.validation.constraints.NotNull;

public class ResidentMoveFlatRequest {

    @NotNull(message = "New flat ID is required")
    private Long newFlatId;

    private String occupancyType; // OWNER, TENANT

    private String reason;

    public Long getNewFlatId() {
        return newFlatId;
    }

    public void setNewFlatId(Long newFlatId) {
        this.newFlatId = newFlatId;
    }

    public String getOccupancyType() {
        return occupancyType;
    }

    public void setOccupancyType(String occupancyType) {
        this.occupancyType = occupancyType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
