package at.technikum.apps.mtcg.controller;

import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;

public class TradingController extends Controller {
    @Override
    public boolean supports(String route) {
        return route.startsWith("/tradings");
    }

    @Override
    public Response handle(Request request) {
        if (request.getRoute().equals("/tradings")) {
            switch (request.getMethod()) {
                case "GET":
                    return getTradings(request);
                case "POST":
                    return createTrading(request);
                case "DELETE":
                    return deleteTrading(request);
            }
            return status(HttpStatus.BAD_REQUEST);
        }
        String[] routeParts = request.getRoute().split("/");
        String tradingDealId = routeParts[2];

        switch (request.getMethod()) {
            case "POST":
                return executeTrade(tradingDealId);
        }
        return status(HttpStatus.BAD_REQUEST);
    }

    private Response executeTrade(String tradingDealId) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "executeTrade");
    }

    private Response deleteTrading(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "deleteTrading");
    }

    private Response createTrading(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "createTrading");
    }

    private Response getTradings(Request request) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, "getTradings");
    }
}
