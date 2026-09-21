package apiParts.assertions.comparison;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rules from model-comparison.yml, loaded once.
 * Paths of a rule are checked against model classes on first use.
 */
public final class ComparisonConfig {

    private static final String FILE = "model-comparison.yml";
    private static final Map<String, ComparisonRule> RULES = load();
    private static final Set<String> VALIDATED = ConcurrentHashMap.newKeySet();

    private ComparisonConfig() {
    }

    public static ComparisonRule ruleFor(Class<?> request, Class<?> response) {
        String key = key(request.getSimpleName(), response.getSimpleName());
        ComparisonRule rule = RULES.get(key);
        if (rule == null) {
            throw new IllegalStateException("No comparison rule " + key + " in " + FILE);
        }
        if (!VALIDATED.contains(key)) {
            rule.fields().forEach(field -> {
                JsonPaths.validate(request, field.requestPath());
                JsonPaths.validate(response, field.responsePath());
            });
            VALIDATED.add(key);
        }
        return rule;
    }

    static String key(String request, String response) {
        return request + " -> " + response;
    }

    // ======== HELPERS ========
    private static Map<String, ComparisonRule> load() {
        try (InputStream input = ComparisonConfig.class.getClassLoader().getResourceAsStream(FILE)) {
            if (input == null) {
                throw new IllegalStateException("Config file not found: " + FILE);
            }
            Map<String, ComparisonRule> rules = new HashMap<>();
            for (JsonNode node : new YAMLMapper().readTree(input).path("rules")) {
                List<FieldRule> fields = new ArrayList<>();
                node.path("fields").forEach(item -> fields.add(FieldRule.parse(item)));
                FieldRule matchBy = matchByOf(node, fields);
                // response: one class or a list of classes with the same fields (POST and GET responses)
                JsonNode responses = node.path("response");
                List<String> responseNames = new ArrayList<>();
                if (responses.isArray()) {
                    responses.forEach(name -> responseNames.add(name.asText()));
                } else {
                    responseNames.add(responses.asText());
                }
                for (String response : responseNames) {
                    ComparisonRule rule = new ComparisonRule(node.path("request").asText(), response, List.copyOf(fields), matchBy);
                    if (rules.put(rule.key(), rule) != null) {
                        throw new IllegalStateException("Duplicate comparison rule " + rule.key() + " in " + FILE);
                    }
                }
            }
            return rules;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + FILE, e);
        }
    }

    // matchBy: request path of one of the fields - its response path and converter are used for the other side
    private static FieldRule matchByOf(JsonNode node, List<FieldRule> fields) {
        String matchBy = node.path("matchBy").asText(null);
        if (matchBy == null) {
            return null;
        }
        return fields.stream()
                .filter(field -> field.requestPath().equals(matchBy))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("matchBy '" + matchBy + "' of rule for "
                        + node.path("request").asText() + " is not in its fields (" + FILE + ")"));
    }
}
