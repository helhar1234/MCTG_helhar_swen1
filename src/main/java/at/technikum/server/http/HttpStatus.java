package at.technikum.server.http;

public enum HttpStatus {
    OK(200, "Request successfully processed"),
    CREATED(201, "Resource successfully created"),
    ACCEPTED(202, "Accepted request"),
    NO_CONTENT(204, "Request processed, no content returned"),
    BAD_REQUEST(400, "Bad request syntax or unsupported request"),
    UNAUTHORIZED(401, "Unauthorized access"),
    FORBIDDEN(403, "Access to the resource is forbidden"),
    NOT_FOUND(404, "Resource not found"),
    METHOD_NOT_ALLOWED(405, "HTTP method not allowed for this endpoint"),
    CONFLICT(409, "Conflict in request, resource already exists"),

    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    NOT_IMPLEMENTED(501, "Requested functionality not implemented");


    private final int code;
    private final String message;

    HttpStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
