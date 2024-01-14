package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.dto.TokenRequest;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.session.SessionRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;

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

    /**
     * Authenticates a request by verifying the provided token.
     *
     * @param request The HTTP request to authenticate.
     * @return The User associated with the authenticated request.
     * @throws HttpStatusException If the token is missing, invalid, or the user does not exist.
     */
    public User authenticateRequest(Request request) {
        // Retrieve the authentication header from the request
        String authHeader = request.getAuthenticationHeader();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "No token provided");
        }

        // Extract the token from the header
        String token = authHeader.split("\\s+")[1];

        // Verify the token's authenticity
        if (!authenticateToken(token)) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        // Retrieve the user associated with the token
        Optional<User> userOpt = getUserByToken(token);
        if (userOpt.isEmpty()) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "User does not exist");
        }

        return userOpt.get();
    }


    /**
     * Retrieves a token for a user based on their login credentials.
     *
     * @param tokenRequest The request containing the user's login credentials.
     * @return An Optional containing the token if authentication is successful, or an empty Optional if not.
     */
    public Optional<String> getToken(TokenRequest tokenRequest) {
        // Find the user by their username
        Optional<User> userOptional = userRepository.findByUsername(tokenRequest.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String hashedPassword = user.getPassword();

            // Verify the password against the stored hash
            if (hashingService.compareHash(tokenRequest.getPassword(), hashedPassword)) {
                // Check for an existing token and delete it
                Optional<String> existingToken = sessionRepository.findTokenByUserId(user.getId());
                if (existingToken.isPresent()) {
                    sessionRepository.deleteToken(user.getId());
                }
                // Generate and return a new token
                return sessionRepository.generateToken(user);
            }
        }
        throw new HttpStatusException(HttpStatus.NOT_FOUND, "User does not exist");
    }


    /**
     * Verifies the authenticity of a token.
     *
     * @param token The token to authenticate.
     * @return True if the token is valid, false otherwise.
     */
    public boolean authenticateToken(String token) {
        // Delegate to the sessionRepository to authenticate the token
        return sessionRepository.authenticateToken(token);
    }

    /**
     * Retrieves a user based on their authentication token.
     *
     * @param token The token associated with the user.
     * @return An Optional containing the User if found, or an empty Optional if not found.
     */
    public Optional<User> getUserByToken(String token) {
        // Delegate to the sessionRepository to find the user by their token
        return sessionRepository.findByToken(token);
    }

}