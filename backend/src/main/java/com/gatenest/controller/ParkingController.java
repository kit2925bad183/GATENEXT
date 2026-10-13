package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.model.ParkingAssignment;
import com.gatenest.model.ParkingSlot;
import com.gatenest.model.Vehicle;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.ParkingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parking")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    public static class AssignVisitorParkingRequest {
        public Long slotId;
        public Long visitId;
        public String notes;
    }

    @GetMapping("/slots")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY', 'RESIDENT')")
    public ResponseEntity<ApiResponse<List<ParkingSlot>>> getAllSlots() {
        List<ParkingSlot> list = parkingService.getAllSlots();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/slots/available-visitors")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<List<ParkingSlot>>> getAvailableVisitorSlots() {
        List<ParkingSlot> list = parkingService.getAvailableVisitorSlots();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/slots")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<ParkingSlot>> createOrUpdateSlot(
            @Valid @RequestBody ParkingSlot slot
    ) {
        ParkingSlot saved = parkingService.createOrUpdateSlot(slot);
        return ResponseEntity.ok(ApiResponse.ok("Parking slot saved", saved));
    }

    @GetMapping("/my-vehicles")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<List<Vehicle>>> getMyVehicles(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Vehicle> list = parkingService.getResidentVehicles(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/vehicles")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<List<Vehicle>>> getAllVehicles() {
        List<Vehicle> list = parkingService.getAllVehicles();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/vehicles")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
    public ResponseEntity<ApiResponse<Vehicle>> registerVehicle(
            @Valid @RequestBody Vehicle vehicle,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Vehicle saved = parkingService.registerVehicle(userDetails.getUser().getId(), vehicle);
        return ResponseEntity.ok(ApiResponse.ok("Vehicle registered successfully", saved));
    }

    @PostMapping("/assign-visitor")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<ParkingAssignment>> assignVisitorSlot(
            @RequestBody AssignVisitorParkingRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ParkingAssignment pa = parkingService.assignVisitorParking(
                request.slotId, request.visitId, userDetails.getUser().getId(), request.notes);
        return ResponseEntity.ok(ApiResponse.ok("Visitor parking slot assigned", pa));
    }

    @PostMapping("/release/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SECURITY')")
    public ResponseEntity<ApiResponse<Void>> releaseAssignment(@PathVariable Long assignmentId) {
        parkingService.releaseSlotAssignment(assignmentId);
        return ResponseEntity.ok(ApiResponse.ok("Parking slot released", null));
    }
}
