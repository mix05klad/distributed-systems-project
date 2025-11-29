package gr.hua.dit.petcare.service.mapper;

import gr.hua.dit.petcare.core.model.Pet;
import gr.hua.dit.petcare.service.model.PetView;
import gr.hua.dit.petcare.service.model.CreatePetRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PetMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "owner.fullName", target = "ownerName")
    PetView toView(Pet pet);

    // δημιουργία Pet entity
    Pet toEntity(CreatePetRequest req);
}
