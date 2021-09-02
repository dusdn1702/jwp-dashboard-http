package nextstep.jwp.controller;

import nextstep.jwp.http.*;
import nextstep.jwp.model.User;
import nextstep.jwp.service.UserService;

import java.util.Map;
import java.util.function.Function;

import static nextstep.jwp.controller.JwpController.*;

public class UserController extends AbstractController {
    @Override
    protected void doGet(final HttpRequest request, HttpResponse response) {
        final Map<String, Function<HttpRequest, HttpResponse>> mappedFunction = Map.of(
                "login", this::getLogin,
                "register", this::getRegister
        );
        response.setResponse(getHttpResponse(request, mappedFunction).getResponse());
    }

    @Override
    protected void doPost(final HttpRequest request, HttpResponse response) {
        final Map<String, Function<HttpRequest, HttpResponse>> mappedFunction = Map.of(
                "login", this::postLogin,
                "register", this::postRegister
        );
        response.setResponse(getHttpResponse(request, mappedFunction).getResponse());
    }

    private HttpResponse getLogin(final HttpRequest request) {
        HttpCookie httpCookie = new HttpCookie(request.getRequestHeaders());
        if (httpCookie.containsLogin()) {
            return new HttpResponse(HttpStatus.FOUND, httpCookie, INDEX_PAGE);
        }
        return new HttpResponse(HttpStatus.OK, HttpContentType.NOTHING, "login.html");
    }

    private HttpResponse getRegister(final HttpRequest request) {
        return new HttpResponse(HttpStatus.OK, HttpContentType.NOTHING, "register.html");
    }

    private HttpResponse postLogin(final HttpRequest request) {
        try {
            HttpCookie httpCookie = new HttpCookie(request.getRequestHeaders());
            if (httpCookie.containsLogin()) {
                throw new IllegalArgumentException("이미 로그인한 유저입니다.");
            }
            Map<String, String> requestParams = request.parseRequestBodyParams();
            User user = UserService.findUser(requestParams);
            httpCookie.setCookies();
            log.info("로그인한 유저 : {}", user);
            return new HttpResponse(HttpStatus.FOUND, httpCookie, INDEX_PAGE);
        } catch (IllegalArgumentException e) {
            log.error("에러 발생 : {}", e.getMessage());
            return UNAUTHORIZED_RESPONSE;
        }
    }

    private HttpResponse postRegister(final HttpRequest request) {
        try {
            Map<String, String> params = request.parseRequestBodyParams();
            User user = UserService.registerUser(params);
            log.info("회원가입된 유저 : {}", user);
            return new HttpResponse(HttpStatus.CREATED, HttpContentType.NOTHING, INDEX_PAGE);
        } catch (IllegalArgumentException e) {
            log.error("에러 발생 : {}", e.getMessage());
            return INTERNAL_SERVER_RESPONSE;
        }
    }
}
