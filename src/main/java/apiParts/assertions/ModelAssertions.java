package apiParts.assertions;

import org.assertj.core.api.SoftAssertions;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Common comparison of actual response model with expected one built from request.
 * <p>
 * Expected model has the same type as actual, so no normalization is needed:
 * <ul>
 *   <li>only non-null fields of expected are checked (server fields like display, orderNumber are skipped);</li>
 *   <li>nested objects are compared field by field;</li>
 *   <li>collections are compared by index - use {@link #assertListMatchesExpected} to sort them first.</li>
 * </ul>
 * Caveat: a field forgotten in expected model is silently not checked.
 */
public class ModelAssertions {

    private ModelAssertions() {
    }

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
}
