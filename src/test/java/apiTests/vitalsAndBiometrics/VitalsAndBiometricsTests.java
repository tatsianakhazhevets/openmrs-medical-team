package apiTests.vitalsAndBiometrics;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.GenerationProfile;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.VitalsConcept;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterRequest.Obs;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.errors.ObsFieldError;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.models.search.SearchResult;
import apiParts.models.encounter.ObsResponse;
import apiParts.models.encounter.ObsSearchParams;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiParts.utils.Uuids;
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

        var request = RandomModelGenerator.generate(
                CreateEncounterRequest.class, GenerationProfile.VITALS, Map.of("obs", obs));

        var encounter = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(request);
        // obs in POST /encounter response are refs (uuid + display) - values are checked via GET /obs
        ModelAssertions.assertThatModels(softly, request, encounter)
                .as("POST /encounter response")
                .match();
        softly.assertThat(encounter.getObs())
                .as("obs in POST /encounter response")
                .hasSize(request.getObs().size());

        var patientObs = getPatientObs();

        ModelAssertions.assertThatModels(softly, request.getObs(), patientObs.results())
                .as("obs saved for patient")
                .match();
        softly.assertThat(patientObs.results().stream().map(o -> o.getPerson().getUuid()).toList())
                .as("obs person is the encounter patient")
                .containsOnly(request.getPatient());
        softly.assertThat(Uuids.of(patientObs.results()))
                .as("obs uuids from GET match POST /encounter")
                .isEqualTo(Uuids.of(encounter.getObs()));
    }

    @ParameterizedTest(name = "{0} = {1} -> {2}")
    @MethodSource("outOfRangeVitals")
    public void adminCannotAddVitalsOutOfRange(VitalsConcept concept, Number value, ObsFieldError error) {

        var request = RandomModelGenerator.generate(
                CreateEncounterRequest.class, GenerationProfile.VITALS, Map.of("obs", List.of(Obs.of(concept, value))));
        var before = getPatientObs().results();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsInvalidSubmission(error)
        )
                .create(request);

        ModelAssertions.assertUnchanged(softly, before, getPatientObs().results(),
                "patient obs after POST /encounter with out-of-range value");
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

        softly.assertThat(getPatientObs().results())
                .as("obs of deleted encounter are not returned for patient")
                .isEmpty();
        softly.assertThat(getEncounter(encounter.getUuid()).getVoided())
                .as("deleted encounter is marked as voided")
                .isTrue();
    }

    @Test
    public void adminCannotDeleteNonExistentEncounter() {
        var encounter = AdminSteps.createVitalsEncounter(patientUUID);
        assertPatientHasObsOf(encounter);
        var before = getPatientObs().results();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsNotFound()
        )
                .delete(UUID.randomUUID().toString());

        ModelAssertions.assertUnchanged(softly, before, getPatientObs().results(),
                "patient obs after delete of non-existent encounter");
    }

    @Test
    public void unauthorizedUserCannotDeleteEncounter() {
        var encounter = AdminSteps.createVitalsEncounter(patientUUID);
        assertPatientHasObsOf(encounter);
        var obsBefore = getPatientObs().results();
        var encounterBefore = getEncounter(encounter.getUuid());

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsUnauthorized()
        )
                .delete(encounter.getUuid());

        ModelAssertions.assertUnchanged(softly, obsBefore, getPatientObs().results(),
                "patient obs after unauthorized delete");
        ModelAssertions.assertUnchanged(softly, encounterBefore, getEncounter(encounter.getUuid()),
                "encounter after unauthorized delete (not voided)");
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

        Set<String> expectedObsUUIDs = new TreeSet<>(Uuids.of(encounter.getObs()));
        expectedObsUUIDs.remove(deletedObsUUID);

        softly.assertThat(Uuids.of(getPatientObs().results()))
                .as("only deleted obs is gone, other obs remain")
                .isEqualTo(expectedObsUUIDs);
    }

    // ======== HELPERS ========
    private SearchResult<ObsResponse> getPatientObs() {
        return new SuccessfulSearchRequester<ObsResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.OBS_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .search(ObsSearchParams.builder()
                        .patient(patientUUID)
                        .representation("full")
                        .build());
    }

    private CreateEncounterResponse getEncounter(String encounterUUID) {
        return new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .get(encounterUUID, Map.of("v", "full"));
    }

    // Precondition (hard assert): all obs of created encounter are saved for patient
    private void assertPatientHasObsOf(CreateEncounterResponse encounter) {
        Set<String> expectedObsUUIDs = Uuids.of(encounter.getObs());
        assertThat(Uuids.of(getPatientObs().results()))
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
}
