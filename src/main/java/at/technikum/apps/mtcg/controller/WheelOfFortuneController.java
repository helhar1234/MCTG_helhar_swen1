package at.technikum.apps.mtcg.controller;

// TODO: implement Wheel of fortune for users -> get either a new card or coins once a day
// PRIZES: good cards or coins (12/16)
// LOOSERS: -5 coins (4/16)

import at.technikum.apps.mtcg.dto.WheelPrize;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.WheelOfFortuneService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WheelOfFortuneController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/wheel");
    }

    @Override
    public Response handle(Request request) {
        // Extract the base route without query parameters
        String baseRoute = request.getRoute().split("\\?")[0];

        if (baseRoute.equals("/wheel")) {
            switch (request.getMethod()) {
                case "POST":
                    return getWheelPrize(request);
            }
        }
        return status(HttpStatus.BAD_REQUEST);
    }


    private final WheelOfFortuneService wheelOfFortuneService;
    private final SessionService sessionService;

    public WheelOfFortuneController(WheelOfFortuneService wheelOfFortuneService, SessionService sessionService) {
        this.sessionService = sessionService;
        this.wheelOfFortuneService = wheelOfFortuneService;
    }

    /**
     * Handles a request to spin the wheel of fortune and retrieve a prize.
     *
     * @param request The HTTP request containing the user's information.
     * @return A Response object containing the prize won in the wheel of fortune spin, in JSON format, or an error message.
     */
    private Response getWheelPrize(Request request) {
        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        // Spin the wheel of fortune for the user and retrieve the prize
        WheelPrize prize = wheelOfFortuneService.spin(requester);

        String prizeJson;
        try {
            // Create an ObjectMapper instance for JSON processing
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert the prize object to a JSON string
            prizeJson = objectMapper.writeValueAsString(prize);
        } catch (JsonProcessingException e) {
            // Return a bad request response in case of JSON parsing errors
            return ResponseHelper.badRequestResponse("Error parsing prize data: " + e.getMessage());
        }

        // Return a response with the prize in JSON format
        return ResponseHelper.okResponse(prizeJson, HttpContentType.APPLICATION_JSON);
    }

}
