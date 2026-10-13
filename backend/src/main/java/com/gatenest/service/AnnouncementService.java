package com.gatenest.service;

import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.Announcement;
import com.gatenest.model.ResidentProfile;
import com.gatenest.repository.AnnouncementRepository;
import com.gatenest.repository.ResidentProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ResidentProfileRepository residentProfileRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               ResidentProfileRepository residentProfileRepository) {
        this.announcementRepository = announcementRepository;
        this.residentProfileRepository = residentProfileRepository;
    }

    public List<Announcement> getFeedForUser(Long userId) {
        Optional<ResidentProfile> profile = residentProfileRepository.findByUserId(userId);
        Long blockId = profile.map(ResidentProfile::getBlockId).orElse(null);
        return announcementRepository.findFeedForUser(userId, blockId);
    }

    public List<Announcement> getAllAnnouncementsAdmin(int limit, int offset) {
        return announcementRepository.findAllAdmin(limit, offset);
    }

    public Announcement getAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));
    }

    public Announcement createOrUpdateAnnouncement(Announcement a, Long authorUserId) {
        if (a.getId() == null) {
            a.setAuthorUserId(authorUserId);
        }
        return announcementRepository.save(a);
    }

    public void markAsRead(Long announcementId, Long userId) {
        announcementRepository.markAsRead(announcementId, userId);
    }
}
