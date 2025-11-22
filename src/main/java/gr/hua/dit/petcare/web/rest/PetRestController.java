package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.PetService;
import gr.hua.dit.petcare.service.model.CreatePetRequest;
import gr.hua.dit.petcare.service.model.PetView;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetRestController {

    private final PetService petService;

    public PetRestController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping
    public ResponseEntity<PetView> createPet(@Valid @RequestBody CreatePetRequest request) {
        Long ownerId = getCurrentUserId();
        PetView pet = petService.createPet(request, ownerId);
        return ResponseEntity.ok(pet);
    }

    @GetMapping
    public ResponseEntity<List<PetView>> getMyPets() {
        Long ownerId = getCurrentUserId();
        List<PetView> pets = petService.getPetsForOwner(ownerId);
        return ResponseEntity.ok(pets);
    }

    // --- ΝΕΑ ---
    @GetMapping("/{id}")
    public ResponseEntity<PetView> getPet(@PathVariable("id") Long id) {
        Long requesterId = getCurrentUserId();
        PetView pet = petService.getPetById(id, requesterId);
        return ResponseEntity.ok(pet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetView> updatePet(@PathVariable("id") Long id,
                                             @Valid @RequestBody CreatePetRequest request) {
        Long requesterId = getCurrentUserId();
        PetView pet = petService.updatePet(id, request, requesterId);
        return ResponseEntity.ok(pet);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePet(@PathVariable("id") Long id) {
        Long requesterId = getCurrentUserId();
        petService.deletePet(id, requesterId);
        return ResponseEntity.noContent().build();
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
