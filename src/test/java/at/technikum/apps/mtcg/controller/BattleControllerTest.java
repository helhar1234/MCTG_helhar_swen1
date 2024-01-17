package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.BattleService;
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

public class BattleControllerTest {
    @Test
    void BattleControllerSupportsCorrectRoute() {
        BattleController controller = new BattleController(null, null);
        assertTrue(controller.supports("/battles"));
    }


    @Test
    void shouldHandleSuccessfulBattleRequest() {
        // Mock Services
        BattleService mockBattleService = mock(BattleService.class);
        SessionService mockSessionService = mock(SessionService.class);

        BattleController controller = new BattleController(mockBattleService, mockSessionService);

        // Manually create POST Request to start a battle
        Request postRequest = new Request();
        postRequest.setRoute("/battles");
        postRequest.setMethod(HttpMethod.POST);

        // Mock the behavior of the services
        User requester = new User("testuser", "password");
        when(mockSessionService.authenticateRequest(any(Request.class))).thenReturn(requester);

        BattleResult mockBattleResult = new BattleResult();
        when(mockBattleService.battle(any(User.class))).thenReturn(mockBattleResult);

        // Call function
        Response response = controller.handle(postRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
    }

}
