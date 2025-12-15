package gr.hua.dit.petcare.web.ui;

import gr.hua.dit.petcare.core.model.VetAvailability;
import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.VetAvailabilityService;
import gr.hua.dit.petcare.service.model.VetAvailabilityRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/ui/vet/availability")
public class VetAvailabilityController {

    private final VetAvailabilityService availabilityService;

    public VetAvailabilityController(VetAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public String showAvailabilityPage(Authentication authentication, Model model) {
        Long vetId = getCurrentUserId(authentication);

        List<VetAvailability> slots = availabilityService.getAvailabilityForVet(vetId);

        if (!model.containsAttribute("availabilityForm")) {
            model.addAttribute("availabilityForm", new VetAvailabilityRequest());
        }
        model.addAttribute("slots", slots);

        return "vet/vet-availability";
    }

    @PostMapping
    public String addAvailability(@Valid @ModelAttribute("availabilityForm") VetAvailabilityRequest form,
                                  BindingResult bindingResult,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        Long vetId = getCurrentUserId(authentication);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.availabilityForm",
                    bindingResult);
            redirectAttributes.addFlashAttribute("availabilityForm", form);
            return "redirect:/ui/vet/availability";
        }

        try {
            availabilityService.addAvailability(vetId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Availability slot saved.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("availabilityForm", form);
        }

        return "redirect:/ui/vet/availability";
    }

    @PostMapping("/{slotId}/delete")
    public String deleteSlot(@PathVariable Long slotId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        Long vetId = getCurrentUserId(authentication);
        try {
            availabilityService.deleteSlot(vetId, slotId);
            redirectAttributes.addFlashAttribute("successMessage", "Availability slot deleted.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/ui/vet/availability";
    }

    private Long getCurrentUserId(Authentication authentication) {
        ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}
