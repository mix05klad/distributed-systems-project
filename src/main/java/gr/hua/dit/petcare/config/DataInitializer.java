package gr.hua.dit.petcare.config;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.core.model.AppointmentStatus;
import gr.hua.dit.petcare.core.model.Pet;
import gr.hua.dit.petcare.core.model.PetType;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.model.VetAvailability;
import gr.hua.dit.petcare.core.model.VisitType;
import gr.hua.dit.petcare.core.repository.AppointmentRepository;
import gr.hua.dit.petcare.core.repository.PetRepository;
import gr.hua.dit.petcare.core.repository.UserRepository;
import gr.hua.dit.petcare.core.repository.VetAvailabilityRepository;
import gr.hua.dit.petcare.security.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      PetRepository petRepository,
                                      AppointmentRepository appointmentRepository,
                                      VetAvailabilityRepository vetAvailabilityRepository,
                                      PasswordEncoder passwordEncoder) {

        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            // 1. Χρήστες (Owners, Vets)

            // Owners
            User owner1 = new User();
            owner1.setUsername("owner1");
            owner1.setPassword(passwordEncoder.encode("owner1pass")); // login: owner1 / owner1pass
            owner1.setFullName("Michael Kladis");
            owner1.setEmail("owner1@example.com");
            owner1.setPhoneNumber("2100000001");
            owner1.setRoles(Set.of(Role.OWNER.name()));
            owner1 = userRepository.save(owner1);

            User owner2 = new User();
            owner2.setUsername("owner2");
            owner2.setPassword(passwordEncoder.encode("owner2pass")); // login: owner2 / owner2pass
            owner2.setFullName("Chris Filip");
            owner2.setEmail("owner2@example.com");
            owner2.setPhoneNumber("2100000002");
            owner2.setRoles(Set.of(Role.OWNER.name()));
            owner2 = userRepository.save(owner2);

            User owner3 = new User();
            owner3.setUsername("owner3");
            owner3.setPassword(passwordEncoder.encode("owner3pass")); // login: owner3 / owner3pass
            owner3.setFullName("Touloumi Dimitra");
            owner3.setEmail("owner3@example.com");
            owner3.setPhoneNumber("2100000003");
            owner3.setRoles(Set.of(Role.OWNER.name()));
            owner3 = userRepository.save(owner3);

            // Vets
            User vet1 = new User();
            vet1.setUsername("vet1");
            vet1.setPassword(passwordEncoder.encode("vet1pass")); // login: vet1 / vet1pass
            vet1.setFullName("Lia Papadopoulou");
            vet1.setEmail("vet1@example.com");
            vet1.setPhoneNumber("6981078397");
            vet1.setRoles(Set.of(Role.VET.name()));
            vet1 = userRepository.save(vet1);

            User vet2 = new User();
            vet2.setUsername("vet2");
            vet2.setPassword(passwordEncoder.encode("vet2pass")); // login: vet2 / vet2pass
            vet2.setFullName("Tasos Kastanis");
            vet2.setEmail("vet2@example.com");
            vet2.setPhoneNumber("6981078397");
            vet2.setRoles(Set.of(Role.VET.name()));
            vet2 = userRepository.save(vet2);

            // 2. Pets

            Pet rex = new Pet();
            rex.setName("Rex");
            rex.setType(PetType.DOG);
            rex.setBreed("Labrador");
            rex.setAge(3);
            rex.setOwner(owner1);
            rex = petRepository.save(rex);

            Pet bella = new Pet();
            bella.setName("Bella");
            bella.setType(PetType.CAT);
            bella.setBreed("Siamese");
            bella.setAge(2);
            bella.setOwner(owner1);
            bella = petRepository.save(bella);

            Pet max = new Pet();
            max.setName("Max");
            max.setType(PetType.DOG);
            max.setBreed("German Shepherd");
            max.setAge(4);
            max.setOwner(owner2);
            max = petRepository.save(max);

            // 3. Διαθεσιμότητες κτηνιάτρων (VetAvailability)

            LocalDate today = LocalDate.now();

            // vet1: αύριο 09:00–12:00 και 13:00–17:00
            LocalDateTime v1Slot1Start = LocalDateTime.of(today.plusDays(1), LocalTime.of(9, 0));
            LocalDateTime v1Slot1End   = LocalDateTime.of(today.plusDays(1), LocalTime.of(12, 0));

            LocalDateTime v1Slot2Start = LocalDateTime.of(today.plusDays(1), LocalTime.of(13, 0));
            LocalDateTime v1Slot2End   = LocalDateTime.of(today.plusDays(1), LocalTime.of(17, 0));

            VetAvailability v1Slot1 = new VetAvailability();
            v1Slot1.setVet(vet1);
            v1Slot1.setStartTime(v1Slot1Start);
            v1Slot1.setEndTime(v1Slot1End);
            v1Slot1 = vetAvailabilityRepository.save(v1Slot1);

            VetAvailability v1Slot2 = new VetAvailability();
            v1Slot2.setVet(vet1);
            v1Slot2.setStartTime(v1Slot2Start);
            v1Slot2.setEndTime(v1Slot2End);
            v1Slot2 = vetAvailabilityRepository.save(v1Slot2);

            // vet2: μεθαύριο 10:00–14:00
            LocalDateTime v2Slot1Start = LocalDateTime.of(today.plusDays(2), LocalTime.of(10, 0));
            LocalDateTime v2Slot1End   = LocalDateTime.of(today.plusDays(2), LocalTime.of(14, 0));

            VetAvailability v2Slot1 = new VetAvailability();
            v2Slot1.setVet(vet2);
            v2Slot1.setStartTime(v2Slot1Start);
            v2Slot1.setEndTime(v2Slot1End);
            v2Slot1 = vetAvailabilityRepository.save(v2Slot1);

            // 4. Ραντεβού (Appointments)

            // Ραντεβού 1: Rex με vet1 αύριο 09:00–10:00 (CONFIRMED)
            LocalDateTime ap1Start = LocalDateTime.of(today.plusDays(1), LocalTime.of(9, 0));
            LocalDateTime ap1End   = LocalDateTime.of(today.plusDays(1), LocalTime.of(10, 0));

            Appointment ap1 = new Appointment();
            ap1.setPet(rex);
            ap1.setVet(vet1);
            ap1.setStartTime(ap1Start);
            ap1.setEndTime(ap1End);
            ap1.setStatus(AppointmentStatus.CONFIRMED);
            ap1.setVisitType(VisitType.TREATMENT);
            ap1.setVetNotes(null);
            appointmentRepository.save(ap1);

            // Ραντεβού 2: Bella με vet1 αύριο 11:00–12:00 (VACCINE, PENDING)
            LocalDateTime ap2Start = LocalDateTime.of(today.plusDays(1), LocalTime.of(11, 0));
            LocalDateTime ap2End   = LocalDateTime.of(today.plusDays(1), LocalTime.of(12, 0));

            Appointment ap2 = new Appointment();
            ap2.setPet(bella);
            ap2.setVet(vet1);
            ap2.setStartTime(ap2Start);
            ap2.setEndTime(ap2End);
            ap2.setStatus(AppointmentStatus.PENDING);
            ap2.setVisitType(VisitType.VACCINE);
            ap2.setVetNotes(null);
            appointmentRepository.save(ap2);

            // Ραντεβού 3: Max με vet2 μεθαύριο 10:30–11:30 (PENDING)
            LocalDateTime ap3Start = LocalDateTime.of(today.plusDays(2), LocalTime.of(10, 30));
            LocalDateTime ap3End   = LocalDateTime.of(today.plusDays(2), LocalTime.of(11, 30));

            Appointment ap3 = new Appointment();
            ap3.setPet(max);
            ap3.setVet(vet2);
            ap3.setStartTime(ap3Start);
            ap3.setEndTime(ap3End);
            ap3.setStatus(AppointmentStatus.PENDING);
            ap3.setVisitType(VisitType.CHECKUP);
            ap3.setVetNotes(null);
            appointmentRepository.save(ap3);
        };
    }
}
