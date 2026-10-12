package uiTests;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.encounter.EncounterType;
import apiParts.specs.RequestSpecs;
import apiParts.steps.VitalsSteps;
import common.annotations.CreateEncounter;
import common.annotations.CreatePatient;
import common.annotations.CreateVisit;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uiParts.elements.PatientChartTab;
import uiParts.models.VitalsFormData;
import uiParts.models.VitalsInterpretation;
import uiParts.pages.BasePage;
import uiParts.pages.PatientChartPage;
import uiParts.pages.VitalsAndBiometricsPage;

import static uiParts.helpers.VitalsTestData.withInterpretation;
import static uiParts.helpers.VitalsUiHelper.*;

// Navigation through the sidebar is checked once (adminCanRecordVitalsAndBiometrics),
// other tests open the tab by direct link.
@CreatePatient
@CreateVisit
public class VitalsAndBiometricsTest extends BaseUiTest {
    private String patientUuid;

    @BeforeEach
    void setUp() {
        patientUuid = SessionStorage.getPatient().getUuid();
        BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);
    }

    @Test
    public void adminCanRecordVitalsAndBiometrics() {
        //1. Test data
        VitalsFormData vitals = RandomModelGenerator.generate(VitalsFormData.class);

        //2. Test steps
        VitalsAndBiometricsPage vitalsPage = new PatientChartPage()
                .open(patientUuid)
                .openTab(PatientChartTab.VITALS_AND_BIOMETRICS)
                .getPage(VitalsAndBiometricsPage.class)
                .shouldBeOpened()
                .clickRecordVitalSigns()
                .shouldBeOpened()
                .fill(vitals)
                .save()
                .shouldHaveVitalsRecordsCount(1);

        //3. Test result
        String encounterUuid = vitalsPage.getVitalsRecordIds().get(0);
        assertRecordShown(vitalsPage, encounterUuid, vitals);
        assertRecordedToday(vitalsPage, encounterUuid);
        assertSavedViaApi(patientUuid, encounterUuid, vitals);
    }

    @Test
    @CreateEncounter(EncounterType.VITALS)
    public void adminCanAddVitalsWhenPatientHasRecords() {
        //1. Test data
        String existingEncounterUuid = SessionStorage.getEncounter().getUuid();
        VitalsFormData vitals = RandomModelGenerator.generate(VitalsFormData.class);

        //2. Test steps
        VitalsAndBiometricsPage vitalsPage = new VitalsAndBiometricsPage()
                .open(patientUuid)
                .shouldBeOpened()
                .shouldHaveRecords(existingEncounterUuid)
                .clickAdd()
                .shouldBeOpened()
                .fill(vitals)
                .save()
                .shouldHaveVitalsRecordsCount(2);

        //3. Test result
        // newest record is on top, the existing one stays below it
        String newEncounterUuid = vitalsPage.getVitalsRecordIds().get(0);
        softly.assertThat(vitalsPage.getVitalsRecordIds())
                .as("UI: new record on top, existing record stays")
                .containsExactly(newEncounterUuid, existingEncounterUuid);
        softly.assertThat(newEncounterUuid)
                .as("UI: new record is a new encounter")
                .isNotEqualTo(existingEncounterUuid);

        assertRecordShown(vitalsPage, newEncounterUuid, vitals);
        assertSavedViaApi(patientUuid, newEncounterUuid, vitals);
    }

    @Test
    @CreateEncounter(EncounterType.VITALS)
    public void adminCanEditVitals() {
        //1. Test data
        String encounterUuid = SessionStorage.getEncounter().getUuid();
        VitalsFormData newVitals = RandomModelGenerator.generate(VitalsFormData.class);

        //2. Test steps
        VitalsAndBiometricsPage vitalsPage = new VitalsAndBiometricsPage()
                .open(patientUuid)
                .shouldBeOpened()
                .editRecord(encounterUuid)
                .shouldBeOpened()
                .fill(newVitals)
                .save();

        //3. Test result
        assertRecordShown(vitalsPage, encounterUuid, newVitals);
        vitalsPage.shouldHaveRecords(encounterUuid);   // the same record is updated, no new one
        assertSavedViaApi(patientUuid, encounterUuid, newVitals);
    }

    @Test
    @CreateEncounter(EncounterType.VITALS)
    public void adminCanDeleteVitals() {
        //1. Test data
        var encounter = SessionStorage.getEncounter();
        VitalsSteps.assertPatientHasObsOf(patientUuid, encounter);

        //2. Test steps + UI result
        new VitalsAndBiometricsPage()
                .open(patientUuid)
                .shouldBeOpened()
                .shouldHaveRecords(encounter.getUuid())
                .deleteRecord(encounter.getUuid())
                .shouldNotHaveRecord(encounter.getUuid())
                .shouldShowNoRecords();

        //3. Test result
        assertDeletedViaApi(patientUuid, encounter.getUuid());
    }

    // Values out of the normal range are highlighted: orange background + ↑ / ↓ for abnormal,
    // red background + ↑↑ / ↓↓ for critical. Limits - VitalsReferenceRange.
    // Limits depend on patient age and gender (BMI) - the patient is an adult male.
    // Vitals are created via API: the test checks only how the UI shows them, not the form.
    @ParameterizedTest(name = "{0}")
    @EnumSource(value = VitalsInterpretation.class, names = {"HIGH", "CRITICALLY_HIGH", "LOW", "CRITICALLY_LOW"})
    @CreatePatient(gender = "M", adult = true)
    public void adminSeesOutOfRangeVitalsHighlighted(VitalsInterpretation interpretation) {
        //1. Test data
        VitalsFormData vitals = withInterpretation(interpretation);
        String encounterUuid = createVitalsViaApi(patientUuid, vitals).getUuid();
        assertInterpretationViaApi(patientUuid, encounterUuid, interpretation);   // server classifies them the same way

        //2. Test steps
        VitalsAndBiometricsPage vitalsPage = new VitalsAndBiometricsPage()
                .open(patientUuid)
                .shouldBeOpened()
                .shouldHaveRecords(encounterUuid);

        //3. Test result
        assertRecordShown(vitalsPage, encounterUuid, vitals);
        assertHighlighted(vitalsPage, encounterUuid, interpretation);
    }
}
