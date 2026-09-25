package apiParts.skelethon.requests.action;

import apiParts.models.BaseModel;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.ActionEndpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

/**
 * Raw action requester. Returns the full response without assuming success,
 * so negative scenarios (unauthorised, unknown resource) live here.
 *
 * URL assembly, using Endpoint.APPOINTMENT_CHANGE_STATUS ("/appointments/{resourceUuid}/status-change"):
 *   pathParam("resourceUuid", "abc-123")  ->  /appointments/abc-123/status-change
 */
public class ActionRequester extends HttpRequest implements ActionEndpoint<ValidatableResponse> {

    private static final String RESOURCE_UUID = "resourceUuid";

    public ActionRequester(RequestSpecification requestSpecification,
                           Endpoint endpoint,
                           ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse perform(String resourceUuid, BaseModel body) {
        return given()
                .spec(requestSpecification)
                .pathParam(RESOURCE_UUID, resourceUuid)
                .body(body)
                .post(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
