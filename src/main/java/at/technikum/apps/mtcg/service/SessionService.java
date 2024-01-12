package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.TokenRequest;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.session.SessionRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;

import java.util.Objects;
import java.util.Optional;

// TODO: ADD COMMENTS
public class SessionService {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final HashingService hashingService;

    public SessionService(UserRepository userRepository, SessionRepository sessionRepository, HashingService hashingService) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.hashingService = hashingService;
    }

    public User authenticateRequest(Request request) {
        String authHeader = request.getAuthenticationHeader();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "No token provided");
        }
        String token = authHeader.split("\\s+")[1];

        if (!authenticateToken(token)) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        Optional<User> userOpt = getUserByToken(token);
        if (userOpt.isEmpty()) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "User does not exist");
        }

        return userOpt.get();
    }

    public Optional<String> getToken(TokenRequest tokenRequest) {
        Optional<User> userOptional = userRepository.findByUsername(tokenRequest.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String hashedPassword = user.getPassword();

            // Verwenden Sie compareHash, um das eingegebene Passwort mit dem gespeicherten gehashten Passwort zu vergleichen
            if (hashingService.compareHash(tokenRequest.getPassword(), hashedPassword)) {
                // Check if a token already exists for the user
                Optional<String> existingToken = sessionRepository.findTokenByUserId(user.getId());
                if (existingToken.isPresent()) {
                    sessionRepository.deleteToken(user.getId());
                }
                // Generate a new token
                return sessionRepository.generateToken(userOptional.get());
            } else {
                throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Incorrect Password!");
            }
        }
        return Optional.empty();
    }



    public boolean authenticateToken(String token) {
        return sessionRepository.authenticateToken(token);
    }

    public Optional<User> getUserByToken(String token) {
        return sessionRepository.findByToken(token);
    }

}