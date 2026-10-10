package uiTests;

import apiParts.generators.RandomNameGenerator;
import apiParts.generators.RandomUuidGenerator;
import apiParts.models.Location;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.auth.SuccessfulAuthRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.Test;
import uiParts.pages.HomeServiceQueues;
import uiParts.pages.LocationPickerPage;
import uiParts.pages.LoginPage;

import static apiParts.specs.ResponseSpecs.SESSION_FIELD;
import static org.assertj.core.api.Assertions.assertThat;
import static uiParts.errorMessages.AuthUiErrorMessage.INVALID_USERNAME_OR_PASSWORD;
import static uiParts.pages.HomeServiceQueues.EXPECTED_HEADER;

public class AuthenticationUiTests extends BaseUiTest {

    @Test
    public void adminCanLoginWithCorrectDataTest() {
        //1. Test data
        String locationUuid = Location.getRandomLocation().getUuid();

        //2. Test steps
        HomeServiceQueues adminLogin = new LoginPage()
                .open()
                .login(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD)
                .getPage(LocationPickerPage.class)
                .selectLocation(locationUuid)
                .confirm()
                .getPage(HomeServiceQueues.class);

        //3. Test result
        SelenideElement queueHeader = adminLogin.getQueueHeader();
        queueHeader.shouldBe(Condition.visible);
        assertThat(queueHeader.isDisplayed()).isTrue();
        assertThat(queueHeader.getText()).contains(EXPECTED_HEADER);

        String sessionId = WebDriverRunner.getWebDriver().manage().getCookieNamed(SESSION_FIELD).getValue();

        LoginAdminResponse session = new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.authenticatedSpec(sessionId),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .getSession();

        assertThat(session.isAuthenticated()).isTrue();
        assertThat(session.getCurrentProvider()).isNotNull();
        assertThat(session.getCurrentProvider().getUuid()).isNotBlank();
        assertThat(session.getSessionLocation().getUuid()).isEqualTo(locationUuid);
    }

    @Test
    public void userCannotLoginWithInvalidCredentialsTest() {
        //1. Test data
        String invalidUsername = RandomNameGenerator.generateNonExistingName();
        String invalidPassword = RandomUuidGenerator.generateUuid();

        //2. Test steps
        LoginPage adminLogin = new LoginPage()
                .open()
                .login(invalidUsername, invalidPassword);

        //3. Test result
        SelenideElement errorMessage = adminLogin.getErrorMessage();
        errorMessage.shouldBe(Condition.visible);
        assertThat(errorMessage.isDisplayed()).isTrue();
        assertThat(errorMessage.getText()).contains(INVALID_USERNAME_OR_PASSWORD.getMessage());

        String sessionId = WebDriverRunner.getWebDriver().manage().getCookieNamed(SESSION_FIELD).getValue();

        LoginAdminResponse session = new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.authenticatedSpec(sessionId),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .getSession();

        assertThat(session.isAuthenticated()).isFalse();
        assertThat(session.getCurrentProvider()).isNull();
    }

    @Test
    public void userCannotSubmitEmptyUsernameTest() {
        //1. Test data
        LoginAdminRequest loginAdminRequest = LoginAdminRequest.builder()
                .username(RequestSpecs.ADMIN_USERNAME)
                .password(RequestSpecs.ADMIN_PASSWORD)
                .build();
        loginAdminRequest.setUsername("");

        //2. Test steps
        LoginPage loginPage = new LoginPage()
                .open()
                .enterUsername(loginAdminRequest.getUsername())
                .clickContinue();

        //3. Test result
        SelenideElement passwordInput = loginPage.getPasswordInput();
        passwordInput.shouldBe(Condition.hidden);
        assertThat(passwordInput.isDisplayed()).isFalse();

        String sessionId = WebDriverRunner.getWebDriver().manage().getCookieNamed(SESSION_FIELD).getValue();

        LoginAdminResponse session = new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.authenticatedSpec(sessionId),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .getSession();

        assertThat(session.isAuthenticated()).isFalse();
        assertThat(session.getCurrentProvider()).isNull();
    }

    @Test
    public void userCannotConfirmLocationWithoutSelectingItTest() {
        //2. Test steps
        LocationPickerPage locationPickerPage = new LoginPage()
                .open()
                .login(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD)
                .getPage(LocationPickerPage.class);

        //3. Test result
        SelenideElement confirmButton = locationPickerPage.getConfirmButton();
        confirmButton.shouldBe(Condition.visible);
        assertThat(confirmButton.isEnabled()).isFalse();

        String sessionId = WebDriverRunner.getWebDriver().manage().getCookieNamed(SESSION_FIELD).getValue();

        LoginAdminResponse session = new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.authenticatedSpec(sessionId),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .getSession();

        assertThat(session.isAuthenticated()).isTrue();
        assertThat(session.getCurrentProvider()).isNotNull();
        assertThat(session.getSessionLocation()).isNull();
    }

    @Test
    public void adminCanLogoutSuccessfullyTest() {
        //1. Test data
        String locationUuid = Location.getRandomLocation().getUuid();

        //2. Test steps
        LoginPage adminLogout = new LoginPage()
                .open()
                .login(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD)
                .getPage(LocationPickerPage.class)
                .selectLocation(locationUuid)
                .confirm()
                .getPage(HomeServiceQueues.class)
                .clickUserAvatarButton()
                .clickLogoutButton()
                .getPage(LoginPage.class);

        //3. Test result
        SelenideElement usernameInput = adminLogout.getUsernameInput();
        usernameInput.shouldBe(Condition.visible);
        assertThat(usernameInput.isDisplayed()).isTrue();

        String sessionId = WebDriverRunner.getWebDriver().manage().getCookieNamed(SESSION_FIELD).getValue();

        LoginAdminResponse session = new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.authenticatedSpec(sessionId),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .getSession();

        assertThat(session.isAuthenticated()).isFalse();
        assertThat(session.getCurrentProvider()).isNull();
    }
}