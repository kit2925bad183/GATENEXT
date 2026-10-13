package com.gatenest.service;

import com.gatenest.exception.BadRequestException;
import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.GroupInvitation;
import com.gatenest.model.GroupInvitationMember;
import com.gatenest.model.ResidentProfile;
import com.gatenest.model.VisitStatus;
import com.gatenest.repository.GroupInvitationRepository;
import com.gatenest.repository.ResidentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class GroupInvitationService {

    private final GroupInvitationRepository groupRepository;
    private final ResidentProfileRepository residentProfileRepository;
    private final PassCredentialService passCredentialService;

    public GroupInvitationService(GroupInvitationRepository groupRepository,
                                  ResidentProfileRepository residentProfileRepository,
                                  PassCredentialService passCredentialService) {
        this.groupRepository = groupRepository;
        this.residentProfileRepository = residentProfileRepository;
        this.passCredentialService = passCredentialService;
    }

    public List<GroupInvitation> getInvitationsForHost(Long hostUserId) {
        return groupRepository.findByHostUserId(hostUserId);
    }

    public GroupInvitation getInvitationById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group invitation not found with id: " + id));
    }

    public GroupInvitation getInvitationByReference(String reference) {
        return groupRepository.findByGroupReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Group invitation not found with reference: " + reference));
    }

    @Transactional
    public GroupInvitation createGroupInvitation(Long hostUserId, GroupInvitation request, List<String> guestNames) {
        ResidentProfile profile = residentProfileRepository.findByUserId(hostUserId)
                .orElseThrow(() -> new BadRequestException("Resident profile not found for user: " + hostUserId));

        if (guestNames == null || guestNames.isEmpty()) {
            throw new BadRequestException("At least one guest name is required");
        }

        String groupRef = "GRP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        request.setId(null);
        request.setHostUserId(hostUserId);
        request.setDestinationFlatId(profile.getFlatId());
        request.setGroupReference(groupRef);
        request.setStatus(VisitStatus.APPROVED);
        if (request.getMaxGuests() <= 0) {
            request.setMaxGuests(Math.max(guestNames.size(), 10));
        }

        GroupInvitation saved = groupRepository.save(request);

        for (String name : guestNames) {
            if (name != null && !name.trim().isEmpty()) {
                GroupInvitationMember member = new GroupInvitationMember();
                member.setGroupInvitationId(saved.getId());
                member.setGuestName(name.trim());
                member.setStatus("EXPECTED");
                String passHash = passCredentialService.hashCredential(groupRef + "-" + name.trim().toUpperCase());
                member.setPassCredentialHash(passHash);
                groupRepository.saveMember(member);
            }
        }

        return getInvitationById(saved.getId());
    }

    @Transactional
    public GroupInvitationMember checkInGuest(Long memberId, Long gateId, Long guardUserId) {
        GroupInvitationMember member = groupRepository.findMemberById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest member not found with id: " + memberId));

        GroupInvitation inv = groupRepository.findById(member.getGroupInvitationId())
                .orElseThrow(() -> new ResourceNotFoundException("Group invitation not found"));

        int currentlyInside = groupRepository.countInsideGuests(inv.getId());
        if (currentlyInside >= inv.getMaxGuests()) {
            throw new BadRequestException("Maximum capacity reached for this group event (" + inv.getMaxGuests() + " guests)");
        }

        if (!"EXPECTED".equals(member.getStatus())) {
            throw new BadRequestException("Guest is in state " + member.getStatus() + ", cannot check in");
        }

        member.setStatus("INSIDE");
        member.setCheckInTime(Instant.now());
        member.setEntryGateId(gateId);
        return groupRepository.saveMember(member);
    }

    @Transactional
    public GroupInvitationMember checkOutGuest(Long memberId, Long exitGateId) {
        GroupInvitationMember member = groupRepository.findMemberById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest member not found with id: " + memberId));

        if (!"INSIDE".equals(member.getStatus())) {
            throw new BadRequestException("Guest is not currently inside (status: " + member.getStatus() + ")");
        }

        member.setStatus("CHECKED_OUT");
        member.setCheckOutTime(Instant.now());
        member.setExitGateId(exitGateId);
        return groupRepository.saveMember(member);
    }
}
