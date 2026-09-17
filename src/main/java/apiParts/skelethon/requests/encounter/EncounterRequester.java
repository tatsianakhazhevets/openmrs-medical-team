package apiParts.skelethon.requests.encounter;

import apiParts.models.BaseModel;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.EncounterEndpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class EncounterRequester extends HttpRequest implements EncounterEndpoint {
    public EncounterRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse create(BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/encounter")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get(String encounterUuid) {
        return given()
                .spec(requestSpecification)
                .queryParam("v", "full")
                .get("/encounter/" + encounterUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse update(String encounterUuid, BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/encounter/" + encounterUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public Object delete(String encounterUuid) {
        return given()
                .spec(requestSpecification)
                .delete("/encounter/" + encounterUuid)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}