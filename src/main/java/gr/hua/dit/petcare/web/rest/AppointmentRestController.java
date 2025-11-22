package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.model.AppointmentView;
import gr.hua.dit.petcare.service.model.CreateAppointmentRequest;
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

    /**
     * Owner δημιουργεί νέο ραντεβού για pet που του ανήκει.
     */
    @PostMapping
    public ResponseEntity<AppointmentView> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {

        Long ownerId = getCurrentUserId();
        AppointmentView view = appointmentService.createAppointment(request, ownerId);
        return ResponseEntity.ok(view);
    }

    /**
     * Όλα τα ραντεβού για τον τρέχον ιδιοκτήτη (owner).
     */
    @GetMapping("/owner")
    public ResponseEntity<List<AppointmentView>> getOwnerAppointments() {
        Long ownerId = getCurrentUserId();
        List<AppointmentView> list = appointmentService.getAppointmentsForOwner(ownerId);
        return ResponseEntity.ok(list);
    }

    /**
     * Όλα τα ραντεβού για τον τρέχον vet.
     */
    @GetMapping("/vet")
    public ResponseEntity<List<AppointmentView>> getVetAppointments() {
        Long vetId = getCurrentUserId();
        List<AppointmentView> list = appointmentService.getAppointmentsForVet(vetId);
        return ResponseEntity.ok(list);
    }

    /**
     * Vet επιβεβαιώνει ραντεβού.
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<AppointmentView> confirmAppointment(@PathVariable Long id) {
        Long vetId = getCurrentUserId();
        AppointmentView view = appointmentService.confirmAppointment(id, vetId);
        return ResponseEntity.ok(view);
    }

    /**
     * Owner ή vet ακυρώνει ραντεβού.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppointmentView> cancelAppointment(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        AppointmentView view = appointmentService.cancelAppointment(id, userId);
        return ResponseEntity.ok(view);
    }

    /**
     * Vet ολοκληρώνει ραντεβού.
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<AppointmentView> completeAppointment(@PathVariable Long id) {
        Long vetId = getCurrentUserId();
        AppointmentView view = appointmentService.completeAppointment(id, vetId);
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
