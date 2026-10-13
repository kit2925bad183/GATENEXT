package com.gatenest.service;

import com.gatenest.exception.BadRequestException;
import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.ResidentProfile;
import com.gatenest.model.SecurityAssistanceRequest;
import com.gatenest.repository.ResidentProfileRepository;
import com.gatenest.repository.SecurityAssistanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SecurityAssistanceService {

    private final SecurityAssistanceRepository assistanceRepository;
    private final ResidentProfileRepository residentProfileRepository;

    public SecurityAssistanceService(SecurityAssistanceRepository assistanceRepository,
                                     ResidentProfileRepository residentProfileRepository) {
        this.assistanceRepository = assistanceRepository;
        this.residentProfileRepository = residentProfileRepository;
    }

    public List<SecurityAssistanceRequest> getRequestsForResident(Long residentUserId) {
        return assistanceRepository.findByResidentUserId(residentUserId);
    }

    public List<SecurityAssistanceRequest> getActiveRequests() {
        return assistanceRepository.findActiveRequests();
    }

    public List<SecurityAssistanceRequest> getRequestHistory(int limit, int offset) {
        return assistanceRepository.findAllHistory(limit, offset);
    }

    public SecurityAssistanceRequest getRequestById(Long id) {
        return assistanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assistance request not found with id: " + id));
    }

    @Transactional
    public SecurityAssistanceRequest submitAssistanceRequest(Long residentUserId, SecurityAssistanceRequest request) {
        ResidentProfile profile = residentProfileRepository.findByUserId(residentUserId)
                .orElseThrow(() -> new BadRequestException("Resident profile not found for user: " + residentUserId));

        request.setId(null);
        request.setResidentUserId(residentUserId);
        request.setFlatId(profile.getFlatId());
        request.setStatus("SUBMITTED");
        return assistanceRepository.save(request);
    }

    @Transactional
    public boolean acknowledgeRequest(Long requestId, Long guardUserId) {
        return assistanceRepository.acknowledgeRequest(requestId, guardUserId);
    }

    @Transactional
    public boolean resolveRequest(Long requestId, Long guardUserId, String resolutionNotes) {
        return assistanceRepository.resolveRequest(requestId, guardUserId, resolutionNotes);
    }
}
