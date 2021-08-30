package nextstep.jwp;

import nextstep.jwp.http.HttpRequest;
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

    private void matchRequest(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        HttpRequest httpRequest = new HttpRequest(inputStreamReader);

        String response = httpRequest.getHttpMethod().matches(httpRequest);

        outputStream.write(response.getBytes());
        outputStream.flush();
    }

    private void close() {
        try {
            connection.close();
        } catch (IOException exception) {
            log.error("Exception closing socket", exception);
        }
    }
}
