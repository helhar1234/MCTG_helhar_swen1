package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.TransactionsService;
import at.technikum.server.http.HttpMethod;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionsControllerTest {
    @Test
    void TransactionsControllerSupportsCorrectRoute() {
        TransactionsController controller = new TransactionsController(null, null);
        assertTrue(controller.supports("/transactions/packages"));
    }

    @Test
    void shouldHandleCorrectRequest() {
        // Mocks
        TransactionsService mockTransactionsService = mock(TransactionsService.class);
        SessionService mockSessionService = mock(SessionService.class);

        TransactionsController controller = new TransactionsController(mockTransactionsService, mockSessionService);

        // Create POST Request with a valid user JSON
        Request postRequest = new Request();
        postRequest.setRoute("/transactions/packages");
        postRequest.setMethod(HttpMethod.POST);
        postRequest.setBody("id");

        // Mock the behavior of the service
        User requester = new User("testuser", "password");
        when(mockSessionService.authenticateRequest(any(Request.class))).thenReturn(requester);
        when(mockTransactionsService.makeTransaction(any(User.class), eq("id"))).thenReturn(true);

        // Call function
        Response response = controller.handle(postRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode()); // Angenommen, CREATED ist der erwartete Statuscode
    }
}
