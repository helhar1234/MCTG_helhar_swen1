package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.customExceptions.NotFoundException;
import at.technikum.apps.mtcg.customExceptions.UnauthorizedException;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserStats;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.ScoreboardService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLException;

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
        try {
            // Authenticate the user
            User user = sessionService.authenticateRequest(request);

            // Retrieve the scoreboard
            UserStats[] scoreboard = scoreboardService.getScoreboard();
            if (scoreboard == null) {
                // Handle the case where scoreboard is null
                return ResponseHelper.internalServerErrorResponse("Internal server error while processing scoreboard.");
            }

            // Respond with the scoreboard in JSON format
            ObjectMapper objectMapper = new ObjectMapper();
            String scoreboardJson = objectMapper.writeValueAsString(scoreboard);
            return ResponseHelper.okResponse(scoreboardJson, HttpContentType.APPLICATION_JSON);

        } catch (UnauthorizedException | NotFoundException e) {
            return ResponseHelper.unauthorizedResponse(e.getMessage());
        } catch (SQLException e) {
            return ResponseHelper.internalServerErrorResponse("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error retrieving scoreboard: " + e.getMessage());
            return ResponseHelper.internalServerErrorResponse("Internal server error while processing scoreboard: " + e.getMessage());
        }
    }

}
