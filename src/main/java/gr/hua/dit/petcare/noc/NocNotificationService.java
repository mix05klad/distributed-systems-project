package gr.hua.dit.petcare.noc;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.noc.model.PhoneNumberValidationResult;
import gr.hua.dit.petcare.noc.model.SendSmsRequest;
import gr.hua.dit.petcare.noc.model.SendSmsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class NocNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NocNotificationService.class);

    private final RestTemplate restTemplate;
    private final String nocBaseUrl;

    public NocNotificationService(RestTemplate restTemplate,
                                  @Value("${noc.base-url}") String nocBaseUrl) {
        this.restTemplate = restTemplate;
        this.nocBaseUrl = nocBaseUrl;
    }


    // Επιστρέφει το κανονικοποιημένο τηλέφωνο σε μορφή E.164 (π.χ. +3069...)
    // Aν είναι έγκυρο σύμφωνα με το NOC, αλλιώς empty.

    public Optional<String> normalizePhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            return Optional.empty();
        }

        try {
            String url = nocBaseUrl + "/api/v1/phone-numbers/{phone}/validations";
            PhoneNumberValidationResult result =
                    restTemplate.getForObject(url, PhoneNumberValidationResult.class, rawPhone);

            if (result != null && result.isValid() && result.getE164() != null) {
                return Optional.of(result.getE164());
            }
        } catch (Exception ex) {
            LOGGER.warn("Error calling NOC phone validation for {}: {}", rawPhone, ex.getMessage());
        }

        return Optional.empty();
    }


    // Στέλνει SMS μέσω NOC. Αν αποτύχει, απλά log (δεν ρίχνουμε exception).

    public void sendSms(String e164, String content) {
        try {
            String url = nocBaseUrl + "/api/v1/sms";

            SendSmsRequest request = new SendSmsRequest();
            request.setE164(e164);
            request.setContent(content);

            SendSmsResult response =
                    restTemplate.postForObject(url, request, SendSmsResult.class);

            if (response == null || !response.isSent()) {
                LOGGER.warn("SMS not sent successfully to {}", e164);
            } else {
                LOGGER.info("SMS successfully sent to {}", e164);
            }

        } catch (Exception ex) {
            LOGGER.warn("Error sending SMS to {}: {}", e164, ex.getMessage());
        }
    }


    // όταν ένα ραντεβού γίνει CONFIRMED, ειδοποιούμε τον ιδιοκτήτη με SMS.

    public void notifyOwnerAppointmentConfirmed(Appointment appointment) {
        if (appointment == null || appointment.getPet() == null) {
            return;
        }
        User owner = appointment.getPet().getOwner();
        if (owner == null) {
            return;
        }

        String phone = owner.getPhoneNumber();
        if (phone == null || phone.isBlank()) {
            LOGGER.info("Owner {} has no phone number, skipping SMS notification", owner.getUsername());
            return;
        }

        Optional<String> e164Opt = normalizePhone(phone);
        if (e164Opt.isEmpty()) {
            LOGGER.info("Owner {} phone {} is invalid, skipping SMS", owner.getUsername(), phone);
            return;
        }

        String e164 = e164Opt.get();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String when = appointment.getStartTime() != null
                ? appointment.getStartTime().format(fmt)
                : "unknown time";

        String message = String.format(
                "Your appointment for %s with vet %s on %s was CONFIRMED.",
                appointment.getPet().getName(),
                appointment.getVet().getFullName(),
                when
        );

        sendSms(e164, message);
    }

    // όταν ένα ραντεβού ακυρώνεται από τον κτηνίατρο ειδοποιούμε τον ιδιοκτήτη με SMS.
    public void notifyOwnerAppointmentCancelledByVet(Appointment appointment) {
        if (appointment == null || appointment.getPet() == null) {
            return;
        }
        User owner = appointment.getPet().getOwner();
        if (owner == null) {
            return;
        }

        String phone = owner.getPhoneNumber();
        if (phone == null || phone.isBlank()) {
            LOGGER.info("Owner {} has no phone number, skipping SMS notification (cancelled)",
                    owner.getUsername());
            return;
        }

        Optional<String> e164Opt = normalizePhone(phone);
        if (e164Opt.isEmpty()) {
            LOGGER.info("Owner {} phone {} is invalid, skipping SMS (cancelled)",
                    owner.getUsername(), phone);
            return;
        }

        String e164 = e164Opt.get();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String when = appointment.getStartTime() != null
                ? appointment.getStartTime().format(fmt)
                : "unknown time";

        String message = String.format(
                "Your appointment for %s with vet %s on %s was CANCELLED by the vet.",
                appointment.getPet().getName(),
                appointment.getVet().getFullName(),
                when
        );

        sendSms(e164, message);
    }

    public void notifyOwnerAppointmentCompleted(Appointment appointment) {
        if (appointment == null || appointment.getPet() == null) {
            return;
        }
        User owner = appointment.getPet().getOwner();
        if (owner == null) {
            return;
        }

        String phone = owner.getPhoneNumber();
        if (phone == null || phone.isBlank()) {
            LOGGER.info("Owner {} has no phone number, skipping SMS notification", owner.getUsername());
            return;
        }

        Optional<String> e164Opt = normalizePhone(phone);
        if (e164Opt.isEmpty()) {
            LOGGER.info("Owner {} phone {} is invalid, skipping SMS", owner.getUsername(), phone);
            return;
        }

        String e164 = e164Opt.get();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String when = appointment.getStartTime() != null
                ? appointment.getStartTime().format(fmt)
                : "unknown time";

        String message = String.format(
                "Your appointment for %s with vet %s on %s was COMPLETED.",
                appointment.getPet().getName(),
                appointment.getVet().getFullName(),
                when
        );

        sendSms(e164, message);
    }

    public void notifyVetNewAppointmentRequested(Appointment appointment) {
        if (appointment == null || appointment.getVet() == null) {
            return;
        }

        User vet = appointment.getVet();

        String phone = vet.getPhoneNumber();
        if (phone == null || phone.isBlank()) {
            LOGGER.info("Vet {} has no phone number, skipping SMS notification", vet.getUsername());
            return;
        }

        Optional<String> e164Opt = normalizePhone(phone);
        if (e164Opt.isEmpty()) {
            LOGGER.info("Vet {} phone {} is invalid, skipping SMS", vet.getUsername(), phone);
            return;
        }

        String e164 = e164Opt.get();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String when = appointment.getStartTime() != null
                ? appointment.getStartTime().format(fmt)
                : "unknown time";

        String petName = (appointment.getPet() != null)
                ? appointment.getPet().getName()
                : "a pet";

        String ownerName = (appointment.getPet() != null && appointment.getPet().getOwner() != null)
                ? appointment.getPet().getOwner().getFullName()
                : "an owner";

        String message = String.format(
                "New appointment request for %s from %s on %s. Please log in to PetCare to confirm or cancel.",
                petName,
                ownerName,
                when
        );

        sendSms(e164, message);
    }

    public void notifyOwnerVisitNotesUpdated(Appointment appointment) {
        if (appointment == null || appointment.getPet() == null) {
            return;
        }

        User owner = appointment.getPet().getOwner();
        if (owner == null) {
            return;
        }

        String phone = owner.getPhoneNumber();
        if (phone == null || phone.isBlank()) {
            LOGGER.info("Owner {} has no phone number, skipping SMS notification", owner.getUsername());
            return;
        }

        Optional<String> e164Opt = normalizePhone(phone);
        if (e164Opt.isEmpty()) {
            LOGGER.info("Owner {} phone {} is invalid, skipping SMS", owner.getUsername(), phone);
            return;
        }

        String e164 = e164Opt.get();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String when = appointment.getStartTime() != null
                ? appointment.getStartTime().format(fmt)
                : "unknown time";

        String petName = appointment.getPet().getName();

        String message = String.format(
                "The vet updated the medical record of %s for the visit on %s. " +
                        "You can view the details in Pet History in the PetCare app.",
                petName,
                when
        );

        sendSms(e164, message);
    }

}
