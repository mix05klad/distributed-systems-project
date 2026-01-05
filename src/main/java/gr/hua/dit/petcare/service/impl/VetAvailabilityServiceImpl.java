package gr.hua.dit.petcare.service.impl;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.model.VetAvailability;
import gr.hua.dit.petcare.core.repository.UserRepository;
import gr.hua.dit.petcare.core.repository.VetAvailabilityRepository;
import gr.hua.dit.petcare.service.VetAvailabilityService;
import gr.hua.dit.petcare.service.exception.NotFoundException;
import gr.hua.dit.petcare.service.model.VetAvailabilityRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class VetAvailabilityServiceImpl implements VetAvailabilityService {

    // για να αποφύγεις ακραίες καταχωρήσεις (π.χ. 5 μέρες availability)
    private static final Duration MAX_SLOT_DURATION = Duration.ofHours(12);

    private final VetAvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    public VetAvailabilityServiceImpl(VetAvailabilityRepository availabilityRepository,
                                      UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public VetAvailability addAvailability(Long vetId, VetAvailabilityRequest req) {
        if (req == null || req.getStartTime() == null || req.getEndTime() == null) {
            throw new IllegalArgumentException("startTime and endTime are required");
        }

        LocalDateTime start = req.getStartTime();
        LocalDateTime end = req.getEndTime();

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Μην επιτρέπεις availability στο παρελθόν (πολύ χρήσιμο στην πράξη)
        LocalDateTime now = LocalDateTime.now();
        if (end.isBefore(now)) {
            throw new IllegalArgumentException("Availability slot cannot be in the past");
        }

        Duration duration = Duration.between(start, end);
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Slot duration must be > 0");
        }
        if (duration.compareTo(MAX_SLOT_DURATION) > 0) {
            throw new IllegalArgumentException("Availability slot is too long (max " + MAX_SLOT_DURATION.toHours() + " hours)");
        }

        User vet = userRepository.findById(vetId)
                .orElseThrow(() -> new NotFoundException("Vet not found: " + vetId));

        boolean isVet = vet.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("VET"));
        if (!isVet) {
            throw new AccessDeniedException("User is not a vet");
        }

        boolean hasOverlap = !availabilityRepository.findOverlappingSlots(vetId, start, end).isEmpty();
        if (hasOverlap) {
            throw new IllegalStateException("Slot overlaps with existing availability");
        }

        VetAvailability slot = new VetAvailability(vet, start, end);
        return availabilityRepository.save(slot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VetAvailability> getAvailabilityForVet(Long vetId) {
        return availabilityRepository.findByVetIdOrderByStartTimeAsc(vetId);
    }

    @Override
    public void deleteSlot(Long vetId, Long slotId) {
        VetAvailability slot = availabilityRepository.findById(slotId)
                .orElseThrow(() -> new NotFoundException("Slot not found: " + slotId));

        if (slot.getVet() == null || slot.getVet().getId() == null || !slot.getVet().getId().equals(vetId)) {
            throw new AccessDeniedException("Cannot delete slot of another vet");
        }

        availabilityRepository.delete(slot);
    }
}
