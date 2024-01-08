/*package at.technikum.apps.mtcg.controller;

// TODO: implement Wheel of fortune for users -> get either a new card or coins once a day
// PRIZES: good cards or coins (12/16)
// LOOSERS: -5 coins (4/16)

import at.technikum.apps.mtcg.service.CardService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;

public class WheelOfFortuneController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/wheel");
    }

    @Override
    public Response handle(Request request) {
        // Extract the base route without query parameters
        String baseRoute = request.getRoute().split("\\?")[0];

        if (baseRoute.equals("/wheel")) {
            switch (request.getMethod()) {
                case "GET":
                    return getWheelPrize(request);
            }
        }
        return status(HttpStatus.BAD_REQUEST);
    }


    private final UserService userService;
    private final SessionService sessionService;
    private final CardService cardService;

    public WheelOfFortuneController() {

    }

    private Response getWheelPrize(Request request) {
        // authenticate token
        // ask if user has already wheeled
        // wheel (make changes)
        // tell player their prize


        return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: User does not exist");
    }
}*/
