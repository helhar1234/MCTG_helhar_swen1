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

// TODO: ADD COMMENTS & MAYBE USE ADDITIONAL FUNCTION FOR TOKEN AUTHENTIFICATION
// TODO: MAKE DECK CONTROLLER SEPERATE
public class CardController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/cards");
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
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private final CardService cardService;
    private final SessionService sessionService;
    private final UserService userService;

    public CardController(CardService cardService, SessionService sessionService, UserService userService) {
        this.cardService = cardService;
        this.sessionService = sessionService;
        this.userService = userService;
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

            Optional<User> user = sessionService.getUserByToken(token);
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
