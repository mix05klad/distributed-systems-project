package gr.hua.dit.petcare.service.impl;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.repository.UserRepository;
import gr.hua.dit.petcare.security.Role;
import gr.hua.dit.petcare.service.UserService;
import gr.hua.dit.petcare.service.model.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already in use");
        }

        // επιβεβαίωση ρόλου μέσω του enum
        String roleStr = req.getRole();
        if (roleStr == null) {
            throw new IllegalArgumentException("Role is required");
        }

        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + roleStr + " (expected OWNER or VET)");
        }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail());
        u.setPhoneNumber(req.getPhoneNumber());
        u.setRoles(Set.of(role.name())); // αποθηκεύουμε ως "OWNER" ή "VET"

        return userRepository.save(u);
    }
}
