package com.gatenest.controller;

import com.gatenest.dto.*;
import com.gatenest.model.User;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        UserDto userDto = authService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", userDto));
    }

    @PostMapping(value = {"/register", "/register-resident"})
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody ResidentRegisterRequest request, HttpServletRequest httpRequest) {
        UserDto userDto = authService.registerResident(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Registration submitted successfully. Please await administrator verification.", userDto));
    }

    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<UserDto>> activateAccount(
            @Valid @RequestBody ActivateAccountRequest request,
            HttpServletRequest httpRequest
    ) {
        UserDto userDto = authService.activateAccount(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Account activated successfully. You may now sign in.", userDto));
    }

    @GetMapping("/activate/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyActivationToken(@RequestParam String token) {
        Map<String, Object> details = authService.verifyActivationToken(token);
        return ResponseEntity.ok(ApiResponse.ok(details));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        authService.forgotPassword(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("If an account matches that email, password reset instructions have been generated.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest
    ) {
        authService.resetPassword(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Password has been reset successfully. Please sign in with your new password.", null));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<UserSessionDto>>> getSessions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        if (userDetails == null) {
            return ResponseEntity.ok(ApiResponse.ok(List.of()));
        }
        List<UserSessionDto> sessions = authService.getActiveSessions(userDetails.getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(sessions));
    }

    @RequestMapping(value = {"/sessions/{id}", "/sessions/revoke/{id}"}, method = {RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authService.revokeSession(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Session revoked successfully", null));
    }

    @RequestMapping(value = {"/sessions/revoke-all", "/sessions/revoke-others"}, method = {RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<ApiResponse<Void>> revokeOtherSessions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        authService.revokeAllOtherSessions(userDetails.getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("All other active sessions have been signed out.", null));
    }

    @GetMapping(value = {"/session", "/me"})
    public ResponseEntity<ApiResponse<UserDto>> getSession(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(ApiResponse.ok("Not authenticated", null));
        }
        UserDto userDto = authService.mapToDto(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(userDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<Map<String, String>>> getCsrfToken(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            return ResponseEntity.ok(ApiResponse.ok(Map.of(
                    "token", token.getToken(),
                    "headerName", token.getHeaderName(),
                    "parameterName", token.getParameterName()
            )));
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        authService.changePassword(userDetails.getId(), request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Password updated successfully", null));
    }

    @PostMapping("/bootstrap-admin")
    public ResponseEntity<ApiResponse<UserDto>> bootstrapAdmin(
            @Valid @RequestBody BootstrapAdminRequest request,
            HttpServletRequest httpRequest
    ) {
        UserDto adminDto = authService.bootstrapAdmin(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Administrator bootstrapped successfully", adminDto));
    }
}
