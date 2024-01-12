package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.TransactionsService;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;

public class TransactionsController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/transactions/packages");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/transactions/packages")) {
            switch (request.getMethod()) {
                case "POST":
                    return buyPackage(request);
            }
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private final TransactionsService transactionsService;
    private final SessionService sessionService;

    public TransactionsController(TransactionsService transactionsService, SessionService sessionService) {
        this.transactionsService = transactionsService;
        this.sessionService = sessionService;
    }

    private Response buyPackage(Request request) {
        User requester = sessionService.authenticateRequest(request);
        // Assuming request.getBody() returns the raw body content that can be cast to String
        String packageId = request.getBody();

        // Proceed with making the transaction after getting the package ID.
        transactionsService.makeTransaction(requester, packageId);

        // Return an OK response indicating the package was bought successfully.
        return ResponseHelper.okResponse("Package has been successfully bought");
    }


}
