package com.gatenest.service;

import com.gatenest.exception.BadRequestException;
import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.*;
import com.gatenest.repository.ComplaintRepository;
import com.gatenest.repository.ResidentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ResidentProfileRepository residentProfileRepository;
    private final NotificationService notificationService;

    private static final String ATTACHMENTS_DIR = "./data/attachments";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public ComplaintService(ComplaintRepository complaintRepository,
                            ResidentProfileRepository residentProfileRepository,
                            NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.residentProfileRepository = residentProfileRepository;
        this.notificationService = notificationService;

        // Ensure attachments directory exists
        try {
            Files.createDirectories(Paths.get(ATTACHMENTS_DIR));
        } catch (IOException ignored) {}
    }

    public List<Complaint> getComplaintsForResident(Long residentUserId) {
        return complaintRepository.findByResidentUserId(residentUserId);
    }

    public List<Complaint> getAllComplaints(String status, String category, int limit, int offset) {
        return complaintRepository.findAll(status, category, limit, offset);
    }

    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
    }

    @Transactional
    public Complaint createComplaint(Long residentUserId, Complaint complaint) {
        ResidentProfile profile = residentProfileRepository.findByUserId(residentUserId)
                .orElseThrow(() -> new BadRequestException("Resident profile not found for user: " + residentUserId));

        complaint.setId(null);
        complaint.setResidentUserId(residentUserId);
        complaint.setFlatId(profile.getFlatId());
        complaint.setStatus("OPEN");
        complaint.setReopenedCount(0);
        return complaintRepository.save(complaint);
    }

    @Transactional
    public Complaint updateStatus(Long complaintId, String newStatus, Long assignedToUserId, User actingUser) {
        Complaint complaint = getComplaintById(complaintId);
        complaint.setStatus(newStatus);
        if (assignedToUserId != null) {
            complaint.setAssignedToUserId(assignedToUserId);
        }
        if ("RESOLVED".equals(newStatus)) {
            complaint.setResolvedAt(Instant.now());
        } else if ("CLOSED".equals(newStatus)) {
            complaint.setClosedAt(Instant.now());
        }

        Complaint saved = complaintRepository.save(complaint);

        // Add auto comment
        ComplaintComment comment = new ComplaintComment();
        comment.setComplaintId(complaintId);
        comment.setAuthorUserId(actingUser.getId());
        comment.setAuthorNameSnapshot(actingUser.getFullName());
        comment.setAuthorRole(actingUser.getRole().name());
        comment.setCommentText("Status changed to " + newStatus + (assignedToUserId != null ? " (Assigned)" : ""));
        comment.setInternalNote(false);
        complaintRepository.saveComment(comment);

        notificationService.sendNotification(
                complaint.getResidentUserId(),
                "Complaint Status Updated",
                "Your complaint '" + complaint.getSubject() + "' status changed to " + newStatus,
                "/complaints",
                NotificationType.COMPLAINT_UPDATE
        );

        return getComplaintById(saved.getId());
    }

    @Transactional
    public Complaint reopenComplaint(Long complaintId, Long residentUserId, String reason, User actingUser) {
        Complaint complaint = getComplaintById(complaintId);
        if (!complaint.getResidentUserId().equals(residentUserId)) {
            throw new BadRequestException("You are not authorized to reopen this complaint");
        }
        if (!"RESOLVED".equals(complaint.getStatus()) && !"CLOSED".equals(complaint.getStatus())) {
            throw new BadRequestException("Only resolved or closed complaints can be reopened");
        }

        complaint.setStatus("OPEN");
        complaint.setReopenedCount(complaint.getReopenedCount() + 1);
        complaintRepository.save(complaint);

        ComplaintComment comment = new ComplaintComment();
        comment.setComplaintId(complaintId);
        comment.setAuthorUserId(residentUserId);
        comment.setAuthorNameSnapshot(actingUser.getFullName());
        comment.setAuthorRole(actingUser.getRole().name());
        comment.setCommentText("Complaint reopened by resident: " + reason);
        comment.setInternalNote(false);
        complaintRepository.saveComment(comment);

        return getComplaintById(complaintId);
    }

    public ComplaintComment addComment(Long complaintId, Long userId, String text, boolean internalNote, User user) {
        Complaint complaint = getComplaintById(complaintId);
        ComplaintComment comment = new ComplaintComment();
        comment.setComplaintId(complaintId);
        comment.setAuthorUserId(userId);
        comment.setAuthorNameSnapshot(user.getFullName());
        comment.setAuthorRole(user.getRole().name());
        comment.setCommentText(text.trim());
        comment.setInternalNote(internalNote);
        ComplaintComment saved = complaintRepository.saveComment(comment);

        if (!internalNote && !user.getId().equals(complaint.getResidentUserId())) {
            notificationService.sendNotification(
                    complaint.getResidentUserId(),
                    "New Note on Complaint",
                    "A new update was added to your complaint '" + complaint.getSubject() + "'",
                    "/complaints",
                    NotificationType.COMPLAINT_UPDATE
            );
        }

        return saved;
    }

    @Transactional
    public ComplaintAttachment uploadAttachment(Long complaintId, MultipartFile file, Long userId) throws IOException {
        if (file.isEmpty()) {
            throw new BadRequestException("Attachment file cannot be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds 10MB limit");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "attachment.bin";
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        }

        String safeFileName = UUID.randomUUID().toString() + extension;
        Path targetPath = Paths.get(ATTACHMENTS_DIR, safeFileName);
        Files.copy(file.getInputStream(), targetPath);

        ComplaintAttachment ca = new ComplaintAttachment();
        ca.setComplaintId(complaintId);
        ca.setFileName(safeFileName);
        ca.setOriginalFileName(originalFilename);
        ca.setFileSizeBytes(file.getSize());
        ca.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        ca.setStoragePath(targetPath.toString());
        ca.setUploadedByUserId(userId);

        return complaintRepository.saveAttachment(ca);
    }

    public File getAttachmentFile(Long attachmentId) {
        ComplaintAttachment ca = complaintRepository.findAttachmentById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
        File file = new File(ca.getStoragePath());
        if (!file.exists()) {
            throw new ResourceNotFoundException("Attachment file on disk not found");
        }
        return file;
    }

    public ComplaintAttachment getAttachmentMetadata(Long attachmentId) {
        return complaintRepository.findAttachmentById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
    }
}
