package uiParts.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

@Getter
public class LoginPage extends BasePage<LoginPage> {

    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement continueButton = $x("//button[@type='submit' and normalize-space()='Continue']");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement loginButton = $x("//button[@type='submit' and normalize-space()='Log in']");
    private final SelenideElement errorMessage = $(".cds--inline-notification__subtitle, [data-testid='login-error-message']");

    @Override
    public String url() {
        return "/login";
    }

    public LoginPage login(String username, String password) {
        setValue(usernameInput, username);
        click(continueButton);
        setValue(passwordInput, password);
        click(loginButton);
        return this;
    }

    public LoginPage enterUsername(String username) {
        getUsernameInput().setValue(username);
        return this;
    }

    public LoginPage clickContinue() {
        getContinueButton().click();
        return this;
    }
}