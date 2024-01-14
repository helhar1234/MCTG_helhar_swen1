package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.dto.TokenRequest;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Handles a login request by validating user credentials and returning a token if successful.
     *
     * @param request The HTTP request containing the login information.
     * @return A Response object containing either the login token or an error message.
     */
    private Response loginUser(Request request) {
        TokenRequest tokenRequest;
        try {
            // Create an ObjectMapper instance for JSON processing
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert the JSON body of the request to a TokenRequest object
            tokenRequest = objectMapper.readValue(request.getBody(), TokenRequest.class);

        } catch (JsonProcessingException e) {
            // Catch block for JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing login request: " + e.getMessage());
        }

        // Attempt to retrieve a login token using the provided credentials
        Optional<String> token = sessionService.getToken(tokenRequest);
        // Login successful, return the token in the response
        return ResponseHelper.okResponse(token.get(), HttpContentType.TEXT_PLAIN);
    }


}
