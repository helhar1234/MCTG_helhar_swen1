package at.technikum.apps.mtcg.controller;

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

import java.util.Optional;

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

    /**
     * Updates the data of an existing user.
     *
     * @param username The username of the user to be updated.
     * @param request  The HTTP request containing the new user data.
     * @return A Response object indicating the success or failure of the user update.
     */
    private Response updateUser(String username, Request request) {
        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        // Create an ObjectMapper instance for JSON processing
        ObjectMapper objectMapper = new ObjectMapper();
        UserData userData;
        try {
            // Convert the JSON body of the request to a UserData object
            userData = objectMapper.readValue(request.getBody(), UserData.class);
        } catch (JsonProcessingException e) {
            // Return a bad request response in case of JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        }

        // Update the user's data and get the updated data
        UserData updatedUserData = userService.updateUserData(requester, username, userData);

        String jsonUserData;
        try {
            // Convert the updated user data to a JSON string
            jsonUserData = objectMapper.writeValueAsString(updatedUserData);
        } catch (JsonProcessingException e) {
            // Return a bad request response in case of JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        }

        // Return a response with the updated user data in JSON format
        return ResponseHelper.okResponse(jsonUserData, HttpContentType.APPLICATION_JSON);
    }


    /**
     * Retrieves the data for a specific user.
     *
     * @param username The username of the user whose data is being requested.
     * @param request  The HTTP request containing the request information.
     * @return A Response object containing the user's data in JSON format or an error message.
     */
    private Response getUser(String username, Request request) {
        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        // Retrieve the user data
        Optional<User> user = userService.getUser(requester, username);

        String userDataJson;
        try {
            // Create an ObjectMapper instance for JSON processing
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert the user data to a JSON string
            userDataJson = objectMapper.writeValueAsString(user.get());
        } catch (JsonProcessingException e) {
            // Return a bad request response in case of JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        }

        // Return a response with the user's data in JSON format
        return ResponseHelper.okResponse(userDataJson, HttpContentType.APPLICATION_JSON);
    }


    /**
     * Creates a new user based on the request data.
     *
     * @param request The HTTP request containing the new user's data.
     * @return A Response object indicating the success or failure of the user creation.
     */
    private Response createUser(Request request) {
        User user;
        try {
            // Create an ObjectMapper instance for JSON processing
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert the JSON body of the request to a User object
            user = objectMapper.readValue(request.getBody(), User.class);
        } catch (JsonProcessingException e) {
            // Return a bad request response in case of JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        }

        // Create the user and get the created user data
        Optional<User> createdUser = userService.createUser(user);

        // Return a response indicating the user was successfully created
        return ResponseHelper.createdResponse("User " + createdUser.get().getUsername() + " successfully created!");
    }


}