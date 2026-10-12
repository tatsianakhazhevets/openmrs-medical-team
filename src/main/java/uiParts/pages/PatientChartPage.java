package uiParts.pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import uiParts.elements.PatientChartDashboardSlot;
import uiParts.elements.PatientChartTab;
import uiParts.helpers.PageLoader;
import uiParts.helpers.UiTimeouts;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$x;

public class PatientChartPage extends BasePage<PatientChartPage> {
    public static final String PATIENT_URL = "/patient/";

    private final SelenideElement patientChartValidationElement =
            $x("//button[normalize-space()='Actions'] | //button[contains(text(), 'visit')]");

    // Left sidebar items - present on every patient chart tab
    private static final String DASHBOARD_SLOT = "[data-extension-slot-name='patient-chart-dashboard-slot']";
    private final ElementsCollection dashboardSlots = $$(DASHBOARD_SLOT + " > div[data-extension-id]");

    @Override
    public String url() {
        return "/patient";
    }

    // chart of the given patient, opens on the default tab (Patient summary)
    public PatientChartPage open(String patientUuid) {
        PageLoader.open(PATIENT_URL + patientUuid + "/chart", sidebarLink());
        return Selenide.page(PatientChartPage.class);
    }

    // sidebar link is rendered only when the patient chart app has loaded - marker of a loaded chart page
    public static SelenideElement sidebarLink() {
        return $(DASHBOARD_SLOT + " a");
    }

    public String getCreatedPatientUuid() {
        patientChartValidationElement.shouldBe(Condition.visible, Duration.ofSeconds(15));
        String currentUrl = WebDriverRunner.url();
        return currentUrl.replaceAll(".*/patient/([^/]+).*", "$1");
    }

    public String getPageUrlAfterSubmit() {
        patientChartValidationElement.shouldBe(Condition.visible, Duration.ofSeconds(15));
        return WebDriverRunner.url();
    }

    public PatientChartPage getPatientChartValidationElement() {
        patientChartValidationElement.shouldBe(Condition.visible);
        return this;
    }

    public List<PatientChartDashboardSlot> getDashboardSlots() {
        // generatePageElements takes a DOM snapshot without waiting - wait for the sidebar to render first
        dashboardSlots.shouldHave(CollectionCondition.sizeGreaterThan(0), UiTimeouts.LONG);
        return generatePageElements(dashboardSlots, PatientChartDashboardSlot::new);
    }

    // Lazy locator by data-extension-id (not a snapshot from getDashboardSlots()): Selenide finds the element
    // again on every action, so a re-rendered sidebar item does not break the click
    public PatientChartDashboardSlot getDashboardSlot(PatientChartTab tab) {
        return new PatientChartDashboardSlot(
                $(DASHBOARD_SLOT + " > div[data-extension-id='" + tab.getExtensionId() + "']"));
    }

    public PatientChartPage openTab(PatientChartTab tab) {
        PatientChartDashboardSlot slot = getDashboardSlot(tab);
        slot.open();
        slot.shouldBeActive();
        return this;
    }
}
