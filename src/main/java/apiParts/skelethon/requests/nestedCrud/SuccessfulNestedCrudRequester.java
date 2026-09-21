package apiParts.skelethon.requests.nestedCrud;

import apiParts.models.BaseModel;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.NestedCrudEndpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Map;

/**
 * Wrapper around {@link NestedCrudRequester} for positive scenarios:
 * responseSpecification asserts the status, and the body is deserialized here
 * into the model declared by Endpoint#getResponseModel().
 */
public class SuccessfulNestedCrudRequester<T extends BaseModel> extends HttpRequest
        implements NestedCrudEndpoint<T> {

    private final NestedCrudRequester nestedCrudRequester;

    public SuccessfulNestedCrudRequester(RequestSpecification requestSpecification,
                                         Endpoint endpoint,
                                         ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.nestedCrudRequester =
                new NestedCrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public T create(String parentUuid, BaseModel model) {
        return as(nestedCrudRequester.create(parentUuid, model));
    }

    @Override
    public T get(String parentUuid, String childUuid) {
        return as(nestedCrudRequester.get(parentUuid, childUuid));
    }

    @Override
    public T get(String parentUuid, String childUuid, Map<String, ?> queryParams) {
        return as(nestedCrudRequester.get(parentUuid, childUuid, queryParams));
    }

    @Override
    public T update(String parentUuid, String childUuid, BaseModel model) {
        return as(nestedCrudRequester.update(parentUuid, childUuid, model));
    }

    @Override
    public ValidatableResponse delete(String parentUuid, String childUuid) {
        return nestedCrudRequester.delete(parentUuid, childUuid);
    }

    @Override
    public ValidatableResponse delete(String parentUuid, String childUuid, Map<String, ?> queryParams) {
        return nestedCrudRequester.delete(parentUuid, childUuid, queryParams);
    }

    @SuppressWarnings("unchecked")
    private T as(ValidatableResponse response) {
        return (T) response.extract().as(endpoint.getResponseModel());
    }
}
