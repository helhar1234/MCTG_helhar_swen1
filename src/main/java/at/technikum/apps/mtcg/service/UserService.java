package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
public class UserService {
    private final UserRepository userRepository;
    private final SessionService sessionService;

    public UserService(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    public Optional<User> createUser(User user) {
        user.setId(UUID.randomUUID().toString());
        user.setAdmin(Objects.equals(user.getUsername(), "admin"));
        if (userRepository.isUsernameExists(user.getUsername())) {
            throw new HttpStatusException(HttpStatus.CONFLICT, "User with same username already registered");
        }
        return userRepository.saveUser(user);
    }

    public Optional<User> findUserById(String userId) {
        return userRepository.findUserById(userId);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserData updateUserData(String username, Request request, UserData userData) {
        User requester = sessionService.authenticateRequest(request);
        if (!requester.getUsername().equals(username) && !requester.isAdmin()) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Access denied");
        }
        return userRepository.updateUserData(userRepository.findByUsername(username).get().getId(), userData);
    }

    public Optional<User> getUser(String username, Request request) {
        User requester = sessionService.authenticateRequest(request);
        if (!requester.getUsername().equals(username) && !requester.isAdmin()) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Access denied");
        }
        return Optional.of(requester);
    }
}
