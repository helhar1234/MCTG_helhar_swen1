package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.responses.ResponseHelper;
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

    public TransactionsController(TransactionsService transactionsService) {
        this.transactionsService = transactionsService;
    }

    private Response buyPackage(Request request) {
        // Assuming request.getBody() returns the raw body content that can be cast to String
        String packageId = request.getBody();

        // Proceed with making the transaction after getting the package ID.
        transactionsService.makeTransaction(packageId, request);

        // Return an OK response indicating the package was bought successfully.
        return ResponseHelper.okResponse("Package has been successfully bought");
    }


}
