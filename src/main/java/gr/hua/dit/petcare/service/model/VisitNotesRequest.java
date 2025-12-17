package gr.hua.dit.petcare.service.model;

import jakarta.validation.constraints.NotBlank;

public class VisitNotesRequest {

    @NotBlank
    private String notes;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
