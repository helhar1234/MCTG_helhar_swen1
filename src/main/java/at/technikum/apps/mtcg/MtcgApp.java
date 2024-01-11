package at.technikum.apps.mtcg;

import at.technikum.apps.mtcg.controller.*;
import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.server.ServerApplication;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;

import java.util.ArrayList;
import java.util.List;

public class MtcgApp implements ServerApplication {

    private List<Controller> controllers = new ArrayList<>();

    public MtcgApp() {
        Injector injector = new Injector();
        this.controllers = injector.createController();
    }

    @Override
    public Response handle(Request request) {
        try {
            // Extrahiert den ersten Teil des Pfades vor dem ersten '/' oder '?'
            String route = request.getRoute().split("[/?]")[1];
            Controller selectedController = null;

            switch (route) {
                case "users":
                    selectedController = getController(UserController.class);
                    break;
                case "stats":
                    selectedController = getController(StatsController.class);
                    break;
                case "scoreboard":
                    selectedController = getController(ScoreboardController.class);
                    break;
                case "cards":
                    selectedController = getController(CardController.class);
                    break;
                case "deck":
                    selectedController = getController(DeckController.class);
                    break;
                case "sessions":
                    selectedController = getController(SessionController.class);
                    break;
                case "packages":
                    selectedController = getController(PackageController.class);
                    break;
                case "transactions":
                    // Spezialfall für transactions/packages
                    if (request.getRoute().startsWith("/transactions/packages")) {
                        selectedController = getController(TransactionsController.class);
                    }
                    break;
                case "battles":
                    selectedController = getController(BattleController.class);
                    break;
                case "tradings":
                    selectedController = getController(TradingController.class);
                    break;
                /*case "wheel":
                    selectedController = getController(WheelOfFortuneController.class);
                    break;*/
                default:
                    // Kein passender Controller gefunden
                    break;
            }

            if (selectedController != null && selectedController.supports(request.getRoute())) {
                try {
                    return selectedController.handle(request);
                } catch (HttpStatusException e) {
                    return new Response(e.getStatus(), HttpContentType.TEXT_PLAIN, e.getMessage());
                }


            } else {
                return new Response(HttpStatus.NOT_FOUND, HttpContentType.TEXT_PLAIN, "Route " + request.getRoute() + " not found in app!");
            }
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, HttpContentType.TEXT_PLAIN, "Internal Server Error: " + e.getMessage());
        }
    }

    private <T extends Controller> T getController(Class<T> controllerClass) {
        for (Controller controller : controllers) {
            if (controllerClass.isInstance(controller)) {
                return controllerClass.cast(controller);
            }
        }
        return null;
    }
}