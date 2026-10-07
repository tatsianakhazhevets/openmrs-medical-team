package uiParts.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;

@Getter
public class HomeServiceQueues extends BasePage<HomeServiceQueues> {
    private final SelenideElement queueHeader = $("div[data-testid='patient-queue-header']");
    private final SelenideElement searchActionButton = $x("//button[contains(@class, 'search')] | //svg[contains(@class, 'search')]");
    private final SelenideElement searchInput = $("input[placeholder='Search for a patient by name or identifier number']");
    private final SelenideElement firstSearchResultRow = $("a[class*='patientBanner']");
    private final ElementsCollection patientSearchLinks = $$("a[class*='patientBanner']");

    @Override
    public String url() {
        return "/home/service-queues";
    }

    public HomeServiceQueues searchPatient(String givenName) {
        queueHeader.shouldBe(Condition.visible, Duration.ofSeconds(15));
        searchActionButton.shouldBe(Condition.visible, Duration.ofSeconds(10)).click();
        searchInput.shouldBe(Condition.visible, Duration.ofSeconds(10))
                .shouldBe(Condition.editable);
        searchInput.setValue(givenName);
        return this;
    }

    public HomeServiceQueues clickCreatedPatient(String expectedGivenName, String expectedFamilyName) {
        String patientXpath = String.format(
                "//a[contains(@class, 'patientSearchResult')]//span[contains(text(), '%s %s')]",
                expectedGivenName, expectedFamilyName
        );
        $x(patientXpath)
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();

        return this;
    }
}