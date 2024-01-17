package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.DeckService;
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

public class DeckControllerTest {
    @Test
    void DeckControllerSupportsCorrectRoute() {
        DeckController controller = new DeckController(null, null);
        assertTrue(controller.supports("/deck"));
    }

    @Test
    void shouldHandleSuccessfulCreateUserDeckRequest() {
        // Mock Services
        DeckService mockDeckService = mock(DeckService.class);
        SessionService mockSessionService = mock(SessionService.class);

        DeckController controller = new DeckController(mockDeckService, mockSessionService);

        // Manually create PUT Request to configure a deck
        Request putRequest = new Request();
        putRequest.setRoute("/deck");
        putRequest.setMethod(HttpMethod.PUT);
        String deckJson = "[\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]";
        putRequest.setBody(deckJson);

        // Mock the behavior of the services
        User requester = new User("testuser", "password");
        when(mockSessionService.authenticateRequest(any(Request.class))).thenReturn(requester);
        when(mockDeckService.configureDeck(any(User.class), any(String[].class))).thenReturn(true);

        // Call function
        Response response = controller.handle(putRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
    }

    @Test
    void shouldHandleSuccessfulGetUserDeckRequest() {
        // Mock Services
        DeckService mockDeckService = mock(DeckService.class);
        SessionService mockSessionService = mock(SessionService.class);

        DeckController controller = new DeckController(mockDeckService, mockSessionService);

        // Manually create GET Request to retrieve a deck
        Request getRequest = new Request();
        getRequest.setRoute("/deck");
        getRequest.setMethod(HttpMethod.GET);

        // Mock the behavior of the services
        User requester = new User("testuser", "password");
        when(mockSessionService.authenticateRequest(any(Request.class))).thenReturn(requester);

        Card[] mockCards = new Card[]{};
        when(mockDeckService.getDeck(any(User.class))).thenReturn(mockCards);

        // Call function
        Response response = controller.handle(getRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
    }


}
