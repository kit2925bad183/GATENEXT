package com.gatenest.service;

import com.gatenest.exception.BadRequestException;
import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.NotificationType;
import com.gatenest.model.Parcel;
import com.gatenest.model.ParcelEvent;
import com.gatenest.model.ParcelStatus;
import com.gatenest.repository.ParcelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ParcelService {

    private final ParcelRepository parcelRepository;
    private final NotificationService notificationService;

    public ParcelService(ParcelRepository parcelRepository, NotificationService notificationService) {
        this.parcelRepository = parcelRepository;
        this.notificationService = notificationService;
    }

    public List<Parcel> getParcelsForResident(Long residentUserId) {
        return parcelRepository.findByRecipientUserId(residentUserId);
    }

    public List<Parcel> getPendingParcels() {
        return parcelRepository.findAllPending();
    }

    public List<Parcel> getParcelHistory(int limit, int offset) {
        return parcelRepository.findAllHistory(limit, offset);
    }

    public Parcel getParcelById(Long id) {
        return parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + id));
    }

    @Transactional
    public Parcel receiveParcel(Parcel parcel, Long guardUserId) {
        parcel.setId(null);
        parcel.setReceivedByUserId(guardUserId);
        parcel.setStatus(ParcelStatus.READY_FOR_COLLECTION);
        Parcel saved = parcelRepository.save(parcel);

        parcelRepository.recordEvent(saved.getId(), "RECEIVED", guardUserId,
                "Received courier " + saved.getCourierName() + " stored at " + saved.getStorageLocation());

        notificationService.sendNotification(
                saved.getRecipientUserId(),
                "Parcel Arrived",
                "A parcel from " + saved.getCourierName() + " has arrived and is stored at " + saved.getStorageLocation(),
                "/parcels",
                NotificationType.PARCEL_RECEIVED
        );

        return getParcelById(saved.getId());
    }

    @Transactional
    public Parcel delegateCollection(Long parcelId, Long residentUserId, String delegateName, String notes) {
        Parcel parcel = getParcelById(parcelId);
        if (!parcel.getRecipientUserId().equals(residentUserId)) {
            throw new BadRequestException("You are not the recipient of this parcel");
        }
        if (parcel.getStatus() == ParcelStatus.COLLECTED || parcel.getStatus() == ParcelStatus.RETURNED) {
            throw new BadRequestException("Parcel has already been completed");
        }

        parcelRepository.updateDelegate(parcelId, residentUserId, delegateName, notes);
        parcelRepository.recordEvent(parcelId, "DELEGATE_ASSIGNED", residentUserId,
                "Authorized " + delegateName + " to collect the parcel. Note: " + notes);

        return getParcelById(parcelId);
    }

    @Transactional
    public Parcel markCollected(Long parcelId, String collectorName, Long collectorUserId, Long guardUserId) {
        Parcel parcel = getParcelById(parcelId);
        if (parcel.getStatus() == ParcelStatus.COLLECTED) {
            throw new BadRequestException("Parcel has already been collected");
        }

        boolean updated = parcelRepository.markCollectedAtomic(parcelId, collectorName, collectorUserId, guardUserId);
        if (!updated) {
            throw new BadRequestException("Failed to mark parcel collected. It may have been collected concurrently.");
        }

        parcelRepository.recordEvent(parcelId, "COLLECTED", guardUserId,
                "Handed over to " + collectorName + " verified by guard user ID " + guardUserId);

        notificationService.sendNotification(
                parcel.getRecipientUserId(),
                "Parcel Collected",
                "Your parcel from " + parcel.getCourierName() + " was handed over to " + collectorName,
                "/parcels",
                NotificationType.PARCEL_COLLECTED
        );

        return getParcelById(parcelId);
    }

    public List<ParcelEvent> getParcelEvents(Long parcelId) {
        return parcelRepository.findEventsByParcelId(parcelId);
    }
}
