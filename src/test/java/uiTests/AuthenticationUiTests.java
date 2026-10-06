package uiTests;

import apiParts.config.Config;
import apiParts.generators.RandomNameGenerator;
import apiParts.generators.RandomUuidGenerator;
import apiParts.specs.RequestSpecs;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.Test;
import uiParts.pages.HomeServiceQueues;
import uiParts.pages.LocationPickerPage;
import uiParts.pages.LoginPage;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

public class AuthenticationUiTests extends BaseUiTest {

    @Test
    public void adminCanLoginWithValidCredentials() {
        new LoginPage().open()
                .login(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        webdriver().shouldHave(urlContaining("/login/location"), Duration.ofSeconds(15));

        new LocationPickerPage()
                .selectLocation(Config.getProperty("test_location_uuid")) //44c3efb0-2583-4c80-a79e-1f756a03c0a1
                .confirm();

        webdriver().shouldHave(urlContaining("/home"), Duration.ofSeconds(15));
    }

    @Test
    public void adminCanLoginWithCorrectDataTest() {
        new LoginPage()
                .open()
                .login(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD)
                .getPage(LocationPickerPage.class)
                .selectLocation(Config.getProperty("test_location_uuid"))
                .confirm()
                .getPage(HomeServiceQueues.class)
                .getQueueHeader()
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Service queues"));
    }

    @Test
    public void userCannotLoginWithInvalidCredentialsTest() {
        new LoginPage()
                .open()
                .login(RandomNameGenerator.generateNonExistingName(), RandomUuidGenerator.generateUuid())
                .getErrorMessage()
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Invalid username or password"));
    }

    @Test
    public void userCannotSubmitEmptyUsernameTest() {
        new LoginPage()
                .open()
                .setValue(new LoginPage().getUsernameInput(), "")
                .click(new LoginPage().getContinueButton())
                .getPasswordInput()
                .shouldNotBe(Condition.visible);
    }

    @Test
    public void userCannotConfirmLocationWithoutSelectingItTest() {
        new LoginPage()
                .open()
                .login(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD)
                .getPage(LocationPickerPage.class)
                .getConfirmButton()
                .shouldBe(Condition.disabled);
    }
}