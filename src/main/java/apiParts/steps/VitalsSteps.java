package apiParts.steps;

import apiParts.models.GetParams;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.vitals.VitalsConcept;
import apiParts.models.encounter.ObsResponse;
import apiParts.models.encounter.ObsSearchParams;
import apiParts.models.encounter.EncounterResponse;
import apiParts.models.search.SearchResult;
import apiParts.models.vitals.Obs;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.utils.Uuids;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps for vitals and biometrics tests: reading obs / encounters and building obs for boundary cases.
 * Boundaries = lowAbsolute / hiAbsolute of the concept reference range (inclusive, ObsValidator), kept in VitalsConcept.
 */
public class VitalsSteps {
    // Step outside the reference range: 0.1 for concepts with decimals, 1 for whole-number ones
    // (for them 0.1 is not a valid value at all and the server would fail on conversion, not on range)
    private static final double DECIMAL_STEP = 0.1;
    private static final double WHOLE_NUMBER_STEP = 1;

    private VitalsSteps() {
    }

    // ======== GET ========
    // GET /obs?patient={uuid}&v=full
    public static SearchResult<ObsResponse> getPatientObs(String patientUUID) {
        return new SuccessfulSearchRequester<ObsResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.OBS_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .search(ObsSearchParams.builder()
                        .patient(patientUUID)
                        .representation(GetParams.FULL)
                        .build());
    }

    // GET /encounter/{uuid}?v=full
    public static EncounterResponse getEncounter(String encounterUUID) {
        return new SuccessfulCrudRequester<EncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .get(encounterUUID, GetParams.builder().v(GetParams.FULL).build().toQueryParams());
    }

    // Precondition (hard assert): all obs of created encounter are saved for patient
    public static void assertPatientHasObsOf(String patientUUID, EncounterResponse encounter) {
        Set<String> expectedObsUUIDs = Uuids.of(encounter.getObs());
        assertThat(Uuids.of(getPatientObs(patientUUID).results()))
                .as("precondition: patient has %d obs of created encounter", expectedObsUUIDs.size())
                .isEqualTo(expectedObsUUIDs);
    }

    // ======== OBS FOR BOUNDARY CASES ========
    // Every vitals concept at its lower boundary (see obsAt)
    public static List<Obs> obsAtLowerBoundary() {
        return obsAt(VitalsConcept::low);
    }

    // Every vitals concept at its upper boundary (see obsAt)
    public static List<Obs> obsAtUpperBoundary() {
        return obsAt(VitalsConcept::high);
    }

    // One obs per concept with absolute limits, one step below the lower boundary
    public static List<Obs> obsBelowLowerBoundary() {
        return obsOutOf(c -> c.low() - step(c));
    }

    // One obs per concept with absolute limits, one step above the upper boundary
    public static List<Obs> obsAboveUpperBoundary() {
        return obsOutOf(c -> c.high() + step(c));
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

    // Concepts without absolute limits (MID_UPPER_ARM_CIRC, TEXT) have no out-of-range values
    private static List<Obs> obsOutOf(ToDoubleFunction<VitalsConcept> value) {
        return Arrays.stream(VitalsConcept.values())
                .filter(VitalsConcept::hasAbsoluteRange)
                .map(c -> Obs.of(c, c.valueOf(value.applyAsDouble(c))))
                .toList();
    }

    // Step outside the range: 1 for whole-number concepts, 0.1 for concepts with decimals
    private static double step(VitalsConcept concept) {
        return concept.getDecimalPlaces() == 0 ? WHOLE_NUMBER_STEP : DECIMAL_STEP;
    }
}
