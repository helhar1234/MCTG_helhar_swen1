package at.technikum.server.http;

public class Response {

    private int statusCode;

    private String statusMessage;

    private String contentType;

    private String body;

    public Response() {
    }

    public Response(HttpStatus httpStatus, HttpContentType httpContentType, String body) {
        this.statusCode = httpStatus.getCode();
        this.statusMessage = httpStatus.getMessage();
        this.contentType = httpContentType.getMimeType();
        this.body = body;
    }

    public void setStatus(HttpStatus httpStatus) {
        this.statusCode = httpStatus.getCode();
        this.statusMessage = httpStatus.getMessage();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(HttpContentType httpContentType) {
        this.contentType = httpContentType.getMimeType();
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
