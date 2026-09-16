package apiParts.skelethon.requests.patient;

import apiParts.models.BaseModel;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.PatientEndpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class PatientRequester extends HttpRequest implements PatientEndpoint {
    public PatientRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse create(String patientUuid, BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post(endpoint.getUrl() + "/" + patientUuid + "/identifier")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse update(String uuid, BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post(endpoint.getUrl() + "/" + uuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse delete(String uuid, Map<String, ?> queryParams) {
        return given()
                .spec(requestSpecification)
                .queryParams(queryParams)
                .delete(endpoint.getUrl() + "/" + uuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
