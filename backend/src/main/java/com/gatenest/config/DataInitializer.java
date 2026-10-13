package com.gatenest.config;

import com.gatenest.model.*;
import com.gatenest.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@Profile({"dev", "default"})
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ResidentProfileRepository residentProfileRepository;
    private final SecurityAssignmentRepository securityAssignmentRepository;
    private final GateRepository gateRepository;
    private final FlatRepository flatRepository;
    private final AnnouncementRepository announcementRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            ResidentProfileRepository residentProfileRepository,
            SecurityAssignmentRepository securityAssignmentRepository,
            GateRepository gateRepository,
            FlatRepository flatRepository,
            AnnouncementRepository announcementRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.residentProfileRepository = residentProfileRepository;
        this.securityAssignmentRepository = securityAssignmentRepository;
        this.gateRepository = gateRepository;
        this.flatRepository = flatRepository;
        this.announcementRepository = announcementRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User admin = seedAdminUser();
        seedSecurityUser();
        seedResidentUser();
        if (admin != null) {
            seedSampleAnnouncements(admin.getId());
        }
    }

    private User seedAdminUser() {
        String email = "admin@greenfield.com";
        Optional<User> existing = userRepository.findByEmail(email);
        User admin;
        if (existing.isEmpty()) {
            admin = new User();
            admin.setEmail(email);
            admin.setFullName("Greenfield Estate Admin");
            admin.setMobile("+91 80 4912 0001");
            admin.setPasswordHash(passwordEncoder.encode("admin1234"));
            admin.setRole(Role.ADMINISTRATOR);
            admin.setStatus(UserStatus.ACTIVE);
            admin = userRepository.save(admin);
            log.info("Seeded demo administrator: {}", email);
        } else {
            admin = existing.get();
            admin.setPasswordHash(passwordEncoder.encode("admin1234"));
            admin.setStatus(UserStatus.ACTIVE);
            admin.setRole(Role.ADMINISTRATOR);
            userRepository.save(admin);
            log.info("Synchronized demo administrator password: {}", email);
        }
        return admin;
    }

    private void seedSecurityUser() {
        String email = "guard.north@greenfield.com";
        Optional<User> existing = userRepository.findByEmail(email);
        Long guardId;
        if (existing.isEmpty()) {
            User guard = new User();
            guard.setEmail(email);
            guard.setFullName("Guard Shankar (North Gate)");
            guard.setMobile("+91 80 4912 0002");
            guard.setPasswordHash(passwordEncoder.encode("guard1234"));
            guard.setRole(Role.SECURITY);
            guard.setStatus(UserStatus.ACTIVE);
            User savedGuard = userRepository.save(guard);
            guardId = savedGuard.getId();
            log.info("Seeded demo security guard: {}", email);
        } else {
            User guard = existing.get();
            guard.setPasswordHash(passwordEncoder.encode("guard1234"));
            guard.setStatus(UserStatus.ACTIVE);
            guard.setRole(Role.SECURITY);
            userRepository.save(guard);
            guardId = guard.getId();
            log.info("Synchronized demo security guard: {}", email);
        }

        // Ensure active gate assignment
        Optional<Gate> gate = gateRepository.findById(1L);
        Long gateId = gate.map(Gate::getId).orElse(1L);
        Optional<SecurityGateAssignment> assignment = securityAssignmentRepository.findActiveByUserId(guardId);
        if (assignment.isEmpty()) {
            SecurityGateAssignment newAssign = new SecurityGateAssignment();
            newAssign.setUserId(guardId);
            newAssign.setGateId(gateId);
            newAssign.setActive(true);
            newAssign.setShiftName("Day Shift (06:00 - 18:00)");
            newAssign.setAssignedAt(Instant.now());
            securityAssignmentRepository.save(newAssign);
        }
    }

    private void seedResidentUser() {
        String email = "resident.101@greenfield.com";
        Optional<User> existing = userRepository.findByEmail(email);
        Long residentId;
        if (existing.isEmpty()) {
            User resident = new User();
            resident.setEmail(email);
            resident.setFullName("Aarav Sharma");
            resident.setMobile("+91 98 4501 0101");
            resident.setPasswordHash(passwordEncoder.encode("resident1234"));
            resident.setRole(Role.RESIDENT);
            resident.setStatus(UserStatus.ACTIVE);
            User savedResident = userRepository.save(resident);
            residentId = savedResident.getId();
            log.info("Seeded demo resident: {}", email);
        } else {
            User resident = existing.get();
            resident.setPasswordHash(passwordEncoder.encode("resident1234"));
            resident.setStatus(UserStatus.ACTIVE);
            resident.setRole(Role.RESIDENT);
            userRepository.save(resident);
            residentId = resident.getId();
            log.info("Synchronized demo resident: {}", email);
        }

        // Link to flat A-101 (flat id 1)
        Optional<Flat> flat = flatRepository.findById(1L);
        Long flatId = flat.map(Flat::getId).orElse(1L);
        Optional<ResidentProfile> profile = residentProfileRepository.findByUserId(residentId);
        if (profile.isEmpty()) {
            ResidentProfile newProfile = new ResidentProfile();
            newProfile.setUserId(residentId);
            newProfile.setFlatId(flatId);
            newProfile.setOccupancyType(OccupancyType.OWNER);
            newProfile.setVerificationStatus(VerificationStatus.APPROVED);
            newProfile.setReviewedAt(Instant.now());
            newProfile.setEmergencyContact("+91 98 4501 0000");
            residentProfileRepository.save(newProfile);
        }

        seedVishvaUser();
    }

    private void seedVishvaUser() {
        String email = "vishvaganesan123@gmail.com";
        Optional<User> existing = userRepository.findByEmail(email);
        Long userId;
        if (existing.isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setFullName("Vishva Ganesan");
            user.setMobile("+91 98765 43219");
            user.setPasswordHash(passwordEncoder.encode("Password123!"));
            user.setRole(Role.RESIDENT);
            user.setStatus(UserStatus.ACTIVE);
            User saved = userRepository.save(user);
            userId = saved.getId();
            log.info("Seeded resident: {}", email);
        } else {
            User user = existing.get();
            user.setPasswordHash(passwordEncoder.encode("Password123!"));
            user.setStatus(UserStatus.ACTIVE);
            user.setRole(Role.RESIDENT);
            userRepository.save(user);
            userId = user.getId();
            log.info("Synchronized resident password: {}", email);
        }

        Optional<ResidentProfile> profile = residentProfileRepository.findByUserId(userId);
        if (profile.isEmpty()) {
            ResidentProfile newProfile = new ResidentProfile();
            newProfile.setUserId(userId);
            newProfile.setFlatId(2L); // Flat A-102
            newProfile.setOccupancyType(OccupancyType.OWNER);
            newProfile.setVerificationStatus(VerificationStatus.APPROVED);
            newProfile.setReviewedAt(Instant.now());
            newProfile.setEmergencyContact("+91 98765 43219");
            residentProfileRepository.save(newProfile);
        } else {
            ResidentProfile existingProf = profile.get();
            existingProf.setVerificationStatus(VerificationStatus.APPROVED);
            existingProf.setReviewedAt(Instant.now());
            residentProfileRepository.save(existingProf);
        }
    }

    private void seedSampleAnnouncements(Long adminUserId) {
        if (announcementRepository.findAllAdmin(1, 0).isEmpty()) {
            Announcement a1 = new Announcement();
            a1.setAuthorUserId(adminUserId);
            a1.setTitle("Society Annual General Meeting (AGM) Scheduled");
            a1.setContent("The Greenfield Heights Annual General Meeting will take place this Sunday at 10:30 AM in the Clubhouse Hall. All apartment owners are cordially invited to participate.");
            a1.setAnnouncementType("EVENT");
            a1.setPriority("IMPORTANT");
            a1.setAudienceType("COMMUNITY");
            a1.setPinned(true);
            a1.setPublished(true);
            announcementRepository.save(a1);

            Announcement a2 = new Announcement();
            a2.setAuthorUserId(adminUserId);
            a2.setTitle("Scheduled Elevator Maintenance - Block A");
            a2.setContent("Elevator Lift 2 in Block A will undergo routine preventive safety servicing on Thursday from 2:00 PM to 4:30 PM. Please utilize Lift 1 during this window.");
            a2.setAnnouncementType("MAINTENANCE");
            a2.setPriority("NORMAL");
            a2.setAudienceType("COMMUNITY");
            a2.setPinned(false);
            a2.setPublished(true);
            announcementRepository.save(a2);

            Announcement a3 = new Announcement();
            a3.setAuthorUserId(adminUserId);
            a3.setTitle("Monsoon Water Drainage Inspection & Advisory");
            a3.setContent("Facility management has cleared all rooftop and terrace drainage conduits ahead of seasonal rains. Residents are requested to ensure balcony drain grates remain unobstructed.");
            a3.setAnnouncementType("GENERAL");
            a3.setPriority("NORMAL");
            a3.setAudienceType("COMMUNITY");
            a3.setPinned(false);
            a3.setPublished(true);
            announcementRepository.save(a3);
            log.info("Seeded sample community announcements");
        }
    }
}
