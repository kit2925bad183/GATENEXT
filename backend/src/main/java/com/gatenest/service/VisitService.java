package com.gatenest.service;

import com.gatenest.dto.*;
import com.gatenest.exception.BadRequestException;
import com.gatenest.exception.ForbiddenException;
import com.gatenest.exception.InvalidVisitStateException;
import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.*;
import com.gatenest.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VisitService {

    private static final Logger log = LoggerFactory.getLogger(VisitService.class);

    private final VisitRepository visitRepository;
    private final VisitEventRepository visitEventRepository;
    private final VisitCorrectionRepository visitCorrectionRepository;
    private final UserRepository userRepository;
    private final ResidentProfileRepository residentProfileRepository;
    private final FlatRepository flatRepository;
    private final GateRepository gateRepository;
    private final PassCredentialService passCredentialService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final CommunitySettingsRepository communitySettingsRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public VisitService(
            VisitRepository visitRepository,
            VisitEventRepository visitEventRepository,
            VisitCorrectionRepository visitCorrectionRepository,
            UserRepository userRepository,
            ResidentProfileRepository residentProfileRepository,
            FlatRepository flatRepository,
            GateRepository gateRepository,
            PassCredentialService passCredentialService,
            NotificationService notificationService,
            AuditLogService auditLogService,
            CommunitySettingsRepository communitySettingsRepository
    ) {
        this.visitRepository = visitRepository;
        this.visitEventRepository = visitEventRepository;
        this.visitCorrectionRepository = visitCorrectionRepository;
        this.userRepository = userRepository;
        this.residentProfileRepository = residentProfileRepository;
        this.flatRepository = flatRepository;
        this.gateRepository = gateRepository;
        this.passCredentialService = passCredentialService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.communitySettingsRepository = communitySettingsRepository;
    }

    private String generateVisitReference() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand = 1000 + secureRandom.nextInt(9000);
        return "GN-" + datePart + "-" + rand;
    }

    @Transactional
    public PassCredentialDto createInvitation(CreateInvitationRequest request, User hostUser, String clientIp) {
        ResidentProfile profile = residentProfileRepository.findByUserId(hostUser.getId())
                .orElseThrow(() -> new ForbiddenException("Only verified residents can create invitations."));

        if (profile.getVerificationStatus() != VerificationStatus.APPROVED || hostUser.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("Your resident registration is not yet approved or is suspended.");
        }

        if (request.getArrivalWindowEnd().isBefore(request.getArrivalWindowStart())) {
            throw new BadRequestException("Arrival window end time must be after start time.");
        }

        String visitReference = generateVisitReference();
        String passToken = passCredentialService.generatePassToken();
        String passHash = passCredentialService.hashCredential(passToken);

        Visit visit = new Visit();
        visit.setVisitReference(visitReference);
        visit.setHostUserId(hostUser.getId());
        visit.setDestinationFlatId(profile.getFlatId());
        visit.setDestinationBlockName(profile.getBlockName());
        visit.setDestinationFlatNumber(profile.getFlatNumber());
        visit.setHostNameSnapshot(hostUser.getFullName());
        visit.setHostMobileSnapshot(hostUser.getMobile());

        visit.setVisitorName(request.getVisitorName().trim());
        visit.setVisitorMobile(request.getVisitorMobile().trim());
        visit.setVisitorCategory(request.getVisitorCategory());
        visit.setPurpose(request.getPurpose().trim());
        visit.setVehicleNumber(request.getVehicleNumber());
        visit.setNotes(request.getNotes());

        visit.setArrivalWindowStart(request.getArrivalWindowStart());
        visit.setArrivalWindowEnd(request.getArrivalWindowEnd());
        visit.setExpectedDurationMinutes(request.getExpectedDurationMinutes());
        visit.setStatus(VisitStatus.APPROVED);

        visit.setPassCredentialHash(passHash);
        visit.setPassGeneratedAt(Instant.now());
        visit.setRegisteredByUserId(hostUser.getId());
        visit.setApprovedByUserId(hostUser.getId());
        visit.setApprovedAt(Instant.now());

        visit = visitRepository.save(visit);

        // Record Visit Events
        VisitEvent createEvent = new VisitEvent();
        createEvent.setVisitId(visit.getId());
        createEvent.setEventType(VisitEventType.CREATED);
        createEvent.setActorUserId(hostUser.getId());
        createEvent.setNotes("Invitation generated by resident host");
        visitEventRepository.save(createEvent);

        VisitEvent approveEvent = new VisitEvent();
        approveEvent.setVisitId(visit.getId());
        approveEvent.setEventType(VisitEventType.APPROVED);
        approveEvent.setActorUserId(hostUser.getId());
        approveEvent.setNotes("Self-approved invitation");
        visitEventRepository.save(approveEvent);

        auditLogService.logAction(hostUser, "INVITATION_CREATED", "VISIT", String.valueOf(visit.getId()), clientIp, Map.of(
                "reference", visitReference,
                "visitorName", visit.getVisitorName()
        ));

        // Generate QR code data URL
        String qrPayload = "GATENEST:" + visitReference + ":" + passToken;
        String qrCodeDataUrl = passCredentialService.generateQrCodeDataUrl(qrPayload);

        PassCredentialDto credDto = new PassCredentialDto();
        credDto.setVisitReference(visitReference);
        credDto.setPassToken(passToken);
        credDto.setQrCodeDataUrl(qrCodeDataUrl);
        credDto.setVisitorName(visit.getVisitorName());
        credDto.setDestinationBlockName(visit.getDestinationBlockName());
        credDto.setDestinationFlatNumber(visit.getDestinationFlatNumber());
        credDto.setHostName(hostUser.getFullName());
        credDto.setArrivalWindowStart(visit.getArrivalWindowStart());
        credDto.setArrivalWindowEnd(visit.getArrivalWindowEnd());

        return credDto;
    }

    @Transactional
    public Visit registerWalkIn(RegisterWalkInRequest request, User securityUser, String clientIp) {
        User hostUser = userRepository.findById(request.getHostUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Host resident not found."));

        ResidentProfile profile = residentProfileRepository.findByUserId(hostUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resident profile not found for host."));

        if (profile.getVerificationStatus() != VerificationStatus.APPROVED || hostUser.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Selected host resident is not currently active or approved.");
        }

        Gate gate = gateRepository.findById(request.getArrivalGateId())
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found."));

        String visitReference = generateVisitReference();
        Instant now = Instant.now();
        Instant windowEnd = now.plusSeconds(12 * 3600L); // 12 hours window for walk-in

        Visit visit = new Visit();
        visit.setVisitReference(visitReference);
        visit.setHostUserId(hostUser.getId());
        visit.setDestinationFlatId(profile.getFlatId());
        visit.setDestinationBlockName(profile.getBlockName());
        visit.setDestinationFlatNumber(profile.getFlatNumber());
        visit.setHostNameSnapshot(hostUser.getFullName());
        visit.setHostMobileSnapshot(hostUser.getMobile());

        visit.setVisitorName(request.getVisitorName().trim());
        visit.setVisitorMobile(request.getVisitorMobile().trim());
        visit.setVisitorCategory(request.getVisitorCategory());
        visit.setPurpose(request.getPurpose().trim());
        visit.setVehicleNumber(request.getVehicleNumber());
        visit.setNotes(request.getNotes());

        visit.setArrivalWindowStart(now);
        visit.setArrivalWindowEnd(windowEnd);
        visit.setExpectedDurationMinutes(request.getExpectedDurationMinutes());
        visit.setStatus(VisitStatus.PENDING_APPROVAL);

        visit.setEntryGateId(gate.getId());
        visit.setRegisteredByUserId(securityUser.getId());

        visit = visitRepository.save(visit);

        // Record Event
        VisitEvent event = new VisitEvent();
        event.setVisitId(visit.getId());
        event.setEventType(VisitEventType.CREATED);
        event.setActorUserId(securityUser.getId());
        event.setGateId(gate.getId());
        event.setNotes("Walk-in visitor registered at " + gate.getName());
        visitEventRepository.save(event);

        // Notify Host Resident
        notificationService.sendNotification(
                hostUser.getId(),
                "Visitor Arrival Request",
                visit.getVisitorName() + " (" + visit.getVisitorCategory() + ") is waiting at " + gate.getName() + " for Flat " + profile.getFlatNumber() + ". Please approve or reject.",
                "/resident/approval-inbox",
                NotificationType.VISITOR_REQUEST
        );

        auditLogService.logAction(securityUser, "WALKIN_REGISTERED", "VISIT", String.valueOf(visit.getId()), clientIp, Map.of(
                "reference", visitReference,
                "visitorName", visit.getVisitorName(),
                "gate", gate.getName()
        ));

        return visit;
    }

    @Transactional
    public Visit approveVisit(Long visitId, ApproveVisitRequest request, User actor, String clientIp) {
        Visit visit = getVisitById(visitId);

        if (visit.getStatus() != VisitStatus.PENDING_APPROVAL) {
            throw new InvalidVisitStateException("Only visits in PENDING_APPROVAL status can be approved. Current status: " + visit.getStatus());
        }

        // Only destination host or admin can approve
        if (actor.getRole() != Role.ADMINISTRATOR && !visit.getHostUserId().equals(actor.getId())) {
            throw new ForbiddenException("You are not authorized to approve visits for this resident.");
        }

        String passToken = passCredentialService.generatePassToken();
        String passHash = passCredentialService.hashCredential(passToken);

        visit.setStatus(VisitStatus.APPROVED);
        visit.setPassCredentialHash(passHash);
        visit.setPassGeneratedAt(Instant.now());
        visit.setApprovedByUserId(actor.getId());
        visit.setApprovedAt(Instant.now());
        if (request != null && request.getNotes() != null && !request.getNotes().isBlank()) {
            visit.setNotes((visit.getNotes() != null ? visit.getNotes() + " | " : "") + "Approval note: " + request.getNotes());
        }

        visit = visitRepository.save(visit);

        VisitEvent event = new VisitEvent();
        event.setVisitId(visit.getId());
        event.setEventType(VisitEventType.APPROVED);
        event.setActorUserId(actor.getId());
        event.setNotes(request != null ? request.getNotes() : "Approved by host");
        visitEventRepository.save(event);

        // Notify security / registrar if different from host
        if (!visit.getRegisteredByUserId().equals(actor.getId())) {
            notificationService.sendNotification(
                    visit.getRegisteredByUserId(),
                    "Visitor Approved",
                    "Visit " + visit.getVisitReference() + " for " + visit.getVisitorName() + " to Flat " + visit.getDestinationFlatNumber() + " has been APPROVED.",
                    "/security/waiting-approvals",
                    NotificationType.VISITOR_APPROVED
            );
        }

        auditLogService.logAction(actor, "VISIT_APPROVED", "VISIT", String.valueOf(visit.getId()), clientIp, Map.of(
                "reference", visit.getVisitReference()
        ));

        return visit;
    }

    @Transactional
    public Visit rejectVisit(Long visitId, RejectVisitRequest request, User actor, String clientIp) {
        Visit visit = getVisitById(visitId);

        if (visit.getStatus() != VisitStatus.PENDING_APPROVAL) {
            throw new InvalidVisitStateException("Only visits in PENDING_APPROVAL status can be rejected. Current status: " + visit.getStatus());
        }

        if (actor.getRole() != Role.ADMINISTRATOR && !visit.getHostUserId().equals(actor.getId())) {
            throw new ForbiddenException("You are not authorized to reject visits for this resident.");
        }

        visit.setStatus(VisitStatus.REJECTED);
        visit.setRejectionReason(request.getReason().trim());
        visit = visitRepository.save(visit);

        VisitEvent event = new VisitEvent();
        event.setVisitId(visit.getId());
        event.setEventType(VisitEventType.REJECTED);
        event.setActorUserId(actor.getId());
        event.setNotes("Rejection reason: " + request.getReason().trim());
        visitEventRepository.save(event);

        if (!visit.getRegisteredByUserId().equals(actor.getId())) {
            notificationService.sendNotification(
                    visit.getRegisteredByUserId(),
                    "Visitor Entry Rejected",
                    "Visit " + visit.getVisitReference() + " for " + visit.getVisitorName() + " to Flat " + visit.getDestinationFlatNumber() + " was REJECTED: " + request.getReason().trim(),
                    "/security/waiting-approvals",
                    NotificationType.VISITOR_REJECTED
            );
        }

        auditLogService.logAction(actor, "VISIT_REJECTED", "VISIT", String.valueOf(visit.getId()), clientIp, Map.of(
                "reference", visit.getVisitReference(),
                "reason", request.getReason()
        ));

        return visit;
    }

    @Transactional
    public Visit adminOverride(Long visitId, AdminOverrideRequest request, User admin, String clientIp) {
        Visit visit = getVisitById(visitId);

        if (visit.getStatus() == VisitStatus.INSIDE || visit.getStatus() == VisitStatus.EXITED) {
            throw new InvalidVisitStateException("Cannot override visit that is already inside or exited.");
        }

        String passToken = passCredentialService.generatePassToken();
        String passHash = passCredentialService.hashCredential(passToken);

        visit.setStatus(VisitStatus.APPROVED);
        visit.setPassCredentialHash(passHash);
        visit.setPassGeneratedAt(Instant.now());
        visit.setAdminOverrideReason(request.getOverrideReason().trim());
        visit.setAdminOverrideByUserId(admin.getId());
        visit.setApprovedByUserId(admin.getId());
        visit.setApprovedAt(Instant.now());
        visit = visitRepository.save(visit);

        VisitEvent event = new VisitEvent();
        event.setVisitId(visit.getId());
        event.setEventType(VisitEventType.OVERRIDDEN);
        event.setActorUserId(admin.getId());
        event.setNotes("Administrator override: " + request.getOverrideReason().trim());
        visitEventRepository.save(event);

        notificationService.sendNotification(
                visit.getHostUserId(),
                "Visitor Approval Override",
                "Administrator approved visit " + visit.getVisitReference() + " for " + visit.getVisitorName() + " with override reason: " + request.getOverrideReason(),
                "/resident/dashboard",
                NotificationType.VISITOR_APPROVED
        );

        auditLogService.logAction(admin, "ADMIN_OVERRIDE_VISIT", "VISIT", String.valueOf(visit.getId()), clientIp, Map.of(
                "reference", visit.getVisitReference(),
                "overrideReason", request.getOverrideReason()
        ));

        return visit;
    }

    public PassVerificationResponse verifyPass(VerifyPassRequest request) {
        String rawInput = request.getCode().trim();
        Visit visit = null;

        // Check if QR code payload formatted as "GATENEST:GN-REF:TOKEN"
        if (rawInput.startsWith("GATENEST:")) {
            String[] parts = rawInput.split(":");
            if (parts.length >= 3) {
                String token = parts[2];
                String hash = passCredentialService.hashCredential(token);
                visit = visitRepository.findByCredentialHash(hash).orElse(null);
            }
        }

        if (visit == null) {
            // Check direct reference
            visit = visitRepository.findByReference(rawInput).orElse(null);
        }

        if (visit == null) {
            // Check direct token string hashed
            String hash = passCredentialService.hashCredential(rawInput);
            visit = visitRepository.findByCredentialHash(hash).orElse(null);
        }

        if (visit == null) {
            return PassVerificationResponse.invalid("Invalid pass: No matching visit record found.", null);
        }

        // Check host account status
        Optional<User> host = userRepository.findById(visit.getHostUserId());
        if (host.isEmpty() || host.get().getStatus() != UserStatus.ACTIVE) {
            return PassVerificationResponse.invalid("Cannot admit visitor: Host resident account is suspended or inactive.", visit);
        }

        Instant now = Instant.now();
        boolean isWithinWindow = !now.isBefore(visit.getArrivalWindowStart()) && !now.isAfter(visit.getArrivalWindowEnd());

        if (visit.getStatus() == VisitStatus.APPROVED) {
            if (!isWithinWindow) {
                return PassVerificationResponse.invalid("Pass is APPROVED but current time is outside the permitted arrival window.", visit);
            }
            return PassVerificationResponse.valid(visit, true, false, true);
        } else if (visit.getStatus() == VisitStatus.INSIDE) {
            return PassVerificationResponse.valid(visit, false, true, true);
        } else {
            return PassVerificationResponse.invalid("Pass cannot be used. Current visit status is " + visit.getStatus(), visit);
        }
    }

    @Transactional
    public Visit checkIn(Long visitId, CheckInRequest request, User securityUser, String clientIp) {
        Visit visit = getVisitById(visitId);

        if (visit.getStatus() != VisitStatus.APPROVED) {
            throw new InvalidVisitStateException("Cannot check-in visitor. Visit status must be APPROVED, but is " + visit.getStatus());
        }

        Gate gate = gateRepository.findById(request.getGateId())
                .orElseThrow(() -> new ResourceNotFoundException("Entry gate not found."));

        if (!gate.isActive()) {
            throw new BadRequestException("Specified entry gate is inactive.");
        }

        Instant now = Instant.now();
        if (now.isAfter(visit.getArrivalWindowEnd())) {
            throw new InvalidVisitStateException("Arrival window for this pass has expired.");
        }

        visit.setStatus(VisitStatus.INSIDE);
        visit.setEntryGateId(gate.getId());
        visit.setCheckInTime(now);
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            visit.setNotes((visit.getNotes() != null ? visit.getNotes() + " | " : "") + "Gate note: " + request.getNotes());
        }
        visit = visitRepository.save(visit);

        VisitEvent event = new VisitEvent();
        event.setVisitId(visit.getId());
        event.setEventType(VisitEventType.CHECKED_IN);
        event.setActorUserId(securityUser.getId());
        event.setGateId(gate.getId());
        event.setNotes("Checked in at " + gate.getName() + (request.getNotes() != null ? ". " + request.getNotes() : ""));
        visitEventRepository.save(event);

        notificationService.sendNotification(
                visit.getHostUserId(),
                "Visitor Checked In",
                visit.getVisitorName() + " has entered the premises via " + gate.getName() + " at " + DateTimeFormatter.ISO_INSTANT.format(now),
                "/resident/my-visitors-inside",
                NotificationType.VISITOR_CHECKED_IN
        );

        auditLogService.logAction(securityUser, "VISITOR_CHECKED_IN", "VISIT", String.valueOf(visit.getId()), clientIp, Map.of(
                "reference", visit.getVisitReference(),
                "gate", gate.getName()
        ));

        return visit;
    }

    @Transactional
    public Visit checkOut(Long visitId, CheckOutRequest request, User securityUser, String clientIp) {
        Visit visit = getVisitById(visitId);

        if (visit.getStatus() != VisitStatus.INSIDE) {
            throw new InvalidVisitStateException("Cannot check-out visitor. Visit status must be INSIDE, but is " + visit.getStatus());
        }

        Gate gate = gateRepository.findById(request.getGateId())
                .orElseThrow(() -> new ResourceNotFoundException("Exit gate not found."));

        Instant now = Instant.now();
        visit.setStatus(VisitStatus.EXITED);
        visit.setExitGateId(gate.getId());
        visit.setCheckOutTime(now);
        visit = visitRepository.save(visit);

        VisitEvent event = new VisitEvent();
        event.setVisitId(visit.getId());
        event.setEventType(VisitEventType.CHECKED_OUT);
        event.setActorUserId(securityUser.getId());
        event.setGateId(gate.getId());
        event.setNotes("Checked out at " + gate.getName() + (request.getNotes() != null ? ". " + request.getNotes() : ""));
        visitEventRepository.save(event);

        notificationService.sendNotification(
                visit.getHostUserId(),
                "Visitor Exited",
                visit.getVisitorName() + " has checked out via " + gate.getName() + " at " + DateTimeFormatter.ISO_INSTANT.format(now),
                "/resident/history",
                NotificationType.VISITOR_CHECKED_OUT
        );

        auditLogService.logAction(securityUser, "VISITOR_CHECKED_OUT", "VISIT", String.valueOf(visit.getId()), clientIp, Map.of(
                "reference", visit.getVisitReference(),
                "gate", gate.getName()
        ));

        return visit;
    }

    @Transactional
    public Visit cancelInvitation(Long visitId, User actor, String clientIp) {
        Visit visit = getVisitById(visitId);

        if (actor.getRole() != Role.ADMINISTRATOR && !visit.getHostUserId().equals(actor.getId())) {
            throw new ForbiddenException("You can only cancel your own visitor invitations.");
        }

        if (visit.getStatus() != VisitStatus.APPROVED && visit.getStatus() != VisitStatus.PENDING_APPROVAL) {
            throw new InvalidVisitStateException("Cannot cancel visit with status: " + visit.getStatus());
        }

        visit.setStatus(VisitStatus.CANCELLED);
        visit = visitRepository.save(visit);

        VisitEvent event = new VisitEvent();
        event.setVisitId(visit.getId());
        event.setEventType(VisitEventType.CANCELLED);
        event.setActorUserId(actor.getId());
        event.setNotes("Invitation cancelled by host");
        visitEventRepository.save(event);

        auditLogService.logAction(actor, "INVITATION_CANCELLED", "VISIT", String.valueOf(visit.getId()), clientIp, Map.of(
                "reference", visit.getVisitReference()
        ));

        return visit;
    }

    @Transactional
    public PassCredentialDto regeneratePass(Long visitId, User actor, String clientIp) {
        Visit visit = getVisitById(visitId);

        if (actor.getRole() != Role.ADMINISTRATOR && !visit.getHostUserId().equals(actor.getId())) {
            throw new ForbiddenException("You can only regenerate passes for your own invitations.");
        }

        if (visit.getStatus() != VisitStatus.APPROVED) {
            throw new InvalidVisitStateException("Can only regenerate pass for visits in APPROVED status.");
        }

        String passToken = passCredentialService.generatePassToken();
        String passHash = passCredentialService.hashCredential(passToken);

        visit.setPassCredentialHash(passHash);
        visit.setPassGeneratedAt(Instant.now());
        visitRepository.save(visit);

        VisitEvent event = new VisitEvent();
        event.setVisitId(visit.getId());
        event.setEventType(VisitEventType.PASS_REGENERATED);
        event.setActorUserId(actor.getId());
        event.setNotes("Pass credential regenerated and previous token invalidated");
        visitEventRepository.save(event);

        auditLogService.logAction(actor, "PASS_REGENERATED", "VISIT", String.valueOf(visit.getId()), clientIp, Map.of(
                "reference", visit.getVisitReference()
        ));

        String qrPayload = "GATENEST:" + visit.getVisitReference() + ":" + passToken;
        String qrCodeDataUrl = passCredentialService.generateQrCodeDataUrl(qrPayload);

        PassCredentialDto credDto = new PassCredentialDto();
        credDto.setVisitReference(visit.getVisitReference());
        credDto.setPassToken(passToken);
        credDto.setQrCodeDataUrl(qrCodeDataUrl);
        credDto.setVisitorName(visit.getVisitorName());
        credDto.setDestinationBlockName(visit.getDestinationBlockName());
        credDto.setDestinationFlatNumber(visit.getDestinationFlatNumber());
        credDto.setHostName(visit.getHostNameSnapshot());
        credDto.setArrivalWindowStart(visit.getArrivalWindowStart());
        credDto.setArrivalWindowEnd(visit.getArrivalWindowEnd());

        return credDto;
    }

    public Visit getVisitById(Long id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit record not found with ID: " + id));
    }

    public Visit getVisitByReference(String reference) {
        return visitRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Visit record not found with reference: " + reference));
    }

    public List<VisitEvent> getVisitTimeline(Long visitId) {
        return visitEventRepository.findByVisitId(visitId);
    }

    public PagedResponse<Visit> searchVisits(
            User currentUser,
            VisitStatus status,
            VisitorCategory category,
            Long gateId,
            String query,
            Instant fromDate,
            Instant toDate,
            String sortBy,
            String sortDirection,
            int page,
            int limit
    ) {
        return searchVisitsAdvanced(
                currentUser, null, null, null,
                gateId, null, null,
                status, category, null, null,
                query, com.gatenest.dto.DateBasis.SCHEDULED_ARRIVAL,
                fromDate, toDate,
                sortBy, sortDirection, page, limit
        );
    }

    public PagedResponse<Visit> searchVisitsAdvanced(
            User currentUser,
            Long blockId,
            Integer floorNumber,
            Long flatId,
            Long entryGateId,
            Long exitGateId,
            Long securityStaffId,
            VisitStatus status,
            VisitorCategory category,
            Boolean isWalkIn,
            Boolean isOverdue,
            String query,
            com.gatenest.dto.DateBasis dateBasis,
            Instant fromDate,
            Instant toDate,
            String sortBy,
            String sortDirection,
            int page,
            int limit
    ) {
        Long hostFilter = (currentUser.getRole() == Role.RESIDENT) ? currentUser.getId() : null;
        int offset = (page - 1) * limit;

        List<Visit> items = visitRepository.searchVisitsAdvanced(
                hostFilter, blockId, floorNumber, flatId,
                entryGateId, exitGateId, securityStaffId,
                status, category, isWalkIn, isOverdue,
                query, dateBasis, fromDate, toDate,
                sortBy, sortDirection, offset, limit
        );
        int total = visitRepository.countSearchVisitsAdvanced(
                hostFilter, blockId, floorNumber, flatId,
                entryGateId, exitGateId, securityStaffId,
                status, category, isWalkIn, isOverdue,
                query, dateBasis, fromDate, toDate
        );

        return new PagedResponse<>(items, total, page, limit);
    }

    @Transactional
    public Visit correctVisitTimes(Long visitId, VisitCorrectionRequest request, User actor, String clientIp) {
        if (actor.getRole() != Role.ADMINISTRATOR && actor.getRole() != Role.SECURITY) {
            throw new ForbiddenException("Only administrators and security staff can perform audited time corrections.");
        }

        Visit visit = getVisitById(visitId);
        String field = request.getFieldName().trim().toLowerCase();
        Instant correctedTime = request.getCorrectedTime();
        String oldValue;

        if ("check_in_time".equals(field)) {
            oldValue = visit.getCheckInTime() != null ? visit.getCheckInTime().toString() : "NULL";
            if (visit.getCheckOutTime() != null && correctedTime.isAfter(visit.getCheckOutTime())) {
                throw new BadRequestException("Check-in time cannot be after existing check-out time.");
            }
            visitRepository.updateCheckInTime(visitId, correctedTime);
            visit.setCheckInTime(correctedTime);
        } else if ("check_out_time".equals(field)) {
            oldValue = visit.getCheckOutTime() != null ? visit.getCheckOutTime().toString() : "NULL";
            if (visit.getCheckInTime() != null && correctedTime.isBefore(visit.getCheckInTime())) {
                throw new BadRequestException("Check-out time cannot be before existing check-in time.");
            }
            visitRepository.updateCheckOutTime(visitId, correctedTime);
            visit.setCheckOutTime(correctedTime);
        } else {
            throw new BadRequestException("Invalid field name for correction. Must be check_in_time or check_out_time.");
        }

        // Log to visit_corrections
        visitCorrectionRepository.logCorrection(
                visitId, actor.getId(), field, oldValue, correctedTime.toString(), request.getReason().trim()
        );

        // Log to visit_events
        VisitEvent event = new VisitEvent();
        event.setVisitId(visit.getId());
        event.setEventType(VisitEventType.OVERRIDDEN);
        event.setActorUserId(actor.getId());
        event.setNotes("Audited correction on " + field + " from [" + oldValue + "] to [" + correctedTime + "]. Reason: " + request.getReason());
        visitEventRepository.save(event);

        // Log to Audit Log
        auditLogService.logAction(actor, "VISIT_TIME_CORRECTED", "VISIT", String.valueOf(visit.getId()), clientIp, Map.of(
                "field", field,
                "oldValue", oldValue,
                "newValue", correctedTime.toString(),
                "reason", request.getReason()
        ));

        return getVisitById(visitId);
    }

    public List<Map<String, Object>> getVisitCorrections(Long visitId) {
        return visitCorrectionRepository.findByVisitId(visitId);
    }

    public List<Visit> getCurrentlyInside(User currentUser) {
        Long hostFilter = (currentUser.getRole() == Role.RESIDENT) ? currentUser.getId() : null;
        return visitRepository.findCurrentlyInside(hostFilter);
    }

    public List<Visit> getWaitingApprovals(User currentUser) {
        Long hostFilter = (currentUser.getRole() == Role.RESIDENT) ? currentUser.getId() : null;
        return visitRepository.findWaitingApprovals(hostFilter);
    }

    public List<Visit> getUpcomingInvitations(User hostUser) {
        return visitRepository.findUpcomingInvitations(hostUser.getId());
    }

    public DashboardStatsDto getDashboardStats(User user) {
        DashboardStatsDto stats = new DashboardStatsDto();

        if (user.getRole() == Role.ADMINISTRATOR) {
            stats.setActiveResidents(userRepository.countActiveResidents());
            stats.setPendingRegistrations(userRepository.countPendingResidents());
            stats.setVisitorsInside(visitRepository.countInsideVisits());
            stats.setEntriesToday(visitRepository.countTodayEntries());
            stats.setExitsToday(visitRepository.countTodayExits());
            stats.setPendingVisitorApprovals(visitRepository.countPendingApprovals());
            stats.setDailyEntries(visitRepository.getDailyEntryCounts(7));
        } else if (user.getRole() == Role.SECURITY) {
            stats.setWaitingApprovals(visitRepository.countPendingApprovals());
            stats.setCurrentVisitors(visitRepository.countInsideVisits());
            stats.setEntriesToday(visitRepository.countTodayEntries());
            stats.setOverdueVisits(visitRepository.countOverdueVisits());
            stats.setDailyEntries(visitRepository.getDailyEntryCounts(7));
        } else if (user.getRole() == Role.RESIDENT) {
            stats.setUpcomingInvitations(visitRepository.findUpcomingInvitations(user.getId()).size());
            stats.setRequestsAwaitingApproval(visitRepository.findWaitingApprovals(user.getId()).size());
            stats.setOwnVisitorsInside(visitRepository.findCurrentlyInside(user.getId()).size());
            stats.setOwnCompletedVisits(visitRepository.countSearchVisits(user.getId(), VisitStatus.EXITED, null, null, null, null, null));
        }

        return stats;
    }

    public String exportVisitsCsv(
            User currentUser,
            VisitStatus status,
            VisitorCategory category,
            Long gateId,
            String query,
            Instant fromDate,
            Instant toDate
    ) {
        Long hostFilter = (currentUser.getRole() == Role.RESIDENT) ? currentUser.getId() : null;
        List<Visit> visits = visitRepository.searchVisits(hostFilter, status, category, gateId, query, fromDate, toDate, "id", "DESC", 0, 5000);

        StringWriter sw = new StringWriter();
        sw.append("Visit Reference,Visitor Name,Mobile,Category,Purpose,Vehicle Number,Block,Flat,Host Name,Status,Check In Time,Check Out Time,Entry Gate,Exit Gate\n");

        for (Visit v : visits) {
            sw.append(sanitizeCsv(v.getVisitReference())).append(",")
                    .append(sanitizeCsv(v.getVisitorName())).append(",")
                    .append(sanitizeCsv(v.getVisitorMobile())).append(",")
                    .append(sanitizeCsv(v.getVisitorCategory().name())).append(",")
                    .append(sanitizeCsv(v.getPurpose())).append(",")
                    .append(sanitizeCsv(v.getVehicleNumber())).append(",")
                    .append(sanitizeCsv(v.getDestinationBlockName())).append(",")
                    .append(sanitizeCsv(v.getDestinationFlatNumber())).append(",")
                    .append(sanitizeCsv(v.getHostNameSnapshot())).append(",")
                    .append(sanitizeCsv(v.getStatus().name())).append(",")
                    .append(sanitizeCsv(v.getCheckInTime() != null ? v.getCheckInTime().toString() : "")).append(",")
                    .append(sanitizeCsv(v.getCheckOutTime() != null ? v.getCheckOutTime().toString() : "")).append(",")
                    .append(sanitizeCsv(v.getEntryGateName())).append(",")
                    .append(sanitizeCsv(v.getExitGateName())).append("\n");
        }

        return sw.toString();
    }

    private String sanitizeCsv(String input) {
        if (input == null) return "";
        String s = input.trim();
        // Protection against spreadsheet formula injection (=, +, -, @)
        if (s.startsWith("=") || s.startsWith("+") || s.startsWith("-") || s.startsWith("@")) {
            s = "'" + s;
        }
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
