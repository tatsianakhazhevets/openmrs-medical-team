package apiParts.skelethon.requests.encounter;

import apiParts.models.BaseModel;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.EncounterEndpoint;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Map;

public class SuccessfulEncounterRequester<T extends BaseModel> extends HttpRequest implements EncounterEndpoint {

    private EncounterRequester encounterRequester;

    public SuccessfulEncounterRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.encounterRequester =
                new EncounterRequester(
                        requestSpecification,
                        endpoint,
                        responseSpecification);
    }

    @Override
    public T create(BaseModel model) {
        return (T) encounterRequester
                .create(model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public T get(String encounterUuid) {
        return (T) encounterRequester
                .get(encounterUuid)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public T update(String encounterUuid, BaseModel model) {
        return (T) encounterRequester
                .update(encounterUuid, model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public Object delete(String encounterUuid) {
        return encounterRequester.delete(encounterUuid);
    }
}