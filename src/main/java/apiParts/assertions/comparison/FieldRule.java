package apiParts.assertions.comparison;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * One line of model-comparison.yml:
 * <pre>
 * - comments                                   # same path in request and response
 * - patient: patient.uuid                      # request path: response path
 * - startDateTime: startDateTime@epochMillis   # + converter for request value
 * </pre>
 */
public record FieldRule(String requestPath, String responsePath, ValueConverter converter) {

    static FieldRule parse(JsonNode item) {
        if (item.isTextual()) {
            return new FieldRule(item.asText().trim(), item.asText().trim(), ValueConverter.NONE);
        }
        if (item.isObject() && item.size() == 1) {
            Map.Entry<String, JsonNode> entry = item.fields().next();
            String[] target = entry.getValue().asText().split("@", 2);
            ValueConverter converter = target.length == 2 ? ValueConverter.byName(target[1].trim()) : ValueConverter.NONE;
            return new FieldRule(entry.getKey().trim(), target[0].trim(), converter);
        }
        throw new IllegalStateException("Field rule must be 'path' or 'requestPath: responsePath[@converter]', got: " + item);
    }

    @Override
    public String toString() {
        String target = requestPath.equals(responsePath) ? "" : " -> " + responsePath;
        String conversion = converter == ValueConverter.NONE ? "" : " @" + converter.name().toLowerCase();
        return requestPath + target + conversion;
    }
}
