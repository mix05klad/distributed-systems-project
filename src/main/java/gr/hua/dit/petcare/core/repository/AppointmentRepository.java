package gr.hua.dit.petcare.core.repository;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.core.model.AppointmentStatus;
import gr.hua.dit.petcare.core.model.VisitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // έλεγχος των PENDING/CONFIRMED ραντεβού ώστε να μην υπάρχει conflict
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

    // δίνει όλα τα ραντεβού για έναν ιδιοκτήτη
    @Query("""
        select a
        from Appointment a
        where a.pet.owner.id = :ownerId
        order by a.startTime desc
    """)
    List<Appointment> findAllByOwner(@Param("ownerId") Long ownerId);

    // δίνει όλα τα ραντεβού για έναν κτηνίατρο
    @Query("""
        select a
        from Appointment a
        where a.vet.id = :vetId
        order by a.startTime desc
    """)
    List<Appointment> findAllByVetId(@Param("vetId") Long vetId);

    // χρήσιμο για vet-only actions (confirm/complete/notes)
    Optional<Appointment> findByIdAndVetId(Long id, Long vetId);

    // χρήσιμο για owner-only actions ( view details)
    @Query("""
        select a
        from Appointment a
        where a.id = :appointmentId
          and a.pet.owner.id = :ownerId
    """)
    Optional<Appointment> findByIdAndOwnerId(@Param("appointmentId") Long appointmentId,
                                             @Param("ownerId") Long ownerId);

    @Query("""
        select a
        from Appointment a
        where a.vet.id = :vetId
          and a.status = gr.hua.dit.petcare.core.model.AppointmentStatus.PENDING
        order by a.startTime asc
    """)
    List<Appointment> findPendingForVet(@Param("vetId") Long vetId);

    // τελευταίο COMPLETED ραντεβού συγκεκριμένου τύπου (VACCINE) για pet
    Appointment findTopByPetIdAndVisitTypeAndStatusOrderByStartTimeDesc(
            Long petId,
            VisitType visitType,
            AppointmentStatus status
    );

    // completed επισκέψεις (ιστορικό) για συγκεκριμένο pet ενός owner
    @Query("""
        select a
        from Appointment a
        where a.pet.id = :petId
          and a.pet.owner.id = :ownerId
          and a.status = gr.hua.dit.petcare.core.model.AppointmentStatus.COMPLETED
        order by a.startTime desc
    """)
    List<Appointment> findCompletedByPetAndOwner(
            @Param("ownerId") Long ownerId,
            @Param("petId") Long petId
    );
}
