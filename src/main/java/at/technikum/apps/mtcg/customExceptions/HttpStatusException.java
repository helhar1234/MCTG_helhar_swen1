package at.technikum.apps.mtcg.customExceptions;

import at.technikum.server.http.HttpStatus;

public class HttpStatusException extends RuntimeException {
    private final HttpStatus status;

    public HttpStatusException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

