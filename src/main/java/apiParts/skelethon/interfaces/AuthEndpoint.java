package apiParts.skelethon.interfaces;

import apiParts.models.LoginAdminRequest;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

public interface AuthEndpoint {
    Object login(LoginAdminRequest loginAdminRequest);
}