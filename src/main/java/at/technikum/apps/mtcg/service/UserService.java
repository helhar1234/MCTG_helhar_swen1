package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;
import at.technikum.apps.mtcg.repository.user.UserRepository;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
// TODO: MAKE STATS & SCOREBOARD SERVICE SEPERATE
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public synchronized Optional<User> createUser(User user) throws SQLException {
        user.setId(UUID.randomUUID().toString());
        user.setAdmin(Objects.equals(user.getUsername(), "admin"));
        if (userRepository.isUsernameExists(user.getUsername())) {
            return Optional.empty();
        }
        return userRepository.saveUser(user);
    }

    public Optional<User> findUserById(String userId) throws SQLException {
        return userRepository.findUserById(userId);
    }

    public Optional<User> findUserByUsername(String username) throws SQLException {
        return userRepository.findByUsername(username);
    }

    public synchronized UserData updateUserData(String username, UserData userData) throws SQLException {
        return userRepository.updateUserData(userRepository.findByUsername(username).get().getId(), userData);
    }

}
