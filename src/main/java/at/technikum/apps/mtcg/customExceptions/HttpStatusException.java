package at.technikum.apps.mtcg.customExceptions;

import at.technikum.server.http.HttpStatus;

/**
 * Custom exception class for handling HTTP status related exceptions.
 * This class extends RuntimeException and includes an HTTP status code.
 */
public class HttpStatusException extends RuntimeException {
    // The HTTP status code associated with this exception
    private final HttpStatus status;

    /**
     * Constructs an HttpStatusException with the specified HTTP status and detail message.
     *
     * @param status  the HTTP status code associated with the exception
     * @param message the detailed message (which is saved for later retrieval by the getMessage() method)
     */
    public HttpStatusException(HttpStatus status, String message) {
        super(message); // Call to the superclass constructor to set the message
        this.status = status;
    }

    /**
     * Retrieves the HTTP status associated with this exception.
     *
     * @return The HTTP status code.
     */
    public HttpStatus getStatus() {
        return status;
    }
}
