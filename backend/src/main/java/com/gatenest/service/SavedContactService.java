package com.gatenest.service;

import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.SavedVisitorContact;
import com.gatenest.repository.SavedVisitorContactRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SavedContactService {

    private final SavedVisitorContactRepository contactRepository;

    public SavedContactService(SavedVisitorContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public List<SavedVisitorContact> getContactsForResident(Long residentUserId) {
        return contactRepository.findByResidentUserId(residentUserId);
    }

    public SavedVisitorContact createContact(Long residentUserId, SavedVisitorContact contact) {
        contact.setId(null);
        contact.setResidentUserId(residentUserId);
        return contactRepository.save(contact);
    }

    public SavedVisitorContact updateContact(Long residentUserId, Long contactId, SavedVisitorContact contact) {
        SavedVisitorContact existing = contactRepository.findByIdAndResidentUserId(contactId, residentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved contact not found with id: " + contactId));
        existing.setVisitorName(contact.getVisitorName());
        existing.setVisitorMobile(contact.getVisitorMobile());
        existing.setVisitorCategory(contact.getVisitorCategory());
        existing.setDefaultPurpose(contact.getDefaultPurpose());
        existing.setDefaultVehicleNumber(contact.getDefaultVehicleNumber());
        existing.setNotes(contact.getNotes());
        return contactRepository.save(existing);
    }

    public void deleteContact(Long residentUserId, Long contactId) {
        boolean deleted = contactRepository.deleteByIdAndResidentUserId(contactId, residentUserId);
        if (!deleted) {
            throw new ResourceNotFoundException("Saved contact not found with id: " + contactId);
        }
    }
}
