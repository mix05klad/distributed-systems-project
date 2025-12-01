package gr.hua.dit.petcare.service.impl;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.core.model.AppointmentStatus;
import gr.hua.dit.petcare.core.model.Pet;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.repository.AppointmentRepository;
import gr.hua.dit.petcare.core.repository.PetRepository;
import gr.hua.dit.petcare.core.repository.UserRepository;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.mapper.AppointmentMapper;
import gr.hua.dit.petcare.service.model.AppointmentView;
import gr.hua.dit.petcare.service.model.CreateAppointmentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository ar;
    private final PetRepository pr;
    private final UserRepository ur;
    private final AppointmentMapper mapper;

    public AppointmentServiceImpl(AppointmentRepository ar,
                                  PetRepository pr,
                                  UserRepository ur,
                                  AppointmentMapper mapper) {
        this.ar = ar;
        this.pr = pr;
        this.ur = ur;
        this.mapper = mapper;
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

        // Έλεγχος για επικαλυπτόμενα ραντεβού του vet
        var overlaps = ar.findOverlappingAppointments(vet.getId(), start, end);
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Vet already has an appointment in this time range");
        }

        Appointment a = new Appointment();
        a.setPet(pet);
        a.setVet(vet);
        a.setStartTime(start);
        a.setEndTime(end);
        a.setStatus(AppointmentStatus.PENDING);
        a.setVetNotes(null);

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
}
