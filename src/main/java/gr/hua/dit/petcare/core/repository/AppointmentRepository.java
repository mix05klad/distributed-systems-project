package gr.hua.dit.petcare.core.repository;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.core.model.AppointmentStatus;
import gr.hua.dit.petcare.core.model.VisitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

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

    // τελευταίο COMPLETED ραντεβού συγκεκριμένου τύπου (VACCINE) για pet
    Appointment findTopByPetIdAndVisitTypeAndStatusOrderByStartTimeDesc(
            Long petId,
            VisitType visitType,
            AppointmentStatus status
    );
}
