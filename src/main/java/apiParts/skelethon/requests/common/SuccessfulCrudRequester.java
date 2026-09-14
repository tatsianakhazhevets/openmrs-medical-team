package apiParts.skelethon.requests.common;

import apiParts.models.BaseModel;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.CrudEndpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class SuccessfulCrudRequester<T extends BaseModel> extends HttpRequest implements CrudEndpoint {

    private CrudRequester crudRequester;

    public SuccessfulCrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public T create(BaseModel model) {
        return (T) crudRequester.create(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public T get(int id) {
        return (T) crudRequester.get(id).extract().as(endpoint.getResponseModel());
    }

    @Override
    public T get() {
        return (T) crudRequester.get().extract().as(endpoint.getResponseModel());
    }

    @Override
    public T update(int id, BaseModel model) {
        return (T) crudRequester.update(id, model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public ValidatableResponse delete(int id) {
        return (ValidatableResponse) crudRequester.delete(id);
    }
}