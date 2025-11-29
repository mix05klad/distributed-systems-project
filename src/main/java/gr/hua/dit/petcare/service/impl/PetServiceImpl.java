package gr.hua.dit.petcare.service.impl;

import gr.hua.dit.petcare.core.model.Pet;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.repository.PetRepository;
import gr.hua.dit.petcare.core.repository.UserRepository;
import gr.hua.dit.petcare.service.PetService;
import gr.hua.dit.petcare.service.mapper.PetMapper;
import gr.hua.dit.petcare.service.model.CreatePetRequest;
import gr.hua.dit.petcare.service.model.PetView;
import gr.hua.dit.petcare.service.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final PetMapper petMapper;

    public PetServiceImpl(PetRepository petRepository,
                          UserRepository userRepository,
                          PetMapper petMapper) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        this.petMapper = petMapper;
    }

    @Override
    @Transactional
    public PetView createPet(CreatePetRequest req, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner not found: " + ownerId));
        Pet pet = petMapper.toEntity(req);
        pet.setOwner(owner);
        pet = petRepository.save(pet);
        return petMapper.toView(pet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetView> getPetsForOwner(Long ownerId) {
        return petRepository.findByOwnerId(ownerId).stream().map(petMapper::toView).toList();
    }


    @Override
    @Transactional(readOnly = true)
    public PetView getPetById(Long petId, Long requesterId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException("Pet not found: " + petId));
        if (!pet.getOwner().getId().equals(requesterId)) {
            throw new AccessDeniedException("You are not the owner of this pet");
        }
        return petMapper.toView(pet);
    }

    @Override
    @Transactional
    public PetView updatePet(Long petId, CreatePetRequest req, Long requesterId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException("Pet not found: " + petId));
        if (!pet.getOwner().getId().equals(requesterId)) {
            throw new AccessDeniedException("You are not the owner of this pet");
        }

        pet.setName(req.getName());
        pet.setType(req.getType());
        pet.setBreed(req.getBreed());
        pet.setAge(req.getAge());

        pet = petRepository.save(pet);
        return petMapper.toView(pet);
    }

    @Override
    @Transactional
    public void deletePet(Long petId, Long requesterId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new NotFoundException("Pet not found: " + petId));
        if (!pet.getOwner().getId().equals(requesterId)) {
            throw new AccessDeniedException("You are not the owner of this pet");
        }
        petRepository.delete(pet);
    }
}
