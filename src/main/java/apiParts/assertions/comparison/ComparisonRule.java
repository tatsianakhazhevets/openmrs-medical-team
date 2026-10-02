package apiParts.assertions.comparison;

import java.util.List;

/**
 * Fields of request model that server should echo in response model.
 * Models are matched by simple class name.
 *
 * @param matchBy field that pairs requests with responses when lists are compared, null if not set
 */
public record ComparisonRule(String request, String response, List<FieldRule> fields, FieldRule matchBy) {

    public String key() {
        return ComparisonConfig.key(request, response);
    }
}
