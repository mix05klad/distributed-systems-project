package gr.hua.dit.petcare.service.impl;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.core.model.AppointmentStatus;
import gr.hua.dit.petcare.core.model.Pet;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.model.VisitType;
import gr.hua.dit.petcare.core.repository.AppointmentRepository;
import gr.hua.dit.petcare.core.repository.PetRepository;
import gr.hua.dit.petcare.core.repository.UserRepository;
import gr.hua.dit.petcare.core.repository.VetAvailabilityRepository;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.mapper.AppointmentMapper;
import gr.hua.dit.petcare.service.model.AppointmentView;
import gr.hua.dit.petcare.service.model.CreateAppointmentRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private static final long MIN_DAYS_BETWEEN_VACCINES = 20;

    private final AppointmentRepository ar;
    private final PetRepository pr;
    private final UserRepository ur;
    private final AppointmentMapper mapper;
    private final VetAvailabilityRepository vetAvailabilityRepository;

    public AppointmentServiceImpl(AppointmentRepository ar,
                                  PetRepository pr,
                                  UserRepository ur,
                                  AppointmentMapper mapper,
                                  VetAvailabilityRepository vetAvailabilityRepository) {
        this.ar = ar;
        this.pr = pr;
        this.ur = ur;
        this.mapper = mapper;
        this.vetAvailabilityRepository = vetAvailabilityRepository;
    }

    @Override
    public AppointmentView createAppointment(CreateAppointmentRequest req, Long ownerId) {
        Pet pet = pr.findById(req.getPetId())
                .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + req.getPetId()));

        if (!pet.getOwner().getId().equals(ownerId)) {
            throw new SecurityException("You are not the owner of this pet");
        }

        User vet = ur.findById(req.getVetId())
                .orElseThrow(() -> new IllegalArgumentException("Vet not found: " + req.getVetId()));

        if (vet.getRoles().stream().noneMatch(r -> r.equalsIgnoreCase("VET"))) {
            throw new IllegalArgumentException("Selected user is not a vet");
        }

        LocalDateTime start = req.getStartTime();
        LocalDateTime end = req.getEndTime();

        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("Invalid appointment time range");
        }

        // Έλεγχος ότι το ραντεβού είναι μέσα σε δηλωμένη διαθεσιμότητα του vet
        boolean covered = !vetAvailabilityRepository
                .findSlotsCovering(vet.getId(), start, end)
                .isEmpty();

        if (!covered) {
            throw new IllegalStateException("Selected time is outside vet availability");
        }

        // Έλεγχος για επικαλυπτόμενα ραντεβού του vet
        var overlaps = ar.findOverlappingAppointments(vet.getId(), start, end);
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Vet already has an appointment in this time range");
        }

        // Ειδικός έλεγχος μόνο για ραντεβού τύπου VACCINE
        if (req.getVisitType() == VisitType.VACCINE) {
            Appointment lastVaccine = ar.findTopByPetIdAndVisitTypeAndStatusOrderByStartTimeDesc(
                    pet.getId(),
                    VisitType.VACCINE,
                    AppointmentStatus.COMPLETED
            );

            if (lastVaccine != null) {
                long daysBetween = ChronoUnit.DAYS.between(
                        lastVaccine.getStartTime().toLocalDate(),
                        start.toLocalDate()
                );
                if (daysBetween < MIN_DAYS_BETWEEN_VACCINES) {
                    throw new IllegalStateException(
                            "This pet had a vaccine " + daysBetween + " days ago. Minimum allowed interval is " + MIN_DAYS_BETWEEN_VACCINES + " days."
                    );
                }
            }
        }

        Appointment a = new Appointment();
        a.setPet(pet);
        a.setVet(vet);
        a.setStartTime(start);
        a.setEndTime(end);
        a.setStatus(AppointmentStatus.PENDING);
        a.setVetNotes(null);
        a.setVisitType(req.getVisitType());

        a = ar.save(a);

        return mapper.toView(a);
    }

    @Override
    public AppointmentView confirmAppointment(Long appointmentId, Long vetId) {
        Appointment a = ar.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));

        if (!a.getVet().getId().equals(vetId)) {
            throw new SecurityException("You are not the vet for this appointment");
        }

        if (a.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING appointments can be confirmed");
        }

        a.setStatus(AppointmentStatus.CONFIRMED);
        a = ar.save(a);

        return mapper.toView(a);
    }

    @Override
    public AppointmentView cancelAppointment(Long appointmentId, Long userId) {
        Appointment a = ar.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));

        Long ownerId = a.getPet().getOwner().getId();
        Long vetId = a.getVet().getId();

        if (!ownerId.equals(userId) && !vetId.equals(userId)) {
            throw new SecurityException("You are not allowed to cancel this appointment");
        }

        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            return mapper.toView(a); // ήδη cancelled
        }

        if (a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Completed appointments cannot be cancelled");
        }

        a.setStatus(AppointmentStatus.CANCELLED);
        a = ar.save(a);

        return mapper.toView(a);
    }

    @Override
    public AppointmentView completeAppointment(Long appointmentId, Long vetId) {
        Appointment a = ar.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));

        if (!a.getVet().getId().equals(vetId)) {
            throw new SecurityException("You are not the vet for this appointment");
        }

        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled appointments cannot be completed");
        }

        a.setStatus(AppointmentStatus.COMPLETED);
        a = ar.save(a);

        return mapper.toView(a);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentView> getAppointmentsForOwner(Long ownerId) {
        return ar.findAllByOwner(ownerId)
                .stream()
                .map(mapper::toView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentView> getAppointmentsForVet(Long vetId) {
        return ar.findAllByVetId(vetId)
                .stream()
                .map(mapper::toView)
                .toList();
    }

    @Override
    public AppointmentView updateVisitNotes(Long appointmentId, Long vetId, String notes) {
        Appointment a = ar.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));

        if (!a.getVet().getId().equals(vetId)) {
            throw new SecurityException("You are not the vet for this appointment");
        }

        if (a.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Only COMPLETED appointments can have visit notes");
        }

        a.setVetNotes(notes);
        a = ar.save(a);

        return mapper.toView(a);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentView> getPetHistory(Long petId, Long ownerId) {
        // έλεγχος ιδιοκτησίας pet
        Pet pet = pr.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));

        if (!pet.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("You are not the owner of this pet");
        }

        return ar.findCompletedByPetAndOwner(ownerId, petId)
                .stream()
                .map(mapper::toView)
                .toList();
    }
}
