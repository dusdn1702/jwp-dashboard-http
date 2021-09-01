package nextstep.jwp;

import nextstep.jwp.http.HttpStatus;
import nextstep.jwp.http.RequestHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

class RequestHandlerTest {
    private static final String LOGIN_HTML = "static/login.html";
    private static final String LOGIN_CONTENT_LENGTH = "3797";
    private static final String INDEX_HTML = "static/index.html";
    private static final String INDEX_CONTENT_LENGTH = "5564";
    private static final String REGISTER_HTML = "static/register.html";
    private static final String REGISTER_CONTENT_LENGTH = "4319";

    @Test
    @DisplayName("인덱스 페이지에 접속한다.")
    void getIndex() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /index.html HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");
        final MockSocket socket = new MockSocket(httpRequest);
        final RequestHandler requestHandler = new RequestHandler(socket);

        // when
        requestHandler.run();

        // then
        String expected = getHttpResponse(HttpStatus.OK, INDEX_HTML, INDEX_CONTENT_LENGTH);
        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    @DisplayName("로그인 페이지에 접속한다.")
    void getLogin() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");
        final MockSocket socket = new MockSocket(httpRequest);
        final RequestHandler requestHandler = new RequestHandler(socket);

        // when
        requestHandler.run();

        // then
        String expected = getHttpResponse(HttpStatus.OK, LOGIN_HTML, LOGIN_CONTENT_LENGTH);
        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    @DisplayName("로그인한다.")
    void postLogin() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "POST /login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Content-Length: 30",
                "",
                "account=gugu&password=password");
        final MockSocket socket = new MockSocket(httpRequest);
        final RequestHandler requestHandler = new RequestHandler(socket);

        // when
        requestHandler.run();

        // then
        String expected = getRedirectHttpResponse(HttpStatus.FOUND, "index.html");
        String output = socket.output();
        assertThat(output).isEqualTo(expected);
    }

    @Test
    @DisplayName("로그인에 실패해 401 페이지로 리다이렉트한다.")
    void postLoginException() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "POST /login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Content-Length: 25",
                "",
                "account=gugu&password=pwd");
        final MockSocket socket = new MockSocket(httpRequest);
        final RequestHandler requestHandler = new RequestHandler(socket);

        // when
        requestHandler.run();

        // then
        String expected = getHttpResponse(HttpStatus.UNAUTHORIZED, "static/401.html", "2426");
        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    @DisplayName("회원가입 페이지에 접속한다.")
    void getRegister() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /register HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");
        final MockSocket socket = new MockSocket(httpRequest);
        final RequestHandler requestHandler = new RequestHandler(socket);

        // when
        requestHandler.run();

        // then
        String expected = getHttpResponse(HttpStatus.OK, REGISTER_HTML, REGISTER_CONTENT_LENGTH);
        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    @DisplayName("회원가입한다.")
    void postRegister() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "POST /register HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Content-Length: 43",
                "Connection: keep-alive ",
                "",
                "account=user&password=pwd&email=email@email");
        final MockSocket socket = new MockSocket(httpRequest);
        final RequestHandler requestHandler = new RequestHandler(socket);

        // when
        requestHandler.run();

        // then
        String expected = getHttpResponse(HttpStatus.CREATED, INDEX_HTML, INDEX_CONTENT_LENGTH);
        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    @DisplayName("회원가입에 실패해 500 페이지로 리다이렉트한다.")
    void postRegisterException() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "POST /register HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Content-Length: 43 ",
                "Connection: keep-alive ",
                "",
                "account=user&password=pwd");
        final MockSocket socket = new MockSocket(httpRequest);
        final RequestHandler requestHandler = new RequestHandler(socket);

        // when
        requestHandler.run();

        // then
        String expected = getHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, "static/500.html", "2357");
        assertThat(socket.output()).isEqualTo(expected);
    }

    private String getHttpResponse(HttpStatus httpStatus, String resource, String length) throws IOException {
        final URL url = getClass().getClassLoader().getResource(resource);
        return "HTTP/1.1 " + httpStatus.toString() + "\r\n" +
                "Content-Type: text/html;charset=utf-8 \r\n" +
                "Content-Length: " + length + " \r\n" +
                "\r\n" +
                new String(Files.readAllBytes(new File(url.getFile()).toPath()));
    }

    private static String getRedirectHttpResponse(final HttpStatus status, final String location) {
        return String.join("\r\n",
                "HTTP/1.1 " + status.toString(),
                "Location: " + location,
                "");
    }
}
