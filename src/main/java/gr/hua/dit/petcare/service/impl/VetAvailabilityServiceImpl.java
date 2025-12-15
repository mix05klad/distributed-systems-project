package gr.hua.dit.petcare.service.impl;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.model.VetAvailability;
import gr.hua.dit.petcare.core.repository.UserRepository;
import gr.hua.dit.petcare.core.repository.VetAvailabilityRepository;
import gr.hua.dit.petcare.service.VetAvailabilityService;
import gr.hua.dit.petcare.service.model.VetAvailabilityRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class VetAvailabilityServiceImpl implements VetAvailabilityService {

    private final VetAvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    public VetAvailabilityServiceImpl(VetAvailabilityRepository availabilityRepository,
                                      UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public VetAvailability addAvailability(Long vetId, VetAvailabilityRequest req) {
        if (req.getEndTime() == null || req.getStartTime() == null ||
                !req.getStartTime().isBefore(req.getEndTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        User vet = userRepository.findById(vetId)
                .orElseThrow(() -> new EntityNotFoundException("Vet not found: " + vetId));

        boolean isVet = vet.getRoles().stream()
                .anyMatch(r -> r.equalsIgnoreCase("VET"));

        if (!isVet) {
            throw new IllegalStateException("User is not a vet");
        }

        LocalDateTime start = req.getStartTime();
        LocalDateTime end = req.getEndTime();

        boolean hasOverlap = !availabilityRepository
                .findOverlappingSlots(vetId, start, end).isEmpty();

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
                .orElseThrow(() -> new EntityNotFoundException("Slot not found: " + slotId));

        if (!slot.getVet().getId().equals(vetId)) {
            throw new IllegalStateException("Cannot delete slot of another vet");
        }

        availabilityRepository.delete(slot);
    }
}
