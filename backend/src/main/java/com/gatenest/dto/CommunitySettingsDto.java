package com.gatenest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class CommunitySettingsDto {
    private Long id;

    @NotBlank(message = "Community name is required")
    private String communityName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Postal code is required")
    private String postalCode;

    private String defaultTimezone = "Asia/Kolkata";
    private String contactPhone;
    private String contactEmail;

    @Positive(message = "Visitor pass validity must be at least 1 hour")
    private int visitorPassValidityHours = 12;

    public CommunitySettingsDto() {}

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
}
