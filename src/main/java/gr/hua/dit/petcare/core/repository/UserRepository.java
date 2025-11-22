package gr.hua.dit.petcare.core.repository;

import gr.hua.dit.petcare.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /**
     * Χρήσιμο για validation στο registration:
     * - να μην επιτρέπεται διπλό username
     */
    boolean existsByUsername(String username);
}
