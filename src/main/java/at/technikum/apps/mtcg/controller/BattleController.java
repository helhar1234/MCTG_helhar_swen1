package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.BattleService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final BattleService battleService;

    public BattleController(BattleService battleService, SessionService sessionService) {
        this.sessionService = sessionService;
        this.battleService = battleService;
    }

    /**
     * Handles a battle request and returns the battle result.
     *
     * @param request The HTTP request containing the battle request information.
     * @return A Response object containing the battle result or an error message.
     */
    private Response battle(Request request) {
        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        // Initiate a battle for the authenticated user and get the result
        BattleResult battleResult = battleService.battle(requester);

        // Prepare to convert the BattleResult object to a JSON string
        String responseBody;
        try {
            // Create an ObjectMapper instance for JSON processing
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert the BattleResult object to a JSON string
            responseBody = objectMapper.writeValueAsString(battleResult);
        } catch (JsonProcessingException e) {
            // In case of JSON processing errors, return a bad request response with error details
            return ResponseHelper.badRequestResponse("Error parsing battle data: " + e.getMessage());
        }

        // Create and return a successful response with the battle result in JSON format
        return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, responseBody);
    }


}
