package apiParts.assertions;

import apiParts.assertions.comparison.ModelListMatcher;
import apiParts.assertions.comparison.ModelMatcher;
import org.assertj.core.api.SoftAssertions;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Entry point for model assertions.
 * <ul>
 *   <li>{@link #assertThatModels} - echo check: fields sent in request came back in response.
 *       Rules (which fields, how paths map, converters, list pairing) are in model-comparison.yml;</li>
 *   <li>{@link #assertMatchesExpected} - two models of the same type, e.g. POST response vs GET response,
 *       or response vs expected model built by entity helpers (VisitAssertions, OrderAssertions);</li>
 *   <li>{@link #assertListMatchesExpected} - same for lists, sorted by key first;</li>
 *   <li>{@link #assertUnchanged} - negative tests: backend state after rejected request equals state before it.</li>
 * </ul>
 */
public class ModelAssertions {

    private ModelAssertions() {
    }

    // echo check request -> response by rule from model-comparison.yml
    public static ModelMatcher assertThatModels(SoftAssertions softly, Object request, Object response) {
        return new ModelMatcher(softly, request, response);
    }

    // same for lists: pairs are found by matchBy of the rule
    public static ModelListMatcher assertThatModels(SoftAssertions softly, List<?> requests, List<?> responses) {
        return new ModelListMatcher(softly, requests, responses);
    }

    // models of the same type: only non-null fields of expected are checked, nested objects field by field
    public static void assertMatchesExpected(SoftAssertions softly, Object actual, Object expected, String description) {
        softly.assertThat(actual)
                .as(description)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }

    // Both lists are sorted by key and compared by index, so failure names the field:
    // (ignoringCollectionOrder() only reports "expected element was not matched" without the field)
    public static <T> void assertListMatchesExpected(SoftAssertions softly,
                                                     List<T> actual,
                                                     List<T> expected,
                                                     Function<T, String> key,
                                                     String description) {
        Comparator<T> byKey = Comparator.comparing(key, Comparator.nullsFirst(Comparator.naturalOrder()));
        assertMatchesExpected(softly,
                actual.stream().sorted(byKey).toList(),
                expected.stream().sorted(byKey).toList(),
                description);
    }

    // state (GET response) after rejected request is the same as before it:
    // all fields including nulls (a field filled by the request is caught), collections in any order
    public static void assertUnchanged(SoftAssertions softly, Object before, Object after, String description) {
        softly.assertThat(after)
                .as(description)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(before);
    }
}
