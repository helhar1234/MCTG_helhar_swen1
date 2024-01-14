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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    @Test
    void authenticateRequestShouldReturnUserForValidToken() {
        UserRepository mockedUserRepository = mock(UserRepository.class);
        SessionRepository mockedSessionRepository = mock(SessionRepository.class);
        HashingService mockedHashingService = mock(HashingService.class);
        Request mockedRequest = mock(Request.class);

        SessionService service = new SessionService(mockedUserRepository, mockedSessionRepository, mockedHashingService);
        String validToken = "validToken";
        User expectedUser = new User("userId", "username", "hashedPassword");

        when(mockedRequest.getAuthenticationHeader()).thenReturn("Bearer " + validToken);
        when(mockedSessionRepository.authenticateToken(validToken)).thenReturn(true);
        when(mockedSessionRepository.findByToken(validToken)).thenReturn(Optional.of(expectedUser));

        User actualUser = service.authenticateRequest(mockedRequest);

        assertEquals(expectedUser.getId(), actualUser.getId());
    }

    @Test
    void authenticateRequestShouldThrowExceptionForInvalidToken() {
        UserRepository mockedUserRepository = mock(UserRepository.class);
        SessionRepository mockedSessionRepository = mock(SessionRepository.class);
        HashingService mockedHashingService = mock(HashingService.class);
        Request mockedRequest = mock(Request.class);

        SessionService service = new SessionService(mockedUserRepository, mockedSessionRepository, mockedHashingService);
        String invalidToken = "invalidToken";

        when(mockedRequest.getAuthenticationHeader()).thenReturn("Bearer " + invalidToken);
        when(mockedSessionRepository.authenticateToken(invalidToken)).thenReturn(false);

        HttpStatusException exception = assertThrows(
                HttpStatusException.class,
                () -> service.authenticateRequest(mockedRequest)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void getTokenShouldReturnTokenForValidCredentials() {
        UserRepository mockedUserRepository = mock(UserRepository.class);
        SessionRepository mockedSessionRepository = mock(SessionRepository.class);
        HashingService mockedHashingService = mock(HashingService.class);

        SessionService service = new SessionService(mockedUserRepository, mockedSessionRepository, mockedHashingService);
        TokenRequest tokenRequest = new TokenRequest("username", "password");
        User user = new User("userId", "username", "hashedPassword");

        when(mockedUserRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(mockedHashingService.compareHash("password", "hashedPassword")).thenReturn(true);
        when(mockedSessionRepository.generateToken(user)).thenReturn(Optional.of("generatedToken"));

        Optional<String> token = service.getToken(tokenRequest);

        assertTrue(token.isPresent());
        assertEquals("generatedToken", token.get());
    }

}
