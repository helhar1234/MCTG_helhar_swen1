package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.UserStats;
import at.technikum.apps.mtcg.service.ScoreboardService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
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

    public ScoreboardController() {
        this.userService = new UserService();
        this.sessionService = new SessionService();
        this.scoreboardService = new ScoreboardService();
    }

    private Response getScoreboard(Request request) {
        try {
            // Extract the token from the Authorization header
            String authHeader = request.getAuthenticationHeader();
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: No token provided");
            }
            String[] authParts = authHeader.split("\\s+");
            String token = authParts[1];

            // Authenticate the token
            boolean isAuthenticated = sessionService.authenticateToken(token);
            if (!isAuthenticated) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: Invalid token");
            }

            // Retrieve the scoreboard
            UserStats[] scoreboard = scoreboardService.getScoreboard();
            if (scoreboard == null) {
                // Handle the case where scoreboard is null
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, HttpContentType.TEXT_PLAIN, "Internal server error while processing scoreboard.");
            }

            // Respond with the scoreboard
            ObjectMapper objectMapper = new ObjectMapper();
            String scoreboardJson = objectMapper.writeValueAsString(scoreboard);
            return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, scoreboardJson);

        } catch (Exception e) {
            System.out.println("Error retrieving scoreboard: " + e.getMessage());
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, HttpContentType.TEXT_PLAIN, "Internal server error while processing scoreboard.");
        }
    }
}
