package gr.hua.dit.petcare.core.repository;

import gr.hua.dit.petcare.core.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    // δίνει όλα τα κατοικίδια ενός ιδιοκτήτη
    List<Pet> findByOwnerId(Long ownerId);

    // χρήσιμο για authorization checks (owner-only)
    Optional<Pet> findByIdAndOwnerId(Long petId, Long ownerId);

    boolean existsByIdAndOwnerId(Long petId, Long ownerId);

    long deleteByIdAndOwnerId(Long petId, Long ownerId);
}
