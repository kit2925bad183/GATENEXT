package com.gatenest.model;

import java.io.Serializable;
import java.time.Instant;

public class ComplaintComment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long complaintId;
    private Long authorUserId;
    private String authorNameSnapshot;
    private String authorRole;
    private String commentText;
    private boolean internalNote;
    private Instant createdAt;

    public ComplaintComment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }

    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long authorUserId) { this.authorUserId = authorUserId; }

    public String getAuthorNameSnapshot() { return authorNameSnapshot; }
    public void setAuthorNameSnapshot(String authorNameSnapshot) { this.authorNameSnapshot = authorNameSnapshot; }

    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public boolean isInternalNote() { return internalNote; }
    public void setInternalNote(boolean internalNote) { this.internalNote = internalNote; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
