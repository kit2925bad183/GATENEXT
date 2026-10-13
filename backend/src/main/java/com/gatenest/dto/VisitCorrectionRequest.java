package com.gatenest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class VisitCorrectionRequest {

    @NotBlank(message = "Field name is required (e.g. check_in_time or check_out_time)")
    private String fieldName;

    @NotNull(message = "Corrected timestamp is required")
    private Instant correctedTime;

    @NotBlank(message = "Reason for time correction is required")
    private String reason;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Instant getCorrectedTime() {
        return correctedTime;
    }

    public void setCorrectedTime(Instant correctedTime) {
        this.correctedTime = correctedTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
