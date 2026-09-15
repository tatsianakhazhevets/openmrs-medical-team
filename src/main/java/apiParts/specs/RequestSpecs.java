package apiParts.specs;

import apiParts.config.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestSpecs {

    private static Map<String, String> authUserTokens = new HashMap<>(Map.of("admin", "Basic YWRtaW46QWRtaW4xMjM="));

    private RequestSpecs() {
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
                .addHeader(Headers.AUTHORIZATION.getHeader(), authUserTokens.get("admin"))
                .build();
    }


}