package gr.hua.dit.petcare.web.ui;

import gr.hua.dit.petcare.core.model.VisitType;
import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.PetService;
import gr.hua.dit.petcare.service.model.AppointmentView;
import gr.hua.dit.petcare.service.model.CreatePetRequest;
import gr.hua.dit.petcare.service.model.PetView;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/ui/owner")
public class OwnerPetController {

    private final PetService petService;
    private final AppointmentService appointmentService;

    public OwnerPetController(PetService petService,
                              AppointmentService appointmentService) {
        this.petService = petService;
        this.appointmentService = appointmentService;
    }

    // -------------------- Register Pet --------------------

    @GetMapping("/register-pet")
    public String registerPetForm(Model model) {
        if (!model.containsAttribute("petForm")) {
            model.addAttribute("petForm", new CreatePetRequest());
        }
        return "owner/register-pet";
    }

    @PostMapping("/register-pet")
    public String registerPetSubmit(
            @ModelAttribute("petForm") @Valid CreatePetRequest petForm,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "owner/register-pet";
        }

        Long ownerId = getCurrentUserId();
        petService.createPet(petForm, ownerId);

        model.addAttribute("successMessage", "Your pet has been successfully registered.");
        model.addAttribute("petForm", new CreatePetRequest());

        return "owner/register-pet";
    }

    // -------------------- Pet Medical History --------------------

    @GetMapping("/pet-history")
    public String viewPetHistory(
            @RequestParam(name = "petId", required = false) Long petId,
            @RequestParam(name = "visitType", required = false) VisitType visitType,
            Model model) {

        Long ownerId = getCurrentUserId();

        // Pets του owner για το dropdown
        List<PetView> pets = petService.getPetsForOwner(ownerId);
        model.addAttribute("pets", pets);
        model.addAttribute("selectedPetId", petId);
        model.addAttribute("selectedVisitType", visitType);

        String selectedPetName = null;
        Integer selectedPetAge = null;
        String selectedPetType = null;

        List<AppointmentView> history = Collections.emptyList();

        if (petId != null) {
            try {
                history = appointmentService.getPetHistory(petId, ownerId);

                for (PetView p : pets) {
                    if (p.getId().equals(petId)) {
                        selectedPetName = p.getName();
                        selectedPetAge = p.getAge();
                        selectedPetType = p.getType() != null ? p.getType().name() : null;
                        break;
                    }
                }

                if (visitType != null) {
                    history = history.stream()
                            .filter(h -> h.getVisitType() != null && h.getVisitType() == visitType)
                            .toList();
                }

            } catch (IllegalArgumentException | AccessDeniedException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        }

        model.addAttribute("history", history);
        model.addAttribute("historyCount", history.size());
        model.addAttribute("selectedPetName", selectedPetName);
        model.addAttribute("selectedPetAge", selectedPetAge);
        model.addAttribute("selectedPetType", selectedPetType);

        // για το dropdown των visit types
        model.addAttribute("allVisitTypes", VisitType.values());

        return "owner/pet-history";
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
