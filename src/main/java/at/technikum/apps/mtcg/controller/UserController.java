package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.UserData;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public class UserController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/users") || route.startsWith("/stats") || route.startsWith("/scoreboard");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/users")) {
            switch (request.getMethod()) {
                case "POST":
                    return createUser(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        } else if (request.getRoute().equals("/stats")) {
            switch (request.getMethod()) {
                case "GET":
                    return getStats(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        } else if (request.getRoute().equals("/scoreboard")) {
            switch (request.getMethod()) {
                case "GET":
                    return getScoreboard(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        }

        String[] routeParts = request.getRoute().split("/");
        String username = routeParts[2];

        switch (request.getMethod()) {
            case "GET":
                return getUser(username);
            case "PUT":
                return updateUser(username, request);
        }

        return status(HttpStatus.BAD_REQUEST);
    }
    private final UserService userService;
    private final SessionService sessionService;

    public UserController() {
        this.userService = new UserService();
        this.sessionService = new SessionService();
    }

    private Response updateUser(String username, Request request) {
        try {
            // Map request to UserData class
            ObjectMapper objectMapper = new ObjectMapper();
            UserData userData = objectMapper.readValue(request.getBody(), UserData.class);

            // Get token and token's username from Authorization header
            String authHeader = request.getAuthenticationHeader();
            if (authHeader == null) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized");
            }
            String[] authParts = authHeader.split("\\s+");
            String token = authParts[1];

            // Authenticate the token
            boolean isAuthenticated = sessionService.authenticateToken(token);
            boolean isMatchingRoute = sessionService.matchRoute(username, token);
            boolean isAdmin = sessionService.isAdmin(username);

            // Check if the authenticated user is admin or the same as the one being updated
            if ((isAuthenticated && isMatchingRoute) || (isAuthenticated && isAdmin)) {
                // Update user data
                UserData updatedUserData = userService.updateUserData(username, userData); // Ensure this method is implemented

                try {
                    String jsonUserData = objectMapper.writeValueAsString(updatedUserData);
                    return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, jsonUserData);
                } catch (Exception e) {
                    // Handle JSON serialization errors
                    System.out.println("Error serializing updated user data: " + e.getMessage());
                    return new Response(HttpStatus.INTERNAL_SERVER_ERROR, HttpContentType.TEXT_PLAIN, "Internal server error");
                }
            } else {
                // Unauthorized or user not found
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized or User not found. Please try logging in again!");
            }


        } catch (Exception e) {
            // Handle parsing errors or other exceptions
            System.out.println(e);
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Error processing update request");
        }
    }


    private Response getUser(String username) {
        Optional<User> user = userService.findUserByUsername(username);
        if (user.isPresent()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String userData = objectMapper.writeValueAsString(user.get());
                return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, userData);
            } catch (Exception e) {
                // Log the exception and return an appropriate error message
                System.out.println("Error converting user data to JSON: " + e.getMessage());
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, HttpContentType.TEXT_PLAIN, "Internal server error while processing user data.");
            }
        } else {
            // User not found
            return new Response(HttpStatus.NOT_FOUND, HttpContentType.TEXT_PLAIN, "User not found!");
        }
    }


    private Response getScoreboard(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "getScoreboard");
    }

    private Response getStats(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "getStats");
    }

    private Response createUser(Request request) {
        try {
            // Deserialize the JSON payload into a User object
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(request.getBody(), User.class);

            // Attempt to create the user
            boolean isCreated = userService.createUser(user);
            if (isCreated) {
                // User successfully created
                return new Response(HttpStatus.CREATED, HttpContentType.TEXT_PLAIN, "User successfully created!");
            } else {
                // User with the same username already exists
                return new Response(HttpStatus.CONFLICT, HttpContentType.TEXT_PLAIN, "User with same name already registered!");
            }
        } catch (Exception e) {
            // Handle any deserialization or other exceptions
            System.out.println(e);
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "An unexpecting error occured" + e);
        }
    }
}