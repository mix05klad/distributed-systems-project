package gr.hua.dit.petcare.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "vet_availability")
public class VetAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "vet is required")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false)
    private User vet;

    @NotNull(message = "startTime is required")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    @Column(nullable = false)
    private LocalDateTime endTime;

    public VetAvailability() {
    }

    public VetAvailability(User vet, LocalDateTime startTime, LocalDateTime endTime) {
        this.vet = vet;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @PrePersist
    @PreUpdate
    public void validateRange() {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalStateException("endTime must be after startTime");
        }
    }

    public Long getId() {
        return id;
    }

    public User getVet() {
        return vet;
    }

    public void setVet(User vet) {
        this.vet = vet;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
