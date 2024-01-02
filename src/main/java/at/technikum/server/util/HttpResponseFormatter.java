
package at.technikum.server.util;

import at.technikum.server.http.Response;

public class HttpResponseFormatter {

    public String toResponseString(Response response) {
        return "HTTP/1.1 " + response.getStatusCode() + " " + response.getStatusMessage() + "\r\n" +
                "Content-Type: " + response.getContentType() + "\r\n" +
                "Content-Length: " + response.getBody().length() + "\r\n" +
                "\r\n" +
                response.getBody();
    }
}
