package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.TradeRequest;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.*;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// TODO: ADD COMMENTS & MAYBE USE ADDITIONAL FUNCTION FOR TOKEN AUTHENTIFICATION
// TODO: SHORTEN CODE BY USING FUNCTIONS FOR DUPLICATE LOGIC

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
    private final CardService cardService;
    private final SessionService sessionService;
    private final UserService userService;
    private final DeckService deckService;


    public TradingController(TradingService tradingService, SessionService sessionService, CardService cardService, UserService userService, DeckService deckService) {
        this.tradingService = tradingService;
        this.sessionService = sessionService;
        this.cardService = cardService;
        this.userService = userService;
        this.deckService = deckService;
    }

    public Response executeTrade(Request request, String tradingId) {
        String offeredCardId;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(request.getBody());
            offeredCardId = jsonNode.asText();
        } catch (JsonProcessingException e) {
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        }
        boolean isTradeExecuted = tradingService.trade(request, tradingId, offeredCardId);
        return ResponseHelper.okResponse("Trading deal successfully executed");

    }


    public Response deleteTrading(Request request, String tradingId) {
        boolean isDeleted = tradingService.deleteTrade(request, tradingId);
        return ResponseHelper.okResponse("Trading deal successfully deleted!");
    }


    private Response createTrading(Request request) {
        TradeRequest tradeRequest;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            tradeRequest = objectMapper.readValue(request.getBody(), TradeRequest.class);
        } catch (JsonProcessingException e) {
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        }
        boolean isCreated = tradingService.createTrade(request, tradeRequest);
        return ResponseHelper.createdResponse("Trading deal successfully created!");

    }

    private Response getTradings(Request request) {
        TradeRequest[] trades = tradingService.getAllTrades(request);
        String tradesJson;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            tradesJson = objectMapper.writeValueAsString(trades);
        } catch (JsonProcessingException e) {
            return ResponseHelper.badRequestResponse("Error parsing trading data: " + e.getMessage());
        }
        return ResponseHelper.okResponse(tradesJson, HttpContentType.APPLICATION_JSON);
    }

}
