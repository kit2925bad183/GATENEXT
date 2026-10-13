package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.model.Complaint;
import com.gatenest.model.ComplaintAttachment;
import com.gatenest.model.ComplaintComment;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.ComplaintService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    public static class UpdateStatusRequest {
        public String status;
        public Long assignedToUserId;
    }

    public static class ReopenRequest {
        public String reason;
    }

    public static class AddCommentRequest {
        public String commentText;
        public boolean internalNote;
    }

    @GetMapping("/my-complaints")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<List<Complaint>>> getMyComplaints(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Complaint> list = complaintService.getComplaintsForResident(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<Complaint>>> getAllComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        List<Complaint> list = complaintService.getAllComplaints(status, category, limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<Complaint>> getComplaintById(@PathVariable Long id) {
        Complaint c = complaintService.getComplaintById(id);
        return ResponseEntity.ok(ApiResponse.ok(c));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<Complaint>> createComplaint(
            @Valid @RequestBody Complaint complaint,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Complaint saved = complaintService.createComplaint(userDetails.getUser().getId(), complaint);
        return ResponseEntity.ok(ApiResponse.ok("Complaint registered successfully", saved));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Complaint>> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Complaint updated = complaintService.updateStatus(
                id, request.status, request.assignedToUserId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Complaint status updated", updated));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<Complaint>> reopenComplaint(
            @PathVariable Long id,
            @RequestBody ReopenRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Complaint reopened = complaintService.reopenComplaint(
                id, userDetails.getUser().getId(), request.reason, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Complaint reopened", reopened));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<ComplaintComment>> addComment(
            @PathVariable Long id,
            @RequestBody AddCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ComplaintComment comment = complaintService.addComment(
                id, userDetails.getUser().getId(), request.commentText, request.internalNote, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Comment added", comment));
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<ComplaintAttachment>> uploadAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        ComplaintAttachment ca = complaintService.uploadAttachment(id, file, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok("Attachment uploaded successfully", ca));
    }

    @GetMapping("/attachments/{attachmentId}/download")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        File file = complaintService.getAttachmentFile(attachmentId);
        ComplaintAttachment meta = complaintService.getAttachmentMetadata(attachmentId);

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(meta.getMimeType()))
                .body(resource);
    }
}
