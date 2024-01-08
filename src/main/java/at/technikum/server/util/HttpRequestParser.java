package at.technikum.server.util;

import at.technikum.server.http.HttpMethod;
import at.technikum.server.http.Request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequestParser {

    public Request toRequestObject(String httpRequest) {
        Request request = new Request();

        request.setMethod(getHttpMethod(httpRequest));
        request.setRoute(getRoute(httpRequest));

        parseHeaders(httpRequest, request);

        retrieveHostAndContentLength(httpRequest, request);

        return request;
    }

    private void parseHeaders(String httpRequest, Request request) {
        Pattern headerPattern = Pattern.compile("^(.+?):\s*(.+)$", Pattern.MULTILINE);
        Matcher matcher = headerPattern.matcher(httpRequest);
        while (matcher.find()) {
            request.addHeader(matcher.group(1), matcher.group(2));
        }
    }

    private void retrieveHostAndContentLength(String httpRequest, Request request) {
        String host = request.getHeader("Host");
        String contentLengthHeader = request.getHeader("Content-Length");

        request.setHost(host);

        if (contentLengthHeader != null) {
            int contentLength = Integer.parseInt(contentLengthHeader);
            request.setContentLength(contentLength);

            if (contentLength > 0) {
                request.setBody(httpRequest.substring(httpRequest.length() - contentLength));
            }
        }
    }

    private HttpMethod getHttpMethod(String httpRequest) {
        String httpMethod = httpRequest.split(" ")[0];
        return HttpMethod.valueOf(httpMethod);
    }

    private String getRoute(String httpRequest) {
        return httpRequest.split(" ")[1];
    }
}