package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.BattleService;
import at.technikum.apps.mtcg.service.DeckService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

// TODO: ADD COMMENTS & MAYBE USE ADDITIONAL FUNCTION FOR TOKEN AUTHENTIFICATION
public class BattleController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/battles");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/battles")) {
            switch (request.getMethod()) {
                case "POST":
                    return battle(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private final SessionService sessionService;
    private final UserService userService;
    private final BattleService battleService;
    private final DeckService deckService;

    public BattleController() {
        this.sessionService = new SessionService();
        this.userService = new UserService();
        this.battleService = new BattleService();
        this.deckService = new DeckService();
    }

    private Response battle(Request request) {
        try {
            // Extract the token from the Authorization header
            String authHeader = request.getAuthenticationHeader();
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorizedResponse("No token provided");
            }
            String[] authParts = authHeader.split("\\s+");
            String token = authParts[1];

            // Authenticate the token and get the player
            if (!sessionService.authenticateToken(token)) {
                return unauthorizedResponse("Invalid token");
            }

            Optional<User> playerOpt = sessionService.getUserByToken(token);
            if (playerOpt.isEmpty()) {
                return unauthorizedResponse("User does not exist");
            }
            User player = playerOpt.get();

            // Check if the player has a deck set up
            if (!deckService.hasDeckSet(player.getId())) {
                return conflictResponse("Player " + player.getUsername() + " has no deck set up");
            }

            // Block and wait for the battle result (not recommended in real applications)
            // Führe den Kampf synchron aus
            BattleResult battleResult = battleService.battle(player);
            if (battleResult == null) {
                return internalServerErrorResponse("Battle Could not be started");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String responseBody = objectMapper.writeValueAsString(battleResult);
            return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, responseBody);
        } catch (RuntimeException e) {
            return internalServerErrorResponse("Error occurred during battle: " + e.getMessage());
        } catch (Exception e) {
            return internalServerErrorResponse("An unexpected error occurred: " + e.getMessage());
        }
    }

    private Response unauthorizedResponse(String message) {
        return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: " + message);
    }

    private Response conflictResponse(String message) {
        return new Response(HttpStatus.CONFLICT, HttpContentType.TEXT_PLAIN, message);
    }

    private Response internalServerErrorResponse(String message) {
        System.out.println(message);
        return new Response(HttpStatus.INTERNAL_SERVER_ERROR, HttpContentType.TEXT_PLAIN, message);
    }


}
