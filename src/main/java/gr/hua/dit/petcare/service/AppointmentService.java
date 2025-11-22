package gr.hua.dit.petcare.service;

import gr.hua.dit.petcare.service.model.AppointmentView;
import gr.hua.dit.petcare.service.model.CreateAppointmentRequest;

import java.util.List;

public interface AppointmentService {

    AppointmentView createAppointment(CreateAppointmentRequest req, Long ownerId);
    AppointmentView confirmAppointment(Long appointmentId, Long vetId);
    AppointmentView cancelAppointment(Long appointmentId, Long userId);
    AppointmentView completeAppointment(Long appointmentId, Long vetId);

    List<AppointmentView> getAppointmentsForOwner(Long ownerId);
    List<AppointmentView> getAppointmentsForVet(Long vetId);
}
