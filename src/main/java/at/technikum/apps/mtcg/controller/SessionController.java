package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.TokenRequest;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

// TODO: ADD COMMENTS & MAYBE USE ADDITIONAL FUNCTION FOR TOKEN AUTHENTIFICATION
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

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    private Response loginUser(Request request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TokenRequest tokenRequest = objectMapper.readValue(request.getBody(), TokenRequest.class);

            Optional<String> token = sessionService.getToken(tokenRequest);
            if (token.isPresent()) {
                // Login successful, token is present
                return ResponseHelper.okResponse(token.get(), HttpContentType.TEXT_PLAIN);
            } else {
                // Invalid username/password
                return ResponseHelper.unauthorizedResponse("Invalid username/password");
            }
        } catch (JsonProcessingException e) {
            // Specific catch for JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing login request: " + e.getMessage());
        } catch (Exception e) {
            // General catch for other exceptions
            System.out.println("Error during user login: " + e.getMessage());
            return ResponseHelper.internalServerErrorResponse("Error processing login request: " + e.getMessage());
        }
    }

}
