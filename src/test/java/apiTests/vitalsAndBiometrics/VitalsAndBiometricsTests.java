package apiTests.vitalsAndBiometrics;

import apiParts.assertions.ModelAssertions;
import apiParts.assertions.ObsAssertions;
import apiParts.generators.RandomModelGenerator;
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
import apiParts.models.search.SearchResult;
import apiParts.models.encounter.ObsResponse;
import apiParts.models.encounter.ObsSearchParams;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CreatePatient
public class VitalsAndBiometricsTests extends BaseTest {
    // Step outside the reference range: 0.1 for concepts with decimals, 1 for whole-number ones
    // (for them 0.1 is not a valid value at all and the server would fail on conversion, not on range)
    private static final double DECIMAL_STEP = 0.1;
    private static final double WHOLE_NUMBER_STEP = 1;

    private String patientUUID;

    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    // Boundaries = lowAbsolute / hiAbsolute of the concept reference range (inclusive, ObsValidator),
    // kept in VitalsConcept. Check: GET /obs?patient={uuid}&concept={uuid}&v=full -> referenceRange
    static Stream<Arguments> validVitalsBoundaries() {
        return Stream.of(
                Arguments.of("lower boundary", obsAt(VitalsConcept::low)),
                Arguments.of("upper boundary", obsAt(VitalsConcept::high))
        );
    }

    // Same boundaries as in validVitalsBoundaries(): each obs is sent alone,
    // because 400 response does not say which obs is out of range.
    // Concepts without absolute limits (MID_UPPER_ARM_CIRC, TEXT) have no negative cases
    static Stream<Arguments> outOfRangeVitals() {
        return Arrays.stream(VitalsConcept.values())
                .filter(VitalsConcept::hasAbsoluteRange)
                .flatMap(VitalsAndBiometricsTests::outOfRange);
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
        Set<String> expectedObsUUIDs = ObsAssertions.uuidsOf(encounter);
        assertThat(ObsAssertions.uuidsOf(getPatientObs()))
                .as("precondition: patient has %d obs of created encounter", expectedObsUUIDs.size())
                .isEqualTo(expectedObsUUIDs);
    }

    // Every vitals concept at one edge of its range: TEXT concept gets free text,
    // concept without absolute limits - any value inside its nominal range
    private static List<Obs> obsAt(ToDoubleFunction<VitalsConcept> boundary) {
        return Arrays.stream(VitalsConcept.values())
                .map(concept -> boundaryObs(concept, boundary))
                .toList();
    }

    private static Obs boundaryObs(VitalsConcept concept, ToDoubleFunction<VitalsConcept> boundary) {
        if (concept.getValueType() == VitalsConcept.ValueType.TEXT) {
            return Obs.of(concept, RandomModelGenerator.randomSentence());
        }
        double value = concept.hasAbsoluteRange()
                ? boundary.applyAsDouble(concept)
                : RandomModelGenerator.randomDouble(concept.low(), concept.high(), concept.getDecimalPlaces());
        return Obs.of(concept, concept.valueOf(value));
    }

    // One step outside each absolute limit of the concept
    private static Stream<Arguments> outOfRange(VitalsConcept concept) {
        double step = concept.getDecimalPlaces() == 0 ? WHOLE_NUMBER_STEP : DECIMAL_STEP;
        return Stream.of(
                Arguments.of(concept, concept.valueOf(concept.low() - step), ObsFieldError.VALUE_OUT_OF_RANGE_LOW),
                Arguments.of(concept, concept.valueOf(concept.high() + step), ObsFieldError.VALUE_OUT_OF_RANGE_HIGH)
        );
    }


    // ==== Search-based variant of getPatientObs(). Original helper untouched. ====
    // GET /obs?patient=... answers a collection, so it is a search, not a CRUD get.
    @Test
    public void addedVitalsAreFoundBySearchViaSearchRequester() {
        List<Obs> obs = List.of(
                Obs.of(VitalsConcept.TEMPERATURE, RandomModelGenerator.randomDouble(36, 37, 1)));

        var request = CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.VITALS)
                .location(Location.OUTPATIENT_CLINIC)
                .obs(obs)
                .build();

        new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(request);

        SearchResult<ObsResponse> patientObs = new SuccessfulSearchRequester<ObsResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.OBS_GET,
                ResponseSpecs.requestReturnsOk())
                .search(ObsSearchParams.builder()
                        .patient(patientUUID)
                        .representation("full")
                        .build());

        ModelAssertions.assertListMatchesExpected(softly,
                patientObs.results(),
                ObsAssertions.expectedObsOf(request),
                ObsAssertions::conceptUuidOf,
                "vitals found by search");
    }

}
