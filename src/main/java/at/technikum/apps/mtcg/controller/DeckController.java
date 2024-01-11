package at.technikum.apps.mtcg.controller;


import at.technikum.apps.mtcg.entity.Card;
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

    private Response createUserDeck(Request request) {
        String[] cardIds;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            cardIds = objectMapper.readValue(request.getBody(), String[].class);
        } catch (JsonProcessingException e) {
            return ResponseHelper.badRequestResponse("Error parsing deck data: " + e.getMessage());
        }

        boolean isDeckConf = deckService.configureDeck(request, cardIds);
        return ResponseHelper.okResponse("The deck has been successfully configured");


    }


    private Response getUserDeck(Request request, String queryParams) {
        boolean isPlainFormat = queryParams.equals("format=plain");
        Card[] cards = deckService.getDeck(request);
        ObjectMapper objectMapper = new ObjectMapper();
        String responseBody;
        HttpContentType contentType;

        if (isPlainFormat) {
            // Convert deck to plain text and set response body and content type
            responseBody = convertDeckToPlainText(cards);
            contentType = HttpContentType.TEXT_PLAIN;
        } else {
            // Convert deck to JSON and catch any potential JsonProcessingException
            try {
                responseBody = objectMapper.writeValueAsString(cards);
                contentType = HttpContentType.APPLICATION_JSON;
            } catch (JsonProcessingException e) {
                // If JSON parsing fails, return the error response immediately
                return ResponseHelper.badRequestResponse("Error parsing deck data: " + e.getMessage());
            }
        }

        // Return the response, contentType is set based on the isPlainFormat flag
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
