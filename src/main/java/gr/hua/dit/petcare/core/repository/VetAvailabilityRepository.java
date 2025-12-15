package gr.hua.dit.petcare.core.repository;

import gr.hua.dit.petcare.core.model.VetAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VetAvailabilityRepository extends JpaRepository<VetAvailability, Long> {

    List<VetAvailability> findByVetIdOrderByStartTimeAsc(Long vetId);

    @Query("""
            select va from VetAvailability va
            where va.vet.id = :vetId
              and va.startTime <= :start
              and va.endTime >= :end
           """)
    List<VetAvailability> findSlotsCovering(
            @Param("vetId") Long vetId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
            select va from VetAvailability va
            where va.vet.id = :vetId
              and va.startTime < :end
              and va.endTime > :start
           """)
    List<VetAvailability> findOverlappingSlots(
            @Param("vetId") Long vetId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
