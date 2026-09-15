package apiParts.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

public class ResponseSpecs {

    private ResponseSpecs() {
    }

    private static ResponseSpecBuilder defaultResponseSpec() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification requestReturnsCreated() {
        return defaultResponseSpec()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    public static ResponseSpecification requestReturnsOk() {
        return defaultResponseSpec()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    // OpenMRS validation error: 400 + {"error": {"code": "webservices.rest.error.invalid.submission", "fieldErrors": {field: [{code}]}}}
    public static ResponseSpecification requestReturnsInvalidSubmission(String field, String errorCode) {
        return defaultResponseSpec()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody("error.code", Matchers.equalTo("webservices.rest.error.invalid.submission"))
                .expectBody("error.fieldErrors." + field + ".code", Matchers.hasItem(errorCode))
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequest(String errorMessage) {
        return defaultResponseSpec()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .expectBody(Matchers.equalTo(errorMessage))
                .build();
    }

    public static ResponseSpecification requestReturnsForbidden(String errorMessage) {
        return defaultResponseSpec()
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .expectBody((Matchers.equalTo(errorMessage)))
                .build();
    }
}