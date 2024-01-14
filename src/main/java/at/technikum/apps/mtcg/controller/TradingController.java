package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.TradeRequest;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.TradingService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class TradingController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/tradings");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/tradings")) {
            switch (request.getMethod()) {
                case "GET":
                    return getTradings(request);
                case "POST":
                    return createTrading(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        }
        String[] routeParts = request.getRoute().split("/");
        String tradingDealId = routeParts[2];

        switch (request.getMethod()) {
            case "POST":
                return executeTrade(request, tradingDealId);
            case "DELETE":
                return deleteTrading(request, tradingDealId);
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private final TradingService tradingService;
    private final SessionService sessionService;

    public TradingController(TradingService tradingService, SessionService sessionService) {
        this.tradingService = tradingService;
        this.sessionService = sessionService;
    }

    /**
     * Executes a trading deal based on the provided trading ID and offered card ID.
     *
     * @param request   The HTTP request containing the trading information.
     * @param tradingId The ID of the trading deal to be executed.
     * @return A Response object indicating the success or failure of the trading execution.
     */
    public Response executeTrade(Request request, String tradingId) {

        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        String offeredCardId;
        try {
            // Create an ObjectMapper instance for JSON processing
            ObjectMapper objectMapper = new ObjectMapper();

            // Parse the offered card ID from the request body
            JsonNode jsonNode = objectMapper.readTree(request.getBody());
            offeredCardId = jsonNode.asText();
        } catch (JsonProcessingException e) {
            // Return a bad request response in case of JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        }

        // Execute the trade using the provided trading ID and offered card ID
        boolean isTradeExecuted = tradingService.trade(requester, tradingId, offeredCardId);

        // Return a response indicating the trade was successfully executed
        return ResponseHelper.okResponse("Trading deal successfully executed");
    }


    /**
     * Deletes a trading deal based on the provided trading ID.
     *
     * @param request   The HTTP request containing the user's information.
     * @param tradingId The ID of the trading deal to be deleted.
     * @return A Response object indicating the success or failure of the trading deletion.
     */
    public Response deleteTrading(Request request, String tradingId) {
        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        // Delete the trade using the provided trading ID
        boolean isDeleted = tradingService.deleteTrade(requester, tradingId);

        // Return a response indicating the trade was successfully deleted
        return ResponseHelper.okResponse("Trading deal successfully deleted!");
    }


    /**
     * Creates a new trading deal based on the user's request.
     *
     * @param request The HTTP request containing the trading creation information.
     * @return A Response object indicating the success or failure of the trading creation.
     */
    private Response createTrading(Request request) {
        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        TradeRequest tradeRequest;
        try {
            // Create an ObjectMapper instance for JSON processing
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert the JSON body of the request to a TradeRequest object
            tradeRequest = objectMapper.readValue(request.getBody(), TradeRequest.class);
        } catch (JsonProcessingException e) {
            // Return a bad request response in case of JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        }

        // Create a new trade using the provided trade request
        boolean isCreated = tradingService.createTrade(requester, tradeRequest);

        // Return a response indicating the trade was successfully created
        return ResponseHelper.createdResponse("Trading deal successfully created!");
    }


    /**
     * Retrieves all trading deals available to the user.
     *
     * @param request The HTTP request containing the user's information.
     * @return A Response object containing the list of trading deals in JSON format or an error message.
     */
    private Response getTradings(Request request) {
        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        // Retrieve all trades available to the user
        TradeRequest[] trades = tradingService.getAllTrades(requester);

        String tradesJson;
        try {
            // Create an ObjectMapper instance for JSON processing
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert the array of trades to a JSON string
            tradesJson = objectMapper.writeValueAsString(trades);
        } catch (JsonProcessingException e) {
            // Return a bad request response in case of JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing trading data: " + e.getMessage());
        }

        // Return a response with the list of trades in JSON format
        return ResponseHelper.okResponse(tradesJson, HttpContentType.APPLICATION_JSON);
    }


}
