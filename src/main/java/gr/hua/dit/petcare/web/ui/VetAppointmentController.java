package gr.hua.dit.petcare.web.ui;

import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.model.AppointmentView;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/ui/vet")
public class VetAppointmentController {

    private final AppointmentService appointmentService;

    public VetAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // Λίστα ραντεβού κτηνιάτρου
    @GetMapping("/appointments")
    public String listAppointments(Authentication authentication,
                                   Model model,
                                   @RequestParam(value = "showCompleted",
                                           required = false,
                                           defaultValue = "false") boolean showCompleted) {

        Long vetId = getCurrentVetId(authentication);

        List<AppointmentView> all = appointmentService.getAppointmentsForVet(vetId);

        List<AppointmentView> filtered = all.stream()
                .filter(a -> showCompleted || !"COMPLETED".equalsIgnoreCase(a.getStatus().toString()))
                .toList();

        model.addAttribute("appointments", filtered);
        model.addAttribute("showCompleted", showCompleted);

        return "vet/appointments"; // templates/vet/appointments.html
    }

    // Επιβεβαίωση ραντεβού
    @PostMapping("/appointments/{id}/confirm")
    public String confirmAppointment(@PathVariable("id") Long id,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {

        Long vetId = getCurrentVetId(authentication);

        try {
            appointmentService.confirmAppointment(id, vetId);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment confirmed.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/ui/vet/appointments";
    }

    // Ακύρωση ραντεβού
    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable("id") Long id,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {

        Long vetId = getCurrentVetId(authentication);

        try {
            appointmentService.cancelAppointment(id, vetId);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment cancelled.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/ui/vet/appointments";
    }

    private Long getCurrentVetId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof ApplicationUserDetails details) {
            return details.getId();
        }
        throw new IllegalStateException("No authenticated vet");
    }
}
