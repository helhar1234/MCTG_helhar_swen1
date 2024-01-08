package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.customExceptions.NotFoundException;
import at.technikum.apps.mtcg.customExceptions.UnauthorizedException;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.PackageService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.TransactionsService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLException;
import java.util.Optional;

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

    private final PackageService packageService;
    private final SessionService sessionService;
    private final TransactionsService transactionsService;
    private final UserService userService;

    public TransactionsController(TransactionsService transactionsService, SessionService sessionService, PackageService packageService, UserService userService) {
        this.packageService = packageService;
        this.sessionService = sessionService;
        this.transactionsService = transactionsService;
        this.userService = userService;
    }

    private Response buyPackage(Request request) {
        try {
            // Authenticate the user
            User user = sessionService.authenticateRequest(request);

            // Extract package ID from the request
            ObjectMapper objectMapper = new ObjectMapper();
            String packageId = objectMapper.convertValue(request.getBody(), String.class);

            if (packageId == null || packageId.isEmpty()) {
                packageId = packageService.getRandomPackage(user.getId());
                if (packageId == null) {
                    return ResponseHelper.notFoundResponse("No available packages for buying");
                }
            }

            Optional<Package> aPackage = packageService.getAvailablePackages(packageId);
            if (aPackage.isEmpty()) {
                return ResponseHelper.notFoundResponse("No card package available for buying");
            }

            if (user.getCoins() < aPackage.get().getPrice()) {
                return ResponseHelper.forbiddenResponse("Not enough money for buying a card package");
            }

            boolean transactionSuccess = transactionsService.makeTransaction(aPackage.get().getId(), user.getId(), aPackage.get().getPrice());
            if (transactionSuccess) {
                return ResponseHelper.okResponse("A package has been successfully bought");
            } else {
                return ResponseHelper.conflictResponse("Conflict occurred during the purchase");
            }

        } catch (UnauthorizedException | NotFoundException e) {
            return ResponseHelper.unauthorizedResponse(e.getMessage());
        } catch (SQLException e) {
            return ResponseHelper.internalServerErrorResponse("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing package acquisition: " + e.getMessage());
            return ResponseHelper.badRequestResponse("Error processing package acquisition request: " + e.getMessage());
        }
    }

}
