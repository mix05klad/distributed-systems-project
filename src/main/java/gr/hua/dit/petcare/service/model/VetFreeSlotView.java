package gr.hua.dit.petcare.service.model;

import java.time.LocalDateTime;

public class VetFreeSlotView {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public VetFreeSlotView() {
    }

    public VetFreeSlotView(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
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
