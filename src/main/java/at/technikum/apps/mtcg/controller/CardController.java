package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.CardService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CardController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/cards");
    }

    @Override
    public Response handle(Request request) {
        // Extract the base route without query parameters
        String baseRoute = request.getRoute().split("\\?")[0];

        if (baseRoute.equals("/cards")) {
            switch (request.getMethod()) {
                case "GET":
                    return getUserCards(request);
            }
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private final CardService cardService;
    private final SessionService sessionService;

    public CardController(CardService cardService, SessionService sessionService) {
        this.cardService = cardService;
        this.sessionService = sessionService;
    }

    /**
     * Retrieves the cards associated with a user and returns them in a JSON formatted response.
     *
     * @param request The HTTP request containing the user's information.
     * @return A Response object containing the user's cards in JSON format or an error message.
     */
    private Response getUserCards(Request request) {
        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        // Retrieve the array of cards belonging to the authenticated user
        Card[] cards = cardService.getCards(requester);

        // Prepare to convert the array of cards to a JSON string
        String cardsJson;
        try {
            // Create an ObjectMapper instance for JSON processing
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert the array of cards to a JSON string
            cardsJson = objectMapper.writeValueAsString(cards);
        } catch (JsonProcessingException e) {
            // In case of JSON processing errors, return a bad request response with error details
            return ResponseHelper.badRequestResponse("Error parsing card data: " + e.getMessage());
        }

        // Create and return a successful response with the user's cards in JSON format
        return ResponseHelper.okResponse(cardsJson, HttpContentType.APPLICATION_JSON);
    }


}
