package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.dto.PackageCard;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.responses.ResponseHelper;
import at.technikum.apps.mtcg.service.PackageService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public PackageController(PackageService packageService, SessionService sessionService) {
        this.packageService = packageService;
        this.sessionService = sessionService;
    }


    /**
     * Handles the creation of a new package of cards.
     *
     * @param request The HTTP request containing the package creation information.
     * @return A Response object indicating the success or failure of the package creation.
     */
    private Response createPackage(Request request) {
        // Authenticate the user making the request
        User requester = sessionService.authenticateRequest(request);

        // Declare an array to hold the package cards from the request
        PackageCard[] packageCards;
        try {
            // Create an ObjectMapper instance for JSON processing
            ObjectMapper objectMapper = new ObjectMapper();

            // Read the package cards from the request body and convert them into a PackageCard array
            packageCards = objectMapper.readValue(request.getBody(), PackageCard[].class);
        } catch (JsonProcessingException e) {
            // In case of JSON processing errors, return a bad request response with error details
            return ResponseHelper.badRequestResponse("Error parsing package data: " + e.getMessage());
        }

        // Save the package of cards for the authenticated user
        boolean isPackageSaved = packageService.savePackage(requester, packageCards);

        // Return a response indicating the package and cards have been successfully created
        return ResponseHelper.createdResponse("Package and cards created");
    }


}
