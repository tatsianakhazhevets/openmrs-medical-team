package uiParts.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

// shown after login when user has no default location: /login/location
@Getter
public class LocationPickerPage extends BasePage<LocationPickerPage> {

    private final SelenideElement confirmButton = $x("//button[@type='submit' and normalize-space()='Confirm']");

    @Override
    public String url() {
        return "/login/location";
    }

    // Carbon radio input is visually hidden - click its label
    public LocationPickerPage selectLocation(String locationUuid) {
        Duration.ofSeconds(20);
        return click($("label[for='" + locationUuid + "']"));
    }

    public LocationPickerPage confirm() {
        return click(confirmButton);
    }
}
