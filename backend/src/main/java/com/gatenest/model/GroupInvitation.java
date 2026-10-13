package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GroupInvitation implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long hostUserId;
    private Long destinationFlatId;
    private String eventTitle;
    private String groupReference;
    private Instant arrivalWindowStart;
    private Instant arrivalWindowEnd;
    private int maxGuests;
    private String notes;
    private VisitStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private String destinationBlockName;
    private String destinationFlatNumber;
    private String hostFullName;
    private List<GroupInvitationMember> members = new ArrayList<>();
    private int arrivedGuestCount;
    private int totalGuestCount;
    private int insideGuestsCount;
    private int checkedOutGuestsCount;

    public GroupInvitation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getHostUserId() { return hostUserId; }
    public void setHostUserId(Long hostUserId) { this.hostUserId = hostUserId; }

    public Long getDestinationFlatId() { return destinationFlatId; }
    public void setDestinationFlatId(Long destinationFlatId) { this.destinationFlatId = destinationFlatId; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public String getGroupReference() { return groupReference; }
    public void setGroupReference(String groupReference) { this.groupReference = groupReference; }

    public Instant getArrivalWindowStart() { return arrivalWindowStart; }
    public void setArrivalWindowStart(Instant arrivalWindowStart) { this.arrivalWindowStart = arrivalWindowStart; }

    public Instant getArrivalWindowEnd() { return arrivalWindowEnd; }
    public void setArrivalWindowEnd(Instant arrivalWindowEnd) { this.arrivalWindowEnd = arrivalWindowEnd; }

    public int getMaxGuests() { return maxGuests; }
    public void setMaxGuests(int maxGuests) { this.maxGuests = maxGuests; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public VisitStatus getStatus() { return status; }
    public void setStatus(VisitStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getDestinationBlockName() { return destinationBlockName; }
    public void setDestinationBlockName(String destinationBlockName) { this.destinationBlockName = destinationBlockName; }

    public String getBlockName() { return destinationBlockName; }
    public void setBlockName(String blockName) { this.destinationBlockName = blockName; }

    public String getDestinationFlatNumber() { return destinationFlatNumber; }
    public void setDestinationFlatNumber(String destinationFlatNumber) { this.destinationFlatNumber = destinationFlatNumber; }

    public String getFlatNumber() { return destinationFlatNumber; }
    public void setFlatNumber(String flatNumber) { this.destinationFlatNumber = flatNumber; }

    public String getHostFullName() { return hostFullName; }
    public void setHostFullName(String hostFullName) { this.hostFullName = hostFullName; }

    public String getHostName() { return hostFullName; }
    public void setHostName(String hostName) { this.hostFullName = hostName; }

    public List<GroupInvitationMember> getMembers() { return members; }
    public void setMembers(List<GroupInvitationMember> members) { this.members = members; }

    public int getArrivedGuestCount() { return arrivedGuestCount; }
    public void setArrivedGuestCount(int arrivedGuestCount) { this.arrivedGuestCount = arrivedGuestCount; }

    public int getTotalGuestCount() { return totalGuestCount; }
    public void setTotalGuestCount(int totalGuestCount) { this.totalGuestCount = totalGuestCount; }

    public int getInsideGuestsCount() { return insideGuestsCount; }
    public void setInsideGuestsCount(int insideGuestsCount) { this.insideGuestsCount = insideGuestsCount; }

    public int getCheckedOutGuestsCount() { return checkedOutGuestsCount; }
    public void setCheckedOutGuestsCount(int checkedOutGuestsCount) { this.checkedOutGuestsCount = checkedOutGuestsCount; }
}
