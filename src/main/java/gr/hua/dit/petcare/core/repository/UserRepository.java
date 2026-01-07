package gr.hua.dit.petcare.core.repository;

import gr.hua.dit.petcare.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    default Optional<User> findByUsername(String username) {
        return findByUsernameIgnoreCase(username);
    }

    default boolean existsByUsername(String username) {
        return existsByUsernameIgnoreCase(username);
    }
}
