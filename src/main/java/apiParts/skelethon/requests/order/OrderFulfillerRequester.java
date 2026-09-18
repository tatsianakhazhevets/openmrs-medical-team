package apiParts.skelethon.requests.order;

import apiParts.models.BaseModel;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.OrderFulfillerEndpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class OrderFulfillerRequester extends HttpRequest implements OrderFulfillerEndpoint {
    public OrderFulfillerRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse updateFulfillerDetails(String orderUuid, BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post(endpoint.getUrl() + "/" + orderUuid + "/fulfillerdetails/")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
