package com.gatenest.service;

import com.gatenest.exception.BadRequestException;
import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.ResidentProfile;
import com.gatenest.model.StaffAttendanceSession;
import com.gatenest.model.StaffAuthorization;
import com.gatenest.model.StaffMember;
import com.gatenest.repository.ResidentProfileRepository;
import com.gatenest.repository.StaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    private final StaffRepository staffRepository;
    private final ResidentProfileRepository residentProfileRepository;

    public StaffService(StaffRepository staffRepository, ResidentProfileRepository residentProfileRepository) {
        this.staffRepository = staffRepository;
        this.residentProfileRepository = residentProfileRepository;
    }

    public List<StaffMember> getAllStaff() {
        return staffRepository.findAllStaff();
    }

    public StaffMember getStaffById(Long id) {
        return staffRepository.findStaffById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found with id: " + id));
    }

    public StaffMember createOrUpdateStaff(StaffMember member) {
        if (member.getFullName() == null || member.getFullName().isBlank()) {
            throw new BadRequestException("Full name is required");
        }
        if (member.getMobile() == null || member.getMobile().isBlank()) {
            throw new BadRequestException("Mobile is required");
        }
        return staffRepository.saveStaff(member);
    }

    public List<StaffAuthorization> getAuthorizationsForResident(Long residentUserId) {
        return staffRepository.findAuthorizationsByResident(residentUserId);
    }

    public List<StaffAuthorization> getAuthorizationsForStaff(Long staffId) {
        return staffRepository.findAuthorizationsByStaff(staffId);
    }

    @Transactional
    public StaffAuthorization authorizeStaffForFlat(Long residentUserId, Long staffMemberId, String schedule) {
        ResidentProfile profile = residentProfileRepository.findByUserId(residentUserId)
                .orElseThrow(() -> new BadRequestException("Resident profile not found for user: " + residentUserId));

        Optional<StaffAuthorization> existing = staffRepository.findAuthorization(staffMemberId, residentUserId, profile.getFlatId());
        if (existing.isPresent()) {
            StaffAuthorization auth = existing.get();
            auth.setActive(true);
            auth.setRevokedAt(null);
            auth.setScheduleDescription(schedule);
            return staffRepository.saveAuthorization(auth);
        }

        StaffAuthorization auth = new StaffAuthorization();
        auth.setStaffMemberId(staffMemberId);
        auth.setResidentUserId(residentUserId);
        auth.setFlatId(profile.getFlatId());
        auth.setScheduleDescription(schedule);
        return staffRepository.saveAuthorization(auth);
    }

    @Transactional
    public void revokeStaffAuthorization(Long residentUserId, Long authorizationId) {
        StaffAuthorization auth = staffRepository.findAuthorizationsByResident(residentUserId).stream()
                .filter(a -> a.getId().equals(authorizationId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Authorization not found"));

        auth.setActive(false);
        auth.setRevokedAt(Instant.now());
        staffRepository.saveAuthorization(auth);
    }

    @Transactional
    public StaffAttendanceSession recordStaffCheckIn(Long staffMemberId, Long gateId, Long guardUserId, String notes) {
        StaffMember staff = getStaffById(staffMemberId);
        if (!staff.isActive()) {
            throw new BadRequestException("This staff member is deactivated");
        }

        Optional<StaffAttendanceSession> activeSession = staffRepository.findActiveSessionForStaff(staffMemberId);
        if (activeSession.isPresent()) {
            throw new BadRequestException("Staff member is already recorded as inside (checked in at " +
                    activeSession.get().getCheckInTime() + ")");
        }

        return staffRepository.createCheckIn(staffMemberId, gateId, guardUserId, notes);
    }

    @Transactional
    public boolean recordStaffCheckOut(Long staffMemberId, Long gateId, Long guardUserId, String notes) {
        StaffAttendanceSession activeSession = staffRepository.findActiveSessionForStaff(staffMemberId)
                .orElseThrow(() -> new BadRequestException("Staff member is not currently inside"));

        return staffRepository.recordCheckOut(activeSession.getId(), gateId, guardUserId, notes);
    }

    public List<StaffAttendanceSession> getActiveAttendanceSessions() {
        return staffRepository.findAllActiveSessions();
    }

    public List<StaffAttendanceSession> getAttendanceHistory(int limit, int offset) {
        return staffRepository.findAttendanceHistory(limit, offset);
    }
}
