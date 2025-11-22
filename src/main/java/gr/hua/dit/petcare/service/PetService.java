package gr.hua.dit.petcare.service;

import gr.hua.dit.petcare.service.model.PetView;
import gr.hua.dit.petcare.service.model.CreatePetRequest;

import java.util.List;

public interface PetService {
    PetView createPet(CreatePetRequest req, Long ownerId);
    List<PetView> getPetsForOwner(Long ownerId);
}
