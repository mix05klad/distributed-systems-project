package gr.hua.dit.petcare.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

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

    @Min(value = 0, message = "Age must be >= 0")
    @Max(value = 60, message = "Age must be <= 60")
    @Column
    private Integer age;

    @NotNull(message = "Owner is required")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Soft delete
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deleted = false;

    @Column
    private LocalDateTime deletedAt;

    public Pet() {}

    public Pet(Long id,
               String name,
               PetType type,
               String breed,
               Integer age,
               User owner) {
        this.id = id;
        this.name = name != null ? name.trim() : null;
        this.type = type;
        this.breed = breed != null ? breed.trim() : null;
        this.age = age;
        this.owner = owner;
        this.deleted = false;
        this.deletedAt = null;
    }

    public void softDelete() {
        softDelete(LocalDateTime.now());
    }

    public void softDelete(LocalDateTime at) {
        this.deleted = true;
        this.deletedAt = at != null ? at : LocalDateTime.now();
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public PetType getType() { return type; }
    public String getBreed() { return breed; }
    public Integer getAge() { return age; }
    public User getOwner() { return owner; }
    public boolean isDeleted() { return deleted; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

    public void setId(Long id) { this.id = id; }

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


    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
        if (!deleted) {
            this.deletedAt = null;
        } else if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
        }
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
