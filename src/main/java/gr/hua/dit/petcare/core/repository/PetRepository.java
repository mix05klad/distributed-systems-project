package gr.hua.dit.petcare.core.repository;

import gr.hua.dit.petcare.core.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {

    /**
     * Βρίσκει όλα τα κατοικίδια για συγκεκριμένο ιδιοκτήτη.
     * Χρησιμοποιείται στο PetService για να περιορίσουμε τα pets
     * που βλέπει κάθε χρήστης στα "δικά του".
     */
    List<Pet> findByOwnerId(Long ownerId);
}
