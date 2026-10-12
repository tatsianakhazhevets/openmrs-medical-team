package uiParts.pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.UIAssertionError;
import uiParts.elements.VitalsAndBiometricsForm;
import uiParts.elements.VitalsTableCell;
import uiParts.helpers.PageLoader;
import uiParts.helpers.UiTimeouts;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

// "Vitals & Biometrics" tab of the patient chart: vitals and biometrics widgets with their tables.
// Every table row has an actions menu button whose id is the encounter uuid - rows are found by it.
public class VitalsAndBiometricsPage extends BasePage<VitalsAndBiometricsPage> {
    public static final String TAB_PATH = "/chart/vitals-and-biometrics";
    public static final String NO_VITALS_MESSAGE = "There are no vital signs to display for this patient";
    public static final String NO_BIOMETRICS_MESSAGE = "There are no biometrics to display for this patient";

    // table column headers
    public static final String DATE_COLUMN = "Date and time";
    public static final String TEMPERATURE_COLUMN = "Temp (DEG C)";
    public static final String BLOOD_PRESSURE_COLUMN = "BP (mmHg)";
    public static final String PULSE_COLUMN = "Pulse (beats/min)";
    public static final String RESPIRATORY_RATE_COLUMN = "R. Rate (breaths/min)";
    public static final String OXYGEN_SATURATION_COLUMN = "SpO2 (%)";
    public static final String WEIGHT_COLUMN = "Weight (kg)";
    public static final String HEIGHT_COLUMN = "Height (cm)";
    public static final String BMI_COLUMN = "BMI (kg / m²)";
    public static final String MUAC_COLUMN = "MUAC (cm)";

    private static final Duration LOAD_TIMEOUT = UiTimeouts.LONG;
    private static final Duration FIRST_RENDER_TIMEOUT = UiTimeouts.LONG;

    private final SelenideElement vitalsWidget = $("[data-extension-id='vitals-details-widget']");
    private final SelenideElement biometricsWidget = $("[data-extension-id='biometrics-details-widget']");
    // empty state button, shown while the patient has no vitals
    private final SelenideElement recordVitalSignsButton =
            vitalsWidget.$x(".//button[normalize-space()='Record vital signs']");
    // widget header button, shown when the patient already has vitals
    private final SelenideElement addButton = vitalsWidget.$x(".//button[normalize-space()='Add']");
    // actions menu buttons of vitals table rows, id = encounter uuid
    private final ElementsCollection vitalsRowMenus = vitalsWidget.$$("tbody tr .cds--overflow-menu");
    private final SelenideElement editMenuItem = $("#editVitalsAndBiometrics");
    private final SelenideElement deleteMenuItem = $("#deleteVitalsAndBiometrics");
    // confirmation modal: rendered by the app shell into .omrs-modals-container as div[role=dialog]
    private final SelenideElement deleteModal =
            $x("//div[@role='dialog'][.//h2[normalize-space()='Delete vitals and biometrics']]");
    private final SelenideElement vitalsForm =
            $x("//form[.//input[contains(@id, '-temperature')]]");

    @Override
    public String url() {
        return PatientChartPage.PATIENT_URL;
    }

    // direct link to the tab of the given patient (without navigation through the sidebar)
    public VitalsAndBiometricsPage open(String patientUuid) {
        PageLoader.open(PatientChartPage.PATIENT_URL + patientUuid + TAB_PATH, PatientChartPage.sidebarLink());
        return Selenide.page(VitalsAndBiometricsPage.class);
    }

    public VitalsAndBiometricsPage shouldBeOpened() {
        webdriver().shouldHave(urlContaining(TAB_PATH), LOAD_TIMEOUT);
        if (!widgetsRendered(FIRST_RENDER_TIMEOUT)) {
            // Under parallel load the app shell sometimes leaves the widget wrappers empty after
            // client-side navigation (header of the tab is rendered, widgets are not mounted at all).
            // Reload of the tab URL renders them; if they are still empty - the check below fails.
            Selenide.refresh();
        }
        vitalsWidget.shouldBe(Condition.visible, LOAD_TIMEOUT);
        biometricsWidget.shouldBe(Condition.visible, LOAD_TIMEOUT);
        return this;
    }

    private boolean widgetsRendered(Duration timeout) {
        try {
            vitalsWidget.shouldBe(Condition.visible, timeout);
            biometricsWidget.shouldBe(Condition.visible, timeout);
            return true;
        } catch (UIAssertionError notRendered) {
            return false;
        }
    }

    // ======== ADD / EDIT / DELETE ========
    public VitalsAndBiometricsForm clickRecordVitalSigns() {
        recordVitalSignsButton.shouldBe(Condition.visible).click();
        return new VitalsAndBiometricsForm(vitalsForm);
    }

    public VitalsAndBiometricsForm clickAdd() {
        addButton.shouldBe(Condition.visible, LOAD_TIMEOUT).click();
        return new VitalsAndBiometricsForm(vitalsForm);
    }

    // opens the edit form prefilled with the saved values of the encounter
    public VitalsAndBiometricsForm editRecord(String encounterUuid) {
        openRowMenu(encounterUuid);
        editMenuItem.shouldBe(Condition.visible).click();
        return new VitalsAndBiometricsForm(vitalsForm);
    }

    // deletes the encounter (vitals and biometrics together) with confirmation in the modal
    public VitalsAndBiometricsPage deleteRecord(String encounterUuid) {
        openRowMenu(encounterUuid);
        deleteMenuItem.shouldBe(Condition.visible).click();
        deleteModal.shouldBe(Condition.visible, LOAD_TIMEOUT).$("button.cds--btn--danger").click();
        deleteModal.shouldNotBe(Condition.visible, LOAD_TIMEOUT);
        return this;
    }

    // ======== TABLES ========
    // encounter uuids of the rows in the vitals table (newest first)
    public List<String> getVitalsRecordIds() {
        vitalsRowMenus.shouldHave(CollectionCondition.sizeGreaterThan(0), LOAD_TIMEOUT);
        return vitalsRowMenus.attributes("id");
    }

    // vitals row of the encounter: column header -> cell text
    public Map<String, String> getVitals(String encounterUuid) {
        return row(vitalsWidget, encounterUuid);
    }

    // biometrics row of the encounter: column header -> cell text
    public Map<String, String> getBiometrics(String encounterUuid) {
        return row(biometricsWidget, encounterUuid);
    }

    // cell of the vitals table in the row of the encounter
    public VitalsTableCell getVitalsCell(String encounterUuid, String column) {
        return cell(vitalsWidget, encounterUuid, column);
    }

    // cell of the biometrics table in the row of the encounter
    public VitalsTableCell getBiometricsCell(String encounterUuid, String column) {
        return cell(biometricsWidget, encounterUuid, column);
    }

    // vitals table shows exactly these records (encounter uuids, newest first)
    public VitalsAndBiometricsPage shouldHaveRecords(String... encounterUuids) {
        vitalsRowMenus.shouldHave(CollectionCondition.attributes("id", encounterUuids), LOAD_TIMEOUT);
        return this;
    }

    public VitalsAndBiometricsPage shouldHaveVitalsRecordsCount(int count) {
        vitalsRowMenus.shouldHave(CollectionCondition.size(count), LOAD_TIMEOUT);
        return this;
    }

    // Tables are refreshed asynchronously after save: waits until the rows of the encounter show the expected cells.
    // On timeout does not fail - the following assertions report the actual values.
    public VitalsAndBiometricsPage waitForRecord(String encounterUuid,
                                                 Map<String, String> expectedVitals,
                                                 Map<String, String> expectedBiometrics) {
        try {
            Selenide.Wait().withTimeout(LOAD_TIMEOUT).ignoring(StaleElementReferenceException.class).until(driver ->
                    row(vitalsWidget, encounterUuid).entrySet().containsAll(expectedVitals.entrySet())
                            && row(biometricsWidget, encounterUuid).entrySet().containsAll(expectedBiometrics.entrySet()));
        } catch (TimeoutException ignored) {
            // actual values are checked by the test
        }
        return this;
    }

    public VitalsAndBiometricsPage shouldNotHaveRecord(String encounterUuid) {
        rowMenu(vitalsWidget, encounterUuid).shouldNot(Condition.exist, LOAD_TIMEOUT);
        rowMenu(biometricsWidget, encounterUuid).shouldNot(Condition.exist);
        return this;
    }

    public VitalsAndBiometricsPage shouldShowNoRecords() {
        vitalsWidget.shouldHave(Condition.text(NO_VITALS_MESSAGE), LOAD_TIMEOUT);
        biometricsWidget.shouldHave(Condition.text(NO_BIOMETRICS_MESSAGE));
        return this;
    }

    private void openRowMenu(String encounterUuid) {
        rowMenu(vitalsWidget, encounterUuid).shouldBe(Condition.visible, LOAD_TIMEOUT).click();
    }

    // both tables have a menu button with the same id, so it is searched inside the widget
    private SelenideElement rowMenu(SelenideElement widget, String encounterUuid) {
        return widget.$("button.cds--overflow-menu[id='" + encounterUuid + "']");
    }

    private VitalsTableCell cell(SelenideElement widget, String encounterUuid, String column) {
        SelenideElement menu = rowMenu(widget, encounterUuid).shouldBe(Condition.exist, LOAD_TIMEOUT);
        int index = widget.$$("thead th").texts().stream().map(String::trim).toList().indexOf(column);
        if (index < 0) {
            throw new IllegalArgumentException("No column '" + column + "' in table " + widget);
        }
        return new VitalsTableCell(menu.closest("tr").$$("td").get(index));
    }

    private Map<String, String> row(SelenideElement widget, String encounterUuid) {
        SelenideElement menu = rowMenu(widget, encounterUuid).shouldBe(Condition.exist, LOAD_TIMEOUT);
        List<String> headers = widget.$$("thead th").texts();
        List<String> cells = menu.closest("tr").$$("td").texts();

        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.size() && i < cells.size(); i++) {
            String header = headers.get(i).trim();
            if (!header.isEmpty()) {   // expand and actions columns have no header
                row.put(header, cells.get(i).trim());
            }
        }
        return row;
    }
}
