package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.TokenRequest;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.UserRepository;
import at.technikum.apps.mtcg.repository.UserRepository_db;

import java.util.Objects;
import java.util.Optional;

public class SessionService {
    private final UserRepository userRepository;

    public SessionService() {
        this.userRepository = new UserRepository_db();
    }

    public Optional<String> getToken(TokenRequest tokenRequest) {
        // Find user by username
        Optional<User> userOptional = userRepository.findByUsername(tokenRequest.getUsername());

        // Check if the user is present and password matches
        if (userOptional.isPresent() && Objects.equals(userOptional.get().getPassword(), tokenRequest.getPassword())) {
            // Generate token if credentials are valid
            return userRepository.generateToken(userOptional.get());
        }

        // Return empty if user is not found or password does not match
        return Optional.empty();
    }

    public boolean authenticateToken(String token) {
        return userRepository.authenticateToken(token);
    }

    public boolean isAdmin(String token) {
        Optional<User> user = userRepository.findByToken(token);
        if (user.isPresent()){
            return user.get().isAdmin();
        }
        return false;
    }

    public boolean matchRoute(String username, String token) {
        Optional<User> userByUsername = userRepository.findByUsername(username);
        Optional<User> userByToken = userRepository.findByToken(token);

        if (!userByUsername.isPresent() || !userByToken.isPresent()) {
            // One of the Optionals is empty, so no match
            return false;
        }

        // Both Optionals have a value; compare the user IDs
        return userByUsername.get().getId().equals(userByToken.get().getId());
    }

}
