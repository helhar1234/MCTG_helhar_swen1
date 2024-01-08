package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.customExceptions.NotFoundException;
import at.technikum.apps.mtcg.customExceptions.UnauthorizedException;
import at.technikum.apps.mtcg.entity.PackageCard;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.CardService;
import at.technikum.apps.mtcg.service.PackageService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLException;
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

    public PackageController(PackageService packageService, SessionService sessionService, UserService userService, CardService cardService) {
        this.packageService = packageService;
        this.sessionService = sessionService;
        this.userService = userService;
        this.cardService = cardService;
    }


    private Response createPackage(Request request) {
        try {
            // Deserialize the request body to PackageCard[]
            ObjectMapper objectMapper = new ObjectMapper();
            PackageCard[] packageCards = objectMapper.readValue(request.getBody(), PackageCard[].class);

            // Check if there are exactly 5 cards in the package
            if (packageCards.length != 5) {
                return ResponseHelper.conflictResponse("A package must contain exactly 5 cards");
            }

            // Check for duplicate or existing cards
            Set<String> cardIds = new HashSet<>();
            for (PackageCard card : packageCards) {
                if (!cardIds.add(card.getId()) || cardService.findCardById(card.getId()).isPresent()) {
                    return ResponseHelper.conflictResponse("Duplicate or existing cards found in the package");
                }
            }

            // Authenticate the user and check if they are admin
            User user = sessionService.authenticateRequest(request);
            if (!user.isAdmin()) {
                return ResponseHelper.forbiddenResponse("User is not an admin");
            }

            // Attempt to create a package
            boolean isPackageSaved = packageService.savePackage(packageCards);
            if (!isPackageSaved) {
                return ResponseHelper.conflictResponse("At least one card in the package already exists");
            }

            // Package created successfully
            return ResponseHelper.createdResponse("Package and cards created");

        } catch (UnauthorizedException | NotFoundException e) {
            return ResponseHelper.unauthorizedResponse(e.getMessage());
        } catch (SQLException e) {
            return ResponseHelper.internalServerErrorResponse("Database error: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseHelper.internalServerErrorResponse("Runtime error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseHelper.internalServerErrorResponse("Unexpected error: " + e.getMessage());
        }
    }


}
