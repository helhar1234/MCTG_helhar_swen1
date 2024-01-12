package at.technikum.apps.mtcg.controller;

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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        User requester = sessionService.authenticateRequest(request);
        PackageCard[] packageCards;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            packageCards = objectMapper.readValue(request.getBody(), PackageCard[].class);
        } catch (JsonProcessingException e) {
            return ResponseHelper.badRequestResponse("Error parsing package data: " + e.getMessage());
        }

        boolean isPackageSaved = packageService.savePackage(requester, packageCards);
        return ResponseHelper.createdResponse("Package and cards created");

    }


}
