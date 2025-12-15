package gr.hua.dit.petcare.service;

import gr.hua.dit.petcare.core.model.VetAvailability;
import gr.hua.dit.petcare.service.model.VetAvailabilityRequest;

import java.util.List;

public interface VetAvailabilityService {

    VetAvailability addAvailability(Long vetId, VetAvailabilityRequest req);

    List<VetAvailability> getAvailabilityForVet(Long vetId);

    void deleteSlot(Long vetId, Long slotId);
}
