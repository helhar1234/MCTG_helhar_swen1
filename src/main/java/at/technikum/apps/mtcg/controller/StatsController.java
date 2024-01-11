package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.StatsService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class StatsController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/stats");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/stats")) {
            switch (request.getMethod()) {
                case "GET":
                    return getStats(request);
            }
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private final UserService userService;
    private final SessionService sessionService;
    private final StatsService statsService;

    public StatsController(StatsService statsService, SessionService sessionService, UserService userService) {
        this.statsService = statsService;
        this.sessionService = sessionService;
        this.userService = userService;
    }

    private Response getStats(Request request) {
        Map<String, Object> userStats = statsService.getUserStats(request);
        String userStatsJson;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            userStatsJson = objectMapper.writeValueAsString(userStats);
        } catch (JsonProcessingException e) {
            return ResponseHelper.badRequestResponse("Error parsing stats data: " + e.getMessage());
        }
        return ResponseHelper.okResponse(userStatsJson, HttpContentType.APPLICATION_JSON);
    }


}
