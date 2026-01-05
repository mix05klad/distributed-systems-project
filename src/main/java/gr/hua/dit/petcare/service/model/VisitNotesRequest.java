package gr.hua.dit.petcare.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VisitNotesRequest {

    @NotBlank(message = "notes is required")
    @Size(max = 500, message = "notes must be at most 500 characters")
    private String notes;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
