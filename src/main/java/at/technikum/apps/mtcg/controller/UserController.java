package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Response updateUser(String username, Request request) {
        ObjectMapper objectMapper = new ObjectMapper();
        UserData userData;
        try {
            // Map request to UserData class
            userData = objectMapper.readValue(request.getBody(), UserData.class);
        } catch (JsonProcessingException e) {
            // Specific catch for JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        }
        // Proceed with update if authorized
        UserData updatedUserData = userService.updateUserData(username, request, userData);

        try {
            String jsonUserData = objectMapper.writeValueAsString(updatedUserData);
            return ResponseHelper.okResponse(jsonUserData, HttpContentType.APPLICATION_JSON);
        } catch (JsonProcessingException e) {
            // Specific catch for JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        }
    }


    private Response getUser(String username, Request request) {
        Optional<User> user = userService.getUser(username, request);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String userDataJson = objectMapper.writeValueAsString(user.get());
            return ResponseHelper.okResponse(userDataJson, HttpContentType.APPLICATION_JSON);
        } catch (JsonProcessingException e) {
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        }

    }


    private Response createUser(Request request) {
        User user;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            user = objectMapper.readValue(request.getBody(), User.class);
        } catch (JsonProcessingException e) {
            return ResponseHelper.badRequestResponse("Error parsing user data: " + e.getMessage());
        }

        Optional<User> createdUser = userService.createUser(user);

        return ResponseHelper.createdResponse("User " + createdUser.get().getUsername() + " successfully created!");


    }

}