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
}
