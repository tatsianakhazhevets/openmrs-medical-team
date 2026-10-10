package apiParts.assertions.comparison;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Paths over JSON of models: {@code patient.uuid}, {@code providers[].uuid} ([] - every element of a list).
 * Models are compared as JSON: request is serialized the same way it is sent,
 * so enums with {@code @JsonValue} become their uuid.
 */
final class JsonPaths {

    static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonPaths() {
    }

    static JsonNode toTree(Object model) {
        return MAPPER.valueToTree(model);
    }

    // value by path; missing / null part of the path gives MissingNode
    static JsonNode read(JsonNode root, String path) {
        return read(root, path.split("\\."), 0);
    }

    static boolean isAbsent(JsonNode value) {
        return value == null || value.isMissingNode() || value.isNull();
    }

    // JSON -> Java value for AssertJ: 100 and 100.0 are equal, lists stay lists
    static Object plain(JsonNode value) {
        if (isAbsent(value)) {
            return null;
        }
        if (value.isNumber()) {
            return new BigDecimal(value.decimalValue().stripTrailingZeros().toPlainString());
        }
        if (value.isTextual()) {
            return value.asText();
        }
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        if (value.isArray()) {
            List<Object> list = new ArrayList<>();
            value.forEach(element -> list.add(plain(element)));
            return list;
        }
        return value.toString();
    }

    // fails if path has no such property in the model class - typo in config must not be silently skipped
    static void validate(Class<?> model, String path) {
        JavaType type = MAPPER.constructType(model);
        for (String segment : path.split("\\.")) {
            boolean each = isEach(segment);
            String name = propertyName(segment);
            JavaType owner = type;
            type = MAPPER.getSerializationConfig().introspect(owner).findProperties().stream()
                    .filter(property -> property.getName().equals(name))
                    .findFirst()
                    .map(BeanPropertyDefinition::getPrimaryType)
                    .orElseThrow(() -> new IllegalStateException(
                            "No property '" + name + "' in " + owner.getRawClass().getSimpleName()
                                    + " (path '" + path + "' of " + model.getSimpleName() + ")"));
            if (each) {
                if (!type.isCollectionLikeType() && !type.isArrayType()) {
                    throw new IllegalStateException("'" + segment + "' is not a list in path '" + path
                            + "' of " + model.getSimpleName());
                }
                type = type.getContentType();
            }
        }
    }

    // ======== HELPERS ========
    private static JsonNode read(JsonNode node, String[] segments, int index) {
        if (index == segments.length) {
            return node;
        }
        if (isAbsent(node)) {
            return MissingNode.getInstance();
        }
        String segment = segments[index];
        JsonNode child = node.path(propertyName(segment));
        if (!isEach(segment) || !child.isArray()) {
            return read(child, segments, index + 1);
        }
        ArrayNode result = JsonNodeFactory.instance.arrayNode();
        child.forEach(element -> result.add(read(element, segments, index + 1)));
        return result;
    }

    private static boolean isEach(String segment) {
        return segment.endsWith("[]");
    }

    private static String propertyName(String segment) {
        return isEach(segment) ? segment.substring(0, segment.length() - 2) : segment;
    }
}
