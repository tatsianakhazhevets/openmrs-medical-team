package apiTests;

import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.auth.AuthRequester;
import apiParts.skelethon.requests.auth.SuccessfulAuthRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class AuthenticationApiTests extends BaseTest {

    private static final String VALID_USERNAME = RequestSpecs.ADMIN_USERNAME;
    private static final String VALID_PASSWORD = RequestSpecs.ADMIN_PASSWORD;

    @Test
    public void adminCanLogin() {
        LoginAdminRequest loginAdminRequest = LoginAdminRequest.builder()
                .username(VALID_USERNAME)
                .password(VALID_PASSWORD)
                .build();

        LoginAdminResponse loginAdminResponse = new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .login(loginAdminRequest);

        softly.assertThat(loginAdminResponse.isAuthenticated()).isTrue();
        softly.assertThat(loginAdminResponse.getUser()).isNotNull();
        softly.assertThat(loginAdminResponse.getUser().getDisplay()).isEqualTo(VALID_USERNAME);
        softly.assertThat(loginAdminResponse.getAllowedLocales()).isNotEmpty();
        softly.assertThat(loginAdminResponse.getCurrentProvider()).isNotNull();
    }


    static Stream<Arguments> invalidCredentials() {
        return Stream.of(
                Arguments.of(VALID_USERNAME, "wrongPassword"),
                Arguments.of("wrongUser", VALID_PASSWORD),
                Arguments.of("wrongUser", "wrongPassword"),
                Arguments.of("", VALID_PASSWORD),
                Arguments.of(VALID_USERNAME, ""),
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
                .username(VALID_USERNAME)
                .password(VALID_PASSWORD)
                .build();

        ValidatableResponse loginResponse = new AuthRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .login(loginAdminRequest);

        String sessionId = loginResponse.extract().cookie("JSESSIONID");

        new AuthRequester(
                RequestSpecs.authenticatedSpec(sessionId),
                Endpoint.LOGOUT_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .logout();

        new AuthRequester(
                RequestSpecs.authenticatedSpec(sessionId),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsUnauthorized())
                .getSession();
    }
}