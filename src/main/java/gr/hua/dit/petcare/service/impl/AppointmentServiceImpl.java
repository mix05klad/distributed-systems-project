package gr.hua.dit.petcare.service.impl;

import gr.hua.dit.petcare.core.model.*;
import gr.hua.dit.petcare.core.repository.*;
import gr.hua.dit.petcare.service.AppointmentService;
import gr.hua.dit.petcare.service.mapper.AppointmentMapper;
import gr.hua.dit.petcare.service.model.CreateAppointmentRequest;
import gr.hua.dit.petcare.service.model.AppointmentView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository ar;
    private final PetRepository pr;
    private final UserRepository ur;
    private final AppointmentMapper mapper;

    public AppointmentServiceImpl(AppointmentRepository ar, PetRepository pr,
                                  UserRepository ur, AppointmentMapper mapper) {
        this.ar = ar;
        this.pr = pr;
        this.ur = ur;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AppointmentView createAppointment(CreateAppointmentRequest req, Long ownerId) {

        Pet pet = pr.findById(req.getPetId())
                .orElseThrow(() -> new IllegalArgumentException("Pet not found"));

        if (!pet.getOwner().getId().equals(ownerId))
            throw new SecurityException("You are not the owner of this pet");

        User vet = ur.findById(req.getVetId())
                .orElseThrow(() -> new IllegalArgumentException("Vet not found"));

        // Overlap check
        List<Appointment> overlaps = ar.findOverlappingAppointments(
                vet.getId(), req.getStartTime(), req.getEndTime());

        if (!overlaps.isEmpty())
            throw new IllegalStateException("Overlapping appointment for vet");

        // Create appointment
        Appointment a = new Appointment();
        a.setPet(pet);
        a.setVet(vet);
        a.setStartTime(req.getStartTime());
        a.setEndTime(req.getEndTime());
        a.setStatus(AppointmentStatus.PENDING);
        a.setOwnerNotes(req.getOwnerNotes());
        a.setCreatedAt(LocalDateTime.now());

        ar.save(a);

        return mapper.toView(a);
    }

    @Override
    @Transactional
    public AppointmentView confirmAppointment(Long appointmentId, Long vetId) {
        Appointment a = ar.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));

        if (!a.getVet().getId().equals(vetId))
            throw new SecurityException("Not authorized");

        a.setStatus(AppointmentStatus.CONFIRMED);
        return mapper.toView(a);
    }

    @Override
    @Transactional
    public AppointmentView cancelAppointment(Long appointmentId, Long userId) {
        Appointment a = ar.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));

        boolean isOwner = a.getPet().getOwner().getId().equals(userId);
        boolean isVet = a.getVet().getId().equals(userId);

        if (!isOwner && !isVet)
            throw new SecurityException("Not authorized to cancel");

        a.setStatus(AppointmentStatus.CANCELLED);
        return mapper.toView(a);
    }

    @Override
    @Transactional
    public AppointmentView completeAppointment(Long appointmentId, Long vetId) {
        Appointment a = ar.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));

        if (!a.getVet().getId().equals(vetId))
            throw new SecurityException("Only vet can complete");

        a.setStatus(AppointmentStatus.COMPLETED);
        return mapper.toView(a);
    }

    @Override
    public List<AppointmentView> getAppointmentsForOwner(Long ownerId) {
        return ar.findAllByOwner(ownerId)
                .stream()
                .map(mapper::toView)
                .toList();
    }

    @Override
    public List<AppointmentView> getAppointmentsForVet(Long vetId) {
        return ar.findAllByVetId(vetId)
                .stream()
                .map(mapper::toView)
                .toList();
    }
}
