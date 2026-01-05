package gr.hua.dit.petcare.service;

import gr.hua.dit.petcare.service.model.CreatePetRequest;
import gr.hua.dit.petcare.service.model.PetView;

import java.util.List;

public interface PetService {
    PetView createPet(CreatePetRequest req, Long ownerId);
    List<PetView> getPetsForOwner(Long ownerId);

    PetView getPetById(Long petId, Long requesterId);
    PetView updatePet(Long petId, CreatePetRequest req, Long requesterId);
    void deletePet(Long petId, Long requesterId);
}
