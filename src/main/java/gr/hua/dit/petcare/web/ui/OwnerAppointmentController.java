package gr.hua.dit.petcare.web.ui;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.model.VetAvailability;
import gr.hua.dit.petcare.core.repository.UserRepository;
import gr.hua.dit.petcare.core.repository.VetAvailabilityRepository;
import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.PetService;
import gr.hua.dit.petcare.service.model.AppointmentView;
import gr.hua.dit.petcare.service.model.CreateAppointmentRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/ui/owner")
public class OwnerAppointmentController {

    private final AppointmentService appointmentService;
    private final PetService petService;
    private final UserRepository userRepository;
    private final VetAvailabilityRepository vetAvailabilityRepository;

    public OwnerAppointmentController(AppointmentService appointmentService,
                                      PetService petService,
                                      UserRepository userRepository,
                                      VetAvailabilityRepository vetAvailabilityRepository) {
        this.appointmentService = appointmentService;
        this.petService = petService;
        this.userRepository = userRepository;
        this.vetAvailabilityRepository = vetAvailabilityRepository;
    }

    // Φόρμα κράτησης ραντεβού + λίστα ραντεβού ιδιοκτήτη
    @GetMapping("/book-appointment")
    public String showBookAppointmentForm(
            Authentication authentication,
            Model model,
            @RequestParam(value = "vetId", required = false) Long vetId
    ) {
        Long ownerId = getCurrentUserId(authentication);

        CreateAppointmentRequest form = (CreateAppointmentRequest) model.getAttribute("appointmentForm");
        if (form == null) {
            form = new CreateAppointmentRequest();
        }

        // Αν ήρθαμε με ?vetId=..., προεπιλογή του vet στη φόρμα
        if (vetId != null) {
            form.setVetId(vetId);
        }

        model.addAttribute("appointmentForm", form);

        populatePetsAndVets(model, ownerId);
        populateOwnerAppointments(model, ownerId);
        populateSelectedVetAvailability(model, form.getVetId());

        return "owner/book-appointment";
    }

    // Υποβολή φόρμας
    @PostMapping("/book-appointment")
    public String submitBookAppointment(
            Authentication authentication,
            @ModelAttribute("appointmentForm") @Valid CreateAppointmentRequest form,
            BindingResult bindingResult,
            Model model
    ) {
        Long ownerId = getCurrentUserId(authentication);

        if (bindingResult.hasErrors()) {
            populatePetsAndVets(model, ownerId);
            populateOwnerAppointments(model, ownerId);
            populateSelectedVetAvailability(model, form.getVetId());
            return "owner/book-appointment";
        }

        try {
            appointmentService.createAppointment(form, ownerId);

            model.addAttribute("successMessage",
                    "Το ραντεβού καταχωρήθηκε (PENDING) και αναμένει επιβεβαίωση από τον κτηνίατρο.");
            model.addAttribute("appointmentForm", new CreateAppointmentRequest());
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("appointmentForm", form);
        }

        populatePetsAndVets(model, ownerId);
        populateOwnerAppointments(model, ownerId);
        populateSelectedVetAvailability(model, form.getVetId());

        return "owner/book-appointment";
    }

    private void populatePetsAndVets(Model model, Long ownerId) {
        // Pets του ιδιοκτήτη
        model.addAttribute("pets", petService.getPetsForOwner(ownerId));

        // Όλοι οι χρήστες με ρόλο VET
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
            return;
        }
        List<VetAvailability> slots =
                vetAvailabilityRepository.findByVetIdOrderByStartTimeAsc(vetId);
        model.addAttribute("selectedVetAvailability", slots);
    }

    private Long getCurrentUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof ApplicationUserDetails details) {
            return details.getId();
        }
        throw new IllegalStateException("No authenticated user");
    }
}
