package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class Parcel implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long recipientUserId;
    private Long destinationFlatId;
    private String courierName;
    private String trackingNumber;
    private String description;
    private String storageLocation;
    private Long receivedByUserId;
    private Instant receivedAt;
    private ParcelStatus status;
    private String collectedByName;
    private Long collectedByUserId;
    private Long verifiedByUserId;
    private Instant collectedAt;
    private String delegateCollectorName;
    private String delegateNotes;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private String recipientFullName;
    private String recipientMobile;
    private String destinationBlockName;
    private String destinationFlatNumber;
    private String receivedByStaffName;
    private String verifiedByStaffName;

    public Parcel() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(Long recipientUserId) { this.recipientUserId = recipientUserId; }

    public Long getDestinationFlatId() { return destinationFlatId; }
    public void setDestinationFlatId(Long destinationFlatId) { this.destinationFlatId = destinationFlatId; }

    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public Long getReceivedByUserId() { return receivedByUserId; }
    public void setReceivedByUserId(Long receivedByUserId) { this.receivedByUserId = receivedByUserId; }

    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }

    public ParcelStatus getStatus() { return status; }
    public void setStatus(ParcelStatus status) { this.status = status; }

    public String getCollectedByName() { return collectedByName; }
    public void setCollectedByName(String collectedByName) { this.collectedByName = collectedByName; }

    public Long getCollectedByUserId() { return collectedByUserId; }
    public void setCollectedByUserId(Long collectedByUserId) { this.collectedByUserId = collectedByUserId; }

    public Long getVerifiedByUserId() { return verifiedByUserId; }
    public void setVerifiedByUserId(Long verifiedByUserId) { this.verifiedByUserId = verifiedByUserId; }

    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }

    public String getDelegateCollectorName() { return delegateCollectorName; }
    public void setDelegateCollectorName(String delegateCollectorName) { this.delegateCollectorName = delegateCollectorName; }

    public String getDelegateNotes() { return delegateNotes; }
    public void setDelegateNotes(String delegateNotes) { this.delegateNotes = delegateNotes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getRecipientFullName() { return recipientFullName; }
    public void setRecipientFullName(String recipientFullName) { this.recipientFullName = recipientFullName; }

    public String getRecipientName() { return recipientFullName; }
    public void setRecipientName(String recipientName) { this.recipientFullName = recipientName; }

    public String getRecipientMobile() { return recipientMobile; }
    public void setRecipientMobile(String recipientMobile) { this.recipientMobile = recipientMobile; }

    public String getDestinationBlockName() { return destinationBlockName; }
    public void setDestinationBlockName(String destinationBlockName) { this.destinationBlockName = destinationBlockName; }

    public String getBlockName() { return destinationBlockName; }
    public void setBlockName(String blockName) { this.destinationBlockName = blockName; }

    public String getDestinationFlatNumber() { return destinationFlatNumber; }
    public void setDestinationFlatNumber(String destinationFlatNumber) { this.destinationFlatNumber = destinationFlatNumber; }

    public String getFlatNumber() { return destinationFlatNumber; }
    public void setFlatNumber(String flatNumber) { this.destinationFlatNumber = flatNumber; }

    public String getReceivedByStaffName() { return receivedByStaffName; }
    public void setReceivedByStaffName(String receivedByStaffName) { this.receivedByStaffName = receivedByStaffName; }

    public String getReceivedByName() { return receivedByStaffName; }
    public void setReceivedByName(String receivedByName) { this.receivedByStaffName = receivedByName; }

    public String getVerifiedByStaffName() { return verifiedByStaffName; }
    public void setVerifiedByStaffName(String verifiedByStaffName) { this.verifiedByStaffName = verifiedByStaffName; }

    public String getVerifiedByName() { return verifiedByStaffName; }
    public void setVerifiedByName(String verifiedByName) { this.verifiedByStaffName = verifiedByName; }
}
