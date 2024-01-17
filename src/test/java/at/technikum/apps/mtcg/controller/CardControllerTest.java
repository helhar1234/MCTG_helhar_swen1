package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.CardService;
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

public class CardControllerTest {
    @Test
    void CardControllerSupportsCorrectRoute() {
        CardController controller = new CardController(null, null);
        assertTrue(controller.supports("/cards"));
    }

    @Test
    void shouldHandleSuccessfulGetUserCardsRequest() {
        // Mock Services
        CardService mockCardService = mock(CardService.class);
        SessionService mockSessionService = mock(SessionService.class);

        CardController controller = new CardController(mockCardService, mockSessionService);

        // Manually create GET Request to retrieve cards
        Request getRequest = new Request();
        getRequest.setRoute("/cards");
        getRequest.setMethod(HttpMethod.GET);

        // Mock the behavior of the services
        User requester = new User("testuser", "password");
        when(mockSessionService.authenticateRequest(any(Request.class))).thenReturn(requester);

        Card[] mockCards = new Card[]{ /* Initialize Card array */};
        when(mockCardService.getCards(any(User.class))).thenReturn(mockCards);

        // Call function
        Response response = controller.handle(getRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
    }


}
