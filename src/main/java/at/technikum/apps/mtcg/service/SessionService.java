package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.TokenRequest;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.session.SessionRepository;
import at.technikum.apps.mtcg.repository.session.SessionRepository_db;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository_db;

import java.util.Objects;
import java.util.Optional;

// TODO: ADD COMMENTS
public class SessionService {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    public SessionService() {
        this.userRepository = new UserRepository_db();
        this.sessionRepository = new SessionRepository_db();
    }

    public Optional<String> getToken(TokenRequest tokenRequest) {
        Optional<User> userOptional = userRepository.findByUsername(tokenRequest.getUsername());

        if (userOptional.isPresent() && Objects.equals(userOptional.get().getPassword(), tokenRequest.getPassword())) {
            // Check if a token already exists for the user
            Optional<String> existingToken = sessionRepository.findTokenByUserId(userOptional.get().getId());
            if (existingToken.isPresent()) {
                sessionRepository.deleteToken(userOptional.get().getId());
            }
            // Generate a new token
            return sessionRepository.generateToken(userOptional.get());
        }
        return Optional.empty();
    }


    public boolean authenticateToken(String token) {
        return sessionRepository.authenticateToken(token);
    }

    public boolean isAdmin(String token) {
        Optional<User> user = sessionRepository.findByToken(token);
        if (user.isPresent()) {
            return user.get().isAdmin();
        }
        return false;
    }

    public boolean matchRoute(String username, String token) {
        Optional<User> userByUsername = userRepository.findByUsername(username);
        Optional<User> userByToken = sessionRepository.findByToken(token);

        if (!userByUsername.isPresent() || !userByToken.isPresent()) {
            // One of the Optionals is empty, so no match
            return false;
        }

        // Both Optionals have a value; compare the user IDs
        return userByUsername.get().getId().equals(userByToken.get().getId());
    }

    public Optional<User> getUserByToken(String token) {
        return sessionRepository.findByToken(token);
    }

}
