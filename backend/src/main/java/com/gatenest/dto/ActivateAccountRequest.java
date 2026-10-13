package com.gatenest.dto;

import jakarta.validation.constraints.Size;

public class ActivateAccountRequest {

    private String token;
    private String password;
    private String newPassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getEffectivePassword() {
        if (password != null && !password.isBlank()) {
            return password;
        }
        return newPassword;
    }
}
