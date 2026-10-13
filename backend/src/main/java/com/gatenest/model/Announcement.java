package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class Announcement implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long authorUserId;
    private String title;
    private String content;
    private String announcementType; // GENERAL, MAINTENANCE, WATER_POWER, EVENT, EMERGENCY
    private String priority; // NORMAL, IMPORTANT, URGENT
    private String audienceType; // COMMUNITY, BLOCK
    private Long targetBlockId;
    private boolean pinned;
    private boolean published;
    private Instant publishedAt;
    private Instant expiresAt;
    private boolean archived;
    private Instant createdAt;
    private Instant updatedAt;

    // Enriched fields
    private String authorName;
    private String targetBlockName;
    private boolean readByCurrentUser;

    public Announcement() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long authorUserId) { this.authorUserId = authorUserId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAnnouncementType() { return announcementType; }
    public void setAnnouncementType(String announcementType) { this.announcementType = announcementType; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getAudienceType() { return audienceType; }
    public void setAudienceType(String audienceType) { this.audienceType = audienceType; }

    public Long getTargetBlockId() { return targetBlockId; }
    public void setTargetBlockId(Long targetBlockId) { this.targetBlockId = targetBlockId; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getTargetBlockName() { return targetBlockName; }
    public void setTargetBlockName(String targetBlockName) { this.targetBlockName = targetBlockName; }

    public boolean isReadByCurrentUser() { return readByCurrentUser; }
    public void setReadByCurrentUser(boolean readByCurrentUser) { this.readByCurrentUser = readByCurrentUser; }
}
