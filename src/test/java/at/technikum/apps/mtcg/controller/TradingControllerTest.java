package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.TradeRequest;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.TradingService;
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

public class TradingControllerTest {
    @Test
    void TransactionsControllerSupportsCorrectRoute() {
        TradingController controller = new TradingController(null, null);
        assertTrue(controller.supports("/tradings"));
    }

    @Test
    void shouldHandleGetTradingsRequest() {
        // Mocks
        TradingService mockTradingService = mock(TradingService.class);
        SessionService mockSessionService = mock(SessionService.class);

        TradingController controller = new TradingController(mockTradingService, mockSessionService);

        // Create GET Request
        Request getRequest = new Request();
        getRequest.setRoute("/tradings");
        getRequest.setMethod(HttpMethod.GET);

        // Mock the behavior of the service
        User requester = new User("testuser", "password");
        when(mockSessionService.authenticateRequest(any(Request.class))).thenReturn(requester);
        TradeRequest[] mockTrades = new TradeRequest[]{new TradeRequest("id", "card", "type", 20)};
        when(mockTradingService.getAllTrades(any(User.class))).thenReturn(mockTrades);

        // Call function
        Response response = controller.handle(getRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
    }

    @Test
    void shouldHandleCreateTradingRequest() {
        // Mocks
        TradingService mockTradingService = mock(TradingService.class);
        SessionService mockSessionService = mock(SessionService.class);

        TradingController controller = new TradingController(mockTradingService, mockSessionService);

        // Create POST Request with trading data
        String tradingDataJson = "{\"id\": \"6cd85277-4590-49d4-b0cf-ba0a921faad0\", \"cardToTrade\": \"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"type\": \"monster\", \"minimumDamage\": 15}";
        Request postRequest = new Request();
        postRequest.setRoute("/tradings");
        postRequest.setMethod(HttpMethod.POST);
        postRequest.setBody(tradingDataJson);

        // Mock the behavior of the service
        User requester = new User("testuser", "password");
        when(mockSessionService.authenticateRequest(any(Request.class))).thenReturn(requester);
        when(mockTradingService.createTrade(any(User.class), any(TradeRequest.class))).thenReturn(true);

        // Call function
        Response response = controller.handle(postRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.getCode(), response.getStatusCode());
    }

    @Test
    void shouldHandleDeleteTradingRequest() {
        // Mocks
        TradingService mockTradingService = mock(TradingService.class);
        SessionService mockSessionService = mock(SessionService.class);

        TradingController controller = new TradingController(mockTradingService, mockSessionService);

        // Create DELETE Request
        Request deleteRequest = new Request();
        deleteRequest.setRoute("/tradings/trade123");
        deleteRequest.setMethod(HttpMethod.DELETE);

        // Mock the behavior of the service
        User requester = new User("testuser", "password");
        when(mockSessionService.authenticateRequest(any(Request.class))).thenReturn(requester);
        when(mockTradingService.deleteTrade(any(User.class), eq("trade123"))).thenReturn(true);

        // Call function
        Response response = controller.deleteTrading(deleteRequest, "trade123");

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
    }

}
