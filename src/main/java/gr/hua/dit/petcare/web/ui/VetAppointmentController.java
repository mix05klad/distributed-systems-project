package gr.hua.dit.petcare.web.ui;

import gr.hua.dit.petcare.core.model.AppointmentStatus;
import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.model.AppointmentView;
import gr.hua.dit.petcare.service.model.VisitNotesRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    @GetMapping("/appointments")
    public String listAppointments(Authentication authentication,
                                   Model model,
                                   @RequestParam(value = "showCompleted", required = false, defaultValue = "false")
                                   boolean showCompleted) {

        Long vetId = getCurrentVetId(authentication);

        List<AppointmentView> all = appointmentService.getAppointmentsForVet(vetId);

        List<AppointmentView> filtered = all.stream()
                .filter(a -> showCompleted || a.getStatus() != AppointmentStatus.COMPLETED)
                .toList();

        model.addAttribute("appointments", filtered);
        model.addAttribute("showCompleted", showCompleted);

        return "vet/appointments";
    }

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

    @PostMapping("/appointments/{id}/complete")
    public String completeAppointment(@PathVariable("id") Long id,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {

        Long vetId = getCurrentVetId(authentication);

        try {
            appointmentService.completeAppointment(id, vetId);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment marked as COMPLETED.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/ui/vet/appointments";
    }

    @GetMapping("/appointments/{id}/notes")
    public String showNotesForm(@PathVariable("id") Long id,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        Long vetId = getCurrentVetId(authentication);

        AppointmentView view;
        try {
            view = appointmentService.getAppointmentsForVet(vetId).stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + id));
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/ui/vet/appointments";
        }

        if (view.getStatus() != AppointmentStatus.COMPLETED) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can add notes only to COMPLETED appointments.");
            return "redirect:/ui/vet/appointments";
        }

        if (view.isPetDeleted()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot add/edit notes: this pet has been deleted by the owner.");
            return "redirect:/ui/vet/appointments";
        }

        if (!model.containsAttribute("notesForm")) {
            VisitNotesRequest form = new VisitNotesRequest();
            form.setNotes(view.getVetNotes());
            model.addAttribute("notesForm", form);
        }

        model.addAttribute("appointment", view);
        return "vet/appointment-notes";
    }

    @PostMapping("/appointments/{id}/notes")
    public String submitNotes(@PathVariable("id") Long id,
                              Authentication authentication,
                              @ModelAttribute("notesForm") @Valid VisitNotesRequest form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        Long vetId = getCurrentVetId(authentication);

        if (bindingResult.hasErrors()) {
            AppointmentView view = appointmentService.getAppointmentsForVet(vetId).stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            model.addAttribute("appointment", view);
            return "vet/appointment-notes";
        }

        try {
            appointmentService.updateVisitNotes(id, vetId, form.getNotes());
            redirectAttributes.addFlashAttribute("successMessage", "Visit notes saved.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/ui/vet/appointments";
    }

    @GetMapping("/record-notes")
    public String recordNotesShortcut() {
        return "redirect:/ui/vet/appointments?showCompleted=true";
    }

    private Long getCurrentVetId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof ApplicationUserDetails details) {
            return details.getId();
        }
        throw new IllegalStateException("No authenticated vet");
    }
}
