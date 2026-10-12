package uiParts.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

@Getter
public class LocationPickerPage extends BasePage<LocationPickerPage> {

    private final SelenideElement confirmButton = $x("//button[@type='submit' and normalize-space()='Confirm']");
    private final String locationLabelSelector = "label[for='%s']";

    @Override
    public String url() {
        return "/login/location";
    }

    public LocationPickerPage selectLocation(String locationUuid) {
        SelenideElement locationLabel = $(String.format(locationLabelSelector, locationUuid));
        locationLabel.shouldBe(Condition.visible);
        return click(locationLabel);
    }

    public LocationPickerPage confirm() {
        return click(confirmButton);
    }
}