package com.gatenest.dto;

import com.gatenest.model.VisitorCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RegisterWalkInRequest {
    @NotNull(message = "Host resident ID is required")
    private Long hostUserId;

    @NotBlank(message = "Visitor name is required")
    private String visitorName;

    @NotBlank(message = "Visitor mobile number is required")
    private String visitorMobile;

    @NotNull(message = "Visitor category is required")
    private VisitorCategory visitorCategory;

    @NotBlank(message = "Purpose of visit is required")
    private String purpose;

    private String vehicleNumber;
    private String notes;

    @NotNull(message = "Current arrival gate ID is required")
    private Long arrivalGateId;

    @Positive(message = "Expected duration in minutes must be positive")
    private int expectedDurationMinutes = 120;

    public RegisterWalkInRequest() {}

    public Long getHostUserId() { return hostUserId; }
    public void setHostUserId(Long hostUserId) { this.hostUserId = hostUserId; }

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

    public Long getArrivalGateId() { return arrivalGateId; }
    public void setArrivalGateId(Long arrivalGateId) { this.arrivalGateId = arrivalGateId; }

    public int getExpectedDurationMinutes() { return expectedDurationMinutes; }
    public void setExpectedDurationMinutes(int expectedDurationMinutes) { this.expectedDurationMinutes = expectedDurationMinutes; }
}
