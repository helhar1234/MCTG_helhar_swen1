package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.customExceptions.NotFoundException;
import at.technikum.apps.mtcg.customExceptions.UnauthorizedException;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLException;
import java.util.Optional;

// TODO: ADD COMMENTS & MAYBE USE ADDITIONAL FUNCTION FOR TOKEN AUTHENTIFICATION

public class UserController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/users");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/users")) {
            switch (request.getMethod()) {
                case "POST":
                    return createUser(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        }

        String[] routeParts = request.getRoute().split("/");
        String username = routeParts[2];

        switch (request.getMethod()) {
            case "GET":
                return getUser(username, request);
            case "PUT":
                return updateUser(username, request);
        }

        return status(HttpStatus.BAD_REQUEST);
    }

    private final UserService userService;
    private final SessionService sessionService;

    public UserController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    private Response updateUser(String username, Request request) {
        try {
            // Authenticate the user
            User requester = sessionService.authenticateRequest(request);

            // Check if the authenticated user is the requested user or an admin
            if (!requester.getUsername().equals(username) && !requester.isAdmin()) {
                return ResponseHelper.forbiddenResponse("Access denied");
            }

            // Map request to UserData class
            ObjectMapper objectMapper = new ObjectMapper();
            UserData userData = objectMapper.readValue(request.getBody(), UserData.class);

            // Proceed with update if authorized
            UserData updatedUserData = userService.updateUserData(username, userData);
            String jsonUserData = objectMapper.writeValueAsString(updatedUserData);
            return ResponseHelper.okResponse(jsonUserData, HttpContentType.APPLICATION_JSON);

        } catch (UnauthorizedException | NotFoundException e) {
            return ResponseHelper.unauthorizedResponse(e.getMessage());
        } catch (JsonProcessingException e) {
            // Specific catch for JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        } catch (SQLException e) {
            return ResponseHelper.internalServerErrorResponse("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error in updateUser: " + e.getMessage());
            return ResponseHelper.internalServerErrorResponse("Error processing update request: " + e.getMessage());
        }
    }


    private Response getUser(String username, Request request) {
        try {
            // Authenticate the user
            User requester = sessionService.authenticateRequest(request);

            // Check if the authenticated user is the requested user or an admin
            if (!requester.getUsername().equals(username) && !requester.isAdmin()) {
                return ResponseHelper.forbiddenResponse("Access denied");
            }

            // Retrieve the user data
            Optional<User> userOptional = userService.findUserByUsername(username);
            if (!userOptional.isPresent()) {
                return ResponseHelper.notFoundResponse("User not found");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String userDataJson = objectMapper.writeValueAsString(userOptional.get());
            return ResponseHelper.okResponse(userDataJson, HttpContentType.APPLICATION_JSON);

        } catch (UnauthorizedException | NotFoundException e) {
            return ResponseHelper.unauthorizedResponse(e.getMessage());
        } catch (SQLException e) {
            return ResponseHelper.internalServerErrorResponse("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error converting user data to JSON: " + e.getMessage());
            return ResponseHelper.internalServerErrorResponse("Internal server error while processing user data: " + e.getMessage());
        }
    }


    private Response createUser(Request request) {
        try {
            // Deserialize the JSON payload into a User object
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(request.getBody(), User.class);

            // Attempt to create the user
            Optional<User> createdUser = userService.createUser(user);
            if (createdUser.isPresent()) {
                // User successfully created
                return ResponseHelper.createdResponse("User successfully created!");
            } else {
                // User with the same username already exists
                return ResponseHelper.conflictResponse("User with same name already registered!");
            }
        } catch (JsonProcessingException e) {
            // Specific catch for JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        } catch (SQLException e) {
            // Catch for database errors
            return ResponseHelper.internalServerErrorResponse("Database error: " + e.getMessage());
        } catch (Exception e) {
            // General catch for other exceptions
            System.out.println("Error creating user: " + e.getMessage());
            return ResponseHelper.badRequestResponse("An unexpected error occurred: " + e.getMessage());
        }
    }

}