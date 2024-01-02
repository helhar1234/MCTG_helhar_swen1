package at.technikum.apps.mtcg.controller;

import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;

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

    private Response loginUser(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "loginUser");
    }
}
