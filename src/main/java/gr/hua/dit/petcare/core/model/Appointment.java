package gr.hua.dit.petcare.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // αναγνωριστικό κατοικιδίου
    @NotNull(message = "Pet is required")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    // αναγνωριστικό κτηνιάτρου
    @NotNull(message = "Vet is required")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false)
    private User vet;

    @NotNull(message = "startTime is required")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    @Column(nullable = false)
    private LocalDateTime endTime;

    @NotNull(message = "status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    // τύπος επίσκεψης (CHECKUP / VACCINE / TREATMENT)
    @NotNull(message = "visitType is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VisitType visitType = VisitType.CHECKUP;

    // σημειώσεις κτηνιάτρου για κατοικίδιο
    @Column(length = 500)
    private String vetNotes;

    // αυτόματη συμπλήρωση της ημερομηνίας εγγραφής
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Appointment() {
    }

    public Appointment(Long id,
                       Pet pet,
                       User vet,
                       LocalDateTime startTime,
                       LocalDateTime endTime,
                       AppointmentStatus status,
                       VisitType visitType,
                       String vetNotes,
                       LocalDateTime createdAt) {
        this.id = id;
        this.pet = pet;
        this.vet = vet;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.visitType = visitType;
        this.vetNotes = vetNotes;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        normalizeDefaultsAndValidate();
    }

    @PreUpdate
    public void preUpdate() {
        normalizeDefaultsAndValidate();
    }

    private void normalizeDefaultsAndValidate() {
        if (status == null) {
            status = AppointmentStatus.PENDING;
        }
        if (visitType == null) {
            visitType = VisitType.CHECKUP;
        }

        // βασικός έλεγχος ορθότητας χρόνων
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalStateException("endTime must be after startTime");
        }
    }

    // getters / setters
    public Long getId() {
        return id;
    }

    public Pet getPet() {
        return pet;
    }

    public User getVet() {
        return vet;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public VisitType getVisitType() {
        return visitType;
    }

    public String getVetNotes() {
        return vetNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public void setVet(User vet) {
        this.vet = vet;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public void setVisitType(VisitType visitType) {
        this.visitType = visitType;
    }

    public void setVetNotes(String vetNotes) {
        this.vetNotes = vetNotes != null ? vetNotes.trim() : null;
    }

    // καλό να ΜΗΝ το πειράζεις από έξω, αλλά το αφήνω για συμβατότητα
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
