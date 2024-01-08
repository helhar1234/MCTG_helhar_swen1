package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.NotFoundException;
import at.technikum.apps.mtcg.customExceptions.UnauthorizedException;
import at.technikum.apps.mtcg.entity.TokenRequest;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.session.SessionRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.server.http.Request;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

// TODO: ADD COMMENTS
public class SessionService {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    public SessionService(UserRepository userRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public User authenticateRequest(Request request) throws UnauthorizedException, NotFoundException, SQLException {
        String authHeader = request.getAuthenticationHeader();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("No token provided");
        }
        String token = authHeader.split("\\s+")[1];

        if (!authenticateToken(token)) {
            throw new UnauthorizedException("Invalid token");
        }

        Optional<User> userOpt = getUserByToken(token);
        if (userOpt.isEmpty()) {
            throw new NotFoundException("User does not exist");
        }

        return userOpt.get();
    }

    public Optional<String> getToken(TokenRequest tokenRequest) throws SQLException {
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


    public boolean authenticateToken(String token) throws SQLException {
        return sessionRepository.authenticateToken(token);
    }

    public Optional<User> getUserByToken(String token) throws SQLException {
        return sessionRepository.findByToken(token);
    }

}