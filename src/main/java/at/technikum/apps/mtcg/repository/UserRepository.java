package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.entity.User;

import java.util.Optional;

public interface UserRepository {
    boolean saveUser(User user);

    boolean isUsernameExists(String username);

    Optional<User> findByUsername(String username);
}
