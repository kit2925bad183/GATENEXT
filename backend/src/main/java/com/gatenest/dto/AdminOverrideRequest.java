package com.gatenest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminOverrideRequest {
    @NotBlank(message = "Mandatory override reason is required")
    @Size(min = 5, max = 500, message = "Override reason must be between 5 and 500 characters")
    private String overrideReason;

    public AdminOverrideRequest() {}

    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }
}
