package gr.hua.dit.petcare.core.repository;

import gr.hua.dit.petcare.core.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Βρίσκει ραντεβού που επικαλύπτονται χρονικά για έναν συγκεκριμένο vet.
     *
     * Συνθήκη επικάλυψης:
     *  - a.startTime < :end
     *  - a.endTime   > :start
     *
     * Επιπλέον, φιλτράρουμε μόνο σε PENDING/CONFIRMED, ώστε
     * CANCELLED/COMPLETED να μην μπλοκάρουν νέα ραντεβού.
     */
    @Query("""
        select a 
        from Appointment a
        where a.vet.id = :vetId
          and a.status in (
                gr.hua.dit.petcare.core.model.AppointmentStatus.PENDING,
                gr.hua.dit.petcare.core.model.AppointmentStatus.CONFIRMED
          )
          and a.startTime < :end
          and a.endTime > :start
    """)
    List<Appointment> findOverlappingAppointments(
            @Param("vetId") Long vetId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Όλα τα ραντεβού για έναν ιδιοκτήτη, μέσω της σχέσης:
     * Appointment -> Pet -> Owner.
     */
    @Query("""
        select a
        from Appointment a
        where a.pet.owner.id = :ownerId
        order by a.startTime desc
    """)
    List<Appointment> findAllByOwner(@Param("ownerId") Long ownerId);

    /**
     * Όλα τα ραντεβού για έναν συγκεκριμένο vet.
     * Χρησιμοποιεί το path vet.id.
     */
    @Query("""
        select a
        from Appointment a
        where a.vet.id = :vetId
        order by a.startTime desc
    """)
    List<Appointment> findAllByVetId(@Param("vetId") Long vetId);
}
