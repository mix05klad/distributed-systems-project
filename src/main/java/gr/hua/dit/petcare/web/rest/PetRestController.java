package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.service.PetService;
import gr.hua.dit.petcare.service.model.CreatePetRequest;
import gr.hua.dit.petcare.service.model.PetView;
import jakarta.validation.Valid;
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
    public PetView create(@Valid @RequestBody CreatePetRequest req,
                          @RequestParam Long ownerId) {
        return petService.createPet(req, ownerId);
    }

    @GetMapping("/owner/{ownerId}")
    public List<PetView> getPets(@PathVariable Long ownerId) {
        return petService.getPetsForOwner(ownerId);
    }
}
