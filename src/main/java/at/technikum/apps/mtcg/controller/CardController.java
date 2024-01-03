package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.Card;
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
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "createUserDeck");
    }

    private Response getUserDeck(Request request) {

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
