package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.security.JwtUtils;
import gr.hua.dit.petcare.service.UserService;
import gr.hua.dit.petcare.service.model.RegisterRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    public AuthRestController(AuthenticationManager authenticationManager,
                              JwtUtils jwtUtils,
                              UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);

        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setFullName(user.getFullName());
        resp.setEmail(user.getEmail());
        resp.setPhoneNumber(user.getPhoneNumber());
        resp.setRoles(user.getRoles().stream().toList());

        // Θα μπορούσες και CREATED(201), αλλά OK για εργασία να μείνει 200
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            String token = jwtUtils.generateToken(authentication);

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority) // π.χ. ROLE_OWNER
                    .collect(Collectors.toList());

            JwtResponse resp = new JwtResponse();
            resp.setToken(token);
            resp.setTokenType("Bearer");
            resp.setUsername(request.getUsername());
            resp.setRoles(roles);

            return ResponseEntity.ok(resp);
        } catch (BadCredentialsException ex) {
            // Επιστρέφουμε καθαρό JSON σώμα (βοηθάει debugging/Swagger)
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", Instant.now().toString());
            body.put("status", HttpStatus.UNAUTHORIZED.value());
            body.put("error", "Unauthorized");
            body.put("message", "Invalid username or password");
            body.put("path", "/api/auth/login");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
    }

    // =========================
    // DTOs (μπορείς αργότερα να τα βγάλεις σε ξεχωριστό package)
    // =========================

    public static class LoginRequest {

        @NotBlank(message = "username is required")
        private String username;

        @NotBlank(message = "password is required")
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class JwtResponse {
        private String token;
        private String tokenType;
        private String username;
        private List<String> roles;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }

    public static class UserResponse {
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
}
