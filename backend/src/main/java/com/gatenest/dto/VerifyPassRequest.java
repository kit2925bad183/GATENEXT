package com.gatenest.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyPassRequest {
    @NotBlank(message = "Pass code or scanned QR payload is required")
    private String code;

    public VerifyPassRequest() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
