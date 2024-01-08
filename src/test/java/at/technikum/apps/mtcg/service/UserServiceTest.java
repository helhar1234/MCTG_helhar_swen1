package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Test
    void shouldSetUserId_whenCreateUser() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);
        User user = new User("MaxMustermann", "max_pw");

        // Mocking to return Optional of the user
        when(userRepository.saveUser(any())).thenReturn(Optional.of(user));

        // Act
        User answer = userService.createUser(user).orElseThrow();

        // Assert
        assertNotEquals("", answer.getId());
        assertEquals("MaxMustermann", answer.getUsername());
        assertEquals("max_pw", answer.getPassword());
    }

    @Test
    void shouldCallUserRepository_whenSaveUser() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);
        User user = new User("MaximeMusterfrau", "maxime_pw");

        // Act
        userService.createUser(user);

        // Assert
        verify(userRepository, times(1)).saveUser(user);
    }

    @Test
    void shouldReturnEmptyOptional_whenUsernameExists() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);
        String existingUsername = "ExistingUser";
        User user = new User(existingUsername, "password");

        // Simulate user creation
        when(userRepository.isUsernameExists(existingUsername)).thenReturn(false);
        when(userRepository.saveUser(any(User.class))).thenReturn(Optional.of(user));

        // Act - Create the user for the first time
        Optional<User> firstCreationResult = userService.createUser(user);

        // Mocking to simulate existing username for subsequent creation attempts
        when(userRepository.isUsernameExists(existingUsername)).thenReturn(true);

        // Act - Attempt to create the same user again
        Optional<User> secondCreationResult = userService.createUser(user);

        // Assert
        assertTrue(firstCreationResult.isPresent()); // First creation should be successful
        assertTrue(secondCreationResult.isEmpty());  // Second creation should fail due to existing username
    }

    @Test
    void shouldHandleNullUserOnCreate() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        // Act and Assert
        assertThrows(NullPointerException.class, () -> userService.createUser(null));
    }

}
