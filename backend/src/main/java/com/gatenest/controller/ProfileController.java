package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.dto.UserDto;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final AuthService authService;

    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserDto>> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserDto dto = authService.mapToDto(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }
}
