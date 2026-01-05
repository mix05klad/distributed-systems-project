package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.core.model.VisitType;
import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.PetService;
import gr.hua.dit.petcare.service.model.AppointmentView;
import gr.hua.dit.petcare.service.model.CreatePetRequest;
import gr.hua.dit.petcare.service.model.PetView;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetRestController {

    private final PetService petService;
    private final AppointmentService appointmentService;

    public PetRestController(PetService petService,
                             AppointmentService appointmentService) {
        this.petService = petService;
        this.appointmentService = appointmentService;
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    public ResponseEntity<PetView> createPet(Authentication auth,
                                             @Valid @RequestBody CreatePetRequest request) {
        Long ownerId = getCurrentUserId(auth);
        PetView pet = petService.createPet(request, ownerId);
        return ResponseEntity.ok(pet);
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping
    public ResponseEntity<List<PetView>> getMyPets(Authentication auth) {
        Long ownerId = getCurrentUserId(auth);
        List<PetView> pets = petService.getPetsForOwner(ownerId);
        return ResponseEntity.ok(pets);
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/{id}")
    public ResponseEntity<PetView> getPet(Authentication auth,
                                          @PathVariable("id") Long id) {
        Long requesterId = getCurrentUserId(auth);
        PetView pet = petService.getPetById(id, requesterId);
        return ResponseEntity.ok(pet);
    }

    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{id}")
    public ResponseEntity<PetView> updatePet(Authentication auth,
                                             @PathVariable("id") Long id,
                                             @Valid @RequestBody CreatePetRequest request) {
        Long requesterId = getCurrentUserId(auth);
        PetView pet = petService.updatePet(id, request, requesterId);
        return ResponseEntity.ok(pet);
    }

    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(Authentication auth,
                                          @PathVariable("id") Long id) {
        Long requesterId = getCurrentUserId(auth);
        petService.deletePet(id, requesterId);
        return ResponseEntity.noContent().build();
    }

    // Medical history για pet (OWNER)
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/{id}/history")
    public ResponseEntity<List<AppointmentView>> getPetHistory(
            Authentication auth,
            @PathVariable("id") Long petId,
            @RequestParam(name = "visitType", required = false) VisitType visitType
    ) {
        Long ownerId = getCurrentUserId(auth);
        List<AppointmentView> history = appointmentService.getPetHistory(petId, ownerId);

        if (visitType != null) {
            history = history.stream()
                    .filter(a -> a.getVisitType() == visitType)
                    .toList();
        }

        return ResponseEntity.ok(history);
    }

    private Long getCurrentUserId(Authentication auth) {
        Object principal = auth != null ? auth.getPrincipal() : null;
        if (principal instanceof ApplicationUserDetails details) {
            return details.getId();
        }
        throw new IllegalStateException("No authenticated user");
    }
}
