package at.technikum.apps.mtcg.controller;

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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        User requester = sessionService.authenticateRequest(request);
        BattleResult battleResult = battleService.battle(requester);
        String responseBody;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            responseBody = objectMapper.writeValueAsString(battleResult);
        } catch (JsonProcessingException e) {
            return ResponseHelper.badRequestResponse("Error parsing battle data: " + e.getMessage());
        }
        return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, responseBody);

    }


}
