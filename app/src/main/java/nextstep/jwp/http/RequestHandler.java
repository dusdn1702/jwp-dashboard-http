package nextstep.jwp.http;

import nextstep.jwp.controller.AbstractController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

public class RequestHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private final RequestMapping requestMapping = new RequestMapping();

    public RequestHandler(Socket connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (final InputStream inputStream = connection.getInputStream();
             final OutputStream outputStream = connection.getOutputStream()) {
            matchRequest(inputStream, outputStream);
        } catch (IOException | IllegalArgumentException exception) {
            log.error("Exception stream", exception);
        } finally {
            close();
        }
    }

    private void matchRequest(final InputStream inputStream, final OutputStream outputStream) {
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        try {
            HttpRequest httpRequest = new HttpRequest(inputStreamReader);
            HttpResponse httpResponse = new HttpResponse();

            AbstractController abstractController = requestMapping.getAbstractController(httpRequest);
            abstractController.service(httpRequest, httpResponse);

            outputStream.write(httpResponse.getResponse().getBytes());
            outputStream.flush();
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private void close() {
        try {
            connection.close();
        } catch (IOException exception) {
            log.error("Exception closing socket", exception);
        }
    }
}
