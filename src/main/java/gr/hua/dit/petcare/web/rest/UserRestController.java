package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.repository.UserRepository;
import gr.hua.dit.petcare.security.ApplicationUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserRepository userRepository;

    public UserRestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Τρέχων χρήστης (profile)
    @GetMapping("/me")
    public ResponseEntity<CurrentUserView> getCurrentUser() {
        ApplicationUserDetails details = getCurrentUserDetails();

        User user = userRepository.findById(details.getId())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        CurrentUserView view = new CurrentUserView();
        view.setId(user.getId());
        view.setUsername(user.getUsername());
        view.setFullName(user.getFullName());
        view.setEmail(user.getEmail());
        view.setPhoneNumber(user.getPhoneNumber());
        view.setRoles(user.getRoles().stream().toList());

        return ResponseEntity.ok(view);
    }

    // Λίστα όλων των VET
    @GetMapping("/vets")
    public ResponseEntity<List<VetSummaryView>> getAllVets() {
        List<VetSummaryView> vets = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("VET")))
                .map(u -> new VetSummaryView(
                        u.getId(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getPhoneNumber()
                ))
                .toList();

        return ResponseEntity.ok(vets);
    }

    private ApplicationUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof ApplicationUserDetails details) {
            return details;
        }
        throw new IllegalStateException("No authenticated user");
    }

    // DTO για το profile
    public static class CurrentUserView {
        private Long id;
        private String username;
        private String fullName;
        private String email;
        private String phoneNumber;
        private List<String> roles;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }

    public record VetSummaryView(
            Long id,
            String fullName,
            String email,
            String phoneNumber
    ) {}
}
