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
                return getUser(username, request);
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
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized");
            }
            String[] authParts = authHeader.split("\\s+");
            String token = authParts[1];

            // Authenticate the token
            // Authenticate the token
            boolean isAuthenticated = sessionService.authenticateToken(token);
            if (!isAuthenticated) {
                // If the token is not authenticated
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: Invalid token");
            }

            // Check if the username in token matches the requested username or if the user is an admin
            boolean isMatchingRoute = sessionService.matchRoute(username, token);
            boolean isAdmin = sessionService.isAdmin(token); // Ensure this method checks the token, not the username
            if (!isMatchingRoute && !isAdmin) {
                // If the authenticated user is neither the requested user nor an admin
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: Access denied");
            }

            // Proceed with update if authorized
            UserData updatedUserData = userService.updateUserData(username, userData);
            String jsonUserData = objectMapper.writeValueAsString(updatedUserData);
            return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, jsonUserData);

        } catch (Exception e) {
            System.out.println("Error in updateUser: " + e.getMessage());
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Error processing update request");
        }
    }


    private Response getUser(String username, Request request) {
        // Extract the token from the Authorization header
        String authHeader = request.getAuthenticationHeader();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: No token provided");
        }
        String[] authParts = authHeader.split("\\s+");
        String token = authParts[1];

        // Authenticate the token and check if the username matches or if the user is admin
        boolean isAuthenticated = sessionService.authenticateToken(token);
        boolean isMatchingRoute = sessionService.matchRoute(username, token);
        boolean isAdmin = sessionService.isAdmin(token);

        if (!isAuthenticated) {
            // If the token is not authenticated
            return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: Invalid token");
        }

        if (!isMatchingRoute && !isAdmin) {
            // If the authenticated user is neither the requested user nor an admin
            return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: Access denied");
        }

        // Retrieve the user data if the token is authenticated and the username matches or the user is admin
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (!userOptional.isPresent()) {
            // If the user with the provided username does not exist
            return new Response(HttpStatus.NOT_FOUND, HttpContentType.TEXT_PLAIN, "User not found");
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String userDataJson = objectMapper.writeValueAsString(userOptional.get());
            return new Response(HttpStatus.OK, HttpContentType.APPLICATION_JSON, userDataJson);
        } catch (Exception e) {
            // If JSON serialization fails
            System.out.println("Error converting user data to JSON: " + e.getMessage());
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, HttpContentType.TEXT_PLAIN, "Internal server error while processing user data.");
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