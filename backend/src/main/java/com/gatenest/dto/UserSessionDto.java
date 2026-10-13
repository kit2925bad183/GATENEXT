package com.gatenest.dto;

import java.time.Instant;

public class UserSessionDto {
    private Long id;
    private String deviceInfo;
    private String ipAddress;
    private Instant lastActivityAt;
    private Instant createdAt;
    private boolean currentSession;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(boolean currentSession) {
        this.currentSession = currentSession;
    }
}
