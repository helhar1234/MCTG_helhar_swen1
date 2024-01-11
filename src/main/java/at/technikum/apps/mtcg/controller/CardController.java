package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.CardService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// TODO: ADD COMMENTS & MAYBE USE ADDITIONAL FUNCTION FOR TOKEN AUTHENTIFICATION
// TODO: MAKE DECK CONTROLLER SEPERATE
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
    private final UserService userService;

    public CardController(CardService cardService, SessionService sessionService, UserService userService) {
        this.cardService = cardService;
        this.sessionService = sessionService;
        this.userService = userService;
    }

    private Response getUserCards(Request request) {
        Card[] cards = cardService.getCards(request);
        String cardsJson;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            cardsJson = objectMapper.writeValueAsString(cards);
        } catch (JsonProcessingException e) {
            return ResponseHelper.badRequestResponse("Error parsing card data: " + e.getMessage());
        }

        return ResponseHelper.okResponse(cardsJson, HttpContentType.APPLICATION_JSON);

    }


}
