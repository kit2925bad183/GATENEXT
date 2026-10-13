package com.gatenest.dto;

import com.gatenest.model.Role;
import com.gatenest.model.UserStatus;

import java.time.Instant;

public class UserDto {
    private Long id;
    private String email;
    private String fullName;
    private String mobile;
    private Role role;
    private UserStatus status;
    private Instant lastLoginAt;
    private Instant createdAt;

    // Attached resident info if applicable
    private Long residentProfileId;
    private Long flatId;
    private String flatNumber;
    private String blockName;
    private String occupancyType;
    private String verificationStatus;
    private String rejectionReason;

    // Attached security info if applicable
    private Long assignedGateId;
    private String assignedGateName;
    private String assignedGateCode;

    public UserDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Long getResidentProfileId() { return residentProfileId; }
    public void setResidentProfileId(Long residentProfileId) { this.residentProfileId = residentProfileId; }

    public Long getFlatId() { return flatId; }
    public void setFlatId(Long flatId) { this.flatId = flatId; }

    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    public String getOccupancyType() { return occupancyType; }
    public void setOccupancyType(String occupancyType) { this.occupancyType = occupancyType; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Long getAssignedGateId() { return assignedGateId; }
    public void setAssignedGateId(Long assignedGateId) { this.assignedGateId = assignedGateId; }

    public String getAssignedGateName() { return assignedGateName; }
    public void setAssignedGateName(String assignedGateName) { this.assignedGateName = assignedGateName; }

    public String getAssignedGateCode() { return assignedGateCode; }
    public void setAssignedGateCode(String assignedGateCode) { this.assignedGateCode = assignedGateCode; }
}
