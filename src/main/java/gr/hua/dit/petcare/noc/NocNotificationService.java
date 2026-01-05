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

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NocNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NocNotificationService.class);

    private final RestTemplate restTemplate;

    private final String nocBaseUrl;
    private final String smsPath;
    private final String phoneValidationPath;
    private final long phoneCacheTtlSeconds;

    private final ConcurrentHashMap<String, CachedPhone> phoneCache = new ConcurrentHashMap<>();

    private record CachedPhone(String e164, Instant expiresAt) {}

    public NocNotificationService(RestTemplate restTemplate,
                                  @Value("${noc.base-url}") String nocBaseUrl,
                                  @Value("${noc.sms-path:/api/v1/sms}") String smsPath,
                                  @Value("${noc.phone-validation-path:/api/v1/phone-numbers/{phone}/validations}") String phoneValidationPath,
                                  @Value("${noc.phone-cache-ttl-seconds:600}") long phoneCacheTtlSeconds) {
        this.restTemplate = restTemplate;
        this.nocBaseUrl = nocBaseUrl;
        this.smsPath = smsPath;
        this.phoneValidationPath = phoneValidationPath;
        this.phoneCacheTtlSeconds = phoneCacheTtlSeconds;
    }

    public Optional<String> normalizePhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            return Optional.empty();
        }

        // cache hit
        CachedPhone cached = phoneCache.get(rawPhone);
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
            return Optional.ofNullable(cached.e164());
        }

        try {
            String url = nocBaseUrl + phoneValidationPath; // contains {phone}
            PhoneNumberValidationResult result =
                    restTemplate.getForObject(url, PhoneNumberValidationResult.class, rawPhone);

            if (result != null && result.isValid() && result.getE164() != null && !result.getE164().isBlank()) {
                String e164 = result.getE164();
                phoneCache.put(rawPhone, new CachedPhone(e164, Instant.now().plusSeconds(phoneCacheTtlSeconds)));
                return Optional.of(e164);
            }
        } catch (Exception ex) {
            LOGGER.warn("Error calling NOC phone validation for {}: {}", rawPhone, ex.getMessage());
        }

        return Optional.empty();
    }

    public void sendSms(String e164, String content) {
        try {
            String url = nocBaseUrl + smsPath;

            SendSmsRequest request = new SendSmsRequest();
            request.setE164(e164);
            request.setContent(content);

            SendSmsResult response = restTemplate.postForObject(url, request, SendSmsResult.class);

            if (response == null || !response.isSent()) {
                LOGGER.warn("SMS not sent successfully to {}", e164);
            } else {
                LOGGER.info("SMS successfully sent to {}", e164);
            }

        } catch (Exception ex) {
            LOGGER.warn("Error sending SMS to {}: {}", e164, ex.getMessage());
        }
    }

    public void notifyOwnerAppointmentConfirmed(Appointment appointment) {
        if (appointment == null || appointment.getPet() == null) return;
        User owner = appointment.getPet().getOwner();
        if (owner == null) return;

        Optional<String> e164Opt = normalizePhone(owner.getPhoneNumber());
        if (e164Opt.isEmpty()) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String when = appointment.getStartTime() != null ? appointment.getStartTime().format(fmt) : "unknown time";

        String message = String.format(
                "Your appointment for %s with vet %s on %s was CONFIRMED.",
                appointment.getPet().getName(),
                appointment.getVet().getFullName(),
                when
        );

        sendSms(e164Opt.get(), message);
    }

    public void notifyOwnerAppointmentCancelledByVet(Appointment appointment) {
        if (appointment == null || appointment.getPet() == null) return;
        User owner = appointment.getPet().getOwner();
        if (owner == null) return;

        Optional<String> e164Opt = normalizePhone(owner.getPhoneNumber());
        if (e164Opt.isEmpty()) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String when = appointment.getStartTime() != null ? appointment.getStartTime().format(fmt) : "unknown time";

        String message = String.format(
                "Your appointment for %s with vet %s on %s was CANCELLED by the vet.",
                appointment.getPet().getName(),
                appointment.getVet().getFullName(),
                when
        );

        sendSms(e164Opt.get(), message);
    }

    public void notifyOwnerAppointmentCompleted(Appointment appointment) {
        if (appointment == null || appointment.getPet() == null) return;
        User owner = appointment.getPet().getOwner();
        if (owner == null) return;

        Optional<String> e164Opt = normalizePhone(owner.getPhoneNumber());
        if (e164Opt.isEmpty()) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String when = appointment.getStartTime() != null ? appointment.getStartTime().format(fmt) : "unknown time";

        String message = String.format(
                "Your appointment for %s with vet %s on %s was COMPLETED.",
                appointment.getPet().getName(),
                appointment.getVet().getFullName(),
                when
        );

        sendSms(e164Opt.get(), message);
    }

    public void notifyVetNewAppointmentRequested(Appointment appointment) {
        if (appointment == null || appointment.getVet() == null) return;
        User vet = appointment.getVet();

        Optional<String> e164Opt = normalizePhone(vet.getPhoneNumber());
        if (e164Opt.isEmpty()) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String when = appointment.getStartTime() != null ? appointment.getStartTime().format(fmt) : "unknown time";

        String petName = appointment.getPet() != null ? appointment.getPet().getName() : "a pet";
        String ownerName = (appointment.getPet() != null && appointment.getPet().getOwner() != null)
                ? appointment.getPet().getOwner().getFullName()
                : "an owner";

        String message = String.format(
                "New appointment request for %s from %s on %s. Please log in to PetCare to confirm or cancel.",
                petName, ownerName, when
        );

        sendSms(e164Opt.get(), message);
    }

    public void notifyOwnerVisitNotesUpdated(Appointment appointment) {
        if (appointment == null || appointment.getPet() == null) return;
        User owner = appointment.getPet().getOwner();
        if (owner == null) return;

        Optional<String> e164Opt = normalizePhone(owner.getPhoneNumber());
        if (e164Opt.isEmpty()) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String when = appointment.getStartTime() != null ? appointment.getStartTime().format(fmt) : "unknown time";

        String message = String.format(
                "The vet updated the medical record of %s for the visit on %s. You can view the details in Pet History in the PetCare app.",
                appointment.getPet().getName(),
                when
        );

        sendSms(e164Opt.get(), message);
    }
}
