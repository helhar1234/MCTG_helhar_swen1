package at.technikum.apps.mtcg.controller;

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
        }

        return status(HttpStatus.BAD_REQUEST);
    }

    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
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