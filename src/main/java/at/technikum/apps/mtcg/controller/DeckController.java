package at.technikum.apps.mtcg.controller;


import at.technikum.apps.mtcg.customExceptions.NotFoundException;
import at.technikum.apps.mtcg.customExceptions.UnauthorizedException;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLException;

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
        try {
            User user = sessionService.authenticateRequest(request);
            ObjectMapper objectMapper = new ObjectMapper();
            String[] cardIds = objectMapper.readValue(request.getBody(), String[].class);

            if (cardIds == null || cardIds.length != 4) {
                return ResponseHelper.badRequestResponse("Exactly four card IDs are required");
            }

            for (String cardId : cardIds) {
                if (!cardService.isCardInStack(user.getId(), cardId)) {
                    return ResponseHelper.forbiddenResponse("One or more cards do not belong to the user");
                }
            }

            boolean isDeckReset = deckService.resetDeck(user.getId());
            boolean areCardsAdded = true;
            for (String cardId : cardIds) {
                areCardsAdded &= deckService.addCardToDeck(user.getId(), cardId);
            }

            if (!isDeckReset || !areCardsAdded) {
                return ResponseHelper.internalServerErrorResponse("Unable to update the deck");
            }

            return ResponseHelper.okResponse("The deck has been successfully configured");

        } catch (UnauthorizedException | NotFoundException e) {
            return ResponseHelper.unauthorizedResponse(e.getMessage());
        } catch (SQLException e) {
            return ResponseHelper.internalServerErrorResponse("Database error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseHelper.badRequestResponse("Error processing deck creation request: " + e.getMessage());
        }
    }


    private Response getUserDeck(Request request, String queryParams) {
        boolean isPlainFormat = queryParams.equals("format=plain");
        try {
            User user = sessionService.authenticateRequest(request);

            Card[] cards = deckService.getUserDeckCards(user.getId());
            if (cards == null || cards.length == 0) {
                return ResponseHelper.noContentResponse("The user doesn't have any cards");
            }

            if (isPlainFormat) {
                String plainDeck = convertDeckToPlainText(cards);
                return ResponseHelper.okResponse(plainDeck, HttpContentType.TEXT_PLAIN);
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                String deckJson = objectMapper.writeValueAsString(cards);
                return ResponseHelper.okResponse(deckJson, HttpContentType.APPLICATION_JSON);
            }

        } catch (UnauthorizedException | NotFoundException e) {
            return ResponseHelper.unauthorizedResponse(e.getMessage());
        } catch (SQLException e) {
            return ResponseHelper.internalServerErrorResponse("Database error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseHelper.badRequestResponse("Error retrieving user's cards: " + e.getMessage());
        }
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
