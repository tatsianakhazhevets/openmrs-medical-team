package apiParts.skelethon.requests.action;

import apiParts.models.BaseModel;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.ActionEndpoint;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

/**
 * Wrapper around {@link ActionRequester} for positive scenarios: the body is
 * deserialized into the model declared by Endpoint#getResponseModel().
 *
 * For actions whose response body is irrelevant (e.g. fulfillerdetails, which
 * answers 201 with nothing useful), use the raw ActionRequester instead.
 */
public class SuccessfulActionRequester<T extends BaseModel> extends HttpRequest implements ActionEndpoint<T> {

    private final ActionRequester actionRequester;

    public SuccessfulActionRequester(RequestSpecification requestSpecification,
                                     Endpoint endpoint,
                                     ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.actionRequester = new ActionRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T perform(String resourceUuid, BaseModel body) {
        return (T) actionRequester.perform(resourceUuid, body).extract().as(endpoint.getResponseModel());
    }
}
