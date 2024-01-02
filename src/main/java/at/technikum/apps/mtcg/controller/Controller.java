package at.technikum.apps.mtcg.controller;

import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;

public abstract class Controller {

    public abstract boolean supports(String route);

    public abstract Response handle(Request request);

    protected Response status(HttpStatus httpStatus) {
        return new Response(httpStatus, HttpContentType.APPLICATION_JSON, "{ \"error\": \""+ httpStatus.getMessage() + "\"}");
    }

    // THOUGHT: more functionality e.g. ok(), json(), etc
}
