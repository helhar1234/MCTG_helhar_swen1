package at.technikum.apps.mtcg;

import at.technikum.apps.mtcg.controller.*;
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
        controllers.add(new UserController());
        controllers.add(new CardController());
        controllers.add(new SessionController());
        controllers.add(new PackageController());
        controllers.add(new TradingController());
        controllers.add(new BattleController());
        controllers.add(new StatsController());
        controllers.add(new ScoreboardController());
        controllers.add(new TransactionsController());
        controllers.add(new DeckController());
    }

    @Override
    public Response handle(Request request) {
        Controller selectedController = null;

        // Überprüfen Sie auf spezifische Routen
        if (request.getRoute().startsWith("/users")) {
            selectedController = getController(UserController.class);
        } else if (request.getRoute().startsWith("/stats")) {
            selectedController = getController(StatsController.class);
        } else if (request.getRoute().startsWith("/scoreboard")) {
            selectedController = getController(ScoreboardController.class);
        } else if (request.getRoute().startsWith("/cards")) {
            selectedController = getController(CardController.class);
        } else if (request.getRoute().startsWith("/deck")) {
            selectedController = getController(DeckController.class);
        } else if (request.getRoute().startsWith("/sessions")) {
            selectedController = getController(SessionController.class);
        } else if (request.getRoute().startsWith("/packages")) {
            selectedController = getController(PackageController.class);
        } else if (request.getRoute().startsWith("/transactions/packages")) {
            selectedController = getController(TransactionsController.class);
        } else if (request.getRoute().startsWith("/battles")) {
            selectedController = getController(BattleController.class);
        } else if (request.getRoute().startsWith("/tradings")) {
            selectedController = getController(TradingController.class);
        }
        // Verwenden Sie den ausgewählten Controller, falls vorhanden
        if (selectedController != null && selectedController.supports(request.getRoute())) {

            return selectedController.handle(request);
        }

        return new Response(HttpStatus.NOT_FOUND, HttpContentType.TEXT_PLAIN, "Route " + request.getRoute() + " not found in app!");
    }

    // Hilfsmethode, um den Controller einer bestimmten Klasse zu erhalten
    private <T extends Controller> T getController(Class<T> controllerClass) {
        for (Controller controller : controllers) {
            if (controllerClass.isInstance(controller)) {
                return controllerClass.cast(controller);
            }
        }
        return null;
    }
}
