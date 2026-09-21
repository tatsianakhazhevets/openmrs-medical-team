package apiParts.assertions.comparison;

import org.assertj.core.api.SoftAssertions;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Echo check for lists: each request has its response, pairs are found by matchBy of the rule
 * (e.g. procedureCoded -> procedureCoded.uuid), then compared by {@link ModelMatcher}.
 * Lists of one element are paired directly, without matchBy.
 */
public final class ModelListMatcher {

    private final SoftAssertions softly;
    private final List<?> requests;
    private final List<?> responses;
    private String description = "";

    public ModelListMatcher(SoftAssertions softly, List<?> requests, List<?> responses) {
        this.softly = softly;
        this.requests = requests;
        this.responses = responses;
    }

    public ModelListMatcher as(String description) {
        this.description = description;
        return this;
    }

    public ModelListMatcher match() {
        softly.assertThat(responses.size())
                .as("%scount of responses", prefix())
                .isEqualTo(requests.size());
        if (requests.isEmpty() || responses.isEmpty()) {
            return this;
        }
        if (requests.size() == 1 && responses.size() == 1) {
            new ModelMatcher(softly, requests.get(0), responses.get(0)).as(description).match();
            return this;
        }

        ComparisonRule rule = ComparisonConfig.ruleFor(requests.get(0).getClass(), responses.get(0).getClass());
        FieldRule key = rule.matchBy();
        if (key == null) {
            throw new IllegalStateException("No matchBy in rule " + rule.key() + " - lists cannot be paired");
        }

        Map<Object, Object> responsesByKey = new LinkedHashMap<>();
        responses.forEach(response -> responsesByKey.put(responseKey(response, key), response));

        Set<Object> requestKeys = new HashSet<>();
        for (Object request : requests) {
            Object requestKey = requestKey(request, key, rule);
            if (!requestKeys.add(requestKey)) {
                throw new IllegalStateException("Several " + rule.request() + " with " + key.requestPath() + "="
                        + requestKey + " - requests in list must have unique matchBy");
            }
            Object response = responsesByKey.get(requestKey);
            String pairDescription = prefix() + key.requestPath() + "=" + requestKey;
            softly.assertThat(response)
                    .as("%s: %s is returned", pairDescription, rule.response())
                    .isNotNull();
            if (response != null) {
                new ModelMatcher(softly, request, response).as(pairDescription).match();
            }
        }
        return this;
    }

    // ======== HELPERS ========
    private String prefix() {
        return description.isEmpty() ? "" : description + ": ";
    }

    private static Object requestKey(Object request, FieldRule key, ComparisonRule rule) {
        Object value = JsonPaths.plain(key.converter().apply(JsonPaths.read(JsonPaths.toTree(request), key.requestPath())));
        if (value == null) {
            throw new IllegalStateException("matchBy '" + key.requestPath() + "' is null in " + rule.request()
                    + " - requests in list cannot be paired with responses");
        }
        return value;
    }

    private static Object responseKey(Object response, FieldRule key) {
        return JsonPaths.plain(JsonPaths.read(JsonPaths.toTree(response), key.responsePath()));
    }
}
