package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.service.CardService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;

public class CardController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/cards") || route.startsWith("/deck");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/cards")) {
            switch (request.getMethod()) {
                case "GET":
                    return getUserCards(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        } else if (request.getRoute().equals("/deck")) {
            switch (request.getMethod()) {
                case "GET":
                    return getUserDeck(request);
                case "PUT":
                    return createUserDeck(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private final UserService userService;
    private final SessionService sessionService;
    private final CardService cardService;

    public CardController() {
        this.userService = new UserService();
        this.sessionService = new SessionService();
        this.cardService = new CardService();
    }

    private Response createUserDeck(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "createUserDeck");
    }

    private Response getUserDeck(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "getUserDeck");
    }

}
