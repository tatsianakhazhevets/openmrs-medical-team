package uiParts.pages;

import com.codeborne.selenide.*;
import uiParts.elements.PatientChartDashboardSlot;
import uiParts.elements.PatientChartTab;
import uiParts.helpers.PageLoader;
import uiParts.helpers.UiTimeouts;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class PatientChartPage extends BasePage<PatientChartPage> {
    public static final String PATIENT_URL = "/patient/";
    private static final String DASHBOARD_SLOT = "[data-extension-slot-name='patient-chart-dashboard-slot']";
    private static final String PATIENT_BANNER = "header[aria-label='patient banner']";
    private static final String PATIENT_NAME = PATIENT_BANNER + " span";
    private static final String PATIENT_SUMMARY_DASHBOARD_SLOT = "[data-extension-slot-name='patient-chart-summary-dashboard-slot']";
    private final SelenideElement patientBanner = $(PATIENT_BANNER);
    private final ElementsCollection patientNames = $$(PATIENT_NAME);
    private final SelenideElement patientSummaryDashboardSlot = $(PATIENT_SUMMARY_DASHBOARD_SLOT);
    private final ElementsCollection dashboardSlots = $$(DASHBOARD_SLOT + " > div[data-extension-id]");
    private final String dashboardSlotSelector = DASHBOARD_SLOT + " > div[data-extension-id='%s']";
    private final String patientUuidRegex = ".*/patient/([^/]+)/.*";

    @Override
    public String url() {
        return "/patient";
    }

    public PatientChartPage open(String patientUuid) {
        PageLoader.open(PATIENT_URL + patientUuid + "/chart", sidebarLink());
        return Selenide.page(PatientChartPage.class);
    }

    public static SelenideElement sidebarLink() {
        return $(DASHBOARD_SLOT + " a");
    }

    public String getCreatedPatientUuid() {
        sidebarLink().shouldBe(Condition.visible, Duration.ofSeconds(15));
        String currentUrl = WebDriverRunner.url();
        return currentUrl.replaceAll(patientUuidRegex, "$1");
    }

    public String getPageUrlAfterSubmit() {
        sidebarLink().shouldBe(Condition.visible, Duration.ofSeconds(15));
        return WebDriverRunner.url();
    }

    public PatientChartDashboardSlot getDashboardSlot(PatientChartTab tab) {
        return new PatientChartDashboardSlot(
                $(String.format(dashboardSlotSelector, tab.getExtensionId()))
        );
    }

    public PatientChartPage openTab(PatientChartTab tab) {
        PatientChartDashboardSlot slot = getDashboardSlot(tab);
        slot.open();
        slot.shouldBeActive();
        return this;
    }

    public PatientChartPage shouldBeLoaded() {
        patientBanner.shouldBe(Condition.visible, UiTimeouts.LONG);
        patientSummaryDashboardSlot.shouldBe(Condition.visible, UiTimeouts.LONG);
        return this;
    }

    public PatientChartPage shouldShowPatientName(String givenName) {
        patientBanner.shouldBe(Condition.visible, UiTimeouts.LONG);
        patientNames.findBy(Condition.text(givenName)).shouldBe(Condition.visible, UiTimeouts.LONG);
        return this;
    }
}