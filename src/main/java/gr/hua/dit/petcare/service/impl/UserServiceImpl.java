package gr.hua.dit.petcare.service.impl;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.repository.UserRepository;
import gr.hua.dit.petcare.security.Role;
import gr.hua.dit.petcare.service.UserService;
import gr.hua.dit.petcare.service.model.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public User register(RegisterRequest req) {

        String username = req.getUsername().trim();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already in use");
        }

        String roleStr = req.getRole();
        if (roleStr == null || roleStr.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }

        Role role;
        try {
            role = Role.valueOf(roleStr.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + roleStr + " (expected OWNER or VET)");
        }

        User u = new User();
        u.setUsername(username);
        u.setPassword(encoder.encode(req.getPassword()));
        u.setFullName(req.getFullName().trim());
        u.setEmail(req.getEmail());
        u.setPhoneNumber(req.getPhoneNumber());
        u.setRoles(Set.of(role.name())); // "OWNER" ή "VET"
        u.setEnabled(true);

        return userRepository.save(u);
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User id is required");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(Long userId) {
        return getById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllVets() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles() != null && u.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("VET")))
                .toList();
    }
}
