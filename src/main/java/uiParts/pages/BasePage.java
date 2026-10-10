package uiParts.pages;

import apiParts.config.Config;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.auth.AuthRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.Cookie;

public abstract class BasePage<T extends BasePage<T>> {

    public abstract String url();

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <P extends BasePage<P>> P getPage(Class<P> pageClass) {
        return Selenide.page(pageClass);
    }

    // Auth via API: creates session, sets its location (so skips location picker)
    // and puts JSESSIONID into browser cookies. After it - open the needed page.
    public static void authAsUser(String username, String password, String locationUuid) {
        String sessionId = RequestSpecs.createUserSession(username, password);

        new AuthRequester(
                RequestSpecs.authenticatedSpec(sessionId),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk()
        ).setSessionLocation(locationUuid);

        // cookie can be added only for the opened domain - open a light static resource instead of SPA
        Selenide.open("/favicon.ico");
        WebDriverRunner.getWebDriver().manage().addCookie(
                new Cookie.Builder("JSESSIONID", sessionId)
                        .isHttpOnly(true)
                        .build()
        );
    }

    public static void authAsUser(String username, String password) {
        authAsUser(username, password, Config.getProperty("test_location_uuid"));
    }

    public static void authAsUser(LoginAdminRequest loginAdminRequest) {
        authAsUser(loginAdminRequest.getUsername(), loginAdminRequest.getPassword());
    }

    public T click(SelenideElement element) {
        element.click();
        return (T) this;
    }

    public T setValue(SelenideElement element, String value) {
        element.setValue(value);
        return (T) this;
    }

}
