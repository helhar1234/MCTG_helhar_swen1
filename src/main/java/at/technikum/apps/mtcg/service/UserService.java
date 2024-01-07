package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository_db;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
// TODO: MAKE STATS & SCOREBOARD SERVICE SEPERATE
public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository_db();
    }

    // FOR TESTING
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> createUser(User user) {
        user.setId(UUID.randomUUID().toString());
        user.setAdmin(Objects.equals(user.getUsername(), "admin"));
        if (userRepository.isUsernameExists(user.getUsername())) {
            return Optional.empty();
        }
        return userRepository.saveUser(user);
    }

    public Optional<User> findUserById(String userId) {
        return userRepository.findUserById(userId);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserData updateUserData(String username, UserData userData) {
        return userRepository.updateUserData(userRepository.findByUsername(username).get().getId(), userData);
    }

}
