package apiTests.vitalsAndBiometrics;

import apiParts.assertions.ModelAssertions;
import apiParts.assertions.ObsAssertions;
import apiParts.models.EncounterType;
import apiParts.models.Location;
import apiParts.models.VitalsConcept;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterRequest.Obs;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.encounter.GetObsResponse;
import apiParts.models.errors.ObsFieldError;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.CrudRequester;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Stream;

import static apiParts.models.VitalsConcept.*;
import static org.assertj.core.api.Assertions.assertThat;

public class VitalsAndBiometricsTests extends BaseTest {
    private static final int VITALS_OBS_COUNT = 11; // obs in AdminSteps.createVitalsEncounter()
    private String patientUUID;

    @BeforeEach
    void setUp() {
        var response = AdminSteps.createPatient();
        patientUUID = response.getUuid();
    }

    // Boundaries = lowAbsolute / hiAbsolute of the concept reference range applied to obs (inclusive, ObsValidator).
    // Check: GET /obs?patient={uuid}&concept={uuid}&v=full -> referenceRange
    // MID_UPPER_ARM_CIRC has no absolute limits (null) - nominal value is used in both sets
    static Stream<Arguments> validVitalsBoundaries() {
        return Stream.of(
                Arguments.of("lower boundary", List.of(
                        Obs.of(SYSTOLIC_BP, 0),
                        Obs.of(DIASTOLIC_BP, 0),
                        Obs.of(RESPIRATORY_RATE, 0),
                        Obs.of(OXYGEN_SATURATION, 0),
                        Obs.of(PULSE, 0),
                        Obs.of(TEMPERATURE, 25),
                        Obs.of(GENERAL_NOTE, "Some note"),
                        Obs.of(WEIGHT, 0),
                        Obs.of(HEIGHT, 10),
                        Obs.of(MID_UPPER_ARM_CIRC, 14),
                        Obs.of(BMI, 0)
                )),
                Arguments.of("upper boundary", List.of(
                        Obs.of(SYSTOLIC_BP, 250),
                        Obs.of(DIASTOLIC_BP, 150),
                        Obs.of(RESPIRATORY_RATE, 99),
                        Obs.of(OXYGEN_SATURATION, 100),
                        Obs.of(PULSE, 230),
                        Obs.of(TEMPERATURE, 47),
                        Obs.of(GENERAL_NOTE, "Some note"),
                        Obs.of(WEIGHT, 250),
                        Obs.of(HEIGHT, 272),
                        Obs.of(MID_UPPER_ARM_CIRC, 14),
                        Obs.of(BMI, 100)
                ))
        );
    }

    // Same reference range boundaries as in validVitalsBoundaries(): each obs is sent alone,
    // because 400 response does not say which obs is out of range.
    // MID_UPPER_ARM_CIRC has no absolute limits - no negative cases
    static Stream<Arguments> outOfRangeVitals() {
        return Stream.of(
                outOfRange(SYSTOLIC_BP, 0, 250),
                outOfRange(DIASTOLIC_BP, 0, 150),
                outOfRange(RESPIRATORY_RATE, 0, 99),
                outOfRange(OXYGEN_SATURATION, 0, 100),
                outOfRange(PULSE, 0, 230),
                outOfRange(TEMPERATURE, 25, 47),
                outOfRange(WEIGHT, 0, 250),
                outOfRange(HEIGHT, 10, 272),
                outOfRange(BMI, 0, 100)
        ).flatMap(cases -> cases);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validVitalsBoundaries")
    public void adminCanAddVitals(String boundary, List<Obs> obs){

        var request = CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.VITALS)
                .location(Location.OUTPATIENT_CLINIC)
                .obs(obs)
                .build();

        var encounter = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(request);

        var patientObs = new SuccessfulCrudRequester<GetObsResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.OBS_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .get(Map.of("patient", patientUUID, "v", "full"));

        ModelAssertions.assertListMatchesExpected(softly,
                patientObs.getResults(),
                ObsAssertions.expectedObsOf(request),
                ObsAssertions::conceptUuidOf,
                "obs saved for patient");
        softly.assertThat(ObsAssertions.uuidsOf(patientObs))
                .as("obs uuids from GET match POST /encounter")
                .isEqualTo(ObsAssertions.uuidsOf(encounter));
    }

    @ParameterizedTest(name = "{0} = {1} -> {2}")
    @MethodSource("outOfRangeVitals")
    public void adminCannotAddVitalsOutOfRange(VitalsConcept concept, Number value, ObsFieldError error) {

        var request = CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.VITALS)
                .location(Location.OUTPATIENT_CLINIC)
                .obs(List.of(Obs.of(concept, value)))
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsInvalidSubmission(error)
        )
                .create(request);
    }

    @Test
    public void adminCanDeleteVitalsEncounter() {
        var encounter = AdminSteps.createVitalsEncounter(patientUUID);
        assertPatientHasObsOf(encounter);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsNoContent()
        )
                .delete(encounter.getUuid());

        softly.assertThat(getPatientObs().getResults())
                .as("obs of deleted encounter are not returned for patient")
                .isEmpty();
    }

    @Test
    public void adminCannotDeleteNonExistentEncounter() {
        var encounter = AdminSteps.createVitalsEncounter(patientUUID);
        assertPatientHasObsOf(encounter);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsNotFound()
        )
                .delete(UUID.randomUUID().toString());

        softly.assertThat(ObsAssertions.uuidsOf(getPatientObs()))
                .as("patient obs are not affected")
                .isEqualTo(ObsAssertions.uuidsOf(encounter));
    }

    @Test
    public void unauthorizedUserCannotDeleteEncounter() {
        var encounter = AdminSteps.createVitalsEncounter(patientUUID);
        assertPatientHasObsOf(encounter);

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsUnauthorized()
        )
                .delete(encounter.getUuid());

        softly.assertThat(ObsAssertions.uuidsOf(getPatientObs()))
                .as("obs are still returned after unauthorized delete")
                .isEqualTo(ObsAssertions.uuidsOf(encounter));
    }

    @Test
    public void adminCanDeleteSingleObs() {
        var encounter = AdminSteps.createVitalsEncounter(patientUUID);
        assertPatientHasObsOf(encounter);

        String deletedObsUUID = encounter.getObs().get(0).getUuid();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.OBS_DELETE,
                ResponseSpecs.requestReturnsNoContent()
        )
                .delete(deletedObsUUID);

        Set<String> expectedObsUUIDs = new TreeSet<>(ObsAssertions.uuidsOf(encounter));
        expectedObsUUIDs.remove(deletedObsUUID);

        softly.assertThat(ObsAssertions.uuidsOf(getPatientObs()))
                .as("only deleted obs is gone, other obs remain")
                .isEqualTo(expectedObsUUIDs);
    }

    // ======== HELPERS ========
    private GetObsResponse getPatientObs() {
        return new SuccessfulCrudRequester<GetObsResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.OBS_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .get(Map.of("patient", patientUUID, "v", "full"));
    }

    // Precondition (hard assert): all obs of created encounter are saved for patient
    private void assertPatientHasObsOf(CreateEncounterResponse encounter) {
        assertThat(ObsAssertions.uuidsOf(getPatientObs()))
                .as("precondition: patient has " + VITALS_OBS_COUNT + " obs of created encounter")
                .hasSize(VITALS_OBS_COUNT)
                .isEqualTo(ObsAssertions.uuidsOf(encounter));
    }

    private static Stream<Arguments> outOfRange(VitalsConcept concept, int lowAbsolute, int hiAbsolute) {
        return Stream.of(
                Arguments.of(concept, lowAbsolute - 1, ObsFieldError.VALUE_OUT_OF_RANGE_LOW),
                Arguments.of(concept, hiAbsolute + 1, ObsFieldError.VALUE_OUT_OF_RANGE_HIGH)
        );
    }

}
