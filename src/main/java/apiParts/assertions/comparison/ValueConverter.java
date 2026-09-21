package apiParts.assertions.comparison;

import apiParts.utils.DateTimeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;

import java.time.Instant;
import java.util.Arrays;
import java.util.function.UnaryOperator;

/**
 * Converts request value to the form server returns it in.
 * Set in model-comparison.yml after '@': {@code startDateTime: startDateTime@epochMillis}.
 * Applied to text values only, arrays are converted element by element.
 */
public enum ValueConverter {

    NONE("", value -> value),

    // "2026-09-17T22:00:00+03:00" -> "2026-09-17T19:00:00Z" (same as setters of response models)
    INSTANT("instant", DateTimeUtils::toInstantString),

    // "2026-09-17T22:00:00+03:00" -> "1789671600000" (appointments return dates as epoch millis)
    EPOCH_MILLIS("epochMillis", value -> String.valueOf(Instant.parse(DateTimeUtils.toInstantString(value)).toEpochMilli()));

    private final String configName;
    private final UnaryOperator<String> converter;

    ValueConverter(String configName, UnaryOperator<String> converter) {
        this.configName = configName;
        this.converter = converter;
    }

    public static ValueConverter byName(String name) {
        return Arrays.stream(values())
                .filter(c -> c.configName.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown converter '@" + name + "', available: "
                        + Arrays.stream(values()).filter(c -> c != NONE).map(c -> c.configName).toList()));
    }

    public JsonNode apply(JsonNode value) {
        if (this == NONE) {
            return value;
        }
        if (value.isArray()) {
            ArrayNode result = JsonNodeFactory.instance.arrayNode();
            value.forEach(element -> result.add(apply(element)));
            return result;
        }
        return value.isTextual() ? TextNode.valueOf(converter.apply(value.asText())) : value;
    }
}
