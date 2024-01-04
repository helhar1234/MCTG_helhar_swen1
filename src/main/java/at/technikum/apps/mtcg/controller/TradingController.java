package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.TradeRequest;
import at.technikum.apps.mtcg.service.CardService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.TradingService;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

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


    public TradingController() {
        this.tradingService = new TradingService();
        this.cardService = new CardService();
        this.sessionService = new SessionService();
        this.userService = new UserService();
    }

    public Response executeTrade(Request request, String tradingId) {
        try {
            String authHeader = request.getAuthenticationHeader();
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: No token provided");
            }
            String[] authParts = authHeader.split("\\s+");
            String token = authParts[1];

            boolean isAuthenticated = sessionService.authenticateToken(token);
            if (!isAuthenticated) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: Invalid token");
            }

            Optional<User> user = userService.getUserByToken(token);
            if (!user.isPresent()) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: User does not exist");
            }

            Optional<TradeRequest> trade = tradingService.getTradeById(tradingId);
            if (!trade.isPresent() || user.get().getId().equals(trade.get().getUserId())) {
                return new Response(HttpStatus.NOT_FOUND, HttpContentType.TEXT_PLAIN, "Trading deal not found or user cannot trade with themselves");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(request.getBody());
            String offeredCardId = jsonNode.asText();

            if (!cardService.isCardInStack(user.get().getId(), offeredCardId) || cardService.isCardInDeck(user.get().getId(), offeredCardId)) {
                return new Response(HttpStatus.FORBIDDEN, HttpContentType.TEXT_PLAIN, "Forbidden: The offered card is not owned by the user or is locked in the deck");
            }

            if (!tradingService.meetsRequirements(trade.get(), offeredCardId)) {
                return new Response(HttpStatus.FORBIDDEN, HttpContentType.TEXT_PLAIN, "Forbidden: The offered card is not owned by the user, or the requirements are not met (Type, MinimumDamage), or the offered card is locked in the deck.");
            }

            boolean isTradeExecuted = tradingService.trade(user.get().getId(), offeredCardId, trade.get().getUserId(), trade.get().getCardToTrade(), tradingId);
            if (isTradeExecuted) {
                return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "Trading deal successfully executed");
            } else {
                return new Response(HttpStatus.CONFLICT, HttpContentType.TEXT_PLAIN, "Conflict: Issue executing the trade");
            }
        } catch (Exception e) {
            System.out.println("Error executing trading request: " + e.getMessage());
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Error executing trading request");
        }
    }

    public Response deleteTrading(Request request, String tradingId) {
        try {
            // Extract the token from the Authorization header
            String authHeader = request.getAuthenticationHeader();
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: No token provided");
            }
            String[] authParts = authHeader.split("\\s+");
            String token = authParts[1];

            // Authenticate the token and get the user
            boolean isAuthenticated = sessionService.authenticateToken(token);
            if (!isAuthenticated) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: Invalid token");
            }

            Optional<User> user = userService.getUserByToken(token);
            if (!user.isPresent()) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: User does not exist");
            }

            // Verify if the user is associated with the trade
            if (!tradingService.isUserTrade(user.get().getId(), tradingId)) {
                return new Response(HttpStatus.FORBIDDEN, HttpContentType.TEXT_PLAIN, "Forbidden: The deal contains a card that is not owned by the user.");
            }

            // Delete the trade
            boolean isDeleted = tradingService.deleteTrade(tradingId);
            if (isDeleted) {
                return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "Trading deal successfully deleted!");
            } else {
                return new Response(HttpStatus.NOT_FOUND, HttpContentType.TEXT_PLAIN, "The provided deal ID was not found.");
            }
        } catch (Exception e) {
            System.out.println("Error processing delete trading request: " + e.getMessage());
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Error processing delete trading request");
        }
    }

    private Response createTrading(Request request) {
        try {
            // Extract the token from the Authorization header
            String authHeader = request.getAuthenticationHeader();
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: No token provided");
            }
            String[] authParts = authHeader.split("\\s+");
            String token = authParts[1];

            // Authenticate the token and get the user
            boolean isAuthenticated = sessionService.authenticateToken(token);
            if (!isAuthenticated) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: Invalid token");
            }

            Optional<User> user = userService.getUserByToken(token);
            if (!user.isPresent()) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: User does not exist");
            }

            // Deserialize request to TradeRequest
            ObjectMapper objectMapper = new ObjectMapper();
            TradeRequest tradeRequest = objectMapper.readValue(request.getBody(), TradeRequest.class);


            // Verify the card belongs to the user
            if (!cardService.isCardInStack(user.get().getId(), tradeRequest.getCardToTrade()) || cardService.isCardInDeck(tradeRequest.getCardToTrade(), user.get().getId())) {
                return new Response(HttpStatus.FORBIDDEN, HttpContentType.TEXT_PLAIN, "Forbidden: The deal contains a card that is not owned by the user or locked in the deck.");
            }

            // Create the trade
            boolean isCreated = tradingService.createTrade(tradeRequest, user.get().getId());
            if (isCreated) {
                // User successfully created
                return new Response(HttpStatus.CREATED, HttpContentType.TEXT_PLAIN, "Trading deal successfully created!");
            } else {
                return new Response(HttpStatus.CONFLICT, HttpContentType.TEXT_PLAIN, "User with same name already registered!");
            }

        } catch (Exception e) {
            System.out.println("Error processing create trading request: " + e.getMessage());
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Conflict: A deal with this deal ID already exists.");
        }
    }

    private Response getTradings(Request request) {
        try {
            String authHeader = request.getAuthenticationHeader();
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: No token provided");
            }

            String[] authParts = authHeader.split("\\s+");
            String token = authParts[1];

            if (!sessionService.authenticateToken(token)) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: Invalid token");
            }

           TradeRequest[] trades = tradingService.getAllTrades();
            if (trades == null || trades.length == 0) {
                return new Response(HttpStatus.NO_CONTENT, HttpContentType.TEXT_PLAIN, "The request was fine, but there are no trading deals available");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String tradesJson = objectMapper.writeValueAsString(trades);
            return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, tradesJson);

        } catch (Exception e) {
            System.out.println("Error retrieving trading deals: " + e.getMessage());
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Error retrieving trading deals");
        }
    }
}
