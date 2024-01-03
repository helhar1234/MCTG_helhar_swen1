package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.CardService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public class CardController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/cards") || route.startsWith("/deck");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/cards")) {
            switch (request.getMethod()) {
                case "GET":
                    return getUserCards(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        } else if (request.getRoute().equals("/deck")) {
            switch (request.getMethod()) {
                case "GET":
                    return getUserDeck(request);
                case "PUT":
                    return createUserDeck(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private final UserService userService;
    private final SessionService sessionService;
    private final CardService cardService;

    public CardController() {
        this.userService = new UserService();
        this.sessionService = new SessionService();
        this.cardService = new CardService();
    }

    private Response createUserDeck(Request request) {
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
            if (user.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: User does not exist");
            }

            // Extract card IDs from the request
            ObjectMapper objectMapper = new ObjectMapper();
            String[] cardIds = objectMapper.readValue(request.getBody(), String[].class);
            if (cardIds == null || cardIds.length != 4) {
                return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Bad Request: Exactly four card IDs are required");
            }

            // Check if each card is in the user's stack
            for (String cardId : cardIds) {
                boolean inStack = cardService.isCardInStack(user.get().getId(), cardId);
                if (!inStack) {
                    return new Response(HttpStatus.FORBIDDEN, HttpContentType.TEXT_PLAIN, "Forbidden: One or more cards do not belong to the user");
                }
            }
            boolean isReset = cardService.resetDeck(user.get().getId());
            if (!isReset) {
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, HttpContentType.TEXT_PLAIN, "Internal Server Error: Unable to update the deck");
            }

            // Add cards to the user's deck
            for (String cardId : cardIds) {
                boolean added = cardService.addCardToDeck(user.get().getId(), cardId);
                if (!added) {
                    // Handle the case where adding a card to the deck fails
                    return new Response(HttpStatus.INTERNAL_SERVER_ERROR, HttpContentType.TEXT_PLAIN, "Internal Server Error: Unable to update the deck");
                }
            }

            return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "The deck has been successfully configured");

        } catch (Exception e) {
            System.out.println("Error creating user deck: " + e.getMessage());
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Error processing deck creation request");
        }
    }


    private Response getUserDeck(Request request) {
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
            if (user.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: User does not exist");
            }

            // Retrieve the user's cards
            Card[] cards = cardService.getUserDeckCards(user.get().getId());
            if (cards == null || cards.length == 0) {
                // The user exists but has no cards
                return new Response(HttpStatus.NO_CONTENT, HttpContentType.TEXT_PLAIN, "The request was fine, but the user doesn't have any cards");
            }

            // Respond with the user's cards in JSON format
            ObjectMapper objectMapper = new ObjectMapper();
            String cardsJson = objectMapper.writeValueAsString(cards);
            return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, cardsJson);

        } catch (Exception e) {
            System.out.println("Error retrieving user's cards: " + e.getMessage());
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Error retrieving user's cards");
        }
    }
    private Response getUserCards(Request request) {
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
            if (user.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: User does not exist");
            }

            // Retrieve the user's cards
            Card[] cards = cardService.getUserCards(user.get().getId());
            if (cards == null || cards.length == 0) {
                // The user exists but has no cards
                return new Response(HttpStatus.NO_CONTENT, HttpContentType.TEXT_PLAIN, "The request was fine, but the user doesn't have any cards");
            }

            // Respond with the user's cards in JSON format
            ObjectMapper objectMapper = new ObjectMapper();
            String cardsJson = objectMapper.writeValueAsString(cards);
            return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, cardsJson);

        } catch (Exception e) {
            System.out.println("Error retrieving user's cards: " + e.getMessage());
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Error retrieving user's cards");
        }
    }

}
