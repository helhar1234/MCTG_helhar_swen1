package at.technikum.apps.mtcg.controller;

import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;

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
            return status(HttpStatus.BAD_REQUEST);
        } else if (request.getRoute().equals("/transactions/packages")) {
            switch (request.getMethod()) {
                case "POST":
                    return buyPackage(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private Response buyPackage(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "buyPackage");
    }

    private Response createPackage(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "createPackage");
    }
}
