package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.core.model.VisitType;
import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.PetService;
import gr.hua.dit.petcare.service.model.AppointmentView;
import gr.hua.dit.petcare.service.model.CreatePetRequest;
import gr.hua.dit.petcare.service.model.PetView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    // Create pet (OWNER)
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    public ResponseEntity<PetView> createPet(Authentication auth,
                                             @Valid @RequestBody CreatePetRequest request) {
        Long ownerId = getCurrentUserId(auth);
        return ResponseEntity.ok(petService.createPet(request, ownerId));
    }

    // My pets (OWNER) -> only active (deleted=false)
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping
    public ResponseEntity<List<PetView>> getMyPets(Authentication auth) {
        Long ownerId = getCurrentUserId(auth);
        return ResponseEntity.ok(petService.getPetsForOwner(ownerId));
    }

    // Get single pet (OWNER) -> must be active + owned
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/{petId}")
    public ResponseEntity<PetView> getPet(Authentication auth,
                                          @PathVariable Long petId) {
        Long ownerId = getCurrentUserId(auth);
        return ResponseEntity.ok(petService.getPetById(petId, ownerId));
    }

    // Update pet (OWNER) -> must be active + owned
    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{petId}")
    public ResponseEntity<PetView> updatePet(Authentication auth,
                                             @PathVariable Long petId,
                                             @Valid @RequestBody CreatePetRequest request) {
        Long ownerId = getCurrentUserId(auth);
        return ResponseEntity.ok(petService.updatePet(petId, request, ownerId));
    }

    @Operation(summary = "Soft delete a pet (OWNER)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pet deleted (soft delete)"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    // Delete pet (OWNER) -> soft delete
    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(Authentication auth,
                                          @PathVariable Long petId) {
        Long ownerId = getCurrentUserId(auth);
        petService.deletePet(petId, ownerId);
        return ResponseEntity.noContent().build();
    }

    // Medical history for pet (OWNER)
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/{petId}/history")
    public ResponseEntity<List<AppointmentView>> getPetHistory(
            Authentication auth,
            @PathVariable Long petId,
            @RequestParam(name = "visitType", required = false) VisitType visitType
    ) {
        Long ownerId = getCurrentUserId(auth);


        petService.getPetById(petId, ownerId);

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
