package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.core.model.VetAvailability;
import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.VetAvailabilityService;
import gr.hua.dit.petcare.service.model.VetAvailabilityRequest;
import gr.hua.dit.petcare.service.model.VetFreeSlotView;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/vet/availability")
@PreAuthorize("hasRole('VET')")
public class VetAvailabilityRestController {

    private final VetAvailabilityService availabilityService;
    private final AppointmentService appointmentService;

    public VetAvailabilityRestController(VetAvailabilityService availabilityService,
                                         AppointmentService appointmentService) {
        this.availabilityService = availabilityService;
        this.appointmentService = appointmentService;
    }

    // GET: τα availability slots του vet
    @GetMapping
    public ResponseEntity<List<VetAvailabilityDto>> getMyAvailability() {
        Long vetId = getCurrentVetId();
        List<VetAvailability> slots = availabilityService.getAvailabilityForVet(vetId);

        List<VetAvailabilityDto> result = slots.stream()
                .map(s -> new VetAvailabilityDto(
                        s.getId(),
                        s.getStartTime(),
                        s.getEndTime()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }

    // POST: προσθήκη νέου availability slot
    @PostMapping
    public ResponseEntity<VetAvailabilityDto> addAvailability(
            @Valid @RequestBody VetAvailabilityRequest request) {

        Long vetId = getCurrentVetId();
        VetAvailability slot = availabilityService.addAvailability(vetId, request);

        VetAvailabilityDto dto = new VetAvailabilityDto(
                slot.getId(),
                slot.getStartTime(),
                slot.getEndTime()
        );
        return ResponseEntity.ok(dto);
    }

    // DELETE: διαγραφή slot
    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId) {
        Long vetId = getCurrentVetId();
        availabilityService.deleteSlot(vetId, slotId);
        return ResponseEntity.noContent().build();
    }

    // πραγματικά free slots (availability - ραντεβού)
    @GetMapping("/free-slots")
    public ResponseEntity<List<VetFreeSlotView>> getMyFreeSlots() {
        Long vetId = getCurrentVetId();
        List<VetFreeSlotView> freeSlots = appointmentService.getFreeSlotsForVet(vetId);
        return ResponseEntity.ok(freeSlots);
    }

    private Long getCurrentVetId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof ApplicationUserDetails details) {
            return details.getId();
        }
        throw new IllegalStateException("No authenticated vet");
    }

    public record VetAvailabilityDto(
            Long id,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {}
}
