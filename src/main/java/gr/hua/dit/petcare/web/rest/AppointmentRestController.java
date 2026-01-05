package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.model.AppointmentView;
import gr.hua.dit.petcare.service.model.CreateAppointmentRequest;
import gr.hua.dit.petcare.service.model.VetFreeSlotView;
import gr.hua.dit.petcare.service.model.VisitNotesRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentRestController {

    private final AppointmentService appointmentService;

    public AppointmentRestController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // καταχώρηση νέου ραντεβού (OWNER)
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    public ResponseEntity<AppointmentView> createAppointment(
            Authentication auth,
            @Valid @RequestBody CreateAppointmentRequest request) {

        Long ownerId = getCurrentUserId(auth);
        AppointmentView view = appointmentService.createAppointment(request, ownerId);
        return ResponseEntity.ok(view);
    }

    // όλα τα ραντεβού ενός ιδιοκτήτη (OWNER)
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/owner")
    public ResponseEntity<List<AppointmentView>> getOwnerAppointments(Authentication auth) {
        Long ownerId = getCurrentUserId(auth);
        List<AppointmentView> list = appointmentService.getAppointmentsForOwner(ownerId);
        return ResponseEntity.ok(list);
    }

    // όλα τα ραντεβού ενός κτηνιάτρου (VET)
    @PreAuthorize("hasRole('VET')")
    @GetMapping("/vet")
    public ResponseEntity<List<AppointmentView>> getVetAppointments(Authentication auth) {
        Long vetId = getCurrentUserId(auth);
        List<AppointmentView> list = appointmentService.getAppointmentsForVet(vetId);
        return ResponseEntity.ok(list);
    }

    // επιβεβαίωση ραντεβού (VET)
    @PreAuthorize("hasRole('VET')")
    @PostMapping("/{id}/confirm")
    public ResponseEntity<AppointmentView> confirmAppointment(Authentication auth,
                                                              @PathVariable Long id) {
        Long vetId = getCurrentUserId(auth);
        AppointmentView view = appointmentService.confirmAppointment(id, vetId);
        return ResponseEntity.ok(view);
    }

    // ακύρωση ραντεβού (OWNER ή VET — ο έλεγχος γίνεται στο service)
    @PreAuthorize("hasAnyRole('OWNER','VET')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppointmentView> cancelAppointment(Authentication auth,
                                                             @PathVariable Long id) {
        Long userId = getCurrentUserId(auth);
        AppointmentView view = appointmentService.cancelAppointment(id, userId);
        return ResponseEntity.ok(view);
    }

    // ολοκλήρωση ραντεβού (VET)
    @PreAuthorize("hasRole('VET')")
    @PostMapping("/{id}/complete")
    public ResponseEntity<AppointmentView> completeAppointment(Authentication auth,
                                                               @PathVariable Long id) {
        Long vetId = getCurrentUserId(auth);
        AppointmentView view = appointmentService.completeAppointment(id, vetId);
        return ResponseEntity.ok(view);
    }

    // update visit notes (VET, μόνο σε COMPLETED)
    @PreAuthorize("hasRole('VET')")
    @PostMapping("/{id}/notes")
    public ResponseEntity<AppointmentView> updateVisitNotes(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody VisitNotesRequest request
    ) {
        Long vetId = getCurrentUserId(auth);
        AppointmentView view = appointmentService.updateVisitNotes(id, vetId, request.getNotes());
        return ResponseEntity.ok(view);
    }

    // Free slots για συγκεκριμένο vet (OWNER — για booking UI/REST)
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/vets/{vetId}/free-slots")
    public ResponseEntity<List<VetFreeSlotView>> getFreeSlotsForVet(@PathVariable Long vetId) {
        return ResponseEntity.ok(appointmentService.getFreeSlotsForVet(vetId));
    }

    // Free slots για τον ίδιο τον vet (VET)
    @PreAuthorize("hasRole('VET')")
    @GetMapping("/vet/free-slots")
    public ResponseEntity<List<VetFreeSlotView>> getMyFreeSlots(Authentication auth) {
        Long vetId = getCurrentUserId(auth);
        return ResponseEntity.ok(appointmentService.getFreeSlotsForVet(vetId));
    }

    private Long getCurrentUserId(Authentication auth) {
        Object principal = auth != null ? auth.getPrincipal() : null;
        if (principal instanceof ApplicationUserDetails details) {
            return details.getId();
        }
        throw new IllegalStateException("No authenticated user");
    }
}
