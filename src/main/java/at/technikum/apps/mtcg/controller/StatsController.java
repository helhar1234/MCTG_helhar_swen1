package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.StatsService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

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

    public StatsController() {
        this.userService = new UserService();
        this.sessionService = new SessionService();
        this.statsService = new StatsService();
    }

    private Response getStats(Request request) {
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

            // Get the user
            Optional<User> user = sessionService.getUserByToken(token);
            if (user.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: User does not exist");
            }

            int wins = statsService.getUserWins(user.get().getId());
            int battles = statsService.getUserBattles(user.get().getId());

            Map<String, Object> userStats = Map.of(
                    "eloRating", user.get().getEloRating(),
                    "wins", wins,
                    "totalBattles", battles
            );

            ObjectMapper objectMapper = new ObjectMapper();
            String userStatsJson = objectMapper.writeValueAsString(userStats);

            return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, userStatsJson);


        } catch (Exception e) {
            System.out.println("Error retrieving user stats: " + e.getMessage());
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, HttpContentType.TEXT_PLAIN, "Internal server error while processing user stats.");
        }
    }

}
