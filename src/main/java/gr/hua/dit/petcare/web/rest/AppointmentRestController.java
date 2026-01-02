package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.model.AppointmentView;
import gr.hua.dit.petcare.service.model.CreateAppointmentRequest;
import gr.hua.dit.petcare.service.model.VisitNotesRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @PostMapping
    public ResponseEntity<AppointmentView> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {

        Long ownerId = getCurrentUserId();
        AppointmentView view = appointmentService.createAppointment(request, ownerId);
        return ResponseEntity.ok(view);
    }

    // όλα τα ραντεβού ενός ιδιοκτήτη
    @GetMapping("/owner")
    public ResponseEntity<List<AppointmentView>> getOwnerAppointments() {
        Long ownerId = getCurrentUserId();
        List<AppointmentView> list = appointmentService.getAppointmentsForOwner(ownerId);
        return ResponseEntity.ok(list);
    }

    // όλα τα ραντεβού ενός κτηνιάτρου
    @GetMapping("/vet")
    public ResponseEntity<List<AppointmentView>> getVetAppointments() {
        Long vetId = getCurrentUserId();
        List<AppointmentView> list = appointmentService.getAppointmentsForVet(vetId);
        return ResponseEntity.ok(list);
    }

    // επιβεβαίωση ραντεβού (VET)
    @PostMapping("/{id}/confirm")
    public ResponseEntity<AppointmentView> confirmAppointment(@PathVariable Long id) {
        Long vetId = getCurrentUserId();
        AppointmentView view = appointmentService.confirmAppointment(id, vetId);
        return ResponseEntity.ok(view);
    }

    // ακύρωση ραντεβού (OWNER ή VET, service κάνει τον έλεγχο)
    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppointmentView> cancelAppointment(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        AppointmentView view = appointmentService.cancelAppointment(id, userId);
        return ResponseEntity.ok(view);
    }

    // ολοκλήρωση ραντεβού (VET)
    @PostMapping("/{id}/complete")
    public ResponseEntity<AppointmentView> completeAppointment(@PathVariable Long id) {
        Long vetId = getCurrentUserId();
        AppointmentView view = appointmentService.completeAppointment(id, vetId);
        return ResponseEntity.ok(view);
    }

    // καταχώρηση / update visit notes (VET, μόνο σε COMPLETED)
    @PostMapping("/{id}/notes")
    public ResponseEntity<AppointmentView> updateVisitNotes(
            @PathVariable Long id,
            @Valid @RequestBody VisitNotesRequest request
    ) {
        Long vetId = getCurrentUserId();
        AppointmentView view = appointmentService.updateVisitNotes(id, vetId, request.getNotes());
        return ResponseEntity.ok(view);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof ApplicationUserDetails details) {
            return details.getId();
        }
        throw new IllegalStateException("No authenticated user");
    }
}
