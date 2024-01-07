package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

}
