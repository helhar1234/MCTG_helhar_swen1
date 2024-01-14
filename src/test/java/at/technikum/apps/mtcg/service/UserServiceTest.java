package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    @Test
    void createUserShouldCreateNewUserWhenUsernameIsUnique() {
        // Mock dependencies
        UserRepository mockedUserRepository = mock(UserRepository.class);
        HashingService mockedHashingService = mock(HashingService.class);

        // Set up expected behavior
        when(mockedUserRepository.isUsernameExists(anyString())).thenReturn(false);
        when(mockedHashingService.encrypt(anyString())).thenReturn("encryptedPassword");

        // Mock the behavior of saveUser to return a User with the encrypted password
        when(mockedUserRepository.saveUser(any(User.class))).thenAnswer(invocation -> {
            User argumentUser = invocation.getArgument(0);
            argumentUser.setPassword("encryptedPassword");
            return Optional.of(argumentUser);
        });

        // Create an instance of UserService
        UserService userService = new UserService(mockedUserRepository, mockedHashingService);

        // Create a new user
        User newUser = new User("123", "testUserService", "password");
        Optional<User> createdUser = userService.createUser(newUser);

        // Assertions
        assertTrue(createdUser.isPresent());
        assertEquals("encryptedPassword", createdUser.get().getPassword());
        assertNotNull(createdUser.get().getId());
    }

    @Test
    void findUserByIdShouldRetrieveUser() {
        UserRepository mockedUserRepository = mock(UserRepository.class);
        when(mockedUserRepository.findUserById(anyString())).thenReturn(Optional.of(new User()));

        UserService userService = new UserService(mockedUserRepository, null);

        // Retrieve a user by ID
        Optional<User> foundUser = userService.findUserById("123");

        assertTrue(foundUser.isPresent());
    }

    @Test
    void findUserByUsernameShouldRetrieveUser() {
        UserRepository mockedUserRepository = mock(UserRepository.class);
        when(mockedUserRepository.findByUsername(anyString())).thenReturn(Optional.of(new User()));

        UserService userService = new UserService(mockedUserRepository, null);

        // Retrieve a user by username
        Optional<User> foundUser = userService.findUserByUsername("testUserService");

        assertTrue(foundUser.isPresent());
    }

    @Test
    void getUserShouldReturnUserForAuthorizedRequest() {
        UserRepository mockedUserRepository = mock(UserRepository.class);
        User requestingUser = new User("123", "testUserService", "password");

        UserService userService = new UserService(mockedUserRepository, null);

        // Get a user for an authorized request
        Optional<User> retrievedUser = userService.getUser(requestingUser, "testUserService");

        assertTrue(retrievedUser.isPresent());
        assertEquals("testUserService", retrievedUser.get().getUsername());
    }


}
