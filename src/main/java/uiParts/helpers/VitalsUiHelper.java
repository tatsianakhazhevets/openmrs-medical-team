package uiParts.helpers;

import apiParts.assertions.ModelAssertions;
import apiParts.assertions.SoftlyContext;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.encounter.EncounterResponse;
import apiParts.models.encounter.ObsResponse;
import apiParts.models.vitals.CreateVitalsRequest;
import apiParts.steps.AdminSteps;
import apiParts.models.vitals.Obs;
import apiParts.steps.VitalsSteps;
import uiParts.elements.VitalsTableCell;
import uiParts.models.VitalsFormData;
import uiParts.models.VitalsInterpretation;
import uiParts.models.VitalsReferenceRange;
import uiParts.pages.VitalsAndBiometricsPage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static apiParts.models.vitals.VitalsConcept.*;
import static uiParts.helpers.UiNumberFormat.format;
import static uiParts.pages.VitalsAndBiometricsPage.*;

/**
 * Expected data and soft assertions for "Vitals & Biometrics" UI tests:
 * UI side - table rows, API side - obs saved for the patient.
 * Assertions use the current test's SoftAssertions from SoftlyContext (set by BaseTest).
 */
public final class VitalsUiHelper {

    private VitalsUiHelper() {
    }

    // ======== EXPECTED DATA ========
    // vitals table row as the UI shows the form data: column header -> cell text
    public static Map<String, String> expectedVitals(VitalsFormData vitals) {
        return Map.of(
                TEMPERATURE_COLUMN, format(vitals.getTemperature()),
                BLOOD_PRESSURE_COLUMN, vitals.getSystolic() + " / " + vitals.getDiastolic(),
                PULSE_COLUMN, format(vitals.getPulse()),
                RESPIRATORY_RATE_COLUMN, format(vitals.getRespiratoryRate()),
                OXYGEN_SATURATION_COLUMN, format(vitals.getOxygenSaturation())
        );
    }

    // biometrics table row as the UI shows the form data: column header -> cell text
    public static Map<String, String> expectedBiometrics(VitalsFormData vitals) {
        return Map.of(
                WEIGHT_COLUMN, format(vitals.getWeight()),
                HEIGHT_COLUMN, format(vitals.getHeight()),
                BMI_COLUMN, format(vitals.calculateBmi()),
                MUAC_COLUMN, format(vitals.getMidUpperArmCircumference())
        );
    }

    // obs the form saves: one per field, BMI is computed by the form
    public static List<Obs> expectedObs(VitalsFormData vitals) {
        return List.of(
                Obs.of(TEMPERATURE, vitals.getTemperature()),
                Obs.of(SYSTOLIC_BP, vitals.getSystolic()),
                Obs.of(DIASTOLIC_BP, vitals.getDiastolic()),
                Obs.of(PULSE, vitals.getPulse()),
                Obs.of(RESPIRATORY_RATE, vitals.getRespiratoryRate()),
                Obs.of(OXYGEN_SATURATION, vitals.getOxygenSaturation()),
                Obs.of(GENERAL_NOTE, vitals.getNotes()),
                Obs.of(WEIGHT, vitals.getWeight()),
                Obs.of(HEIGHT, vitals.getHeight()),
                Obs.of(BMI, vitals.calculateBmi()),
                Obs.of(MID_UPPER_ARM_CIRC, vitals.getMidUpperArmCircumference())
        );
    }

    // ======== API PRECONDITIONS ========
    // POST /encounter: vitals encounter of the patient with the form data as obs (BMI as the form computes it)
    public static EncounterResponse createVitalsViaApi(String patientUuid, VitalsFormData vitals) {
        return AdminSteps.createEncounter(RandomModelGenerator.generate(CreateVitalsRequest.class, Map.of(
                CreateVitalsRequest.Fields.patient, patientUuid,
                CreateVitalsRequest.Fields.obs, expectedObs(vitals))));
    }

    // ======== UI ASSERTIONS ========
    // both table rows of the encounter show the form data (waits for the async table refresh first)
    public static void assertRecordShown(VitalsAndBiometricsPage page, String encounterUuid, VitalsFormData vitals) {
        Map<String, String> expectedVitals = expectedVitals(vitals);
        Map<String, String> expectedBiometrics = expectedBiometrics(vitals);
        page.waitForRecord(encounterUuid, expectedVitals, expectedBiometrics);

        SoftlyContext.get().assertThat(page.getVitals(encounterUuid))
                .as("UI: vitals row of encounter %s", encounterUuid)
                .containsAllEntriesOf(expectedVitals);
        SoftlyContext.get().assertThat(page.getBiometrics(encounterUuid))
                .as("UI: biometrics row of encounter %s", encounterUuid)
                .containsAllEntriesOf(expectedBiometrics);
    }

    // both table rows of the encounter are dated today
    public static void assertRecordedToday(VitalsAndBiometricsPage page, String encounterUuid) {
        SoftlyContext.get().assertThat(page.getVitals(encounterUuid).get(DATE_COLUMN))
                .as("UI: vitals are recorded today")
                .startsWith("Today");
        SoftlyContext.get().assertThat(page.getBiometrics(encounterUuid).get(DATE_COLUMN))
                .as("UI: biometrics are recorded today")
                .startsWith("Today");
    }

    // Every value cell of the encounter rows is marked as expected: columns with reference limits that support
    // the interpretation get its background and arrow, all other columns (weight, height, MUAC, SpO2 for high
    // cases) stay unmarked
    public static void assertHighlighted(VitalsAndBiometricsPage page, String encounterUuid,
                                         VitalsInterpretation interpretation) {
        Set<String> markedColumns = rangesSupporting(interpretation).stream()
                .map(VitalsReferenceRange::getColumn)
                .collect(Collectors.toSet());

        List.of(TEMPERATURE_COLUMN, BLOOD_PRESSURE_COLUMN, PULSE_COLUMN, RESPIRATORY_RATE_COLUMN, OXYGEN_SATURATION_COLUMN)
                .forEach(column -> assertCell(page.getVitalsCell(encounterUuid, column), column,
                        markedColumns.contains(column) ? interpretation : VitalsInterpretation.NORMAL));
        List.of(WEIGHT_COLUMN, HEIGHT_COLUMN, BMI_COLUMN, MUAC_COLUMN)
                .forEach(column -> assertCell(page.getBiometricsCell(encounterUuid, column), column,
                        markedColumns.contains(column) ? interpretation : VitalsInterpretation.NORMAL));
    }

    private static void assertCell(VitalsTableCell cell, String column, VitalsInterpretation expected) {
        SoftlyContext.get().assertThat(cell.getBackgroundColor())
                .as("UI: background of '%s' cell (%s)", column, expected)
                .isEqualTo(expected.getBackgroundColor());
        SoftlyContext.get().assertThat(cell.getFlag())
                .as("UI: arrow of '%s' cell (%s)", column, expected)
                .isEqualTo(expected.getFlag());
    }

    private static List<VitalsReferenceRange> rangesSupporting(VitalsInterpretation interpretation) {
        return Arrays.stream(VitalsReferenceRange.values())
                .filter(range -> range.supports(interpretation))
                .toList();
    }

    // ======== API ASSERTIONS ========
    // GET /obs?patient={uuid}&v=full: obs of the encounter match the form data (same rules as in API tests).
    // Voided obs (replaced on edit) are not returned, so after edit only the new values are compared.
    public static void assertSavedViaApi(String patientUuid, String encounterUuid, VitalsFormData vitals) {
        List<ObsResponse> encounterObs = VitalsSteps.getPatientObs(patientUuid).results().stream()
                .filter(obs -> encounterUuid.equals(obs.getEncounter().getUuid()))
                .toList();

        ModelAssertions.assertThatModels(expectedObs(vitals), encounterObs)
                .as("API: obs of encounter " + encounterUuid + " saved from UI form")
                .match();
    }

    // GET /obs?patient={uuid}&v=full: server interpretation of the obs with reference limits
    public static void assertInterpretationViaApi(String patientUuid, String encounterUuid,
                                                  VitalsInterpretation interpretation) {
        Map<String, String> interpretationByConcept = VitalsSteps.getPatientObs(patientUuid).results().stream()
                .filter(obs -> encounterUuid.equals(obs.getEncounter().getUuid()))
                .filter(obs -> obs.getInterpretation() != null)
                .collect(Collectors.toMap(obs -> obs.getConcept().getUuid(), ObsResponse::getInterpretation));

        rangesSupporting(interpretation).forEach(range ->
                SoftlyContext.get().assertThat(interpretationByConcept.get(range.getConcept().getUuid()))
                        .as("API: interpretation of %s obs", range.getConcept())
                        .isEqualTo(interpretation.name()));
    }

    // GET /obs and GET /encounter after delete: patient has no obs, encounter is voided
    public static void assertDeletedViaApi(String patientUuid, String encounterUuid) {
        SoftlyContext.get().assertThat(VitalsSteps.getPatientObs(patientUuid).results())
                .as("API: obs of deleted encounter are not returned for patient")
                .isEmpty();
        SoftlyContext.get().assertThat(VitalsSteps.getEncounter(encounterUuid).getVoided())
                .as("API: deleted encounter is marked as voided")
                .isTrue();
    }
}
