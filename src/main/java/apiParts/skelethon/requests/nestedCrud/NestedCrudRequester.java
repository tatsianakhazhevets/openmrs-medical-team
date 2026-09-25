package apiParts.skelethon.requests.nestedCrud;

import apiParts.models.BaseModel;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.NestedCrudEndpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Raw requester for nested resources. Returns the full response without assuming
 * success, so negative scenarios live here.
 *
 * URL assembly, using Endpoint.PATIENT_ALLERGY_NESTED ("/patient/{parentUuid}/allergy"):
 *   pathParam("parentUuid", "abc-123")  ->  /patient/abc-123/allergy
 *   + "/" + childUuid                   ->  /patient/abc-123/allergy/xyz-999
 *
 * parentUuid goes through pathParam because it sits in the middle of the template;
 * childUuid is simply appended because it is always the last segment
 * (and does not exist yet for create()).
 */
public class NestedCrudRequester extends HttpRequest implements NestedCrudEndpoint<ValidatableResponse> {

    private static final String PARENT_UUID = "parentUuid";

    public NestedCrudRequester(RequestSpecification requestSpecification,
                               Endpoint endpoint,
                               ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse create(String parentUuid, BaseModel model) {
        return given()
                .spec(requestSpecification)
                .pathParam(PARENT_UUID, parentUuid)
                .body(model)
                .post(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get(String parentUuid, String childUuid) {
        return given()
                .spec(requestSpecification)
                .pathParam(PARENT_UUID, parentUuid)
                .get(endpoint.getUrl() + "/" + childUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get(String parentUuid, String childUuid, Map<String, ?> queryParams) {
        return given()
                .spec(requestSpecification)
                .pathParam(PARENT_UUID, parentUuid)
                .queryParams(queryParams)
                .get(endpoint.getUrl() + "/" + childUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    // OpenMRS updates via POST to the resource address, not via PUT
    @Override
    public ValidatableResponse update(String parentUuid, String childUuid, BaseModel model) {
        return given()
                .spec(requestSpecification)
                .pathParam(PARENT_UUID, parentUuid)
                .body(model)
                .post(endpoint.getUrl() + "/" + childUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse delete(String parentUuid, String childUuid) {
        return given()
                .spec(requestSpecification)
                .pathParam(PARENT_UUID, parentUuid)
                .delete(endpoint.getUrl() + "/" + childUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse delete(String parentUuid, String childUuid, Map<String, ?> queryParams) {
        return given()
                .spec(requestSpecification)
                .pathParam(PARENT_UUID, parentUuid)
                .queryParams(queryParams)
                .delete(endpoint.getUrl() + "/" + childUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
