package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.TokenRequest;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public class SessionController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/sessions");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/sessions")) {
            switch (request.getMethod()) {
                case "POST":
                    return loginUser(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private final SessionService sessionService;

    public SessionController() {
        this.sessionService = new SessionService();
    }

    private Response loginUser(Request request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Assuming TokenRequest is the class used to represent the login credentials
            TokenRequest tokenRequest = objectMapper.readValue(request.getBody(), TokenRequest.class);

            // Assuming SessionService has a method getToken that returns an Optional<String>
            Optional<String> token = sessionService.getToken(tokenRequest);

            if (token.isPresent()) {
                // Login successful, token is present
                return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, token.get());
            } else {
                // Invalid username/password
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Invalid username/password");
            }
        } catch (Exception e) {
            // Handle deserialization errors or other exceptions
            System.out.println("Error during user login: " + e.getMessage());
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Error processing login request");
        }
    }
}
