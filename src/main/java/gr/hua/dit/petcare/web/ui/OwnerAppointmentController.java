package gr.hua.dit.petcare.web.ui;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.PetService;
import gr.hua.dit.petcare.service.model.AppointmentView;
import gr.hua.dit.petcare.service.model.CreateAppointmentRequest;
import gr.hua.dit.petcare.service.model.VetFreeSlotView;
import gr.hua.dit.petcare.core.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/ui/owner")
public class OwnerAppointmentController {

    private final AppointmentService appointmentService;
    private final PetService petService;
    private final UserRepository userRepository;

    public OwnerAppointmentController(AppointmentService appointmentService,
                                      PetService petService,
                                      UserRepository userRepository) {
        this.appointmentService = appointmentService;
        this.petService = petService;
        this.userRepository = userRepository;
    }

    @GetMapping("/book-appointment")
    public String showBookAppointmentForm(Authentication authentication,
                                          Model model,
                                          @RequestParam(value = "vetId", required = false) Long vetId) {

        Long ownerId = getCurrentUserId(authentication);

        if (!model.containsAttribute("appointmentForm")) {
            CreateAppointmentRequest form = new CreateAppointmentRequest();
            if (vetId != null) form.setVetId(vetId);
            model.addAttribute("appointmentForm", form);
        } else {
            // αν ήρθε από redirect με form, αλλά έχει και vetId param, κράτα το
            CreateAppointmentRequest form = (CreateAppointmentRequest) model.getAttribute("appointmentForm");
            if (form != null && form.getVetId() == null && vetId != null) {
                form.setVetId(vetId);
            }
        }

        populatePetsAndVets(model, ownerId);
        populateOwnerAppointments(model, ownerId);

        CreateAppointmentRequest current = (CreateAppointmentRequest) model.getAttribute("appointmentForm");
        populateSelectedVetAvailability(model, current != null ? current.getVetId() : vetId);

        return "owner/book-appointment";
    }

    @PostMapping("/book-appointment")
    public String submitBookAppointment(Authentication authentication,
                                        @ModelAttribute("appointmentForm") @Valid CreateAppointmentRequest form,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {

        Long ownerId = getCurrentUserId(authentication);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.appointmentForm", bindingResult);
            redirectAttributes.addFlashAttribute("appointmentForm", form);
            return "redirect:/ui/owner/book-appointment" + (form.getVetId() != null ? "?vetId=" + form.getVetId() : "");
        }

        try {
            AppointmentView created = appointmentService.createAppointment(form, ownerId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Το ραντεβού καταχωρήθηκε (PENDING) και αναμένει επιβεβαίωση από τον κτηνίατρο.");

            if (created.getWarnings() != null && !created.getWarnings().isEmpty()) {
                // είτε μόνο το πρώτο:
                //redirectAttributes.addFlashAttribute("warningMessage", created.getWarnings().get(0));

                redirectAttributes.addFlashAttribute("warningMessage", String.join(" ", created.getWarnings()));
            }

        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("appointmentForm", form);
            return "redirect:/ui/owner/book-appointment" + (form.getVetId() != null ? "?vetId=" + form.getVetId() : "");
        }

        return "redirect:/ui/owner/book-appointment" + (form.getVetId() != null ? "?vetId=" + form.getVetId() : "");
    }

    private void populatePetsAndVets(Model model, Long ownerId) {
        model.addAttribute("pets", petService.getPetsForOwner(ownerId));

        List<User> vets = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("VET")))
                .toList();
        model.addAttribute("vets", vets);
    }

    private void populateOwnerAppointments(Model model, Long ownerId) {
        List<AppointmentView> appointments = appointmentService.getAppointmentsForOwner(ownerId);
        model.addAttribute("appointments", appointments);
    }

    private void populateSelectedVetAvailability(Model model, Long vetId) {
        if (vetId == null) {
            model.addAttribute("selectedVetAvailability", null);
            return;
        }
        List<VetFreeSlotView> freeSlots = appointmentService.getFreeSlotsForVet(vetId);
        model.addAttribute("selectedVetAvailability", freeSlots);
    }

    private Long getCurrentUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof ApplicationUserDetails details) {
            return details.getId();
        }
        throw new IllegalStateException("No authenticated user");
    }
}
