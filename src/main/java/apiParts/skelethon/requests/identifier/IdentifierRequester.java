package apiParts.skelethon.requests.identifier;

import apiParts.models.BaseModel;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.IdentifierEndpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class IdentifierRequester extends HttpRequest implements IdentifierEndpoint {
    public IdentifierRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse get(String patientUuid, String identifierUuid) {
        return given()
                .spec(requestSpecification)
                .get(endpoint.getUrl() + "/" + patientUuid + "/identifier/" + identifierUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse update(String patientUuid, String identifierUuid, BaseModel model) {

        return given()
                .spec(requestSpecification)
                .body(model)
                .post(endpoint.getUrl() + "/" + patientUuid + "/identifier/" + identifierUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse delete(String patientUuid, String identifierUuid, Map<String, ?> queryParams) {

        return given()
                .spec(requestSpecification)
                .queryParams(queryParams)
                .delete(endpoint.getUrl() + "/" + patientUuid + "/identifier/" + identifierUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}