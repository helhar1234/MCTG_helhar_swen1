package at.technikum.apps.mtcg.responses;

import at.technikum.server.http.HttpContentType;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Response;

public class ResponseHelper {

    public static Response unauthorizedResponse(String message) {
        return new Response(HttpStatus.UNAUTHORIZED, HttpContentType.TEXT_PLAIN, "Unauthorized: " + message);
    }

    public static Response conflictResponse(String message) {
        return new Response(HttpStatus.CONFLICT, HttpContentType.TEXT_PLAIN, message);
    }

    public static Response internalServerErrorResponse(String message) {
        return new Response(HttpStatus.INTERNAL_SERVER_ERROR, HttpContentType.TEXT_PLAIN, message);
    }

    public static Response forbiddenResponse(String message) {
        return new Response(HttpStatus.FORBIDDEN, HttpContentType.TEXT_PLAIN, "Forbidden: " + message);
    }

    public static Response createdResponse(String message) {
        return new Response(HttpStatus.CREATED, HttpContentType.TEXT_PLAIN, message);
    }

    public static Response okResponse(String message) {
        return new Response(HttpStatus.OK, HttpContentType.TEXT_PLAIN, message);
    }

    public static Response notFoundResponse(String message) {
        return new Response(HttpStatus.NOT_FOUND, HttpContentType.TEXT_PLAIN, "Not Found: " + message);
    }

    public static Response okResponse(String message, HttpContentType contentType) {
        return new Response(HttpStatus.OK, contentType, message);
    }

    public static Response badRequestResponse(String message) {
        return new Response(HttpStatus.BAD_REQUEST, HttpContentType.TEXT_PLAIN, "Bad Request: " + message);
    }

    public static Response noContentResponse(String message) {
        return new Response(HttpStatus.NO_CONTENT, HttpContentType.TEXT_PLAIN, message);
    }
}
