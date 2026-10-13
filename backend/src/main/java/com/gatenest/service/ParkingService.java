package com.gatenest.service;

import com.gatenest.exception.BadRequestException;
import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.ParkingAssignment;
import com.gatenest.model.ParkingSlot;
import com.gatenest.model.ResidentProfile;
import com.gatenest.model.Vehicle;
import com.gatenest.repository.ParkingRepository;
import com.gatenest.repository.ResidentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ParkingService {

    private final ParkingRepository parkingRepository;
    private final ResidentProfileRepository residentProfileRepository;

    public ParkingService(ParkingRepository parkingRepository, ResidentProfileRepository residentProfileRepository) {
        this.parkingRepository = parkingRepository;
        this.residentProfileRepository = residentProfileRepository;
    }

    public List<ParkingSlot> getAllSlots() {
        return parkingRepository.findAllSlots();
    }

    public List<ParkingSlot> getAvailableVisitorSlots() {
        return parkingRepository.findAvailableVisitorSlots();
    }

    public ParkingSlot createOrUpdateSlot(ParkingSlot slot) {
        return parkingRepository.saveSlot(slot);
    }

    public List<Vehicle> getResidentVehicles(Long residentUserId) {
        return parkingRepository.findVehiclesByOwner(residentUserId);
    }

    public List<Vehicle> getAllVehicles() {
        return parkingRepository.findAllVehicles();
    }

    public Vehicle registerVehicle(Long residentUserId, Vehicle vehicle) {
        ResidentProfile profile = residentProfileRepository.findByUserId(residentUserId)
                .orElseThrow(() -> new BadRequestException("Resident profile not found for user: " + residentUserId));

        if (vehicle.getVehicleNumber() == null || vehicle.getVehicleNumber().isBlank()) {
            throw new BadRequestException("Vehicle number is required");
        }

        vehicle.setId(null);
        vehicle.setOwnerUserId(residentUserId);
        vehicle.setFlatId(profile.getFlatId());
        vehicle.setActive(true);
        return parkingRepository.saveVehicle(vehicle);
    }

    @Transactional
    public ParkingAssignment assignVisitorParking(Long slotId, Long visitId, Long assignedByUserId, String notes) {
        ParkingSlot slot = parkingRepository.findSlotById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + slotId));

        if (!slot.isActive()) {
            throw new BadRequestException("Parking slot is inactive");
        }
        if (slot.isReservedForResidents()) {
            throw new BadRequestException("This slot is reserved for residents and cannot be assigned to visitors");
        }

        Optional<ParkingAssignment> activeOnSlot = parkingRepository.findActiveAssignmentForSlot(slotId);
        if (activeOnSlot.isPresent()) {
            throw new BadRequestException("Parking slot " + slot.getSlotNumber() + " is currently occupied");
        }

        Optional<ParkingAssignment> activeOnVisit = parkingRepository.findActiveAssignmentForVisit(visitId);
        if (activeOnVisit.isPresent()) {
            throw new BadRequestException("This visit already has an active parking slot assigned (" + activeOnVisit.get().getSlotNumber() + ")");
        }

        return parkingRepository.assignSlot(slotId, null, visitId, null, assignedByUserId, notes);
    }

    @Transactional
    public void releaseVisitParking(Long visitId) {
        parkingRepository.releaseAssignmentByVisitId(visitId);
    }

    @Transactional
    public void releaseSlotAssignment(Long assignmentId) {
        parkingRepository.releaseAssignment(assignmentId);
    }
}
