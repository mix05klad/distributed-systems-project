package gr.hua.dit.petcare.service.model;

import gr.hua.dit.petcare.core.model.AppointmentStatus;

import java.time.LocalDateTime;

public class AppointmentView {

    private Long id;

    private Long petId;
    private String petName;

    private Long vetId;
    private String vetName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private AppointmentStatus status;

    private String vetNotes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public Long getVetId() {
        return vetId;
    }

    public void setVetId(Long vetId) {
        this.vetId = vetId;
    }

    public String getVetName() {
        return vetName;
    }

    public void setVetName(String vetName) {
        this.vetName = vetName;
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

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getVetNotes() {
        return vetNotes;
    }

    public void setVetNotes(String vetNotes) {
        this.vetNotes = vetNotes;
    }
}
