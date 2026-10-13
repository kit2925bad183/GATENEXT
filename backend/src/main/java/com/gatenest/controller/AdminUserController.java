package com.gatenest.controller;

import com.gatenest.dto.*;
import com.gatenest.model.Role;
import com.gatenest.model.User;
import com.gatenest.model.UserStatus;
import com.gatenest.repository.ResidentMoveRepository;
import com.gatenest.repository.UserRepository;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminUserController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final ResidentMoveRepository residentMoveRepository;

    public AdminUserController(AuthService authService, UserRepository userRepository, ResidentMoveRepository residentMoveRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.residentMoveRepository = residentMoveRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        int offset = (page - 1) * limit;
        List<User> users = userRepository.searchUsers(query, role, status, offset, limit);
        int total = userRepository.countUsers(query, role, status);

        List<UserDto> dtos = users.stream().map(authService::mapToDto).toList();
        return ResponseEntity.ok(ApiResponse.ok(new PagedResponse<>(dtos, total, page, limit)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createUser(
            @Valid @RequestBody AdminCreateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        String clientIp = getClientIp(httpRequest);
        Map<String, Object> result = authService.adminCreateUser(request, userDetails.getUser(), clientIp);
        return ResponseEntity.ok(ApiResponse.ok("User created successfully. Activation link generated.", result));
    }

    @PostMapping("/{id}/regenerate-activation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> regenerateActivation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        String clientIp = getClientIp(httpRequest);
        Map<String, Object> result = authService.adminRegenerateActivation(id, userDetails.getUser(), clientIp);
        return ResponseEntity.ok(ApiResponse.ok("Activation link regenerated successfully.", result));
    }

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<UserDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeUserStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        String clientIp = getClientIp(httpRequest);
        UserDto updated = authService.adminChangeUserStatus(id, request, userDetails.getUser(), clientIp);
        return ResponseEntity.ok(ApiResponse.ok("User status updated successfully.", updated));
    }

    @RequestMapping(value = "/{id}/role", method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<UserDto>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeUserRoleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        String clientIp = getClientIp(httpRequest);
        UserDto updated = authService.adminChangeUserRole(id, request, userDetails.getUser(), clientIp);
        return ResponseEntity.ok(ApiResponse.ok("User role updated successfully.", updated));
    }

    @PostMapping("/{id}/move-flat")
    public ResponseEntity<ApiResponse<Void>> moveResidentFlat(
            @PathVariable Long id,
            @Valid @RequestBody ResidentMoveFlatRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        String clientIp = getClientIp(httpRequest);
        authService.adminMoveResidentFlat(id, request, userDetails.getUser(), clientIp);
        return ResponseEntity.ok(ApiResponse.ok("Resident relocated successfully. Affected future invitations have been cancelled.", null));
    }

    @GetMapping("/{id}/move-history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMoveHistory(@PathVariable Long id) {
        List<Map<String, Object>> history = residentMoveRepository.findByResidentUserId(id);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
