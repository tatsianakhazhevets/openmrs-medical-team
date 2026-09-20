package apiParts.skelethon.requests.search;

import apiParts.models.BaseModel;
import apiParts.models.search.SearchParams;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.SearchEndpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

/**
 * Raw search requester. Returns the full response without assuming success,
 * so negative scenarios (unauthorised, invalid paging, malformed filter) live here.
 */
public class SearchRequester extends HttpRequest implements SearchEndpoint<ValidatableResponse> {

    public SearchRequester(RequestSpecification requestSpecification,
                           Endpoint endpoint,
                           ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse search(SearchParams params) {
        return given()
                .spec(requestSpecification)
                .queryParams(params.toQueryParams())
                .get(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse searchByBody(BaseModel body) {
        return given()
                .spec(requestSpecification)
                .body(body)
                .post(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
