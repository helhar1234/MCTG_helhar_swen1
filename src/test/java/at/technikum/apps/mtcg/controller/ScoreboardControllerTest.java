package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.dto.UserStats;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.ScoreboardService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.server.http.HttpMethod;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScoreboardControllerTest {
    @Test
    void SessionControllerSupportsCorrectRoute() {
        ScoreboardController controller = new ScoreboardController(null, null);
        assertTrue(controller.supports("/scoreboard"));
    }

    @Test
    void shouldHandleSuccessfulGetScoreboardRequest() {
        // Mock Services
        ScoreboardService mockScoreboardService = mock(ScoreboardService.class);
        SessionService mockSessionService = mock(SessionService.class);

        ScoreboardController controller = new ScoreboardController(mockScoreboardService, mockSessionService);

        // Manually create GET Request for scoreboard
        Request getRequest = new Request();
        getRequest.setRoute("/scoreboard");
        getRequest.setMethod(HttpMethod.GET);

        // Mock the behavior of the services
        User requester = new User("testuser", "password");
        when(mockSessionService.authenticateRequest(any(Request.class))).thenReturn(requester);

        UserStats[] mockScoreboard = new UserStats[]{};
        when(mockScoreboardService.getScoreboard(any(User.class))).thenReturn(mockScoreboard);

        // Call function
        Response response = controller.handle(getRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
    }

}
