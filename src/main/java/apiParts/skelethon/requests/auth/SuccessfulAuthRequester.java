package apiParts.skelethon.requests.auth;

import apiParts.models.BaseModel;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.AuthEndpoint;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class SuccessfulAuthRequester<T extends BaseModel> extends HttpRequest implements AuthEndpoint {

    private AuthRequester authRequester;

    public SuccessfulAuthRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.authRequester = new AuthRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public T login(LoginAdminRequest loginAdminRequest) {
        return (T) authRequester.login(loginAdminRequest).extract().as(LoginAdminResponse.class);
    }
}