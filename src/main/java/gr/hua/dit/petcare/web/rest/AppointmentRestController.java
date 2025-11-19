package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.model.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentRestController {

    private final AppointmentService service;

    public AppointmentRestController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    public AppointmentView create(@Valid @RequestBody CreateAppointmentRequest req,
                                  @RequestParam Long ownerId) {
        return service.createAppointment(req, ownerId);
    }

    @PutMapping("/{id}/confirm")
    public AppointmentView confirm(@PathVariable Long id,
                                   @RequestParam Long vetId) {
        return service.confirmAppointment(id, vetId);
    }

    @PutMapping("/{id}/cancel")
    public AppointmentView cancel(@PathVariable Long id,
                                  @RequestParam Long userId) {
        return service.cancelAppointment(id, userId);
    }

    @GetMapping("/owner/{ownerId}")
    public List<AppointmentView> getOwner(@PathVariable Long ownerId) {
        return service.getAppointmentsForOwner(ownerId);
    }

    @GetMapping("/vet/{vetId}")
    public List<AppointmentView> getVet(@PathVariable Long vetId) {
        return service.getAppointmentsForVet(vetId);
    }
}
