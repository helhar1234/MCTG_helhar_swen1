package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.dto.UserStats;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.ScoreboardService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ScoreboardController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/scoreboard");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/scoreboard")) {
            switch (request.getMethod()) {
                case "GET":
                    return getScoreboard(request);
            }
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private final UserService userService;
    private final SessionService sessionService;
    private final ScoreboardService scoreboardService;

    public ScoreboardController(ScoreboardService scoreboardService, SessionService sessionService, UserService userService) {
        this.scoreboardService = scoreboardService;
        this.sessionService = sessionService;
        this.userService = userService;
    }

    /**
     * Retrieves the scoreboard and returns it in a JSON formatted response.
     *
     * @param request The HTTP request containing the user's information.
     * @return A Response object containing the scoreboard in JSON format or an error message.
     */
    private Response getScoreboard(Request request) {
        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        // Retrieve the array of UserStats representing the scoreboard
        UserStats[] scoreboard = scoreboardService.getScoreboard(requester);

        // Prepare to convert the scoreboard to a JSON string
        String scoreboardJson;
        try {
            // Create an ObjectMapper instance for JSON processing
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert the scoreboard to a JSON string
            scoreboardJson = objectMapper.writeValueAsString(scoreboard);
        } catch (JsonProcessingException e) {
            // In case of JSON processing errors, return a bad request response with error details
            return ResponseHelper.badRequestResponse("Error parsing scoreboard data: " + e.getMessage());
        }

        // Create and return a successful response with the scoreboard in JSON format
        return ResponseHelper.okResponse(scoreboardJson, HttpContentType.APPLICATION_JSON);
    }


}
