package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.server.http.HttpStatus;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository;
    private final HashingService hashingService;

    public UserService(UserRepository userRepository, HashingService hashingService) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
    }

    /**
     * Creates a new user in the system.
     *
     * @param user The User object containing the user's information.
     * @return An Optional containing the created User object, or an empty Optional if creation fails.
     * @throws HttpStatusException If a user with the same username already exists.
     */
    public Optional<User> createUser(User user) {
        // Generate a new unique ID for the user if not already set
        if (user.getId() == null) user.setId(UUID.randomUUID().toString());

        // Encrypt the user's password
        user.setPassword(hashingService.encrypt(user.getPassword()));

        // Set the user as admin if the username is 'admin'
        user.setAdmin(Objects.equals(user.getUsername(), "admin"));

        // Check if the username already exists in the system
        if (userRepository.isUsernameExists(user.getUsername())) {
            throw new HttpStatusException(HttpStatus.CONFLICT, "User with same username already registered");
        }

        // Save the user in the repository and return the result
        return userRepository.saveUser(user);
    }


    /**
     * Finds a user by their unique ID.
     *
     * @param userId The unique ID of the user.
     * @return An Optional containing the User object if found, or an empty Optional if not found.
     */
    public Optional<User> findUserById(String userId) {
        // Delegate to the userRepository to find the user by ID
        return userRepository.findUserById(userId);
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user.
     * @return An Optional containing the User object if found, or an empty Optional if not found.
     */
    public Optional<User> findUserByUsername(String username) {
        // Delegate to the userRepository to find the user by username
        return userRepository.findByUsername(username);
    }

    /**
     * Updates the data of a user.
     *
     * @param user     The current authenticated user.
     * @param username The username of the user to be updated.
     * @param userData The new data to be updated.
     * @return The updated UserData object.
     * @throws HttpStatusException If the current user is not authorized to update the data.
     */
    public UserData updateUserData(User user, String username, UserData userData) {
        // Check if the current user is authorized to update the data
        if (!user.getUsername().equals(username) && !user.isAdmin()) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Access denied");
        }

        // Update the user data in the repository and return the updated data
        return userRepository.updateUserData(userRepository.findByUsername(username).get().getId(), userData);
    }


    /**
     * Retrieves the details of a specified user.
     *
     * @param user     The current authenticated user.
     * @param username The username of the user whose details are to be retrieved.
     * @return An Optional containing the User object if found, or an empty Optional if not found.
     * @throws HttpStatusException If the current user is not authorized to access the details.
     */
    public Optional<User> getUser(User user, String username) {
        // Check if the current user is authorized to get the user details
        if (!user.getUsername().equals(username) && !user.isAdmin()) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Access denied");
        }

        // Return the user details
        return Optional.of(user);
    }

}
