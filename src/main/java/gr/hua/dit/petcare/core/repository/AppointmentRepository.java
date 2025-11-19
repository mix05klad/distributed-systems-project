package gr.hua.dit.petcare.core.repository;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.core.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
        select a from Appointment a 
        where a.vet.id = :vetId 
          and a.startTime < :end 
          and a.endTime > :start
          and a.status != 'CANCELLED'
    """)
    List<Appointment> findOverlappingAppointments(
            @Param("vetId") Long vetId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
        select a from Appointment a 
        where a.pet.owner.id = :ownerId
    """)
    List<Appointment> findAllByOwner(@Param("ownerId") Long ownerId);

    List<Appointment> findAllByVetId(Long vetId);
}
