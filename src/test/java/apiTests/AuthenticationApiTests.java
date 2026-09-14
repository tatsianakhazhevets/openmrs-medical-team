package apiTests;

import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.auth.AuthRequester;
import apiParts.skelethon.requests.auth.SuccessfulAuthRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import org.junit.jupiter.api.Test;

public class AuthenticationApiTests extends BaseTest {

    @Test
    public void adminCanLogin() {
        LoginAdminRequest loginAdminRequest = LoginAdminRequest.builder()
                .username("admin")
                .password("Admin123")
                .build();

        new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .login(loginAdminRequest);
    }

    @Test
    public void adminCannotLoginWithInvalidPassword() {

        LoginAdminRequest loginAdminRequest = LoginAdminRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new AuthRequester(RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .login(loginAdminRequest);
    }

    @Test
    public void userCannotLoginWithInvalidUsername() {

        LoginAdminRequest loginAdminRequest = LoginAdminRequest.builder()
                .username("Admin123")
                .password("Admin123")
                .build();

        new AuthRequester(RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .login(loginAdminRequest);
    }
}