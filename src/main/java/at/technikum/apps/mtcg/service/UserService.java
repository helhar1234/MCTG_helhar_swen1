package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;
import at.technikum.apps.mtcg.entity.UserStats;
import at.technikum.apps.mtcg.repository.UserRepository;
import at.technikum.apps.mtcg.repository.UserRepository_db;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository_db();
    }

    public boolean createUser(User user) {
        user.setId(UUID.randomUUID().toString());
        user.setAdmin(Objects.equals(user.getUsername(), "admin"));
        if (userRepository.isUsernameExists(user.getUsername())){
            return false;
        }
        return userRepository.saveUser(user);
    }

    public Optional <User> findUserById(String userId){
        return userRepository.findUserById(userId);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserData updateUserData(String username, UserData userData) {
        return userRepository.updateUserData(userRepository.findByUsername(username).get().getId(), userData);
    }

    public Optional<User> getUserByToken(String token) {
        return userRepository.findByToken(token);
    }

    public UserStats[] getScoreboard() {
        return userRepository.getScoreboard();
    }
}
