package gr.hua.dit.petcare.service.mapper;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.service.model.AppointmentView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(source = "pet.id", target = "petId")
    @Mapping(source = "pet.name", target = "petName")
    @Mapping(source = "vet.id", target = "vetId")
    @Mapping(source = "vet.fullName", target = "vetName")
    @Mapping(target = "warnings", expression = "java(java.util.List.of())")
    AppointmentView toView(Appointment a);
}
