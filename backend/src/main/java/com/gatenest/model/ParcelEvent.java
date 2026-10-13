package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class ParcelEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parcelId;
    private String eventType;
    private Long actorUserId;
    private String notes;
    private Instant createdAt;

    // Enriched fields
    private String actorFullName;

    public ParcelEvent() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getParcelId() { return parcelId; }
    public void setParcelId(Long parcelId) { this.parcelId = parcelId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getActorFullName() { return actorFullName; }
    public void setActorFullName(String actorFullName) { this.actorFullName = actorFullName; }

    public String getActorName() { return actorFullName; }
    public void setActorName(String actorName) { this.actorFullName = actorName; }
}
