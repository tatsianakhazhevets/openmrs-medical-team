package apiTests;

import apiParts.assertions.ModelAssertions;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.auth.AuthRequester;
import apiParts.skelethon.requests.auth.SuccessfulAuthRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.LoginSteps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class AuthenticationApiTests extends BaseTest {

    @Test
    public void adminCanLogin() {
        LoginAdminRequest loginAdminRequest = LoginAdminRequest.builder()
                .username(RequestSpecs.ADMIN_USERNAME)
                .password(RequestSpecs.ADMIN_PASSWORD)
                .build();

        LoginAdminResponse loginAdminResponse = new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .login(loginAdminRequest);

        ModelAssertions.assertThatModels(softly, loginAdminRequest, loginAdminResponse).match();
        softly.assertThat(loginAdminResponse.isAuthenticated()).isTrue();
        softly.assertThat(loginAdminResponse.getUser()).isNotNull();
        softly.assertThat(loginAdminResponse.getAllowedLocales()).isNotEmpty();
        softly.assertThat(loginAdminResponse.getCurrentProvider()).isNotNull();
    }

    static Stream<Arguments> invalidCredentials() {
        return Stream.of(
                Arguments.of(RequestSpecs.ADMIN_USERNAME, "wrongPassword"),
                Arguments.of("wrongUser", RequestSpecs.ADMIN_PASSWORD),
                Arguments.of("wrongUser", "wrongPassword"),
                Arguments.of("", RequestSpecs.ADMIN_PASSWORD),
                Arguments.of(RequestSpecs.ADMIN_USERNAME, ""),
                Arguments.of("", ""));
    }

    @ParameterizedTest
    @MethodSource("invalidCredentials")
    public void adminCannotLoginWithInvalidCredentials(String username, String password) {
        LoginAdminRequest loginAdminRequest = LoginAdminRequest.builder()
                .username(username)
                .password(password)
                .build();

        LoginAdminResponse loginAdminResponse = new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .login(loginAdminRequest);

        softly.assertThat(loginAdminResponse.isAuthenticated()).isFalse();
        softly.assertThat(loginAdminResponse.getUser()).isNull();
        softly.assertThat(loginAdminResponse.getCurrentProvider()).isNull();
        softly.assertThat(loginAdminResponse.getAllowedLocales()).isNotEmpty();
    }

    @Test
    public void adminCanLogout() {
        LoginAdminRequest loginAdminRequest = LoginAdminRequest.builder()
                .username(RequestSpecs.ADMIN_USERNAME)
                .password(RequestSpecs.ADMIN_PASSWORD)
                .build();

        LoginAdminResponse loginResponse = new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .login(loginAdminRequest);

        String sessionId = loginResponse.getSessionId();

        new AuthRequester(
                RequestSpecs.authenticatedSpec(sessionId),
                Endpoint.LOGOUT_DELETE,
                ResponseSpecs.requestReturnsUnauthorized())
                .logout();

        LoginAdminResponse loginAfter = LoginSteps.getSessionCheck(sessionId);
        softly.assertThat(loginAfter.isAuthenticated()).isFalse();
    }
}