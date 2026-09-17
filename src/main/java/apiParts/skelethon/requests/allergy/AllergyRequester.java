package apiParts.skelethon.requests.allergy;

import apiParts.models.BaseModel;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.AllergyEndpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class AllergyRequester extends HttpRequest implements AllergyEndpoint {
    public AllergyRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse create(String patientUuid, BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/patient/" + patientUuid + "/allergy/")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get(String patientUuid, String allergyUuid) {
        return given()
                .spec(requestSpecification)
                .queryParam("v", "full")
                .get("/patient/" + patientUuid + "/allergy/" + allergyUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse update(String patientUuid, String allergyUuid, BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/patient/" + patientUuid + "/allergy/" + allergyUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse delete(String patientUuid, String allergyUuid) {
        return given()
                .spec(requestSpecification)
                .delete("/patient/" + patientUuid + "/allergy/" + allergyUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}