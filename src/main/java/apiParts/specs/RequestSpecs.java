package apiParts.specs;

import apiParts.config.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestSpecs {

    public static final String ADMIN_USERNAME = Config.getProperty("adminUsername");
    public static final String ADMIN_PASSWORD = Config.getProperty("adminPassword");

    private static Map<String, String> authUserTokens =
            new HashMap<>(Map.of(ADMIN_USERNAME, basicAuthHeader(ADMIN_USERNAME, ADMIN_PASSWORD)));

    private RequestSpecs() {
    }

    private static String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private static RequestSpecBuilder defaultRequestSpec() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri(Config.getProperty("apiBaseUrl") + Config.getProperty("apiVersion"));
    }

    public static RequestSpecification unAuthSpec() {
        return defaultRequestSpec()
                .build();
    }

    public static RequestSpecification adminSpec() {
        return defaultRequestSpec()
                .addHeader(Headers.AUTHORIZATION.getHeader(), authUserTokens.get(ADMIN_USERNAME))
                .build();
    }

    public static RequestSpecification authenticatedSpec(String sessionId) {
        return defaultRequestSpec()
                .addCookie("JSESSIONID", sessionId)
                .build();
    }


    /*
    public static RequestSpecification authUserSpec(String username, String password) {
        return defaultRequestSpec()
                .addHeader(Headers.AUTHORIZATION.getHeader(),
                        getUserAuthHeader(username, password))
                .build();
    }

    public static String getUserAuthHeader(String username, String password) {

       String userAuthHeader;

        if (!authUserTokens.containsKey(username)) {
            userAuthHeader = new CrudRequester(
                    RequestSpecs.unAuthSpec(),
                    Endpoint.LOGIN_POST,
                    ResponseSpecs.requestReturnsOk())
                    .post(LoginUserRequest.builder()
                            .username(username)
                            .password(username)
                            .build())
                    .extract()
                    .header("Authorization");

            authUserTokens.put(username, userAuthHeader);
        } else {
            userAuthHeader = authUserTokens.get(username);
        }

        return userAuthHeader;
    }*/


}