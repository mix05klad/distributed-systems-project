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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PetType getType() {
        return type;
    }

    public void setType(PetType type) {
        this.type = type;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
