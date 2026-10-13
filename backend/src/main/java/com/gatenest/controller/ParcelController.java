package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.model.Parcel;
import com.gatenest.model.ParcelEvent;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.ParcelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parcels")
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    public static class DelegateRequest {
        public String delegateName;
        public String notes;
    }

    public static class CollectRequest {
        public String collectorName;
        public Long collectorUserId;
    }

    @GetMapping("/my-parcels")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<List<Parcel>>> getMyParcels(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Parcel> list = parcelService.getParcelsForResident(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<List<Parcel>>> getPendingParcels() {
        List<Parcel> list = parcelService.getPendingParcels();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<List<Parcel>>> getParcelHistory(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        List<Parcel> list = parcelService.getParcelHistory(limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY', 'RESIDENT')")
    public ResponseEntity<ApiResponse<Parcel>> getParcelById(@PathVariable Long id) {
        Parcel p = parcelService.getParcelById(id);
        return ResponseEntity.ok(ApiResponse.ok(p));
    }

    @GetMapping("/{id}/events")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY', 'RESIDENT')")
    public ResponseEntity<ApiResponse<List<ParcelEvent>>> getParcelEvents(@PathVariable Long id) {
        List<ParcelEvent> list = parcelService.getParcelEvents(id);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<Parcel>> receiveParcel(
            @Valid @RequestBody Parcel parcel,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Parcel saved = parcelService.receiveParcel(parcel, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok("Parcel intake recorded successfully", saved));
    }

    @PostMapping("/{id}/delegate")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<Parcel>> delegateCollection(
            @PathVariable Long id,
            @RequestBody DelegateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Parcel updated = parcelService.delegateCollection(
                id, userDetails.getUser().getId(), request.delegateName, request.notes);
        return ResponseEntity.ok(ApiResponse.ok("Collector delegation updated", updated));
    }

    @PostMapping("/{id}/collect")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<Parcel>> collectParcel(
            @PathVariable Long id,
            @RequestBody CollectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Parcel updated = parcelService.markCollected(
                id, request.collectorName, request.collectorUserId, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok("Parcel marked as collected", updated));
    }
}
