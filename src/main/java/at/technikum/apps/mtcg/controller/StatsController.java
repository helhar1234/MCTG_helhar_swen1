package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.User;
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

    /**
     * Retrieves the statistics (ELO, wins, total battles played) for a user and returns them in a JSON formatted response.
     *
     * @param request The HTTP request containing the user's information.
     * @return A Response object containing the user's statistics in JSON format or an error message.
     */
    private Response getStats(Request request) {
        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        // Retrieve the statistics for the authenticated user
        Map<String, Object> userStats = statsService.getUserStats(requester);

        // Prepare to convert the user's statistics to a JSON string
        String userStatsJson;
        try {
            // Create an ObjectMapper instance for JSON processing
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert the user's statistics to a JSON string
            userStatsJson = objectMapper.writeValueAsString(userStats);
        } catch (JsonProcessingException e) {
            // In case of JSON processing errors, return a bad request response with error details
            return ResponseHelper.badRequestResponse("Error parsing stats data: " + e.getMessage());
        }

        // Create and return a successful response with the user's statistics in JSON format
        return ResponseHelper.okResponse(userStatsJson, HttpContentType.APPLICATION_JSON);
    }


}
