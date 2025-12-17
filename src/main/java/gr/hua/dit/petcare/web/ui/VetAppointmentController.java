package gr.hua.dit.petcare.web.ui;

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

        return "vet/appointments";
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

    // Mark as COMPLETED
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

    // φόρμα καταχώρισης σημειώσεων για COMPLETED ραντεβού
    @GetMapping("/appointments/{id}/notes")
    public String showNotesForm(@PathVariable("id") Long id,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        Long vetId = getCurrentVetId(authentication);

        AppointmentView view;
        try {
            List<AppointmentView> all = appointmentService.getAppointmentsForVet(vetId);
            view = all.stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + id));
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/ui/vet/appointments";
        }

        // Επιτρέπουμε notes μόνο σε COMPLETED
        if (!"COMPLETED".equalsIgnoreCase(view.getStatus().toString())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You can add notes only to COMPLETED appointments.");
            return "redirect:/ui/vet/appointments";
        }

        if (!model.containsAttribute("notesForm")) {
            VisitNotesRequest form = new VisitNotesRequest();
            form.setNotes(view.getVetNotes()); // αν υπάρχουν ήδη σημειώσεις, τις δείχνουμε
            model.addAttribute("notesForm", form);
        }

        model.addAttribute("appointment", view);

        return "vet/appointment-notes";
    }

    // submit των σημειώσεων
    @PostMapping("/appointments/{id}/notes")
    public String submitNotes(@PathVariable("id") Long id,
                              Authentication authentication,
                              @ModelAttribute("notesForm") @Valid VisitNotesRequest form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        Long vetId = getCurrentVetId(authentication);

        if (bindingResult.hasErrors()) {
            List<AppointmentView> all = appointmentService.getAppointmentsForVet(vetId);
            AppointmentView view = all.stream()
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

    private Long getCurrentVetId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof ApplicationUserDetails details) {
            return details.getId();
        }
        throw new IllegalStateException("No authenticated vet");
    }

    // Shortcut στο screen με ραντεβού
    @GetMapping("/record-notes")
    public String recordNotesShortcut() {
        // δείχνει όλα τα ραντεβού και τα COMPLETED,
        // ώστε ο vet να επιλέξει και να πατήσει "Notes"
        return "redirect:/ui/vet/appointments?showCompleted=true";
    }

}
