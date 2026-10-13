package com.gatenest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateSecurityRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "Mobile number is required")
    private String mobile;

    @NotNull(message = "Primary gate assignment is required")
    private Long primaryGateId;

    private String shiftName;

    @NotBlank(message = "Temporary initial password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    public CreateSecurityRequest() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public Long getPrimaryGateId() { return primaryGateId; }
    public void setPrimaryGateId(Long primaryGateId) { this.primaryGateId = primaryGateId; }

    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
