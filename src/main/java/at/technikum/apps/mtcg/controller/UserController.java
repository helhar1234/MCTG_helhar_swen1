package at.technikum.apps.mtcg.controller;

import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;

public class UserController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/users") || route.startsWith("/stats") || route.startsWith("/scoreboard");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/users")) {
            switch (request.getMethod()) {
                case "POST":
                    return createUser(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        } else if (request.getRoute().equals("/stats")) {
            switch (request.getMethod()) {
                case "GET":
                    return getStats(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        } else if (request.getRoute().equals("/scoreboard")) {
            switch (request.getMethod()) {
                case "GET":
                    return getScoreboard(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        }

        String[] routeParts = request.getRoute().split("/");
        String username = routeParts[2];

        switch (request.getMethod()) {
            case "GET":
                return getUser(username);
        }

        return status(HttpStatus.BAD_REQUEST);
    }

    private Response getUser(String username) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "getUser" + username);
    }

    private Response getScoreboard(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "getScoreboard");
    }

    private Response getStats(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "getStats");
    }

    private Response createUser(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "createUser");
    }
}
