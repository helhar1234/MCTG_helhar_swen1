package at.technikum.apps.mtcg.controller;

import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;

public class BattleController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/battles");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/battles")) {
            switch (request.getMethod()) {
                case "POST":
                    return startBattle(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private Response startBattle(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "startBattle");
    }
}
