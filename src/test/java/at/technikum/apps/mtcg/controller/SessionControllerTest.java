package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.dto.TokenRequest;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.server.http.HttpMethod;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SessionControllerTest {
    @Test
    void SessionControllerSupportsCorrectRoute() {
        SessionController controller = new SessionController(null);
        assertTrue(controller.supports("/sessions"));
    }

    @Test
    void shouldHandleSuccessfulLoginRequest() {
        // Mock SessionService
        SessionService mockSessionService = mock(SessionService.class);

        SessionController controller = new SessionController(mockSessionService);

        // Manually create POST Request with login credentials
        Request postRequest = new Request();
        postRequest.setRoute("/sessions");
        postRequest.setMethod(HttpMethod.POST);
        String loginJson = "{\"username\":\"user\", \"password\":\"pass\"}";
        postRequest.setBody(loginJson);

        // Mock the behavior of the service
        when(mockSessionService.getToken(any(TokenRequest.class))).thenReturn(Optional.of("mockToken"));

        // Call function
        Response response = controller.handle(postRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
    }

}
