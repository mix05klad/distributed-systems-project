package gr.hua.dit.petcare.core.repository;

import gr.hua.dit.petcare.core.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    // όλα τα ενεργά pets ενός owner
    List<Pet> findByOwnerIdAndDeletedFalse(Long ownerId);

    // owner-only lookup (ενεργό)
    Optional<Pet> findByIdAndOwnerIdAndDeletedFalse(Long petId, Long ownerId);

    // γενικό lookup (ενεργό)
    Optional<Pet> findByIdAndDeletedFalse(Long petId);

    // exists (ενεργό + owner)
    boolean existsByIdAndOwnerIdAndDeletedFalse(Long petId, Long ownerId);

    // exists (ενεργό)
    boolean existsByIdAndDeletedFalse(Long petId);

    /**
     * Soft delete ενός pet που ανήκει στον συγκεκριμένο owner.
     * Επιστρέφει πόσες γραμμές ενημερώθηκαν (0 ή 1).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Pet p
              set p.deleted = true,
                  p.deletedAt = :deletedAt
            where p.id = :petId
              and p.owner.id = :ownerId
              and p.deleted = false
           """)
    int softDeleteByIdAndOwnerId(@Param("petId") Long petId,
                                 @Param("ownerId") Long ownerId,
                                 @Param("deletedAt") LocalDateTime deletedAt);

    // (προαιρετικό) soft delete χωρίς owner check (για admin use-cases)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Pet p
              set p.deleted = true,
                  p.deletedAt = :deletedAt
            where p.id = :petId
              and p.deleted = false
           """)
    int softDeleteById(@Param("petId") Long petId,
                       @Param("deletedAt") LocalDateTime deletedAt);
}
