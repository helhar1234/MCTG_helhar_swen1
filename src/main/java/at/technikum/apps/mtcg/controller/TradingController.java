package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.customExceptions.NotFoundException;
import at.technikum.apps.mtcg.customExceptions.UnauthorizedException;
import at.technikum.apps.mtcg.entity.TradeRequest;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.*;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLException;
import java.util.Optional;

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
        try {
            // Authenticate the user
            User user = sessionService.authenticateRequest(request);

            Optional<TradeRequest> trade = tradingService.getTradeById(tradingId);
            if (!trade.isPresent() || user.getId().equals(trade.get().getUserId())) {
                return ResponseHelper.notFoundResponse("Trading deal not found or user cannot trade with themselves");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(request.getBody());
            String offeredCardId = jsonNode.asText();

            if (!cardService.isCardInStack(user.getId(), offeredCardId) || deckService.isCardInDeck(user.getId(), offeredCardId)) {
                return ResponseHelper.forbiddenResponse("The offered card is not owned by the user or is locked in the deck");
            }

            if (!tradingService.meetsRequirements(trade.get(), offeredCardId)) {
                return ResponseHelper.forbiddenResponse("The offered card does not meet the trade requirements or is locked in the deck.");
            }

            boolean isTradeExecuted = tradingService.trade(user.getId(), offeredCardId, trade.get().getUserId(), trade.get().getCardToTrade(), tradingId);
            if (isTradeExecuted) {
                return ResponseHelper.okResponse("Trading deal successfully executed");
            } else {
                return ResponseHelper.conflictResponse("Issue executing the trade");
            }

        } catch (UnauthorizedException | NotFoundException e) {
            return ResponseHelper.unauthorizedResponse(e.getMessage());
        } catch (SQLException e) {
            return ResponseHelper.internalServerErrorResponse("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error executing trading request: " + e.getMessage());
            return ResponseHelper.badRequestResponse("Error executing trading request: " + e.getMessage());
        }
    }


    public Response deleteTrading(Request request, String tradingId) {
        try {
            // Authenticate the user
            User user = sessionService.authenticateRequest(request);

            // Verify if the user is associated with the trade
            if (!tradingService.isUserTrade(user.getId(), tradingId)) {
                return ResponseHelper.forbiddenResponse("The deal is not owned by the user.");
            }

            // Delete the trade
            boolean isDeleted = tradingService.deleteTrade(tradingId);
            if (isDeleted) {
                return ResponseHelper.okResponse("Trading deal successfully deleted!");
            } else {
                return ResponseHelper.notFoundResponse("The provided deal ID was not found.");
            }

        } catch (UnauthorizedException | NotFoundException e) {
            return ResponseHelper.unauthorizedResponse(e.getMessage());
        } catch (SQLException e) {
            return ResponseHelper.internalServerErrorResponse("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing delete trading request: " + e.getMessage());
            return ResponseHelper.badRequestResponse("Error processing delete trading request: " + e.getMessage());
        }
    }


    private Response createTrading(Request request) {
        try {
            // Authenticate the user
            User user = sessionService.authenticateRequest(request);

            // Deserialize request to TradeRequest
            ObjectMapper objectMapper = new ObjectMapper();
            TradeRequest tradeRequest = objectMapper.readValue(request.getBody(), TradeRequest.class);

            // Verify the card belongs to the user
            if (!cardService.isCardInStack(user.getId(), tradeRequest.getCardToTrade()) || deckService.isCardInDeck(tradeRequest.getCardToTrade(), user.getId())) {
                return ResponseHelper.forbiddenResponse("The deal contains a card that is not owned by the user or locked in the deck.");
            }

            // Create the trade
            boolean isCreated = tradingService.createTrade(tradeRequest, user.getId());
            if (isCreated) {
                return ResponseHelper.createdResponse("Trading deal successfully created!");
            } else {
                return ResponseHelper.conflictResponse("Conflict: A deal with this deal ID already exists.");
            }

        } catch (UnauthorizedException | NotFoundException e) {
            return ResponseHelper.unauthorizedResponse(e.getMessage());
        } catch (SQLException e) {
            return ResponseHelper.internalServerErrorResponse("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing create trading request: " + e.getMessage());
            return ResponseHelper.badRequestResponse("Error processing create trading request: " + e.getMessage());
        }
    }

    private Response getTradings(Request request) {
        try {
            // Authenticate the user
            User user = sessionService.authenticateRequest(request);

            TradeRequest[] trades = tradingService.getAllTrades();
            if (trades == null || trades.length == 0) {
                return ResponseHelper.noContentResponse("There are no trading deals available");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String tradesJson = objectMapper.writeValueAsString(trades);
            return ResponseHelper.okResponse(tradesJson, HttpContentType.APPLICATION_JSON);

        } catch (UnauthorizedException | NotFoundException e) {
            return ResponseHelper.unauthorizedResponse(e.getMessage());
        } catch (SQLException e) {
            return ResponseHelper.internalServerErrorResponse("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error retrieving trading deals: " + e.getMessage());
            return ResponseHelper.badRequestResponse("Error retrieving trading deals: " + e.getMessage());
        }
    }

}
