package com.gatenest.model;

import java.time.Instant;

public class VisitEvent {
    private Long id;
    private Long visitId;
    private VisitEventType eventType;
    private Long actorUserId;
    private Long gateId;
    private String notes;
    private String metadataJson;
    private Instant createdAt;

    // Joins
    private String actorName;
    private String actorRole;
    private String gateName;

    public VisitEvent() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVisitId() { return visitId; }
    public void setVisitId(Long visitId) { this.visitId = visitId; }

    public VisitEventType getEventType() { return eventType; }
    public void setEventType(VisitEventType eventType) { this.eventType = eventType; }

    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }

    public Long getGateId() { return gateId; }
    public void setGateId(Long gateId) { this.gateId = gateId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public String getGateName() { return gateName; }
    public void setGateName(String gateName) { this.gateName = gateName; }
}
