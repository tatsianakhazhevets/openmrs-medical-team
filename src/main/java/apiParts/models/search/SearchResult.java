package apiParts.models.search;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * What a successful search returns.
 *
 * Holds the lookup mechanics that used to be repeated in every test as
 * getResults().stream().filter(...).findFirst().orElseThrow(...).
 *
 * Note the semantics: a search that matches nothing is a normal 200 with an
 * empty list, not a 404. That is exactly why search cannot be folded into
 * CrudEndpoint.get(uuid), which answers 404 for a missing resource.
 */
public record SearchResult<T>(List<T> results) {

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public int size() {
        return results.size();
    }

    public Optional<T> firstMatching(Predicate<T> predicate) {
        return results.stream().filter(predicate).findFirst();
    }

    /**
     * Returns the single element matching the predicate, or fails with a message
     * that says how many results were actually searched through.
     */
    public T requireOne(Predicate<T> predicate, String what) {
        return firstMatching(predicate).orElseThrow(() -> new AssertionError(
                what + " was not found among " + results.size() + " search results"));
    }
}
