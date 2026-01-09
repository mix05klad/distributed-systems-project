package gr.hua.dit.petcare.web.ui;

import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.PetService;
import gr.hua.dit.petcare.service.exception.NotFoundException;
import gr.hua.dit.petcare.service.model.PetView;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class OwnerHomeController {

    private final PetService petService;

    public OwnerHomeController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/ui/owner/home")
    public String ownerHome(Authentication auth, Model model) {
        Long ownerId = getCurrentUserId(auth);

        List<PetView> pets = petService.getPetsForOwner(ownerId);
        model.addAttribute("pets", pets);

        return "owner/home"; // owner/home.html
    }

    // Soft delete pet (OWNER)
    @PostMapping("/ui/owner/pets/{petId}/delete")
    public String deletePet(Authentication auth,
                            @PathVariable Long petId,
                            RedirectAttributes redirectAttributes) {

        Long ownerId = getCurrentUserId(auth);

        try {
            petService.deletePet(petId, ownerId);
            redirectAttributes.addFlashAttribute("successMessage", "Pet deleted.");
        } catch (NotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are not allowed to delete this pet.");
        } catch (RuntimeException ex) {
            // fallback (π.χ. unexpected)
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete pet: " + ex.getMessage());
        }

        return "redirect:/ui/owner/home";
    }

    private Long getCurrentUserId(Authentication auth) {
        Object principal = auth != null ? auth.getPrincipal() : null;
        if (principal instanceof ApplicationUserDetails details) {
            return details.getId();
        }
        throw new IllegalStateException("No authenticated user");
    }
}
