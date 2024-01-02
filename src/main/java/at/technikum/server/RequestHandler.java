package at.technikum.server;

import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.util.HttpRequestParser;
import at.technikum.server.util.HttpResponseFormatter;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestHandler {

    private BufferedReader in;
    private PrintWriter out;

    private final Socket client;

    private final ServerApplication app;

    // Instances of HttpRequestParser and HttpResponseFormatter
    private final HttpRequestParser requestParser = new HttpRequestParser();
    private final HttpResponseFormatter responseFormatter = new HttpResponseFormatter();


    public RequestHandler(Socket client, ServerApplication app) {
        this.client = client;
        this.app = app;
    }

    public void handle() throws IOException {
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        String httpRequest = getHttpStringFromStream(in);

        // Use instance of HttpRequestParser
        Request request = requestParser.toRequestObject(httpRequest);
        Response response = app.handle(request);

        out = new PrintWriter(client.getOutputStream(), true);

        // Use instance of HttpResponseFormatter
        out.write(responseFormatter.toResponseString(response));

        out.close();
        in.close();
        client.close();
    }

    // THOUGHT: create a SocketReader class
    private String getHttpStringFromStream(BufferedReader in) throws IOException {
        StringBuilder builder = new StringBuilder();

        String inputLine;
        while ((inputLine = in.readLine()) != null && !inputLine.equals("")) {
            builder
                    .append(inputLine)
                    .append(System.lineSeparator());
        }

        String httpRequest = builder.toString();

        Pattern regex = Pattern.compile("^Content-Length:\\s(.+)", Pattern.MULTILINE);
        Matcher matcher = regex.matcher(httpRequest);

        if (!matcher.find()) {
            return builder.toString();
        }

        builder.append(System.lineSeparator());

        int contentLength = Integer.parseInt(matcher.group(1));
        char[] buffer = new char[contentLength];
        in.read(buffer, 0, contentLength);
        builder.append(buffer);

        return builder.toString();
    }
}
