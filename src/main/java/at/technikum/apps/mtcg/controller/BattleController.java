package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.customExceptions.NotFoundException;
import at.technikum.apps.mtcg.customExceptions.UnauthorizedException;
import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.BattleService;
import at.technikum.apps.mtcg.service.DeckService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLException;

// TODO: ADD COMMENTS & MAYBE USE ADDITIONAL FUNCTION FOR TOKEN AUTHENTIFICATION
// TODO: ADD ELO Abfrage
// TODO: ADD game feature
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

    public BattleController(BattleService battleService, SessionService sessionService, UserService userService, DeckService deckService) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.battleService = battleService;
        this.deckService = deckService;
    }

    private Response battle(Request request) {
        try {
            User player = sessionService.authenticateRequest(request);

            if (!deckService.hasDeckSet(player.getId())) {
                return ResponseHelper.conflictResponse("Player " + player.getUsername() + " has no deck set up");
            }

            BattleResult battleResult = battleService.battle(player);
            if (battleResult.getStatus().equals("no_opponent")) {
                return ResponseHelper.okResponse("No opponent found for battle - Try again later:)");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            String responseBody = objectMapper.writeValueAsString(battleResult);
            return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, responseBody);
        } catch (UnauthorizedException | NotFoundException e) {
            return ResponseHelper.unauthorizedResponse(e.getMessage());
        } catch (SQLException e) {
            // Hier könnte mehr Kontext hinzugefügt werden, um die Fehlerursache besser zu verstehen.
            return ResponseHelper.internalServerErrorResponse("Database error during battle: " + e.getMessage());
        } catch (RuntimeException e) {
            // Statt RuntimeException könnten Sie spezifischere Exceptions fangen, falls möglich.
            return ResponseHelper.internalServerErrorResponse("Runtime error during battle: " + e.getMessage());
        } catch (Exception e) {
            // Generische Exception als letztes Sicherheitsnetz.
            return ResponseHelper.internalServerErrorResponse("Unexpected error during battle: " + e.getMessage());
        }
    }


}
