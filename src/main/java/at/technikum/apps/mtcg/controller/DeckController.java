package at.technikum.apps.mtcg.controller;


import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.CardService;
import at.technikum.apps.mtcg.service.DeckService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeckController extends Controller {
    public boolean supports(String route) {
        return route.startsWith("/deck");
    }

    @Override
    public Response handle(Request request) {
        // Extract the base route without query parameters
        String baseRoute = request.getRoute().split("\\?")[0];

        if (baseRoute.equals("/deck")) {
            switch (request.getMethod()) {
                case "GET":
                    // Check for query parameters
                    String queryParams = request.getRoute().contains("?") ? request.getRoute().split("\\?")[1] : "";
                    return getUserDeck(request, queryParams);
                case "PUT":
                    return createUserDeck(request);
            }
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private final UserService userService;
    private final SessionService sessionService;
    private final CardService cardService;
    private final DeckService deckService;


    public DeckController(DeckService deckService, SessionService sessionService, UserService userService, CardService cardService) {
        this.deckService = deckService;
        this.sessionService = sessionService;
        this.userService = userService;
        this.cardService = cardService;
    }

    /**
     * Creates or updates a user's deck based on the provided card IDs.
     *
     * @param request The HTTP request containing the deck configuration information.
     * @return A Response object indicating the success or failure of the deck configuration.
     */
    private Response createUserDeck(Request request) {
        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        // Declare an array to hold the card IDs from the request
        String[] cardIds;
        try {
            // Create an ObjectMapper instance for JSON processing
            ObjectMapper objectMapper = new ObjectMapper();

            // Read the card IDs from the request body and convert them into a String array
            cardIds = objectMapper.readValue(request.getBody(), String[].class);
        } catch (JsonProcessingException e) {
            // In case of JSON processing errors, return a bad request response with error details
            return ResponseHelper.badRequestResponse("Error parsing deck data: " + e.getMessage());
        }

        // Configure the user's deck with the provided card IDs
        boolean isDeckConf = deckService.configureDeck(requester, cardIds);

        // Return a response indicating the deck has been successfully configured
        return ResponseHelper.okResponse("The deck has been successfully configured");
    }


    /**
     * Retrieves the user's deck and returns it in either plain text or JSON format.
     *
     * @param request     The HTTP request containing the user's information.
     * @param queryParams The query parameters from the HTTP request to determine the response format.
     * @return A Response object containing the user's deck in the specified format or an error message.
     */
    private Response getUserDeck(Request request, String queryParams) {
        // Check if the request specifies plain text format
        boolean isPlainFormat = queryParams.equals("format=plain");

        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        // Retrieve the array of cards representing the user's deck
        Card[] cards = deckService.getDeck(requester);

        // Create an ObjectMapper instance for potential JSON processing
        ObjectMapper objectMapper = new ObjectMapper();

        // Variables to store the response body and content type
        String responseBody;
        HttpContentType contentType;

        if (isPlainFormat) {
            // Convert the deck to plain text format and set the response body and content type
            responseBody = convertDeckToPlainText(cards);
            contentType = HttpContentType.TEXT_PLAIN;
        } else {
            // Convert the deck to JSON format and set the response body and content type
            try {
                responseBody = objectMapper.writeValueAsString(cards);
                contentType = HttpContentType.APPLICATION_JSON;
            } catch (JsonProcessingException e) {
                // If JSON processing fails, return an error response immediately
                return ResponseHelper.badRequestResponse("Error parsing deck data: " + e.getMessage());
            }
        }

        // Return the response, with the content type set based on the isPlainFormat flag
        return ResponseHelper.okResponse(responseBody, contentType);
    }


    private String convertDeckToPlainText(Card[] deckCards) {
        // Convert the array of Cards into a plain text string
        StringBuilder plainTextBuilder = new StringBuilder();
        for (Card card : deckCards) {
            plainTextBuilder.append("Card Id: ").append(card.getId())
                    .append(", Name: ").append(card.getName())
                    .append(", Damage ").append(card.getDamage())
                    .append(", Type ").append(card.getCardType())
                    .append(", Element ").append(card.getElementType())
                    .append("\n");
        }
        return plainTextBuilder.toString();
    }
}
