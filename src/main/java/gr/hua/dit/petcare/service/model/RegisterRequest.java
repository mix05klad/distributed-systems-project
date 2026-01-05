package gr.hua.dit.petcare.service.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 3, max = 100, message = "password must be between 3 and 100 characters")
    private String password;

    @NotBlank(message = "fullName is required")
    @Size(min = 2, max = 100, message = "fullName must be between 2 and 100 characters")
    private String fullName;

    @Email(message = "email must be a valid email address")
    @Size(max = 150, message = "email too long")
    private String email;

    // απλό validation για τηλέφωνο (επιτρέπει +, κενά, παύλες)
    @Pattern(
            regexp = "^[0-9+\\-\\s]{6,30}$",
            message = "phoneNumber must contain only digits, spaces, + or - (6-30 chars)"
    )
    private String phoneNumber;

    @NotBlank(message = "role is required")
    private String role; // OWNER or VET

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
