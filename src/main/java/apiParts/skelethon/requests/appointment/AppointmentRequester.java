package apiParts.skelethon.requests.appointment;

import apiParts.models.BaseModel;
import apiParts.models.appointment.AppointmentSearchRequest;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class AppointmentRequester extends HttpRequest {
    public AppointmentRequester(
            RequestSpecification requestSpecification,
            Endpoint endpoint,
            ResponseSpecification responseSpecification) {

        super(requestSpecification, endpoint, responseSpecification);
    }

    public ValidatableResponse get(Map<String, ?> queryParams) {
        return given()
                .spec(requestSpecification)
                .queryParams(queryParams)
                .get(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
    public ValidatableResponse changeStatus(
            String appointmentUUID,
            BaseModel model) {

        return given()
                .spec(requestSpecification)
                .body(model)
                .post(endpoint.getUrl() + "/" + appointmentUUID + "/status-change")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
    public ValidatableResponse update(BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
    public ValidatableResponse search(AppointmentSearchRequest request) {
        return given()
                .spec(requestSpecification)
                .body(request)
                .post(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
