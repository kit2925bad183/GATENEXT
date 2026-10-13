package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class SecurityGateAssignment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long gateId;
    private boolean active;
    private String shiftName;
    private Instant assignedAt;
    private Instant createdAt;

    // Joins
    private String userFullName;
    private String userEmail;
    private String userMobile;
    private String gateName;
    private String gateCode;

    public SecurityGateAssignment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getGateId() { return gateId; }
    public void setGateId(Long gateId) { this.gateId = gateId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }

    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserMobile() { return userMobile; }
    public void setUserMobile(String userMobile) { this.userMobile = userMobile; }

    public String getGateName() { return gateName; }
    public void setGateName(String gateName) { this.gateName = gateName; }

    public String getGateCode() { return gateCode; }
    public void setGateCode(String gateCode) { this.gateCode = gateCode; }
}
