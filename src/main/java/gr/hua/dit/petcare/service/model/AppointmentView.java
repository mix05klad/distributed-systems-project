package gr.hua.dit.petcare.service.model;

import gr.hua.dit.petcare.core.model.AppointmentStatus;
import gr.hua.dit.petcare.core.model.VisitType;

import java.time.LocalDateTime;
import java.util.List;

public class AppointmentView {

    private Long id;

    private Long petId;
    private String petName;

    private Long vetId;
    private String vetName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private AppointmentStatus status;

    private VisitType visitType;

    private String vetNotes;

    private List<String> warnings;

    private boolean petDeleted;

    public boolean isPetDeleted() {
        return petDeleted;
    }

    public void setPetDeleted(boolean petDeleted) {
        this.petDeleted = petDeleted;
    }

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

    public VisitType getVisitType() {
        return visitType;
    }

    public void setVisitType(VisitType visitType) {
        this.visitType = visitType;
    }

    public String getVetNotes() {
        return vetNotes;
    }

    public void setVetNotes(String vetNotes) {
        this.vetNotes = vetNotes;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
