package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

public class RecurringVisitRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long residentUserId;
    private Long destinationFlatId;
    private String visitorName;
    private String visitorMobile;
    private VisitorCategory visitorCategory;
    private String purpose;
    private String allowedWeekdays; // e.g. "MON,TUE,WED,THU,FRI"
    private LocalDate validFromDate;
    private LocalDate validToDate;
    private String allowedStartTime; // "08:00"
    private String allowedEndTime;   // "18:00"
    private int expectedDurationMinutes;
    private boolean active;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private String destinationBlockName;
    private String destinationFlatNumber;
    private String residentFullName;

    public RecurringVisitRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResidentUserId() { return residentUserId; }
    public void setResidentUserId(Long residentUserId) { this.residentUserId = residentUserId; }

    public Long getDestinationFlatId() { return destinationFlatId; }
    public void setDestinationFlatId(Long destinationFlatId) { this.destinationFlatId = destinationFlatId; }

    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }

    public String getVisitorMobile() { return visitorMobile; }
    public void setVisitorMobile(String visitorMobile) { this.visitorMobile = visitorMobile; }

    public VisitorCategory getVisitorCategory() { return visitorCategory; }
    public void setVisitorCategory(VisitorCategory visitorCategory) { this.visitorCategory = visitorCategory; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getAllowedWeekdays() { return allowedWeekdays; }
    public void setAllowedWeekdays(String allowedWeekdays) { this.allowedWeekdays = allowedWeekdays; }

    public LocalDate getValidFromDate() { return validFromDate; }
    public void setValidFromDate(LocalDate validFromDate) { this.validFromDate = validFromDate; }

    public LocalDate getValidToDate() { return validToDate; }
    public void setValidToDate(LocalDate validToDate) { this.validToDate = validToDate; }

    public String getAllowedStartTime() { return allowedStartTime; }
    public void setAllowedStartTime(String allowedStartTime) { this.allowedStartTime = allowedStartTime; }

    public String getAllowedEndTime() { return allowedEndTime; }
    public void setAllowedEndTime(String allowedEndTime) { this.allowedEndTime = allowedEndTime; }

    public int getExpectedDurationMinutes() { return expectedDurationMinutes; }
    public void setExpectedDurationMinutes(int expectedDurationMinutes) { this.expectedDurationMinutes = expectedDurationMinutes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getDestinationBlockName() { return destinationBlockName; }
    public void setDestinationBlockName(String destinationBlockName) { this.destinationBlockName = destinationBlockName; }

    public String getBlockName() { return destinationBlockName; }
    public void setBlockName(String blockName) { this.destinationBlockName = blockName; }

    public String getDestinationFlatNumber() { return destinationFlatNumber; }
    public void setDestinationFlatNumber(String destinationFlatNumber) { this.destinationFlatNumber = destinationFlatNumber; }

    public String getFlatNumber() { return destinationFlatNumber; }
    public void setFlatNumber(String flatNumber) { this.destinationFlatNumber = flatNumber; }

    public String getResidentFullName() { return residentFullName; }
    public void setResidentFullName(String residentFullName) { this.residentFullName = residentFullName; }

    public String getResidentName() { return residentFullName; }
    public void setResidentName(String residentName) { this.residentFullName = residentName; }
}
