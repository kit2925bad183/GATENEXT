package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class AnnouncementRead implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long announcementId;
    private Long userId;
    private Instant readAt;

    public AnnouncementRead() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(Long announcementId) { this.announcementId = announcementId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
}
