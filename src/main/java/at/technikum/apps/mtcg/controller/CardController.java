package at.technikum.apps.mtcg.controller;

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

    private Response createUserDeck(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "createUserDeck");
    }

    private Response getUserDeck(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "getUserDeck");
    }

    private Response getUserCards(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "getUserCards");
    }
}
