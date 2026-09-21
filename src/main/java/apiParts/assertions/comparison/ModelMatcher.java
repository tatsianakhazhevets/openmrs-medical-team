package apiParts.assertions.comparison;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.SoftAssertions;

/**
 * Echo check: fields sent in request came back in response, by rule from model-comparison.yml.
 * <ul>
 *   <li>field absent / null in request is not checked;</li>
 *   <li>each field is a separate soft assertion named "description: Request.path -> Response.path";</li>
 *   <li>missing rule or wrong path in config fails the test at once.</li>
 * </ul>
 */
public final class ModelMatcher {

    private final SoftAssertions softly;
    private final Object request;
    private final Object response;
    private String description = "";

    public ModelMatcher(SoftAssertions softly, Object request, Object response) {
        this.softly = softly;
        this.request = request;
        this.response = response;
    }

    // prefix of each field assertion, e.g. "updated procedure"
    public ModelMatcher as(String description) {
        this.description = description;
        return this;
    }

    public ModelMatcher match() {
        ComparisonRule rule = ComparisonConfig.ruleFor(request.getClass(), response.getClass());
        JsonNode requestJson = JsonPaths.toTree(request);
        JsonNode responseJson = JsonPaths.toTree(response);
        String prefix = description.isEmpty() ? "" : description + ": ";

        for (FieldRule field : rule.fields()) {
            JsonNode expected = field.converter().apply(JsonPaths.read(requestJson, field.requestPath()));
            if (JsonPaths.isAbsent(expected)) {
                continue;
            }
            JsonNode actual = JsonPaths.read(responseJson, field.responsePath());
            softly.assertThat(JsonPaths.plain(actual))
                    .as("%s%s.%s -> %s.%s", prefix, rule.request(), field.requestPath(), rule.response(), field.responsePath())
                    .isEqualTo(JsonPaths.plain(expected));
        }
        return this;
    }
}
