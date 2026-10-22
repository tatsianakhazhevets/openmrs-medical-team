package apiParts.skelethon.base_request;

import apiParts.skelethon.endpoints.Endpoint;
import io.qameta.allure.Allure;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.function.Supplier;

public abstract class HttpRequest {
    protected RequestSpecification requestSpecification;
    protected Endpoint endpoint;
    protected ResponseSpecification responseSpecification;

    public HttpRequest(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        this.requestSpecification = requestSpecification;
        this.endpoint = endpoint;
        this.responseSpecification = responseSpecification;
    }

    protected ValidatableResponse requestStep(String method, String path, Supplier<ValidatableResponse> request) {
        return Allure.step(method + " " + path, request::get);
    }
}