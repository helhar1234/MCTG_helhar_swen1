package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.dto.TokenRequest;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.session.SessionRepository;
import at.technikum.apps.mtcg.repository.user.UserRepository;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionServiceTest {

    @Test
    void authenticateRequestShouldReturnUserForValidToken() {
        // Mock dependencies and create an instance of SessionService
        UserRepository mockedUserRepository = mock(UserRepository.class);
        SessionRepository mockedSessionRepository = mock(SessionRepository.class);
        HashingService mockedHashingService = mock(HashingService.class);
        Request mockedRequest = mock(Request.class);

        SessionService service = new SessionService(mockedUserRepository, mockedSessionRepository, mockedHashingService);

        // Define a valid token and the expected user
        String validToken = "validToken";
        User expectedUser = new User("userId", "username", "hashedPassword");

        // Configure mock behaviors for request, token validation, and user retrieval
        when(mockedRequest.getAuthenticationHeader()).thenReturn("Bearer " + validToken);
        when(mockedSessionRepository.authenticateToken(validToken)).thenReturn(true);
        when(mockedSessionRepository.findByToken(validToken)).thenReturn(Optional.of(expectedUser));

        // Call the method under test and assert that it returns the expected user
        User actualUser = service.authenticateRequest(mockedRequest);
        assertEquals(expectedUser.getId(), actualUser.getId());
    }

    @Test
    void authenticateRequestShouldThrowExceptionForInvalidToken() {
        // Mock dependencies and create an instance of SessionService
        UserRepository mockedUserRepository = mock(UserRepository.class);
        SessionRepository mockedSessionRepository = mock(SessionRepository.class);
        HashingService mockedHashingService = mock(HashingService.class);
        Request mockedRequest = mock(Request.class);

        SessionService service = new SessionService(mockedUserRepository, mockedSessionRepository, mockedHashingService);

        // Define an invalid token
        String invalidToken = "invalidToken";

        // Configure mock behaviors for request and token validation
        when(mockedRequest.getAuthenticationHeader()).thenReturn("Bearer " + invalidToken);
        when(mockedSessionRepository.authenticateToken(invalidToken)).thenReturn(false);

        // Assert that an HttpStatusException is thrown with a specific status code
        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> service.authenticateRequest(mockedRequest)
        );
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void getTokenShouldReturnTokenForValidCredentials() {
        // Mock dependencies and create an instance of SessionService
        UserRepository mockedUserRepository = mock(UserRepository.class);
        SessionRepository mockedSessionRepository = mock(SessionRepository.class);
        HashingService mockedHashingService = mock(HashingService.class);

        SessionService service = new SessionService(mockedUserRepository, mockedSessionRepository, mockedHashingService);

        // Define a token request and the user with valid credentials
        TokenRequest tokenRequest = new TokenRequest("username", "password");
        User user = new User("userId", "username", "hashedPassword");

        // Configure mock behaviors for user retrieval, password comparison, and token generation
        when(mockedUserRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(mockedHashingService.compareHash("password", "hashedPassword")).thenReturn(true);
        when(mockedSessionRepository.generateToken(user)).thenReturn(Optional.of("generatedToken"));

        // Call the method under test and assert that it returns the expected token
        Optional<String> token = service.getToken(tokenRequest);
        assertTrue(token.isPresent());
        assertEquals("generatedToken", token.get());
    }

}
