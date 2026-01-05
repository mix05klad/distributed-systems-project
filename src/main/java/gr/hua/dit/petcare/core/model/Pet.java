package gr.hua.dit.petcare.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "pet")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Pet name is required")
    @Size(max = 50, message = "Pet name must be at most 50 characters")
    @Column(nullable = false, length = 50)
    private String name;

    @NotNull(message = "Pet type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PetType type;

    @Size(max = 50, message = "Breed must be at most 50 characters")
    @Column(length = 50)
    private String breed;

    // ηλικία (ακέραιος) ή null
    @Min(value = 0, message = "Age must be >= 0")
    @Max(value = 60, message = "Age must be <= 60")
    @Column
    private Integer age;

    // αναγνωριστικό ιδιοκτήτη
    @NotNull(message = "Owner is required")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    public Pet() {
    }

    public Pet(Long id,
               String name,
               PetType type,
               String breed,
               Integer age,
               User owner) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.age = age;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PetType getType() {
        return type;
    }

    public String getBreed() {
        return breed;
    }

    public Integer getAge() {
        return age;
    }

    public User getOwner() {
        return owner;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public void setType(PetType type) {
        this.type = type;
    }

    public void setBreed(String breed) {
        this.breed = breed != null ? breed.trim() : null;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
