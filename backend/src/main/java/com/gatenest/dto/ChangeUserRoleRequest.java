package com.gatenest.dto;

import com.gatenest.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChangeUserRoleRequest {

    @NotNull(message = "New role is required")
    private Role newRole;

    @NotBlank(message = "Reason for role change is required")
    private String reason;

    @NotBlank(message = "Administrator password verification is required")
    private String adminPasswordVerification;

    public Role getNewRole() {
        return newRole;
    }

    public void setNewRole(Role newRole) {
        this.newRole = newRole;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAdminPasswordVerification() {
        return adminPasswordVerification;
    }

    public void setAdminPasswordVerification(String adminPasswordVerification) {
        this.adminPasswordVerification = adminPasswordVerification;
    }
}
