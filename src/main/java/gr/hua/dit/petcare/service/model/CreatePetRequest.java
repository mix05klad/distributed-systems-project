package gr.hua.dit.petcare.service.model;

import gr.hua.dit.petcare.core.model.PetType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreatePetRequest {

    @NotBlank(message = "name is required")
    @Size(max = 50, message = "name must be at most 50 characters")
    private String name;

    @NotNull(message = "type is required")
    private PetType type;

    @Size(max = 50, message = "breed must be at most 50 characters")
    private String breed;

    @Min(value = 0, message = "age must be >= 0")
    @Max(value = 60, message = "age must be <= 60")
    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
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
        this.breed = breed != null ? breed.trim() : null;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
