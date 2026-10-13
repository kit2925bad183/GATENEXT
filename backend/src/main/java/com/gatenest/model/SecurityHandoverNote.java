package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class SecurityHandoverNote implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String shiftName;
    private Long gateId;
    private Long authorUserId;
    private String noteText;
    private boolean acknowledged;
    private Long acknowledgedByUserId;
    private Instant acknowledgedAt;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private String gateName;
    private String authorName;
    private String acknowledgedByName;

    public SecurityHandoverNote() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }

    public Long getGateId() { return gateId; }
    public void setGateId(Long gateId) { this.gateId = gateId; }

    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long authorUserId) { this.authorUserId = authorUserId; }

    public String getNoteText() { return noteText; }
    public void setNoteText(String noteText) { this.noteText = noteText; }

    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }

    public Long getAcknowledgedByUserId() { return acknowledgedByUserId; }
    public void setAcknowledgedByUserId(Long acknowledgedByUserId) { this.acknowledgedByUserId = acknowledgedByUserId; }

    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Instant acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getGateName() { return gateName; }
    public void setGateName(String gateName) { this.gateName = gateName; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAcknowledgedByName() { return acknowledgedByName; }
    public void setAcknowledgedByName(String acknowledgedByName) { this.acknowledgedByName = acknowledgedByName; }
}
