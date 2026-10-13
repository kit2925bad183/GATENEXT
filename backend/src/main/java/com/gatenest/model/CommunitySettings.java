package com.gatenest.model;

import java.time.Instant;

public class CommunitySettings {
    private Long id;
    private String communityName;
    private String address;
    private String city;
    private String postalCode;
    private String defaultTimezone;
    private String contactPhone;
    private String contactEmail;
    private int visitorPassValidityHours;
    private boolean demoMode;
    private Instant createdAt;
    private Instant updatedAt;

    public CommunitySettings() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCommunityName() { return communityName; }
    public void setCommunityName(String communityName) { this.communityName = communityName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getDefaultTimezone() { return defaultTimezone; }
    public void setDefaultTimezone(String defaultTimezone) { this.defaultTimezone = defaultTimezone; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public int getVisitorPassValidityHours() { return visitorPassValidityHours; }
    public void setVisitorPassValidityHours(int visitorPassValidityHours) { this.visitorPassValidityHours = visitorPassValidityHours; }

    public boolean isDemoMode() { return demoMode; }
    public void setDemoMode(boolean demoMode) { this.demoMode = demoMode; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
