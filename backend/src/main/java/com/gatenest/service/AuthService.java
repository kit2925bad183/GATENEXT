package com.gatenest.service;

import com.gatenest.dto.*;
import com.gatenest.exception.BadRequestException;
import com.gatenest.exception.ConflictException;
import com.gatenest.exception.ForbiddenException;
import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.*;
import com.gatenest.repository.*;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.security.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ResidentProfileRepository residentProfileRepository;
    private final FlatRepository flatRepository;
    private final BlockRepository blockRepository;
    private final SecurityAssignmentRepository securityAssignmentRepository;
    private final GateRepository gateRepository;
    private final UserActivationRepository userActivationRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final UserSessionRepository userSessionRepository;
    private final ResidentMoveRepository residentMoveRepository;
    private final VisitRepository visitRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RateLimiterService rateLimiterService;
    private final AuditLogService auditLogService;
    private final PassCredentialService passCredentialService;

    @Value("${gatenest.bootstrap.admin-token:bootstrap-gatenest-admin-secret-2026}")
    private String configuredBootstrapToken;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            ResidentProfileRepository residentProfileRepository,
            FlatRepository flatRepository,
            BlockRepository blockRepository,
            SecurityAssignmentRepository securityAssignmentRepository,
            GateRepository gateRepository,
            UserActivationRepository userActivationRepository,
            PasswordResetRepository passwordResetRepository,
            UserSessionRepository userSessionRepository,
            ResidentMoveRepository residentMoveRepository,
            VisitRepository visitRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            RateLimiterService rateLimiterService,
            AuditLogService auditLogService,
            PassCredentialService passCredentialService
    ) {
        this.userRepository = userRepository;
        this.residentProfileRepository = residentProfileRepository;
        this.flatRepository = flatRepository;
        this.blockRepository = blockRepository;
        this.securityAssignmentRepository = securityAssignmentRepository;
        this.gateRepository = gateRepository;
        this.userActivationRepository = userActivationRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.userSessionRepository = userSessionRepository;
        this.residentMoveRepository = residentMoveRepository;
        this.visitRepository = visitRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.rateLimiterService = rateLimiterService;
        this.auditLogService = auditLogService;
        this.passCredentialService = passCredentialService;
    }

    public UserDto login(LoginRequest request, HttpServletRequest httpRequest, jakarta.servlet.http.HttpServletResponse httpResponse) {
        String clientIp = getClientIp(httpRequest);
        String rateLimitKey = "login:" + clientIp;
        if (!rateLimiterService.isAllowed(rateLimitKey, 15, 60)) {
            throw new BadRequestException("Too many login attempts. Please wait a minute and try again.");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Invalid email or password."));

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ForbiddenException("Your account is currently suspended. Please contact the community management.");
        }
        if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new ForbiddenException("Your account has been deactivated.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            org.springframework.security.web.context.SecurityContextRepository repo =
                    new org.springframework.security.web.context.HttpSessionSecurityContextRepository();
            repo.saveContext(context, httpRequest, httpResponse);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            userRepository.updateLastLogin(user.getId());
            user.setLastLoginAt(Instant.now());

            // Track user session
            String sessionHash = passCredentialService.hashCredential(session.getId());
            String userAgent = httpRequest.getHeader("User-Agent");
            userSessionRepository.registerOrUpdateSession(user.getId(), sessionHash, userAgent, clientIp);

            auditLogService.logAction(user, "LOGIN_SUCCESS", "USER", String.valueOf(user.getId()), clientIp, null);
            return mapToDto(user);
        } catch (BadCredentialsException e) {
            userRepository.recordFailedLogin(user.getId());
            auditLogService.logAction(user, "LOGIN_FAILED", "USER", String.valueOf(user.getId()), clientIp, Map.of("email", normalizedEmail));
            throw new BadRequestException("Invalid email or password.");
        }
    }

    @Transactional
    public UserDto registerResident(ResidentRegisterRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        String rateLimitKey = "register:" + clientIp;
        if (!rateLimiterService.isAllowed(rateLimitKey, 10, 60)) {
            throw new BadRequestException("Too many registration requests. Please wait a moment.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match.");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("An account with this email address already exists.");
        }

        Flat flat = flatRepository.findById(request.getFlatId())
                .orElseThrow(() -> new ResourceNotFoundException("Selected flat does not exist."));

        if (!flat.isActive()) {
            throw new BadRequestException("Selected flat is currently inactive.");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setFullName(request.getFullName().trim());
        user.setMobile(request.getMobile().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.RESIDENT);
        user.setStatus(UserStatus.PENDING); // Compulsory: public registration is always PENDING
        user = userRepository.save(user);

        ResidentProfile profile = new ResidentProfile();
        profile.setUserId(user.getId());
        profile.setFlatId(flat.getId());
        profile.setOccupancyType(request.getOccupancyType());
        profile.setVerificationStatus(VerificationStatus.PENDING);
        profile.setEmergencyContact(request.getEmergencyContact());
        residentProfileRepository.save(profile);

        auditLogService.logAction(user, "RESIDENT_REGISTERED", "USER", String.valueOf(user.getId()), clientIp, Map.of(
                "flatId", flat.getId(),
                "flatNumber", flat.getFlatNumber(),
                "occupancyType", request.getOccupancyType().name()
        ));

        return mapToDto(user);
    }

    @Transactional
    public Map<String, Object> adminCreateUser(AdminCreateUserRequest request, User admin, String clientIp) {
        // If creating an ADMINISTRATOR, require password reauthentication
        if (request.getRole() == Role.ADMINISTRATOR) {
            if (request.getAdminPasswordVerification() == null || !passwordEncoder.matches(request.getAdminPasswordVerification(), admin.getPasswordHash())) {
                throw new ForbiddenException("Creating an Administrator requires valid reauthentication with your current password.");
            }
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("User with this email already exists.");
        }

        // Generate temporary strong random password
        String tempPass = UUID.randomUUID().toString() + UUID.randomUUID().toString();

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setFullName(request.getFullName().trim());
        user.setMobile(request.getMobile().trim());
        user.setPasswordHash(passwordEncoder.encode(tempPass));
        user.setRole(request.getRole());
        user.setStatus(UserStatus.PENDING); // Pending activation
        user = userRepository.save(user);

        // Role specific profile setup
        if (request.getRole() == Role.RESIDENT) {
            if (request.getFlatId() == null) {
                throw new BadRequestException("Flat assignment is required for resident creation.");
            }
            Flat flat = flatRepository.findById(request.getFlatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Selected flat does not exist."));

            ResidentProfile profile = new ResidentProfile();
            profile.setUserId(user.getId());
            profile.setFlatId(flat.getId());
            profile.setOccupancyType(request.getOccupancyType() != null ? OccupancyType.valueOf(request.getOccupancyType()) : OccupancyType.OWNER);
            profile.setVerificationStatus(VerificationStatus.APPROVED); // Admin created accounts have verified residence
            profile.setReviewedByUserId(admin.getId());
            profile.setReviewedAt(Instant.now());
            residentProfileRepository.save(profile);
        } else if (request.getRole() == Role.SECURITY) {
            if (request.getGateId() != null) {
                SecurityGateAssignment assignment = new SecurityGateAssignment();
                assignment.setUserId(user.getId());
                assignment.setGateId(request.getGateId());
                assignment.setShiftName(request.getShiftName() != null ? request.getShiftName() : "General Shift");
                assignment.setActive(true);
                securityAssignmentRepository.save(assignment);
            }
        }

        // Generate single-use activation token
        String rawToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String tokenHash = passCredentialService.hashCredential(rawToken);
        Instant expiresAt = Instant.now().plus(48, ChronoUnit.HOURS);
        userActivationRepository.createActivation(user.getId(), tokenHash, expiresAt, admin.getId());

        auditLogService.logAction(admin, "USER_CREATED_BY_ADMIN", "USER", String.valueOf(user.getId()), clientIp, Map.of(
                "targetEmail", user.getEmail(),
                "role", user.getRole().name()
        ));

        Map<String, Object> result = new HashMap<>();
        result.put("user", mapToDto(user));
        result.put("activationToken", rawToken);
        result.put("activationLink", "/auth/activate?token=" + rawToken);
        result.put("expiresAt", expiresAt);
        return result;
    }

    @Transactional
    public Map<String, Object> adminRegenerateActivation(Long userId, User admin, String clientIp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        String rawToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String tokenHash = passCredentialService.hashCredential(rawToken);
        Instant expiresAt = Instant.now().plus(48, ChronoUnit.HOURS);
        userActivationRepository.createActivation(user.getId(), tokenHash, expiresAt, admin.getId());

        auditLogService.logAction(admin, "ACTIVATION_REGENERATED", "USER", String.valueOf(user.getId()), clientIp, Map.of(
                "targetEmail", user.getEmail()
        ));

        Map<String, Object> result = new HashMap<>();
        result.put("activationToken", rawToken);
        result.put("activationLink", "/auth/activate?token=" + rawToken);
        result.put("expiresAt", expiresAt);
        return result;
    }

    @Transactional
    public UserDto adminChangeUserStatus(Long targetUserId, ChangeUserStatusRequest request, User admin, String clientIp) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + targetUserId));

        UserStatus newStatus = UserStatus.valueOf(request.getNewStatus().toUpperCase());

        // Protect against demoting/suspending the LAST active administrator
        if (targetUser.getRole() == Role.ADMINISTRATOR && newStatus != UserStatus.ACTIVE) {
            int activeAdmins = userRepository.countActiveAdmins();
            if (activeAdmins <= 1) {
                throw new BadRequestException("Cannot suspend or deactivate the last active administrator.");
            }
            if (request.getAdminPasswordVerification() == null || !passwordEncoder.matches(request.getAdminPasswordVerification(), admin.getPasswordHash())) {
                throw new ForbiddenException("Modifying administrator account status requires valid reauthentication.");
            }
        }

        userRepository.updateStatus(targetUserId, newStatus);
        targetUser.setStatus(newStatus);

        // If suspending or deactivating, revoke all sessions
        if (newStatus != UserStatus.ACTIVE) {
            userSessionRepository.revokeAllUserSessions(targetUserId);
        }

        auditLogService.logAction(admin, "USER_STATUS_CHANGED", "USER", String.valueOf(targetUserId), clientIp, Map.of(
                "oldStatus", targetUser.getStatus().name(),
                "newStatus", newStatus.name(),
                "reason", request.getReason() != null ? request.getReason() : ""
        ));

        return mapToDto(targetUser);
    }

    @Transactional
    public UserDto adminChangeUserRole(Long targetUserId, ChangeUserRoleRequest request, User admin, String clientIp) {
        // Must verify admin password
        if (request.getAdminPasswordVerification() == null || !passwordEncoder.matches(request.getAdminPasswordVerification(), admin.getPasswordHash())) {
            throw new ForbiddenException("Changing user role requires valid administrator password reauthentication.");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + targetUserId));

        // Protect last active administrator
        if (targetUser.getRole() == Role.ADMINISTRATOR && request.getNewRole() != Role.ADMINISTRATOR) {
            int activeAdmins = userRepository.countActiveAdmins();
            if (activeAdmins <= 1) {
                throw new BadRequestException("Cannot demote the last active administrator.");
            }
        }

        // Prevent unauthorized self-promotion if not already admin
        if (admin.getId().equals(targetUserId) && targetUser.getRole() != Role.ADMINISTRATOR && request.getNewRole() == Role.ADMINISTRATOR) {
            throw new ForbiddenException("Self-promotion to administrator is not allowed.");
        }

        Role oldRole = targetUser.getRole();
        targetUser.setRole(request.getNewRole());
        userRepository.save(targetUser);

        // Revoke active sessions for target user so their security context is re-evaluated
        userSessionRepository.revokeAllUserSessions(targetUserId);

        auditLogService.logAction(admin, "USER_ROLE_CHANGED", "USER", String.valueOf(targetUserId), clientIp, Map.of(
                "oldRole", oldRole.name(),
                "newRole", request.getNewRole().name(),
                "reason", request.getReason()
        ));

        return mapToDto(targetUser);
    }

    @Transactional
    public void adminMoveResidentFlat(Long residentUserId, ResidentMoveFlatRequest request, User admin, String clientIp) {
        User resident = userRepository.findById(residentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident user not found."));

        ResidentProfile profile = residentProfileRepository.findByUserId(residentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident profile not found."));

        Long oldFlatId = profile.getFlatId();
        Flat newFlat = flatRepository.findById(request.getNewFlatId())
                .orElseThrow(() -> new ResourceNotFoundException("New flat does not exist."));

        profile.setFlatId(newFlat.getId());
        if (request.getOccupancyType() != null) {
            profile.setOccupancyType(OccupancyType.valueOf(request.getOccupancyType()));
        }
        residentProfileRepository.save(profile);

        // Invalidate future invitations
        int invalidatedCount = visitRepository.invalidateFutureInvitationsForResident(residentUserId);

        // Log to resident_moves table
        residentMoveRepository.logResidentMove(
                residentUserId, oldFlatId, newFlat.getId(), admin.getId(),
                request.getReason() != null ? request.getReason() : "Flat relocation", invalidatedCount
        );

        auditLogService.logAction(admin, "RESIDENT_FLAT_MOVED", "USER", String.valueOf(residentUserId), clientIp, Map.of(
                "oldFlatId", oldFlatId,
                "newFlatId", newFlat.getId(),
                "invalidatedInvitations", invalidatedCount
        ));
    }

    @Transactional
    public UserDto activateAccount(ActivateAccountRequest request, HttpServletRequest httpRequest) {
        String tokenHash = passCredentialService.hashCredential(request.getToken().trim());
        Map<String, Object> activation = userActivationRepository.findValidByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired activation token."));

        Long userId = ((Number) activation.get("user_id")).longValue();
        Long activationId = ((Number) activation.get("id")).longValue();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        String effectivePassword = request.getEffectivePassword();
        if (effectivePassword == null || effectivePassword.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long.");
        }

        user.setPasswordHash(passwordEncoder.encode(effectivePassword));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        userActivationRepository.markConsumed(activationId);

        auditLogService.logAction(user, "ACCOUNT_ACTIVATED", "USER", String.valueOf(user.getId()), getClientIp(httpRequest), null);
        return mapToDto(user);
    }

    public Map<String, Object> verifyActivationToken(String token) {
        String tokenHash = passCredentialService.hashCredential(token.trim());
        Map<String, Object> activation = userActivationRepository.findValidByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Activation token is invalid or has expired."));

        return Map.of(
                "valid", true,
                "email", activation.get("email"),
                "fullName", activation.get("full_name"),
                "role", activation.get("role")
        );
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String rawToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
            String tokenHash = passCredentialService.hashCredential(rawToken);
            Instant expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES);

            passwordResetRepository.createPasswordReset(user.getId(), tokenHash, expiresAt, getClientIp(httpRequest));

            auditLogService.logAction(user, "PASSWORD_RESET_REQUESTED", "USER", String.valueOf(user.getId()), getClientIp(httpRequest), null);
        }
        // Always return clean status without leaking whether email exists
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request, HttpServletRequest httpRequest) {
        String tokenHash = passCredentialService.hashCredential(request.getToken().trim());
        Map<String, Object> reset = passwordResetRepository.findValidByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token."));

        Long userId = ((Number) reset.get("user_id")).longValue();
        Long resetId = ((Number) reset.get("id")).longValue();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetRepository.markConsumed(resetId);

        // Revoke all sessions on password reset
        userSessionRepository.revokeAllUserSessions(userId);

        auditLogService.logAction(user, "PASSWORD_RESET_COMPLETED", "USER", String.valueOf(user.getId()), getClientIp(httpRequest), null);
    }

    public List<UserSessionDto> getActiveSessions(Long userId, HttpServletRequest httpRequest) {
        String currentSessionHash = "";
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            currentSessionHash = passCredentialService.hashCredential(session.getId());
        }
        return userSessionRepository.findActiveSessions(userId, currentSessionHash);
    }

    public void revokeSession(Long userId, Long sessionId) {
        userSessionRepository.revokeSession(userId, sessionId);
    }

    public void revokeAllOtherSessions(Long userId, HttpServletRequest httpRequest) {
        String currentSessionHash = "";
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            currentSessionHash = passCredentialService.hashCredential(session.getId());
        }
        userSessionRepository.revokeAllOtherSessions(userId, currentSessionHash);
    }

    public UserDto getCurrentSession(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BadRequestException("No active session found.");
        }
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user no longer exists."));
        return mapToDto(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request, HttpServletRequest httpRequest) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("New password and confirmation do not match.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        userRepository.updatePassword(userId, passwordEncoder.encode(request.getNewPassword()));

        auditLogService.logAction(user, "PASSWORD_CHANGED", "USER", String.valueOf(user.getId()), getClientIp(httpRequest), null);
    }

    @Transactional
    public UserDto bootstrapAdmin(BootstrapAdminRequest request, HttpServletRequest httpRequest) {
        if (request.getBootstrapToken() == null || !request.getBootstrapToken().equals(configuredBootstrapToken)) {
            throw new ForbiddenException("Invalid bootstrap token.");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("User with this email already exists.");
        }

        User admin = new User();
        admin.setEmail(normalizedEmail);
        admin.setFullName(request.getFullName().trim());
        admin.setMobile(request.getMobile().trim());
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setRole(Role.ADMINISTRATOR);
        admin.setStatus(UserStatus.ACTIVE);
        admin = userRepository.save(admin);

        auditLogService.logAction(admin, "ADMIN_BOOTSTRAPPED", "USER", String.valueOf(admin.getId()), getClientIp(httpRequest), null);
        return mapToDto(admin);
    }

    public UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setMobile(user.getMobile());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());

        if (user.getRole() == Role.RESIDENT) {
            residentProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                dto.setResidentProfileId(profile.getId());
                dto.setFlatId(profile.getFlatId());
                dto.setFlatNumber(profile.getFlatNumber());
                dto.setBlockName(profile.getBlockName());
                dto.setOccupancyType(profile.getOccupancyType().name());
                dto.setVerificationStatus(profile.getVerificationStatus().name());
                dto.setRejectionReason(profile.getRejectionReason());
            });
        } else if (user.getRole() == Role.SECURITY) {
            List<SecurityGateAssignment> assignments = securityAssignmentRepository.findByUserId(user.getId());
            if (!assignments.isEmpty()) {
                SecurityGateAssignment primary = assignments.get(0);
                dto.setAssignedGateId(primary.getGateId());
                dto.setAssignedGateName(primary.getGateName());
                dto.setAssignedGateCode(primary.getGateCode());
            }
        }

        return dto;
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
