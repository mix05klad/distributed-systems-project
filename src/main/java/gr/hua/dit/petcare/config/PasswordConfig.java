package gr.hua.dit.petcare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    /**
     * Global PasswordEncoder bean used by:
     * - UserServiceImpl when αποθηκεύει νέους χρήστες
     * - DaoAuthenticationProvider για authentication
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Το default strength (10) είναι μια χαρά για τις ανάγκες μας
        return new BCryptPasswordEncoder();
        // Αν θέλεις πιο "βαρύ" hashing:
        // return new BCryptPasswordEncoder(12);
    }
}
