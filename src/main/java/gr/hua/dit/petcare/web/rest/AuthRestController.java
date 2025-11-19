package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.security.ApplicationUserDetails;
import gr.hua.dit.petcare.security.JwtUtils;
import gr.hua.dit.petcare.service.UserService;
import gr.hua.dit.petcare.service.model.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthenticationManager authManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthRestController(AuthenticationManager authManager, UserService userService, JwtUtils jwtUtils) {
        this.authManager = authManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody RegisterRequest req) {
        return userService.register(req);
    }

    @PostMapping("/login")
    public Map<String,Object> login(@RequestParam String username,
                                    @RequestParam String password) {

        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
            String token = jwtUtils.generateToken(user);

            Map<String,Object> resp = new HashMap<>();
            resp.put("token", token);
            resp.put("username", user.getUsername());
            resp.put("userId", user.getUser().getId());
            resp.put("roles", user.getUser().getRoles());

            return resp;

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid user/password");
        }
    }
}
