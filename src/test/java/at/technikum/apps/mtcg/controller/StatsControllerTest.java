package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.StatsService;
import at.technikum.server.http.HttpMethod;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatsControllerTest {
    @Test
    void StatsControllerSupportsCorrectRoute() {
        StatsController controller = new StatsController(null, null);
        assertTrue(controller.supports("/stats"));
    }

    @Test
    void shouldHandleCorrectRequest() {
        // Mocks
        StatsService mockStatsService = mock(StatsService.class);
        SessionService mockSessionService = mock(SessionService.class);

        StatsController controller = new StatsController(mockStatsService, mockSessionService);

        // Create GET Request
        Request getRequest = new Request();
        getRequest.setRoute("/stats");
        getRequest.setMethod(HttpMethod.GET);

        // Mock the behavior of the services
        User requester = new User("testuser", "password");
        when(mockSessionService.authenticateRequest(any(Request.class))).thenReturn(requester);

        // Creating a mock response for user stats
        Map<String, Object> mockUserStats = Map.of(
                "eloRating", 1200, // Example ELO rating
                "wins", 10,       // Example total number of wins
                "totalBattles", 20 // Example total number of battles played
        );
        when(mockStatsService.getUserStats(any(User.class))).thenReturn(mockUserStats);

        // Call function
        Response response = controller.handle(getRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
    }
}
