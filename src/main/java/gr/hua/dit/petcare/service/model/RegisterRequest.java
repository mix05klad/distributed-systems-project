package gr.hua.dit.petcare.service.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotNull @Size(min = 3, max = 50)
    private String username;

    @NotNull @Size(min = 3, max = 100)
    private String password;

    @NotNull
    private String fullName;

    private String email;
    private String phoneNumber;

    @NotNull
    private String role; // OWNER or VET

    // getters/setters
}
