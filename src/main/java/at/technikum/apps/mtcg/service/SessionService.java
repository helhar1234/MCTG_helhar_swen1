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
        Optional<User> userOptional = userRepository.findByUsername(tokenRequest.getUsername());

        if (userOptional.isPresent() && Objects.equals(userOptional.get().getPassword(), tokenRequest.getPassword())) {
            // Check if a token already exists for the user
            Optional<String> existingToken = userRepository.findTokenByUserId(userOptional.get().getId());
            if (existingToken.isPresent()) {
                userRepository.deleteToken(userOptional.get().getId());
            }
            // Generate a new token
            return userRepository.generateToken(userOptional.get());
        }
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
