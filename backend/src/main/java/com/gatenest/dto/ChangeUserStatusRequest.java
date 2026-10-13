package com.gatenest.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangeUserStatusRequest {

    @NotBlank(message = "New status is required (e.g. ACTIVE, SUSPENDED, DEACTIVATED)")
    private String newStatus;

    private String reason;

    private String adminPasswordVerification;

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
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
