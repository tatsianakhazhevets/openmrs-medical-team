package apiParts.assertions;

import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.encounter.ObsResponse;
import apiParts.models.encounter.Ref;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Builds expected obs from request (for {@link ModelAssertions#assertListMatchesExpected})
 * and extracts obs uuids from POST / GET responses.
 */
public class ObsAssertions {

    private ObsAssertions() {
    }

    // what GET /obs?patient={uuid}&v=full should return for obs sent in POST /encounter
    public static List<ObsResponse> expectedObsOf(CreateEncounterRequest request) {
        return request.getObs().stream()
                .map(obs -> ObsResponse.builder()
                        .person(Ref.of(request.getPatient()))   // patient uuid == person uuid
                        .concept(Ref.of(obs.getConcept().getUuid()))
                        .value(expectedValue(obs.getValue()))
                        .build())
                .toList();
    }

    // sort key for ModelAssertions.assertListMatchesExpected
    public static String conceptUuidOf(ObsResponse obs) {
        return obs.getConcept() == null ? null : obs.getConcept().getUuid();
    }

    // obs uuids returned by POST /encounter
    public static Set<String> uuidsOf(CreateEncounterResponse response) {
        return response.getObs().stream()
                .map(Ref::getUuid)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // obs uuids from a search result (SuccessfulSearchRequester already unwrapped {"results": [...]})
    public static Set<String> uuidsOf(List<ObsResponse> results) {
        return results.stream()
                .map(ObsResponse::getUuid)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // ======== HELPERS ========
    // Actual numeric value is always Double (see ObsResponse.setValue), builder bypasses the setter,
    // so expected is converted here: Integer 100 and Double 100.0 are not equal in recursive comparison.
    private static Object expectedValue(Object value) {
        return value instanceof Number number ? number.doubleValue() : value;
    }
}
