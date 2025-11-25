package gr.hua.dit.petcare.web.ui;

import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.PetService;
import gr.hua.dit.petcare.service.model.CreatePetRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ui/owner")
public class OwnerPetController {

    private final PetService petService;

    public OwnerPetController(PetService petService) {
        this.petService = petService;
    }

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
            return "owner/register-pet"; // ΟΧΙ REDIRECT
        }

        Long ownerId = getCurrentUserId();
        petService.createPet(petForm, ownerId);

        model.addAttribute("successMessage", "Your pet has been successfully registered.");
        model.addAttribute("petForm", new CreatePetRequest());

        return "owner/register-pet";
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
