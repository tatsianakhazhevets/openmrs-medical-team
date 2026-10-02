package apiParts.steps;

import apiParts.models.auth.LoginAdminResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.auth.AuthRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;

public class LoginSteps {
    public static LoginAdminResponse getSessionCheck(String sessionId) {

        new AuthRequester(
                RequestSpecs.authenticatedSpec(sessionId),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsUnauthorized()) // Валидирует статус 401
                .getSession();

        return new LoginAdminResponse();
    }
}