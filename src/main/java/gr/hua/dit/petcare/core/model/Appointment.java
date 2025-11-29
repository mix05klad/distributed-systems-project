package gr.hua.dit.petcare.core.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // αναγνωριστικό κατοικιδίου
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    // αναγνωριστικό κτηνιάτρου
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false)
    private User vet;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    // σημειώσεις κτηνιάτρου για κατοικίδιο
    @Column(length = 500)
    private String vetNotes;

    // αυτόματη συμπλήρωση της ημερομηνίας εγγραφής
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Appointment() {
    }

    public Appointment(Long id,
                       Pet pet,
                       User vet,
                       LocalDateTime startTime,
                       LocalDateTime endTime,
                       AppointmentStatus status,
                       String vetNotes,
                       LocalDateTime createdAt) {
        this.id = id;
        this.pet = pet;
        this.vet = vet;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.vetNotes = vetNotes;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
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

    public void setVetNotes(String vetNotes) {
        this.vetNotes = vetNotes;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
