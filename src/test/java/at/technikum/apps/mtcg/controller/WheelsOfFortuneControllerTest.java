package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.dto.WheelPrize;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.WheelOfFortuneService;
import at.technikum.server.http.HttpMethod;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WheelsOfFortuneControllerTest {

    @Test
    void WheelControllerSupportsCorrectRoute() {
        WheelOfFortuneController controller = new WheelOfFortuneController(null, null);
        assertTrue(controller.supports("/wheel"));
    }

    @Test
    void shouldHandleCorrectRequestMethod() {
        // Mocks
        WheelOfFortuneService mockWheelOfFortuneService = mock(WheelOfFortuneService.class);
        SessionService mockSessionService = mock(SessionService.class);

        WheelOfFortuneController controller = new WheelOfFortuneController(mockWheelOfFortuneService, mockSessionService);

        // Create POST Request
        Request postRequest = new Request();
        postRequest.setRoute("/wheel");
        postRequest.setMethod(HttpMethod.POST);

        User testUser = new User("testUser", "testPw");
        // Mock the behavior of the service
        when(mockSessionService.authenticateRequest(any())).thenReturn(testUser);
        when(mockWheelOfFortuneService.spin(any())).thenReturn(new WheelPrize(testUser, "COIN", 5, null));

        // Call function
        Response response = controller.handle(postRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
    }


}
