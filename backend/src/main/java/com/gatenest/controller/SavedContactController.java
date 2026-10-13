package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.model.SavedVisitorContact;
import com.gatenest.security.CustomUserDetails;
import com.gatenest.service.SavedContactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resident/saved-contacts")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RESIDENT')")
public class SavedContactController {

    private final SavedContactService contactService;

    public SavedContactController(SavedContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavedVisitorContact>>> getMyContacts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<SavedVisitorContact> list = contactService.getContactsForResident(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SavedVisitorContact>> createContact(
            @Valid @RequestBody SavedVisitorContact contact,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SavedVisitorContact saved = contactService.createContact(userDetails.getUser().getId(), contact);
        return ResponseEntity.ok(ApiResponse.ok("Contact saved successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SavedVisitorContact>> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody SavedVisitorContact contact,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SavedVisitorContact updated = contactService.updateContact(userDetails.getUser().getId(), id, contact);
        return ResponseEntity.ok(ApiResponse.ok("Contact updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContact(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        contactService.deleteContact(userDetails.getUser().getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Contact deleted successfully", null));
    }
}
