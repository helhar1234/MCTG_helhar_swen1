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

// TODO: ADD COMMENTS & MAYBE USE ADDITIONAL FUNCTION FOR TOKEN AUTHENTIFICATION
// TODO: MAKE DECK CONTROLLER SEPERATE
public class CardController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/cards") || route.startsWith("/deck");
    }

    @Override
    public Response handle(Request request) {
        // Extract the base route without query parameters
        String baseRoute = request.getRoute().split("\\?")[0];

        if (baseRoute.equals("/cards")) {
            switch (request.getMethod()) {
                case "GET":
                    return getUserCards(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        } else if (baseRoute.equals("/deck")) {
            switch (request.getMethod()) {
                case "GET":
                    // Check for query parameters
                    String queryParams = request.getRoute().contains("?") ? request.getRoute().split("\\?")[1] : "";
                    return getUserDeck(request, queryParams);
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


    private Response getUserDeck(Request request, String queryParams) {
        boolean isPlainFormat = queryParams.equals("format=plain");
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

            if (isPlainFormat) {
                // Return a plain text representation of the deck
                String plainDeck = convertDeckToPlainText(cards);
                return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, plainDeck);
            } else {
                // Return the default JSON representation
                ObjectMapper objectMapper = new ObjectMapper();
                String deckJson = objectMapper.writeValueAsString(cards);
                return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, deckJson);
            }

        } catch (Exception e) {
            System.out.println("Error retrieving user's cards: " + e.getMessage());
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Error retrieving user's cards");
        }
    }
    private String convertDeckToPlainText(Card[] deckCards) {
        // Convert the array of Cards into a plain text string
        StringBuilder plainTextBuilder = new StringBuilder();
        for (Card card : deckCards) {
            plainTextBuilder.append("Card Id: ").append(card.getId())
                    .append(", Name: ").append(card.getName())
                    .append(", Damage ").append(card.getDamage())
                    .append(", Type ").append(card.getCardType())
                    .append(", Element ").append(card.getElementType())
                    .append("\n");
        }
        return plainTextBuilder.toString();
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
