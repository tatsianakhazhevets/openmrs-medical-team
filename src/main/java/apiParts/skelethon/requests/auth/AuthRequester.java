package apiParts.skelethon.requests.auth;

import apiParts.models.auth.LoginAdminRequest;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.AuthEndpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class AuthRequester extends HttpRequest implements AuthEndpoint {
    public AuthRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse login(LoginAdminRequest loginAdminRequest) {
        return given()
                .spec(requestSpecification)
                .auth()
                .preemptive()
                .basic(loginAdminRequest.getUsername(),
                        loginAdminRequest.getPassword())
                .get(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}