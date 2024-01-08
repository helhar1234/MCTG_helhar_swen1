package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.customExceptions.NotFoundException;
import at.technikum.apps.mtcg.customExceptions.UnauthorizedException;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.StatsService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLException;
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
        try {
            // Authenticate the user
            User user = sessionService.authenticateRequest(request);

            // Get user statistics
            int wins = statsService.getUserWins(user.getId());
            int battles = statsService.getUserBattles(user.getId());

            Map<String, Object> userStats = Map.of(
                    "eloRating", user.getEloRating(),
                    "wins", wins,
                    "totalBattles", battles
            );

            ObjectMapper objectMapper = new ObjectMapper();
            String userStatsJson = objectMapper.writeValueAsString(userStats);

            return ResponseHelper.okResponse(userStatsJson, HttpContentType.APPLICATION_JSON);

        } catch (UnauthorizedException | NotFoundException e) {
            return ResponseHelper.unauthorizedResponse(e.getMessage());
        } catch (SQLException e) {
            return ResponseHelper.internalServerErrorResponse("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error retrieving user stats: " + e.getMessage());
            return ResponseHelper.internalServerErrorResponse("Internal server error while processing user stats: " + e.getMessage());
        }
    }


}
