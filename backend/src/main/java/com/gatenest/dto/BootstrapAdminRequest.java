package com.gatenest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BootstrapAdminRequest {
    @NotBlank(message = "Bootstrap authorization secret token is required")
    private String bootstrapToken;

    @NotBlank(message = "Admin full name is required")
    private String fullName;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "Admin mobile is required")
    private String mobile;

    @NotBlank(message = "Admin password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    public BootstrapAdminRequest() {}

    public String getBootstrapToken() { return bootstrapToken; }
    public void setBootstrapToken(String bootstrapToken) { this.bootstrapToken = bootstrapToken; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
