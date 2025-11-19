package gr.hua.dit.petcare.service.model;

import gr.hua.dit.petcare.core.model.PetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreatePetRequest {

    @NotNull
    @Size(min = 1, max = 50)
    private String name;

    @NotNull
    private PetType type;

    @Size(max = 50)
    private String breed;

    private Integer age;

    // getters and setters
}
