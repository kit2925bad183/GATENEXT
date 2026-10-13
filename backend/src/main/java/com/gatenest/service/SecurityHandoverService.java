package com.gatenest.service;

import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.SecurityHandoverNote;
import com.gatenest.repository.SecurityHandoverRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityHandoverService {

    private final SecurityHandoverRepository handoverRepository;

    public SecurityHandoverService(SecurityHandoverRepository handoverRepository) {
        this.handoverRepository = handoverRepository;
    }

    public List<SecurityHandoverNote> getRecentNotes(int limit) {
        return handoverRepository.findRecentNotes(limit);
    }

    public SecurityHandoverNote createNote(SecurityHandoverNote note, Long authorUserId) {
        note.setId(null);
        note.setAuthorUserId(authorUserId);
        note.setAcknowledged(false);
        return handoverRepository.save(note);
    }

    public boolean acknowledgeNote(Long noteId, Long guardUserId) {
        return handoverRepository.acknowledgeNote(noteId, guardUserId);
    }
}
