package apiParts.assertions;

import apiParts.assertions.comparison.ModelListMatcher;
import apiParts.assertions.comparison.ModelMatcher;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Entry point for model assertions. Uses the current test's SoftAssertions from {@link SoftlyContext}
 * (set by BaseTest), so it does not need to be passed as a parameter.
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
    public static ModelMatcher assertThatModels(Object request, Object response) {
        return new ModelMatcher(SoftlyContext.get(), request, response);
    }

    // same for lists: pairs are found by matchBy of the rule
    public static ModelListMatcher assertThatModels(List<?> requests, List<?> responses) {
        return new ModelListMatcher(SoftlyContext.get(), requests, responses);
    }

    // models of the same type: only non-null fields of expected are checked, nested objects field by field
    public static void assertMatchesExpected(Object actual, Object expected, String description) {
        SoftlyContext.get().assertThat(actual)
                .as(description)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }

    public static void assertMatchesExpectedIgnoringFields(Object actual,
                                                           Object expected,
                                                           String description,
                                                           String... ignoredFields) {
        SoftlyContext.get().assertThat(actual)
                .as(description)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .ignoringFields(ignoredFields)
                .isEqualTo(expected);
    }

    // Both lists are sorted by key and compared by index, so failure names the field:
    // (ignoringCollectionOrder() only reports "expected element was not matched" without the field)
    public static <T> void assertListMatchesExpected(List<T> actual,
                                                     List<T> expected,
                                                     Function<T, String> key,
                                                     String description) {
        Comparator<T> byKey = Comparator.comparing(key, Comparator.nullsFirst(Comparator.naturalOrder()));
        assertMatchesExpected(
                actual.stream().sorted(byKey).toList(),
                expected.stream().sorted(byKey).toList(),
                description);
    }

    // state (GET response) after rejected request is the same as before it:
    // all fields including nulls (a field filled by the request is caught), collections in any order
    public static void assertUnchanged(Object before, Object after, String description) {
        SoftlyContext.get().assertThat(after)
                .as(description)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(before);
    }
}
