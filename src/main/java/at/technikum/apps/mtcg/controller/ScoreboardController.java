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

    private Response getScoreboard(Request request) {
        User requester = sessionService.authenticateRequest(request);
        UserStats[] scoreboard = scoreboardService.getScoreboard(requester);
        String scoreboardJson;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            scoreboardJson = objectMapper.writeValueAsString(scoreboard);

        } catch (JsonProcessingException e) {
            return ResponseHelper.badRequestResponse("Error parsing scoreboard data: " + e.getMessage());
        }
        return ResponseHelper.okResponse(scoreboardJson, HttpContentType.APPLICATION_JSON);
    }

}
