package apiParts.skelethon.requests.search;

import apiParts.models.BaseModel;
import apiParts.models.search.SearchParams;
import apiParts.models.search.SearchResponse;
import apiParts.models.search.SearchResult;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.SearchEndpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

/**
 * Wrapper around {@link SearchRequester} for positive scenarios: returns the
 * results already extracted into a {@link SearchResult}, so tests stop doing
 * getResults().stream().filter(...).orElseThrow(...) themselves.
 *
 * OpenMRS answers searches in two shapes, and both are handled here:
 *  - a wrapper object {"results": [...]} - Endpoint#getResponseModel() is the
 *    wrapper (e.g. ListOrdersResponse), which implements SearchResponse;
 *  - a bare JSON array - Endpoint#getResponseModel() is the ITEM model
 *    (e.g. CreateAppointmentResponse for POST /appointments/search).
 */
public class SuccessfulSearchRequester<T> extends HttpRequest implements SearchEndpoint<SearchResult<T>> {

    private final SearchRequester searchRequester;

    public SuccessfulSearchRequester(RequestSpecification requestSpecification,
                                     Endpoint endpoint,
                                     ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.searchRequester =
                new SearchRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public SearchResult<T> search(SearchParams params) {
        return toResult(searchRequester.search(params));
    }

    @Override
    public SearchResult<T> searchByBody(BaseModel body) {
        return toResult(searchRequester.searchByBody(body));
    }

    @SuppressWarnings("unchecked")
    private SearchResult<T> toResult(ValidatableResponse response) {
        Class<? extends BaseModel> model = endpoint.getResponseModel();

        if (SearchResponse.class.isAssignableFrom(model)) {
            SearchResponse<T> wrapper = (SearchResponse<T>) response.extract().as(model);
            return new SearchResult<>(wrapper.getResults());
        }

        return new SearchResult<>((java.util.List<T>) response.extract().jsonPath().getList("", model));
    }
}
