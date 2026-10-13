package com.gatenest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejectVisitRequest {
    @NotBlank(message = "Rejection reason is required")
    @Size(min = 3, max = 500, message = "Rejection reason must be between 3 and 500 characters")
    private String reason;

    public RejectVisitRequest() {}

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
