package com.gatenest.service;

import com.gatenest.dto.PagedResponse;
import com.gatenest.exception.BadRequestException;
import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.*;
import com.gatenest.repository.ResidentProfileRepository;
import com.gatenest.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ResidentManagementService {

    private final ResidentProfileRepository residentProfileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public ResidentManagementService(
            ResidentProfileRepository residentProfileRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            AuditLogService auditLogService
    ) {
        this.residentProfileRepository = residentProfileRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    public List<ResidentProfile> getPendingRegistrations(int offset, int limit) {
        return residentProfileRepository.findPendingProfiles(offset, limit);
    }

    public PagedResponse<ResidentProfile> searchResidents(String query, Long blockId, Long flatId, VerificationStatus status, int page, int limit) {
        int offset = (page - 1) * limit;
        List<ResidentProfile> items = residentProfileRepository.searchResidents(query, blockId, flatId, status, offset, limit);
        int total = residentProfileRepository.countResidents(query, blockId, flatId, status);
        return new PagedResponse<>(items, total, page, limit);
    }

    public ResidentProfile getResidentProfileById(Long id) {
        return residentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident profile not found with ID: " + id));
    }

    @Transactional
    public ResidentProfile approveResident(Long profileId, User admin, String clientIp) {
        ResidentProfile profile = getResidentProfileById(profileId);
        User user = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for resident profile."));

        profile.setVerificationStatus(VerificationStatus.APPROVED);
        profile.setRejectionReason(null);
        profile.setReviewedByUserId(admin.getId());
        profile.setReviewedAt(Instant.now());
        residentProfileRepository.save(profile);

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        notificationService.sendNotification(
                user.getId(),
                "Resident Registration Approved",
                "Congratulations! Your registration for Flat " + profile.getFlatNumber() + " (" + profile.getBlockName() + ") has been approved by the management.",
                "/resident/dashboard",
                NotificationType.RESIDENT_APPROVED
        );

        auditLogService.logAction(admin, "RESIDENT_APPROVED", "RESIDENT_PROFILE", String.valueOf(profileId), clientIp, Map.of(
                "residentEmail", user.getEmail(),
                "flatNumber", profile.getFlatNumber()
        ));

        return profile;
    }

    @Transactional
    public ResidentProfile rejectResident(Long profileId, String reason, User admin, String clientIp) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException("Rejection reason is required.");
        }

        ResidentProfile profile = getResidentProfileById(profileId);
        User user = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for resident profile."));

        profile.setVerificationStatus(VerificationStatus.REJECTED);
        profile.setRejectionReason(reason.trim());
        profile.setReviewedByUserId(admin.getId());
        profile.setReviewedAt(Instant.now());
        residentProfileRepository.save(profile);

        user.setStatus(UserStatus.DEACTIVATED);
        userRepository.save(user);

        notificationService.sendNotification(
                user.getId(),
                "Resident Registration Update",
                "Your registration for Flat " + profile.getFlatNumber() + " could not be approved. Reason: " + reason.trim(),
                "/auth/application-status",
                NotificationType.RESIDENT_REJECTED
        );

        auditLogService.logAction(admin, "RESIDENT_REJECTED", "RESIDENT_PROFILE", String.valueOf(profileId), clientIp, Map.of(
                "residentEmail", user.getEmail(),
                "reason", reason.trim()
        ));

        return profile;
    }

    @Transactional
    public void setResidentStatus(Long userId, UserStatus newStatus, User admin, String clientIp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getRole() != Role.RESIDENT) {
            throw new BadRequestException("Only resident accounts can be updated via this endpoint.");
        }

        user.setStatus(newStatus);
        userRepository.save(user);

        auditLogService.logAction(admin, "RESIDENT_STATUS_CHANGED", "USER", String.valueOf(userId), clientIp, Map.of(
                "newStatus", newStatus.name()
        ));
    }

    public List<ResidentProfile> getApprovedResidentsForFlat(Long flatId) {
        return residentProfileRepository.findApprovedByFlatId(flatId);
    }
}
