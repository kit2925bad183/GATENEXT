package com.gatenest.dto;

import jakarta.validation.constraints.NotNull;

public class CheckOutRequest {
    @NotNull(message = "Exit gate ID is required")
    private Long gateId;

    private String notes;

    public CheckOutRequest() {}

    public Long getGateId() { return gateId; }
    public void setGateId(Long gateId) { this.gateId = gateId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
