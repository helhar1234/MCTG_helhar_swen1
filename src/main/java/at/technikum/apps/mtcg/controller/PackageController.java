package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.PackageCard;
import at.technikum.apps.mtcg.service.CardService;
import at.technikum.apps.mtcg.service.PackageService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;

// TODO: ADD COMMENTS & MAYBE USE ADDITIONAL FUNCTION FOR TOKEN AUTHENTIFICATION
// TODO: MAKE TRANSACTIONS CONTROLLER SEPERATE
public class PackageController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/packages") || route.startsWith("/transactions/packages");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/packages")) {
            switch (request.getMethod()) {
                case "POST":
                    return createPackage(request);
            }
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private final PackageService packageService;
    private final SessionService sessionService;
    private final CardService cardService;

    private final UserService userService;

    public PackageController() {
        this.packageService = new PackageService();
        this.sessionService = new SessionService();
        this.cardService = new CardService();
        this.userService = new UserService();
    }


    private Response createPackage(Request request) {
        try {
            // Map request to Card[] cards
            ObjectMapper objectMapper = new ObjectMapper();
            PackageCard[] packageCards = objectMapper.readValue(request.getBody(), PackageCard[].class);

            // Check if there are exactly 5 cards
            if (packageCards.length != 5) {
                return new Response(HttpStatus.CONFLICT, HttpContentType.TEXT_PLAIN, "Conflict: A package must contain exactly 5 cards");
            }

            // Check for duplicate cards in the array and if cards already exist in the database
            Set<String> cardIds = new HashSet<>();
            for (PackageCard card : packageCards) {
                if (cardIds.contains(card.getId()) || cardService.findCardById(card.getId()).isPresent()) {
                    return new Response(HttpStatus.CONFLICT, HttpContentType.TEXT_PLAIN, "Conflict: Duplicate or existing cards found in the package");
                }
                cardIds.add(card.getId());
            }

            // Extract the token from the Authorization header
            String authHeader = request.getAuthenticationHeader();
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: No token provided");
            }
            String[] authParts = authHeader.split("\\s+");
            String token = authParts[1];

            // Authenticate the token and check if the user is admin
            boolean isAuthenticated = sessionService.authenticateToken(token);
            if (!isAuthenticated) {
                return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: Invalid token");
            }

            boolean isAdmin = sessionService.isAdmin(token);
            if (!isAdmin) {
                return new Response(HttpStatus.FORBIDDEN, HttpContentType.TEXT_PLAIN, "Forbidden: User not admin");
            }

            // If admin is logged in, attempt to create a package
            boolean isPackageSaved = packageService.savePackage(packageCards);
            if (!isPackageSaved) {
                return new Response(HttpStatus.CONFLICT, HttpContentType.TEXT_PLAIN, "Conflict: At least one card in the package already exists");
            }

            // Package created successfully
            return new Response(HttpStatus.CREATED, HttpContentType.TEXT_PLAIN, "Package and cards created");

        } catch (Exception e) {
            // Handle parsing errors or other exceptions
            System.out.println("Error creating package: " + e.getMessage());
            return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Error processing package creation request");
        }
    }


}
