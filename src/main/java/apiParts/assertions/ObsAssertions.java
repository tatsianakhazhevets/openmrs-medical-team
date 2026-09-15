package apiParts.assertions;

import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.encounter.GetObsResponse;
import apiParts.models.encounter.ObsResponse;
import apiParts.models.encounter.Ref;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Brings obs from request / POST response / GET response to a common form,
 * so they can be compared with a single AssertJ isEqualTo().
 */
public class ObsAssertions {

    private ObsAssertions() {
    }

    // concept uuid -> value, from what was sent
    public static Map<String, String> valuesOf(CreateEncounterRequest request) {
        return toValueMap(request.getObs().stream(),
                obs -> obs.getConcept().getUuid(),
                CreateEncounterRequest.Obs::getValue);
    }

    // concept uuid -> value, from what was saved (GET /obs?v=full)
    public static Map<String, String> valuesOf(GetObsResponse response) {
        return toValueMap(response.getResults().stream(),
                obs -> obs.getConcept().getUuid(),
                ObsResponse::getValue);
    }

    // obs uuids returned by POST /encounter
    public static Set<String> uuidsOf(CreateEncounterResponse response) {
        return response.getObs().stream()
                .map(Ref::getUuid)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // obs uuids returned by GET /obs
    public static Set<String> uuidsOf(GetObsResponse response) {
        return response.getResults().stream()
                .map(ObsResponse::getUuid)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // ======== HELPERS ========
    private static <T> Map<String, String> toValueMap(Stream<T> obs,
                                                      Function<T, String> concept,
                                                      Function<T, Object> value) {
        return obs.collect(Collectors.toMap(
                concept,
                o -> normalize(value.apply(o)),
                (a, b) -> { throw new IllegalStateException("Duplicate concept in obs: " + a + " / " + b); },
                TreeMap::new));
    }

    // 100, 100.0 -> "100"; 28.70 -> "28.7"; text stays as is
    private static String normalize(Object value) {
        if (value instanceof Number number) {
            return new BigDecimal(number.toString()).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value);
    }
}
