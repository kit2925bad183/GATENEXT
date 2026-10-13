package com.gatenest.controller;

import com.gatenest.dto.*;
import com.gatenest.model.*;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.CommunityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/community", "/api/v1"})
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/public-settings")
    public ResponseEntity<ApiResponse<CommunitySettings>> getPublicSettings() {
        return ResponseEntity.ok(ApiResponse.ok(communityService.getSettings()));
    }

    @GetMapping("/public-structure")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublicStructure() {
        return ResponseEntity.ok(ApiResponse.ok(communityService.getPublicStructure()));
    }

    @GetMapping("/blocks")
    public ResponseEntity<ApiResponse<List<Block>>> getBlocks() {
        return ResponseEntity.ok(ApiResponse.ok(communityService.getAllBlocks()));
    }

    @GetMapping("/flats")
    public ResponseEntity<ApiResponse<List<Flat>>> getFlats(@RequestParam(required = false) Long blockId) {
        if (blockId != null) {
            return ResponseEntity.ok(ApiResponse.ok(communityService.getFlatsByBlockId(blockId)));
        }
        return ResponseEntity.ok(ApiResponse.ok(communityService.getAllFlats()));
    }

    @GetMapping("/gates")
    public ResponseEntity<ApiResponse<List<Gate>>> getGates() {
        return ResponseEntity.ok(ApiResponse.ok(communityService.getAllGates()));
    }

    // Admin management endpoints
    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<CommunitySettings>> updateSettings(
            @Valid @RequestBody CommunitySettingsDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        CommunitySettings updated = communityService.updateSettings(dto, userDetails.getUser(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Community settings updated", updated));
    }

    @PostMapping("/blocks")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Block>> createBlock(
            @Valid @RequestBody BlockDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        Block created = communityService.createBlock(dto, userDetails.getUser(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Block created", created));
    }

    @PutMapping("/blocks/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Block>> updateBlock(
            @PathVariable Long id,
            @Valid @RequestBody BlockDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        Block updated = communityService.updateBlock(id, dto, userDetails.getUser(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Block updated", updated));
    }

    @PostMapping("/flats")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Flat>> createFlat(
            @Valid @RequestBody FlatDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        Flat created = communityService.createFlat(dto, userDetails.getUser(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Flat created", created));
    }

    @PutMapping("/flats/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Flat>> updateFlat(
            @PathVariable Long id,
            @Valid @RequestBody FlatDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        Flat updated = communityService.updateFlat(id, dto, userDetails.getUser(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Flat updated", updated));
    }

    @PostMapping("/gates")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Gate>> createGate(
            @Valid @RequestBody GateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        Gate created = communityService.createGate(dto, userDetails.getUser(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Gate created", created));
    }

    @PutMapping("/gates/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Gate>> updateGate(
            @PathVariable Long id,
            @Valid @RequestBody GateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        Gate updated = communityService.updateGate(id, dto, userDetails.getUser(), request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Gate updated", updated));
    }
}
