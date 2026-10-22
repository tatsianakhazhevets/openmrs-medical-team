package uiParts.pages;

import com.codeborne.selenide.*;
import lombok.Getter;
import uiParts.helpers.UiTimeouts;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static uiParts.pages.PatientChartPage.PATIENT_URL;

@Getter
public class HomeServiceQueues extends BasePage<HomeServiceQueues> {
    public static final String EXPECTED_HEADER = "Service queues";

    private final SelenideElement queueHeader = $("div[data-testid='patient-queue-header']");
    private final SelenideElement searchActionButton = $x("//button[contains(@class, 'search')] | //svg[contains(@class, 'search')]");
    private final SelenideElement searchInput = $("input[placeholder='Search for a patient by name or identifier number']");
    private final SelenideElement firstSearchResultRow = $("a[class*='patientBanner']");
    private final ElementsCollection patientSearchLinks = $$("a[class*='patientBanner']");
    private final ElementsCollection patientSearchResult = $$(".patientSearchResult");
    private final SelenideElement userAvatarButton = $("button[data-tutorial-target='user-settings']");
    private final SelenideElement logoutButton = $("button[class*='logout']");
    private final SelenideElement searchResultsContainer = $("[data-testid='floatingSearchResultsContainer']");
    private final ElementsCollection patientSearchResults = searchResultsContainer.$$("a[href*='/spa/patient/']");

    @Override
    public String url() {
        return "/home/service-queues";
    }

    public HomeServiceQueues searchPatient(String givenName) {
        queueHeader.shouldBe(visible, Duration.ofSeconds(15));
        searchActionButton.shouldBe(visible, Duration.ofSeconds(10)).click();
        searchInput.shouldBe(visible, Duration.ofSeconds(10)).shouldBe(Condition.editable);
        searchInput.setValue(givenName);
        return this;
    }

    public HomeServiceQueues clickCreatedPatient(String expectedGivenName, String expectedFamilyName) {
        String fullName = expectedGivenName + " " + expectedFamilyName;
        patientSearchResults.findBy(text(fullName)).shouldBe(visible, UiTimeouts.LONG).click();
        return this;
    }

    public HomeServiceQueues clickUserAvatarButton() {
        userAvatarButton.shouldBe(visible).click();
        return this;
    }

    public LoginPage clickLogoutButton() {
        logoutButton.shouldBe(visible).click();
        return new LoginPage();
    }

    public HomeServiceQueues shouldNotFindPatient() {
        patientSearchResult.shouldBe(CollectionCondition.size(0));
        return this;
    }

    public String getCurrentUrl() {
        return WebDriverRunner.url();
    }

    public String getPatientUuidFromUrl() {
        String currentUrl = WebDriverRunner.url();

        if (!currentUrl.contains(PATIENT_URL)) {
            return "";
        }

        return currentUrl.substring(currentUrl.lastIndexOf("/") + 1);
    }
}