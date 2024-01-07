package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.PackageService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.TransactionsService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public TransactionsController() {
        this.packageService = new PackageService();
        this.sessionService = new SessionService();
        this.transactionsService = new TransactionsService();
        this.userService = new UserService();
    }

    private Response buyPackage(Request request) {
        try {
            // Extract the token from the Authorization header
            String authHeader = request.getAuthenticationHeader();
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: No token provided");
            }
            String[] authParts = authHeader.split("\\s+");
            String token = authParts[1];

            // Authenticate the token and get the user
            boolean isAuthenticated = sessionService.authenticateToken(token);
            if (!isAuthenticated) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: Invalid token");
            }

            Optional<User> user = sessionService.getUserByToken(token);
            if (user.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: User does not exist");
            }

            // Extract package ID from the request (assuming it's in the request body or path)
            ObjectMapper objectMapper = new ObjectMapper();
            String packageId = objectMapper.convertValue(request.getBody(), String.class);

            // If no package ID provided, get a random package ID
            if (packageId == null || packageId.isEmpty()) {
                packageId = packageService.getRandomPackage(user.get().getId());
                if (packageId == null) {
                    return new Response(HttpStatus.NOT_FOUND, HttpContentType.TEXT_PLAIN, "No available packages for buying");
                }
            }
            // Retrieve the package
            Optional<Package> aPackage = packageService.getAvailablePackages(packageId);
            if (aPackage.isEmpty()) {
                return new Response(HttpStatus.NOT_FOUND, HttpContentType.TEXT_PLAIN, "No card package available for buying");
            }

            // Check if the user has enough coins
            if (user.get().getCoins() < aPackage.get().getPrice()) {
                return new Response(HttpStatus.FORBIDDEN, HttpContentType.TEXT_PLAIN, "Not enough money for buying a card package");
            }

            // Make the transaction
            boolean transactionSuccess = transactionsService.makeTransaction(aPackage.get().getId(), user.get().getId(), aPackage.get().getPrice());
            if (transactionSuccess) {
                return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "A package has been successfully bought");
            } else {
                return new Response(HttpStatus.CONFLICT, HttpContentType.TEXT_PLAIN, "Conflict occurred during the purchase");
            }

        } catch (Exception e) {
            System.out.println("Error processing package acquisition: " + e.getMessage());
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Error processing package acquisition request");
        }
    }
}
