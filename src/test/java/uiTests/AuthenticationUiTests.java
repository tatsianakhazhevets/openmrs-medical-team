package uiTests;

import apiParts.config.Config;
import apiParts.specs.RequestSpecs;
import org.junit.jupiter.api.Test;
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
}
