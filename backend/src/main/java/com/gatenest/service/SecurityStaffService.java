package com.gatenest.service;

import com.gatenest.dto.CreateSecurityRequest;
import com.gatenest.dto.PagedResponse;
import com.gatenest.dto.UserDto;
import com.gatenest.exception.ConflictException;
import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.*;
import com.gatenest.repository.GateRepository;
import com.gatenest.repository.SecurityAssignmentRepository;
import com.gatenest.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SecurityStaffService {

    private final UserRepository userRepository;
    private final GateRepository gateRepository;
    private final SecurityAssignmentRepository securityAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    public SecurityStaffService(
            UserRepository userRepository,
            GateRepository gateRepository,
            SecurityAssignmentRepository securityAssignmentRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService,
            AuthService authService
    ) {
        this.userRepository = userRepository;
        this.gateRepository = gateRepository;
        this.securityAssignmentRepository = securityAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.authService = authService;
    }

    public PagedResponse<UserDto> getSecurityStaff(String query, UserStatus status, int page, int limit) {
        int offset = (page - 1) * limit;
        List<User> users = userRepository.searchUsers(query, Role.SECURITY, status, offset, limit);
        int total = userRepository.countUsers(query, Role.SECURITY, status);

        List<UserDto> dtoList = users.stream().map(authService::mapToDto).collect(Collectors.toList());
        return new PagedResponse<>(dtoList, total, page, limit);
    }

    @Transactional
    public UserDto createSecurityStaff(CreateSecurityRequest request, User admin, String clientIp) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("A user with email '" + normalizedEmail + "' already exists.");
        }

        Gate gate = gateRepository.findById(request.getPrimaryGateId())
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found with ID: " + request.getPrimaryGateId()));

        User securityUser = new User();
        securityUser.setEmail(normalizedEmail);
        securityUser.setFullName(request.getFullName().trim());
        securityUser.setMobile(request.getMobile().trim());
        securityUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        securityUser.setRole(Role.SECURITY);
        securityUser.setStatus(UserStatus.ACTIVE);
        securityUser = userRepository.save(securityUser);

        SecurityGateAssignment assignment = new SecurityGateAssignment();
        assignment.setUserId(securityUser.getId());
        assignment.setGateId(gate.getId());
        assignment.setActive(true);
        assignment.setShiftName(request.getShiftName());
        securityAssignmentRepository.save(assignment);

        auditLogService.logAction(admin, "SECURITY_STAFF_CREATED", "USER", String.valueOf(securityUser.getId()), clientIp, Map.of(
                "email", securityUser.getEmail(),
                "gateCode", gate.getCode()
        ));

        return authService.mapToDto(securityUser);
    }

    @Transactional
    public void assignGate(Long userId, Long gateId, String shiftName, User admin, String clientIp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        Gate gate = gateRepository.findById(gateId)
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found with ID: " + gateId));

        securityAssignmentRepository.deactivateUserAssignments(userId);

        SecurityGateAssignment assignment = new SecurityGateAssignment();
        assignment.setUserId(user.getId());
        assignment.setGateId(gate.getId());
        assignment.setActive(true);
        assignment.setShiftName(shiftName);
        securityAssignmentRepository.save(assignment);

        auditLogService.logAction(admin, "SECURITY_GATE_ASSIGNED", "SECURITY_ASSIGNMENT", String.valueOf(assignment.getId()), clientIp, Map.of(
                "userEmail", user.getEmail(),
                "gateCode", gate.getCode()
        ));
    }

    @Transactional
    public void setSecurityStatus(Long userId, UserStatus status, User admin, String clientIp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Security user not found."));

        user.setStatus(status);
        userRepository.save(user);

        auditLogService.logAction(admin, "SECURITY_STATUS_CHANGED", "USER", String.valueOf(userId), clientIp, Map.of(
                "status", status.name()
        ));
    }
}
